package com.breakinblocks.nautec.gametest.suite;

import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.content.blockentities.EnergyConverterBlockEntity;
import com.breakinblocks.nautec.content.blockentities.generators.ThermalVentTapBlockEntity;
import com.breakinblocks.nautec.content.blockentities.generators.TidalRotorBlockEntity;
import com.breakinblocks.nautec.registries.NTBlocks;
import com.breakinblocks.nautec.registries.NTFluids;
import com.breakinblocks.nautec.registries.NTItems;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.fluids.FluidStack;

public final class OceanGeneratorTests {
    private static final BlockPos ROTOR = new BlockPos(4, 1, 4);
    private static final BlockPos TAP = new BlockPos(4, 2, 4);

    private OceanGeneratorTests() {
    }

    private static void flood(NTGameTestHelper helper) {
        BlockPos from = helper.absolutePos(new BlockPos(1, 1, 1));
        BlockPos to = helper.absolutePos(new BlockPos(7, 8, 7));
        for (BlockPos pos : BlockPos.betweenClosed(from, to)) {
            helper.getLevel().setBlock(pos, Blocks.WATER.defaultBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    private static TidalRotorBlockEntity rotor(NTGameTestHelper helper, boolean wet) {
        helper.getLevel().setBlock(helper.absolutePos(ROTOR), NTBlocks.TIDAL_ROTOR.get().defaultBlockState()
                .setValue(BlockStateProperties.WATERLOGGED, wet), Block.UPDATE_ALL);
        return (TidalRotorBlockEntity) helper.getLevel().getBlockEntity(helper.absolutePos(ROTOR));
    }

    private static ThermalVentTapBlockEntity tap(NTGameTestHelper helper, int hotBlocks) {
        ServerLevel level = helper.getLevel();
        int placed = 0;
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                BlockPos below = helper.absolutePos(TAP).offset(x, -1, z);
                level.setBlock(below, (placed++ < hotBlocks ? Blocks.MAGMA_BLOCK : Blocks.STONE).defaultBlockState(), Block.UPDATE_ALL);
            }
        }
        level.setBlock(helper.absolutePos(TAP), NTBlocks.THERMAL_VENT_TAP.get().defaultBlockState(), Block.UPDATE_ALL);
        ThermalVentTapBlockEntity tap = (ThermalVentTapBlockEntity) level.getBlockEntity(helper.absolutePos(TAP));
        tap.scan(level);
        return tap;
    }

    public static void register(NTTestRegistrar r) {
        r.add("generators/tidal_rotor_scales_with_open_water", 20, helper -> {
            boolean ocean = NTConfig.drainRequiresOcean;
            NTConfig.drainRequiresOcean = false;
            try {
                flood(helper);
                TidalRotorBlockEntity rotor = rotor(helper, true);
                rotor.scan(helper.getLevel());
                int depth = rotor.getDepth();
                helper.assertValueEqual(depth, 7, "water above the rotor");
                helper.assertValueEqual(Math.round(rotor.getOpenWater() * 26), 17, "water blocks around a rotor on the floor");
                int expected = (int) Math.round(NTConfig.tidalRotorMinOutput + (NTConfig.tidalRotorMaxOutput - NTConfig.tidalRotorMinOutput)
                        * (0.5 * rotor.getOpenWater() + 0.5 * depth / (double) TidalRotorBlockEntity.MAX_DEPTH));
                helper.assertValueEqual(rotor.getRate(), expected, "rate in open water");
                helper.assertTrue(rotor.getRate() > NTConfig.tidalRotorMinOutput && rotor.getRate() < NTConfig.tidalRotorMaxOutput,
                        "the rate sits between the minimum and maximum");
                helper.getLevel().setBlock(helper.absolutePos(ROTOR).north(), Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);
                rotor.scan(helper.getLevel());
                helper.assertTrue(rotor.getRate() < expected, "a solid block beside it lowers the rate");
            } finally {
                NTConfig.drainRequiresOcean = ocean;
            }
            helper.succeed();
        });

        r.add("generators/tidal_rotor_needs_water_and_an_ocean", 20, helper -> {
            TidalRotorBlockEntity dry = rotor(helper, false);
            dry.scan(helper.getLevel());
            helper.assertValueEqual(dry.getStatus(), TidalRotorBlockEntity.Status.DRY, "a dry rotor");
            helper.assertValueEqual(dry.getRate(), 0, "a dry rotor makes nothing");
            boolean ocean = NTConfig.drainRequiresOcean;
            NTConfig.drainRequiresOcean = true;
            try {
                TidalRotorBlockEntity wet = rotor(helper, true);
                wet.scan(helper.getLevel());
                helper.assertValueEqual(wet.getStatus(), TidalRotorBlockEntity.Status.NOT_OCEAN, "underwater outside an ocean biome");
            } finally {
                NTConfig.drainRequiresOcean = ocean;
            }
            helper.succeed();
        });

        r.add("generators/tidal_rotor_feeds_a_neighbour", 120, helper -> {
            boolean ocean = NTConfig.drainRequiresOcean;
            NTConfig.drainRequiresOcean = false;
            flood(helper);
            rotor(helper, true).scan(helper.getLevel());
            BlockPos converterPos = helper.absolutePos(ROTOR).east();
            helper.getLevel().setBlock(converterPos, NTBlocks.ENERGY_CONVERTER.get().defaultBlockState(), Block.UPDATE_ALL);
            helper.succeedWhen(() -> {
                EnergyConverterBlockEntity converter = (EnergyConverterBlockEntity) helper.getLevel().getBlockEntity(converterPos);
                helper.assertTrue(converter.getFeBuffer().getAmountAsInt() > 0, "the rotor pushes FE into the converter");
                NTConfig.drainRequiresOcean = ocean;
            });
        });

        r.add("generators/vent_tap_heat_sets_output", 20, helper -> {
            ThermalVentTapBlockEntity one = tap(helper, 1);
            helper.assertValueEqual(one.getHeat(), 1, "one magma block");
            helper.assertValueEqual(one.rate(), NTConfig.ventTapMinOutput, "one hot block gives the minimum");
            ThermalVentTapBlockEntity all = tap(helper, 9);
            helper.assertValueEqual(all.rate(), NTConfig.ventTapMaxOutput, "nine hot blocks give the maximum");
            ThermalVentTapBlockEntity cold = tap(helper, 0);
            helper.assertValueEqual(cold.rate(), 0, "no heat, no power");
            helper.succeed();
        });

        r.add("generators/vent_tap_boils_salt_water_into_fe_and_salt", 200, helper -> {
            ThermalVentTapBlockEntity tap = tap(helper, 1);
            tap.getFuelTank().fill(new FluidStack(NTFluids.SALT_WATER.getStillFluid(), 2_000));
            helper.succeedWhen(() -> {
                helper.assertValueEqual(tap.getStatus(), ThermalVentTapBlockEntity.Status.RUNNING, "status");
                helper.assertTrue(tap.getEnergyStorage().getAmountAsInt() > 0, "it stores FE");
                helper.assertTrue(tap.getItemStackHandler().getStackInSlot(0).is(NTItems.SALT.get()), "it leaves salt behind");
                helper.assertTrue(helper.getBlockState(TAP).getValue(BlockStateProperties.LIT), "it lights up while running");
            });
        });

        r.add("generators/vent_tap_salt_item_follows_the_config", 5, helper -> {
            String original = NTConfig.ventTapSaltItem;
            try {
                NTConfig.ventTapSaltItem = "minecraft:sugar";
                helper.assertTrue(ThermalVentTapBlockEntity.saltByproduct().is(Items.SUGAR), "the configured item is used");
                NTConfig.ventTapSaltItem = "";
                helper.assertTrue(ThermalVentTapBlockEntity.saltByproduct().isEmpty(), "an empty value leaves nothing behind");
                NTConfig.ventTapSaltItem = "minecraft:air";
                helper.assertTrue(ThermalVentTapBlockEntity.saltByproduct().isEmpty(), "air leaves nothing behind");
                NTConfig.ventTapSaltItem = "nautec:not_an_item";
                helper.assertTrue(ThermalVentTapBlockEntity.saltByproduct().is(NTItems.SALT.get()), "an unknown id falls back to salt");
            } finally {
                NTConfig.ventTapSaltItem = original;
            }
            helper.succeed();
        });

        r.add("generators/vent_tap_pushes_salt_into_adjacent_inventory", 60, helper -> {
            ThermalVentTapBlockEntity tap = tap(helper, 1);
            BlockPos chestPos = TAP.east();
            helper.setBlock(chestPos, Blocks.CHEST);
            tap.getItemStackHandler().setStackInSlot(0, new ItemStack(NTItems.SALT.get(), 5));
            helper.succeedWhen(() -> {
                ChestBlockEntity chest = helper.getBlockEntity(chestPos, ChestBlockEntity.class);
                int salt = 0;
                for (int i = 0; i < chest.getContainerSize(); i++) {
                    if (chest.getItem(i).is(NTItems.SALT.get())) {
                        salt += chest.getItem(i).getCount();
                    }
                }
                helper.assertValueEqual(salt, 5, "salt in the chest");
                helper.assertTrue(tap.getItemStackHandler().getStackInSlot(0).isEmpty(), "the tap's salt slot is emptied");
            });
        });

        r.add("generators/vent_tap_stops_without_fuel", 40, helper -> {
            ThermalVentTapBlockEntity tap = tap(helper, 9);
            helper.runAfterDelay(20, () -> {
                helper.assertValueEqual(tap.getStatus(), ThermalVentTapBlockEntity.Status.NO_FUEL, "status");
                helper.assertValueEqual(tap.getEnergyStorage().getAmountAsInt(), 0, "no FE without fuel");
                helper.succeed();
            });
        });
    }
}
