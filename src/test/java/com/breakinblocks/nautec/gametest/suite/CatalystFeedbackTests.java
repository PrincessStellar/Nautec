package com.breakinblocks.nautec.gametest.suite;

import com.breakinblocks.nautec.api.blockentities.BeamScan;
import com.breakinblocks.nautec.content.blockentities.AquaticCatalystBlockEntity;
import com.breakinblocks.nautec.content.blockentities.MixerBlockEntity;
import com.breakinblocks.nautec.content.blockentities.multiblock.semi.PrismarineCrystalBlockEntity;
import com.breakinblocks.nautec.content.blocks.AquaticCatalystBlock;
import com.breakinblocks.nautec.content.blocks.PrismarineLaserRelayBlock;
import com.breakinblocks.nautec.content.blocks.multiblock.semi.PrismarineCrystalBlock;
import com.breakinblocks.nautec.registries.NTBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;

import java.util.List;

public final class CatalystFeedbackTests {
    private static final BlockPos CATALYST_POS = new BlockPos(2, 1, 4);

    private CatalystFeedbackTests() {
    }

    private static AquaticCatalystBlockEntity placeCatalyst(GameTestHelper helper) {
        helper.setBlock(CATALYST_POS, NTBlocks.AQUATIC_CATALYST.get().defaultBlockState()
                .setValue(BlockStateProperties.FACING, Direction.WEST));
        return helper.getBlockEntity(CATALYST_POS, AquaticCatalystBlockEntity.class);
    }

    private static void placeRelay(GameTestHelper helper, BlockPos pos, Direction facing) {
        helper.setBlock(pos, NTBlocks.PRISMARINE_RELAY.get().defaultBlockState().setValue(PrismarineLaserRelayBlock.FACING, facing));
    }

    private static BlockState catalystState(GameTestHelper helper) {
        return helper.getBlockState(CATALYST_POS);
    }

    private static boolean hasLine(List<Component> lines, String key) {
        return lines.stream().anyMatch(line -> line.getContents() instanceof TranslatableContents contents && contents.getKey().equals(key));
    }

    private static Direction placeRelayAgainst(GameTestHelper helper, BlockPos against, Direction face, float yRot, float xRot) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setYRot(yRot);
        player.setYHeadRot(yRot);
        player.setXRot(xRot);
        ItemStack stack = new ItemStack(NTBlocks.PRISMARINE_RELAY.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        BlockPos absolute = helper.absolutePos(against);
        BlockHitResult hit = new BlockHitResult(absolute.getCenter().relative(face, 0.5), face, absolute, false);
        BlockPlaceContext context = new BlockPlaceContext(helper.getLevel(), player, InteractionHand.MAIN_HAND, stack, hit);
        BlockState state = NTBlocks.PRISMARINE_RELAY.get().getStateForPlacement(context);
        if (state == null) {
            throw helper.assertionException("relay refused placement");
        }
        return state.getValue(PrismarineLaserRelayBlock.FACING);
    }

    public static void register(NTTestRegistrar r) {
        r.add("catalyst_feedback/crystal_takes_a_beam_at_any_height", 120, helper -> {
            AquaticCatalystBlockEntity catalyst = placeCatalyst(helper);
            BlockPos core = new BlockPos(5, 4, 4);
            PrismarineCrystalBlock.build(helper.getLevel(), helper.absolutePos(core), false);
            PrismarineCrystalBlockEntity crystal = helper.getBlockEntity(core, PrismarineCrystalBlockEntity.class);
            helper.runAfterDelay(1, () -> catalyst.getItemStackHandler().setStackInSlot(0, new ItemStack(Items.PRISMARINE_CRYSTALS, 4)));
            helper.runAfterDelay(60, () -> {
                helper.assertValueEqual(BeamScan.Status.CONNECTED, catalyst.getBeamScan().status(), "a beam into the bottom of the crystal connects");
                helper.assertTrue(catalyst.isActive(), "the catalyst burns into the crystal");
                helper.assertTrue(crystal.getPower() > 0, "the crystal core counts the beam its part took");
                helper.succeed();
            });
        });

        r.add("catalyst_feedback/redstone_pauses_and_keeps_fuel", 120, helper -> {
            AquaticCatalystBlockEntity catalyst = placeCatalyst(helper);
            BlockPos mixerPos = CATALYST_POS.east(3);
            BlockPos leverPos = CATALYST_POS.north();
            helper.setBlock(mixerPos, NTBlocks.MIXER.get().defaultBlockState());
            helper.runAfterDelay(1, () -> {
                catalyst.getItemStackHandler().setStackInSlot(0, new ItemStack(Items.PRISMARINE_CRYSTALS, 4));
                helper.setBlock(leverPos, Blocks.REDSTONE_BLOCK.defaultBlockState());
            });
            helper.runAfterDelay(40, () -> {
                helper.assertTrue(catalyst.isPaused(), "a redstone signal pauses the catalyst");
                helper.assertTrue(!catalyst.isActive(), "a paused catalyst does not burn");
                helper.assertTrue(!catalyst.isWaiting(), "paused is not reported as waiting for a receiver");
                helper.assertValueEqual(4, catalyst.getProcessingItem().getCount(), "fuel kept while paused");
                helper.assertValueEqual(0, catalyst.getDuration(), "no burn progress while paused");
                helper.assertValueEqual(0, helper.getBlockEntity(mixerPos, MixerBlockEntity.class).getPower(), "no beam while paused");
                helper.assertTrue(hasLine(catalyst.diagnosticLines(), "nautec.catalyst.diagnostics.status.paused"), "diagnostics report the pause");
                helper.setBlock(leverPos, Blocks.AIR.defaultBlockState());
            });
            helper.runAfterDelay(80, () -> {
                helper.assertTrue(!catalyst.isPaused(), "removing the signal resumes the catalyst");
                helper.assertTrue(catalyst.isActive(), "the catalyst burns again once unpaused");
                helper.assertValueEqual(3, catalyst.getProcessingItem().getCount(), "one fuel item taken on resume");
                helper.assertValueEqual(6, helper.getBlockEntity(mixerPos, MixerBlockEntity.class).getPower(), "beam back at 3750 / 600");
                helper.succeed();
            });
        });

        r.add("catalyst_feedback/waits_and_holds_fuel_without_receiver", 80, helper -> {
            AquaticCatalystBlockEntity catalyst = placeCatalyst(helper);
            helper.runAfterDelay(1, () -> catalyst.getItemStackHandler().setStackInSlot(0, new ItemStack(Items.PRISMARINE_CRYSTALS, 4)));
            helper.runAfterDelay(40, () -> {
                helper.assertTrue(!catalyst.isActive(), "catalyst must not burn without a receiver");
                helper.assertTrue(catalyst.isWaiting(), "catalyst should report waiting");
                helper.assertValueEqual(0, catalyst.getDuration(), "burn progress without a receiver");
                helper.assertValueEqual(0, catalyst.getPowerToTransfer(), "transfer without a receiver");
                helper.assertTrue(!catalystState(helper).getValue(AquaticCatalystBlock.ACTIVE), "active blockstate while waiting");
                helper.assertTrue(!catalystState(helper).getValue(AquaticCatalystBlock.LINKED), "lamp should be red while waiting");
                helper.succeed();
            });
        });

        r.add("catalyst_feedback/empty_off_hand_keeps_fed_fuel", 20, helper -> {
            AquaticCatalystBlockEntity catalyst = placeCatalyst(helper);
            Player player = helper.makeMockPlayer(GameType.SURVIVAL);
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.PRISMARINE_CRYSTALS, 4));
            BlockPos absolute = helper.absolutePos(CATALYST_POS);
            BlockHitResult hit = new BlockHitResult(absolute.getCenter(), Direction.UP, absolute, false);
            BlockState state = helper.getLevel().getBlockState(absolute);
            InteractionResult fed = state.useItemOn(player.getMainHandItem(), helper.getLevel(), player, InteractionHand.MAIN_HAND, hit);
            InteractionResult offHand = state.useItemOn(player.getOffhandItem(), helper.getLevel(), player, InteractionHand.OFF_HAND, hit);
            helper.assertTrue(fed.consumesAction(), "main hand fuel is taken");
            helper.assertTrue(!offHand.consumesAction(), "an empty off hand does nothing");
            helper.assertValueEqual(4, catalyst.getItemStackHandler().getStackInSlot(0).getCount(), "fuel stays in the catalyst");
            helper.assertTrue(player.getMainHandItem().isEmpty(), "fuel left the main hand");
            helper.succeed();
        });

        r.add("catalyst_feedback/backwards_relay_reported_then_fixed", 120, helper -> {
            AquaticCatalystBlockEntity catalyst = placeCatalyst(helper);
            BlockPos relayPos = CATALYST_POS.east();
            placeRelay(helper, relayPos, Direction.WEST);
            helper.runAfterDelay(1, () -> catalyst.getItemStackHandler().setStackInSlot(0, new ItemStack(Items.PRISMARINE_CRYSTALS, 4)));
            helper.runAfterDelay(30, () -> {
                BeamScan scan = catalyst.getBeamScan();
                helper.assertValueEqual(BeamScan.Status.WRONG_SIDE, scan.status(), "scan status for a backwards relay");
                helper.assertValueEqual(1, scan.distance(), "backwards relay distance");
                helper.assertTrue(!catalyst.isActive(), "catalyst must not burn into a backwards relay");
                helper.assertTrue(!catalystState(helper).getValue(AquaticCatalystBlock.LINKED), "lamp red for a backwards relay");
                List<Component> lines = catalyst.diagnosticLines();
                helper.assertTrue(hasLine(lines, "nautec.catalyst.diagnostics.beam.wrong_side"), "diagnostics name the backwards relay");
                helper.assertTrue(hasLine(lines, "nautec.catalyst.diagnostics.hint.wrong_side"), "diagnostics give the wrench hint");
                helper.assertTrue(hasLine(lines, "nautec.catalyst.diagnostics.status.waiting"), "diagnostics report waiting");
                placeRelay(helper, relayPos, Direction.EAST);
            });
            helper.runAfterDelay(70, () -> {
                helper.assertValueEqual(BeamScan.Status.CONNECTED, catalyst.getBeamScan().status(), "scan status after turning the relay");
                helper.assertTrue(catalyst.isActive(), "catalyst burns once the relay faces away");
                helper.assertTrue(catalyst.getPowerToTransfer() > 0, "catalyst transfers power once connected");
                helper.assertTrue(catalystState(helper).getValue(AquaticCatalystBlock.ACTIVE), "active blockstate when burning");
                helper.assertTrue(catalystState(helper).getValue(AquaticCatalystBlock.LINKED), "lamp green when connected");
                helper.assertTrue(hasLine(catalyst.diagnosticLines(), "nautec.catalyst.diagnostics.beam.connected"), "diagnostics report the connection");
                helper.succeed();
            });
        });

        r.add("catalyst_feedback/solid_block_reported_as_blocked", 60, helper -> {
            AquaticCatalystBlockEntity catalyst = placeCatalyst(helper);
            helper.setBlock(CATALYST_POS.east(2), Blocks.STONE.defaultBlockState());
            placeRelay(helper, CATALYST_POS.east(3), Direction.EAST);
            helper.runAfterDelay(20, () -> {
                BeamScan scan = catalyst.getBeamScan();
                helper.assertValueEqual(BeamScan.Status.BLOCKED, scan.status(), "scan status behind stone");
                helper.assertValueEqual(2, scan.distance(), "blocking stone distance");
                helper.assertTrue(hasLine(catalyst.diagnosticLines(), "nautec.catalyst.diagnostics.beam.blocked"), "diagnostics report the block");
                helper.succeed();
            });
        });

        r.add("catalyst_feedback/lamp_green_with_receiver_and_no_fuel", 60, helper -> {
            AquaticCatalystBlockEntity catalyst = placeCatalyst(helper);
            helper.setBlock(CATALYST_POS.east(3), NTBlocks.MIXER.get().defaultBlockState());
            helper.runAfterDelay(20, () -> {
                helper.assertTrue(catalystState(helper).getValue(AquaticCatalystBlock.LINKED), "lamp green with a receiver in range");
                helper.assertTrue(!catalystState(helper).getValue(AquaticCatalystBlock.ACTIVE), "not burning without fuel");
                helper.assertTrue(!catalyst.isWaiting(), "no fuel is not waiting");
                helper.assertTrue(hasLine(catalyst.diagnosticLines(), "nautec.catalyst.diagnostics.fuel.empty"), "diagnostics report no fuel");
                helper.succeed();
            });
        });

        r.add("catalyst_feedback/relay_placed_on_source_faces_away", 20, helper -> {
            placeCatalyst(helper);
            Direction facing = placeRelayAgainst(helper, CATALYST_POS, Direction.EAST, 90, 0);
            helper.assertValueEqual(Direction.EAST, facing, "relay placed on the catalyst lens face");
            helper.succeed();
        });

        r.add("catalyst_feedback/relay_placed_on_receiver_points_into_it", 20, helper -> {
            BlockPos mixerPos = new BlockPos(4, 1, 4);
            helper.setBlock(mixerPos, NTBlocks.MIXER.get().defaultBlockState());
            Direction facing = placeRelayAgainst(helper, mixerPos, Direction.EAST, 90, 0);
            helper.assertValueEqual(Direction.WEST, facing, "relay placed on a mixer");
            helper.succeed();
        });

        r.add("catalyst_feedback/relay_chain_continues_forward", 20, helper -> {
            BlockPos firstPos = new BlockPos(3, 1, 4);
            placeRelay(helper, firstPos, Direction.EAST);
            Direction facing = placeRelayAgainst(helper, firstPos, Direction.EAST, 90, 0);
            helper.assertValueEqual(Direction.EAST, facing, "relay placed on the front of another relay");
            helper.succeed();
        });

        r.add("catalyst_feedback/relay_on_plain_block_uses_look_direction", 20, helper -> {
            BlockPos stonePos = new BlockPos(4, 1, 4);
            helper.setBlock(stonePos, Blocks.STONE.defaultBlockState());
            Direction facing = placeRelayAgainst(helper, stonePos, Direction.UP, 180, 0);
            helper.assertValueEqual(Direction.NORTH, facing, "relay placed on stone while looking north");
            helper.succeed();
        });
    }
}
