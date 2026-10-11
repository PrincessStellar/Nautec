package com.breakinblocks.nautec.gametest.suite;

import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.data.NTDataComponents;
import com.breakinblocks.nautec.api.bacteria.BacteriaInstance;
import com.breakinblocks.nautec.capabilities.NTCapabilities;
import com.breakinblocks.nautec.capabilities.bacteria.IBacteriaStorage;
import com.breakinblocks.nautec.content.biometank.BiomeTankBlockEntity;
import com.breakinblocks.nautec.content.blockentities.AquaticCatalystBlockEntity;
import com.breakinblocks.nautec.content.blockentities.GraftingStationBlockEntity;
import com.breakinblocks.nautec.content.conduit.ConduitBeaconBlock;
import com.breakinblocks.nautec.content.conduit.ConduitBeaconBlockEntity;
import com.breakinblocks.nautec.content.conduit.ConduitBeaconTracker;
import com.breakinblocks.nautec.content.dishstorage.DishStorageBlockEntity;
import com.breakinblocks.nautec.content.dishstorage.DishStorageMenu;
import com.breakinblocks.nautec.registries.NTBacterias;
import com.breakinblocks.nautec.registries.NTBlocks;
import com.breakinblocks.nautec.registries.NTFluids;
import com.breakinblocks.nautec.registries.NTItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.AmethystClusterBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

public final class ExpansionFeatureTests {
    private static final BlockPos CATALYST = new BlockPos(2, 1, 4);
    private static final BlockPos BEACON = new BlockPos(4, 4, 4);
    private static final BlockPos MACHINE = new BlockPos(4, 1, 4);

    private ExpansionFeatureTests() {
    }

    public static void register(NTTestRegistrar r) {
        r.add("catalyst_accelerator/budding_prismarine_boosts_output_and_purity", 80, helper -> {
            AquaticCatalystBlockEntity catalyst = catalyst(helper);
            helper.runAfterDelay(30, () -> {
                int plain = catalyst.transferAmount();
                float plainPurity = catalyst.boostedPurity();
                helper.assertValueEqual(0, catalyst.getBoosters(), "boosters before any budding prismarine");
                helper.setBlock(CATALYST.north(), NTBlocks.BUDDING_PRISMARINE.get());
                helper.setBlock(CATALYST.south(), NTBlocks.BUDDING_PRISMARINE.get());
                helper.runAfterDelay(15, () -> {
                    helper.assertValueEqual(2, catalyst.getBoosters(), "budding prismarine blocks counted");
                    helper.assertValueEqual((int) Math.round(plain * (1 + 2 * NTConfig.catalystBuddingOutputBonus)), catalyst.transferAmount(),
                            "boosted AP output");
                    helper.assertTrue(catalyst.boostedPurity() > plainPurity + 0.15F, "boosted purity");
                    helper.assertValueEqual(catalyst.transferAmount(), catalyst.getPowerToTransfer(), "the beam carries the boosted output");
                    helper.succeed();
                });
            });
        });

        r.add("catalyst_accelerator/eats_clusters_grown_on_its_budding_blocks", 80, helper -> {
            catalyst(helper);
            BlockPos budding = CATALYST.north();
            helper.setBlock(budding, NTBlocks.BUDDING_PRISMARINE.get());
            helper.setBlock(budding.north(), NTBlocks.PRISMARINE_CLUSTER.get().defaultBlockState()
                    .setValue(AmethystClusterBlock.FACING, Direction.NORTH));
            BlockPos distant = new BlockPos(6, 1, 1);
            helper.setBlock(distant, NTBlocks.BUDDING_PRISMARINE.get());
            helper.setBlock(distant.north(), NTBlocks.PRISMARINE_CLUSTER.get().defaultBlockState()
                    .setValue(AmethystClusterBlock.FACING, Direction.NORTH));
            helper.runAfterDelay(45, () -> {
                helper.assertBlockNotPresent(NTBlocks.PRISMARINE_CLUSTER.get(), budding.north());
                helper.assertBlockPresent(NTBlocks.PRISMARINE_CLUSTER.get(), distant.north());
                helper.succeed();
            });
        });

        r.add("conduit_beacon/runs_with_frame_water_and_power", 60, helper -> {
            ConduitBeaconBlockEntity beacon = buildBeacon(helper, true);
            for (int tick = 0; tick < 5; tick++) {
                feed(helper, beacon, Direction.UP, 200);
            }
            helper.assertTrue(beacon.isFormed(), "a full frame in water is formed");
            helper.assertValueEqual(42, beacon.getFrameSize(), "frame blocks");
            helper.assertValueEqual(96, beacon.effectRange(), "Conduit Power reach of a full frame");
            helper.assertTrue(beacon.isRunning(), "the beacon runs on laser power");
            helper.assertTrue(helper.getBlockState(BEACON).getValue(ConduitBeaconBlock.ACTIVE), "the block shows it is running");

            BlockPos center = helper.absolutePos(BEACON);
            var dimension = helper.getLevel().dimension();
            helper.assertTrue(ConduitBeaconTracker.blocks(dimension, MobCategory.MONSTER, EntitySpawnReason.NATURAL, center.offset(100, 20, 0)),
                    "natural monsters are blocked inside the radius");
            helper.assertTrue(ConduitBeaconTracker.blocks(dimension, MobCategory.CREATURE, EntitySpawnReason.CHUNK_GENERATION, center.offset(0, 0, -120)),
                    "passive mobs are blocked inside the radius");
            helper.assertFalse(ConduitBeaconTracker.blocks(dimension, MobCategory.MONSTER, EntitySpawnReason.NATURAL, center.offset(200, 0, 0)),
                    "spawns outside the radius are untouched");
            helper.assertFalse(ConduitBeaconTracker.blocks(dimension, MobCategory.MONSTER, EntitySpawnReason.SPAWNER, center.offset(10, 0, 0)),
                    "spawners still work");
            helper.assertFalse(ConduitBeaconTracker.blocks(dimension, MobCategory.WATER_CREATURE, EntitySpawnReason.NATURAL, center.offset(10, 0, 0)),
                    "water creatures still spawn");
            helper.succeed();
        });

        r.add("conduit_beacon/needs_water_and_a_frame", 40, helper -> {
            helper.setBlock(BEACON, NTBlocks.CONDUIT_BEACON.get());
            ConduitBeaconBlockEntity beacon = helper.getBlockEntity(BEACON, ConduitBeaconBlockEntity.class);
            for (int tick = 0; tick < 5; tick++) {
                feed(helper, beacon, Direction.UP, 200);
            }
            beacon.checkFrame(helper.getLevel().getGameTime());
            helper.assertFalse(beacon.isFormed(), "no frame and no water");
            helper.assertFalse(beacon.isRunning(), "an unformed beacon stays off");
            helper.assertValueEqual("nautec.conduit_beacon.no_water", beacon.statusKey(), "status");
            helper.succeed();
        });

        r.add("conduit_beacon/beam_gap_counts_as_frame", 60, helper -> {
            ConduitBeaconBlockEntity beacon = buildBeacon(helper, true);
            helper.setBlock(BEACON.east(2), Blocks.WATER);
            beacon.checkFrame(helper.getLevel().getGameTime());
            helper.assertValueEqual(41, beacon.getFrameSize(), "frame with the east gap open");
            feed(helper, beacon, Direction.WEST, 200);
            beacon.checkFrame(helper.getLevel().getGameTime());
            helper.assertValueEqual(42, beacon.getFrameSize(), "a beam through the gap fills that frame slot");
            helper.succeed();
        });

        r.add("conduit_beacon/buffer_keeps_it_running_then_runs_dry", 200, helper -> {
            ConduitBeaconBlockEntity beacon = buildBeacon(helper, true);
            int saved = NTConfig.conduitBeaconPowerUsage;
            for (int tick = 0; tick < 30; tick++) {
                feed(helper, beacon, Direction.UP, NTConfig.conduitBeaconBuffer);
            }
            helper.assertValueEqual(NTConfig.conduitBeaconBuffer - saved, beacon.getBuffer(), "buffer after filling and one tick of use");
            int ticksOfReserve = NTConfig.conduitBeaconBuffer / Math.max(1, saved);
            helper.runAfterDelay(Math.min(60, ticksOfReserve / 2), () -> {
                helper.assertTrue(beacon.isRunning(), "the buffer keeps it running after the beam stops");
                helper.assertTrue(beacon.getBuffer() < NTConfig.conduitBeaconBuffer, "the buffer drains");
            });
            helper.runAfterDelay(ticksOfReserve + 20, () -> {
                helper.assertFalse(beacon.isRunning(), "an empty buffer turns it off");
                helper.assertFalse(ConduitBeaconTracker.isTracked(helper.getLevel().dimension(), helper.absolutePos(BEACON)),
                        "a stopped beacon no longer blocks spawns");
                helper.succeed();
            });
        });

        r.add("conduit_beacon/redstone_on_the_frame_turns_it_off", 60, helper -> {
            ConduitBeaconBlockEntity beacon = buildBeacon(helper, true);
            feed(helper, beacon, Direction.UP, NTConfig.conduitBeaconBuffer);
            helper.assertTrue(beacon.isRunning(), "running on its buffer");
            helper.setBlock(BEACON.above(3), Blocks.REDSTONE_BLOCK);
            helper.runAfterDelay(15, () -> {
                helper.assertFalse(beacon.isRunning(), "a powered frame turns the beacon off");
                helper.assertValueEqual("nautec.conduit_beacon.redstone", beacon.statusKey(), "status");
                helper.setBlock(BEACON.above(3), Blocks.AIR);
            });
            helper.runAfterDelay(30, () -> {
                helper.assertTrue(beacon.isRunning(), "removing the signal turns it back on");
                helper.succeed();
            });
        });

        r.add("dish_storage/tiers_hold_48_96_and_192", 20, helper -> {
            helper.setBlock(MACHINE, NTBlocks.AQUARINE_DISH_STORAGE.get());
            helper.setBlock(MACHINE.east(2), NTBlocks.DEEP_STEEL_DISH_STORAGE.get());
            helper.setBlock(MACHINE.west(2), NTBlocks.ATLANTIC_GOLD_DISH_STORAGE.get());
            helper.assertValueEqual(48, helper.getBlockEntity(MACHINE, DishStorageBlockEntity.class).getItemStackHandler().getSlots(), "aquarine");
            helper.assertValueEqual(96, helper.getBlockEntity(MACHINE.east(2), DishStorageBlockEntity.class).getItemStackHandler().getSlots(),
                    "deep steel");
            helper.assertValueEqual(192, helper.getBlockEntity(MACHINE.west(2), DishStorageBlockEntity.class).getItemStackHandler().getSlots(),
                    "atlantic gold");
            helper.succeed();
        });

        r.add("dish_storage/pipes_insert_and_extract_only_dishes", 20, helper -> {
            helper.setBlock(MACHINE, NTBlocks.AQUARINE_DISH_STORAGE.get());
            DishStorageBlockEntity storage = helper.getBlockEntity(MACHINE, DishStorageBlockEntity.class);
            ResourceHandler<ItemResource> side = helper.getLevel().getCapability(Capabilities.Item.BLOCK, helper.absolutePos(MACHINE), Direction.UP);
            helper.assertTrue(side != null, "the storage exposes items on every side");
            helper.assertValueEqual(0, insert(side, new ItemStack(Items.DIRT)), "dirt refused");
            helper.assertValueEqual(1, insert(side, new ItemStack(NTItems.PETRI_DISH.get())), "an empty dish");
            helper.assertValueEqual(1, insert(side, colonyDish(helper)), "a dish with a colony");
            helper.assertValueEqual(2, storage.storedCount(), "each dish takes its own slot");
            helper.assertTrue(storage.comparatorSignal() > 0, "a comparator reads the fill level");
            try (Transaction tx = Transaction.openRoot()) {
                int taken = side.extract(1, side.getResource(1), 1, tx);
                tx.commit();
                helper.assertValueEqual(1, taken, "dishes come back out");
            }
            helper.succeed();
        });

        r.add("dish_storage/menu_scrolls_through_every_row", 20, helper -> {
            helper.setBlock(MACHINE, NTBlocks.DEEP_STEEL_DISH_STORAGE.get());
            DishStorageBlockEntity storage = helper.getBlockEntity(MACHINE, DishStorageBlockEntity.class);
            storage.getItemStackHandler().setStackInSlot(90, colonyDish(helper));
            Player player = helper.makeMockPlayer(GameType.SURVIVAL);
            DishStorageMenu menu = new DishStorageMenu(1, player.getInventory(), storage);
            helper.assertValueEqual(6, menu.maxOffset(), "rows past the first screen");
            helper.assertTrue(menu.slots.get(36 + 42).getItem().isEmpty(), "row 11 is off screen at the top");
            menu.clickMenuButton(player, 100);
            helper.assertValueEqual(6, menu.getOffset(), "scrolling clamps to the last row");
            helper.assertFalse(menu.slots.get(36 + 42).getItem().isEmpty(), "the dish in slot 90 shows after scrolling");

            for (int hotbar = 0; hotbar < 3; hotbar++) {
                player.getInventory().setItem(hotbar, new ItemStack(NTItems.PETRI_DISH.get()));
                menu.quickMoveStack(player, 27 + hotbar);
            }
            helper.assertValueEqual(4, storage.storedCount(), "shift-click fills the first free slots, even off screen");
            helper.assertTrue(storage.getItemStackHandler().getStackInSlot(0).is(NTItems.PETRI_DISH.get()), "slot 0 filled first");
            helper.succeed();
        });

        r.add("grafting_anchor/keeps_the_sample_block", 80, helper -> {
            helper.setBlock(MACHINE, NTBlocks.GRAFTING_STATION.get());
            GraftingStationBlockEntity station = helper.getBlockEntity(MACHINE, GraftingStationBlockEntity.class);
            station.getItemStackHandler().setStackInSlot(GraftingStationBlockEntity.SAMPLE_SLOT, new ItemStack(Items.STONE));
            station.getItemStackHandler().setStackInSlot(GraftingStationBlockEntity.ANCHOR_SLOT, new ItemStack(NTItems.GRAFTING_ANCHOR.get()));
            helper.assertTrue(station.hasAnchor(), "the anchor is fitted");
            for (int graft = 0; graft < 2; graft++) {
                station.getItemStackHandler().setStackInSlot(GraftingStationBlockEntity.DISH_SLOT, new ItemStack(NTItems.PETRI_DISH.get()));
                station.getItemStackHandler().setStackInSlot(GraftingStationBlockEntity.OUTPUT_SLOT, ItemStack.EMPTY);
                station.getFluidTank().setFluid(new FluidStack(NTFluids.SALT_WATER.getStillFluid(), NTConfig.graftingStationSaltWaterUsage));
                BlockPos origin = helper.absolutePos(MACHINE.above());
                for (int tick = 0; tick < NTConfig.graftingStationDuration + 5; tick++) {
                    station.receivePower(NTConfig.graftingStationPowerUsage, Direction.UP, origin);
                    station.receiveNewPurity(3.0F, Direction.UP, origin);
                    station.commonTick();
                }
                helper.assertFalse(station.getItemStackHandler().getStackInSlot(GraftingStationBlockEntity.OUTPUT_SLOT).isEmpty(), "graft made");
                helper.assertValueEqual(1, station.getItemStackHandler().getStackInSlot(GraftingStationBlockEntity.SAMPLE_SLOT).getCount(),
                        "the sample block stays");
            }
            helper.assertFalse(station.getItemStackHandler().isItemValid(GraftingStationBlockEntity.ANCHOR_SLOT, new ItemStack(Items.STONE)),
                    "only the anchor fits its slot");
            helper.succeed();
        });

        r.add("grafting_anchor/advanced_is_faster_and_hungrier", 40, helper -> {
            helper.setBlock(MACHINE, NTBlocks.GRAFTING_STATION.get());
            GraftingStationBlockEntity station = helper.getBlockEntity(MACHINE, GraftingStationBlockEntity.class);
            int baseDuration = station.getDuration();
            int basePower = station.getRequiredPower();
            station.getItemStackHandler().setStackInSlot(GraftingStationBlockEntity.SAMPLE_SLOT, new ItemStack(Items.STONE));
            station.getItemStackHandler().setStackInSlot(GraftingStationBlockEntity.DISH_SLOT, new ItemStack(NTItems.PETRI_DISH.get()));
            station.getFluidTank().setFluid(new FluidStack(NTFluids.SALT_WATER.getStillFluid(), NTConfig.graftingStationSaltWaterUsage * 2));
            helper.assertTrue(station.getItemStackHandler().isItemValid(GraftingStationBlockEntity.ANCHOR_SLOT,
                    new ItemStack(NTItems.ADVANCED_GRAFTING_ANCHOR.get())), "the advanced anchor fits the anchor slot");
            station.getItemStackHandler().setStackInSlot(GraftingStationBlockEntity.ANCHOR_SLOT, new ItemStack(NTItems.ADVANCED_GRAFTING_ANCHOR.get()));
            helper.assertTrue(station.hasAnchor(), "the advanced anchor counts as an anchor");
            helper.assertValueEqual(station.getDuration(), (int) Math.round(baseDuration / NTConfig.advancedGraftingAnchorSpeed), "grafts faster");
            helper.assertValueEqual(station.getRequiredPower(), (int) Math.ceil(basePower * NTConfig.advancedGraftingAnchorPowerMultiplier),
                    "needs more power");
            BlockPos origin = helper.absolutePos(MACHINE.above());
            station.receivePower(basePower, Direction.UP, origin);
            station.receiveNewPurity(3.0F, Direction.UP, origin);
            station.commonTick();
            helper.assertValueEqual(station.getStatus(), GraftingStationBlockEntity.STATUS_LOW_POWER, "the base beam is too weak");
            for (int tick = 0; tick < station.getDuration() + 5; tick++) {
                station.receivePower(station.getRequiredPower(), Direction.UP, origin);
                station.receiveNewPurity(3.0F, Direction.UP, origin);
                station.commonTick();
            }
            helper.assertFalse(station.getItemStackHandler().getStackInSlot(GraftingStationBlockEntity.OUTPUT_SLOT).isEmpty(),
                    "graft made in the shortened time");
            helper.assertValueEqual(1, station.getItemStackHandler().getStackInSlot(GraftingStationBlockEntity.SAMPLE_SLOT).getCount(),
                    "the sample block stays");
            helper.succeed();
        });

        r.add("biome_tank/seeds_from_a_tagged_plant_and_keeps_it_when_broken", 60, helper -> {
            helper.setBlock(MACHINE, NTBlocks.BIOME_TANK.get());
            BiomeTankBlockEntity tank = helper.getBlockEntity(MACHINE, BiomeTankBlockEntity.class);
            Player player = helper.makeMockPlayer(GameType.SURVIVAL);
            BlockPos abs = helper.absolutePos(MACHINE);
            BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(abs), Direction.UP, abs, false);

            ItemStack dirt = new ItemStack(Items.DIRT, 4);
            InteractionResult refused = helper.getLevel().getBlockState(abs).useItemOn(dirt, helper.getLevel(), player, InteractionHand.MAIN_HAND, hit);
            helper.assertFalse(refused.consumesAction(), "an untagged item does not seed the tank");
            helper.assertTrue(tank.getPlant() == null, "the tank stays unseeded");
            helper.assertValueEqual(4, dirt.getCount(), "the untagged item is not used up");

            ItemStack bamboo = new ItemStack(Items.BAMBOO, 3);
            player.setItemInHand(InteractionHand.MAIN_HAND, bamboo);
            InteractionResult seeded = helper.getLevel().getBlockState(abs).useItemOn(bamboo, helper.getLevel(), player, InteractionHand.MAIN_HAND, hit);
            helper.assertTrue(seeded.consumesAction(), "a tagged plant seeds the tank");
            helper.assertTrue(tank.getPlant() != null && tank.getPlant().value() == Items.BAMBOO, "the tank now grows bamboo");
            helper.assertValueEqual(2, bamboo.getCount(), "seeding uses one plant");

            ItemStack drop = new ItemStack(NTBlocks.BIOME_TANK.get());
            drop.applyComponents(tank.collectComponents());
            helper.assertTrue(drop.has(NTDataComponents.BIOME_TANK_PLANT.get())
                    && drop.get(NTDataComponents.BIOME_TANK_PLANT.get()).value() == Items.BAMBOO, "the dropped tank remembers its plant");
            helper.succeed();
        });

        r.add("biome_tank/grows_its_plant_for_pipes_to_take", 120, helper -> {
            int saved = NTConfig.biomeTankTicks;
            NTConfig.biomeTankTicks = 20;
            helper.setBlock(MACHINE, NTBlocks.BIOME_TANK.get());
            helper.setBlock(MACHINE.east(2), NTBlocks.BIOME_TANK.get());
            BiomeTankBlockEntity kelp = helper.getBlockEntity(MACHINE, BiomeTankBlockEntity.class);
            kelp.setPlant(Items.KELP.builtInRegistryHolder());
            helper.getBlockEntity(MACHINE.east(2), BiomeTankBlockEntity.class).setPlant(Items.CACTUS.builtInRegistryHolder());
            helper.runAfterDelay(50, () -> {
                NTConfig.biomeTankTicks = saved;
                ItemStack grown = kelp.getItemStackHandler().getStackInSlot(0);
                helper.assertTrue(grown.is(Items.KELP), "the kelp tank grows kelp");
                helper.assertTrue(grown.getCount() >= 2, "one per cycle");
                ItemStack cactus = helper.getBlockEntity(MACHINE.east(2), BiomeTankBlockEntity.class).getItemStackHandler().getStackInSlot(0);
                helper.assertTrue(cactus.is(Items.CACTUS), "the cactus tank grows cactus");
                ResourceHandler<ItemResource> side = helper.getLevel().getCapability(Capabilities.Item.BLOCK, helper.absolutePos(MACHINE), Direction.DOWN);
                helper.assertValueEqual(0, insert(side, new ItemStack(Items.KELP)), "nothing can be piped in");
                try (Transaction tx = Transaction.openRoot()) {
                    int taken = side.extract(0, side.getResource(0), 64, tx);
                    tx.commit();
                    helper.assertTrue(taken >= 2, "pipes take the plants out");
                }
                helper.succeed();
            });
        });
    }

    private static AquaticCatalystBlockEntity catalyst(GameTestHelper helper) {
        helper.setBlock(CATALYST, NTBlocks.AQUATIC_CATALYST.get().defaultBlockState().setValue(BlockStateProperties.FACING, Direction.WEST));
        helper.setBlock(CATALYST.east(3), NTBlocks.OXYGEN_DIFFUSER.get());
        AquaticCatalystBlockEntity catalyst = helper.getBlockEntity(CATALYST, AquaticCatalystBlockEntity.class);
        helper.runAfterDelay(1, () -> catalyst.getItemStackHandler().setStackInSlot(0, new ItemStack(Items.PRISMARINE_SHARD, 64)));
        return catalyst;
    }

    private static ConduitBeaconBlockEntity buildBeacon(GameTestHelper helper, boolean frame) {
        for (int x = -2; x <= 2; x++) {
            for (int y = -2; y <= 2; y++) {
                for (int z = -2; z <= 2; z++) {
                    BlockPos pos = BEACON.offset(x, y, z);
                    if (frame && ConduitBeaconBlockEntity.isFrameSlot(x, y, z)) {
                        helper.setBlock(pos, Blocks.PRISMARINE);
                    } else if (Math.abs(x) <= 1 && Math.abs(y) <= 1 && Math.abs(z) <= 1) {
                        helper.setBlock(pos, Blocks.WATER);
                    }
                }
            }
        }
        helper.setBlock(BEACON, NTBlocks.CONDUIT_BEACON.get().defaultBlockState().setValue(BlockStateProperties.WATERLOGGED, true));
        ConduitBeaconBlockEntity beacon = helper.getBlockEntity(BEACON, ConduitBeaconBlockEntity.class);
        beacon.checkFrame(helper.getLevel().getGameTime());
        return beacon;
    }

    private static void feed(GameTestHelper helper, ConduitBeaconBlockEntity beacon, Direction travel, int power) {
        beacon.receivePower(power, travel, helper.absolutePos(BEACON.relative(travel.getOpposite(), 3)));
        beacon.receiveNewPurity(1.0F, travel, helper.absolutePos(BEACON.relative(travel.getOpposite(), 3)));
        beacon.commonTick();
    }

    private static ItemStack colonyDish(GameTestHelper helper) {
        ItemStack dish = new ItemStack(NTItems.PETRI_DISH.get());
        IBacteriaStorage storage = dish.getCapability(NTCapabilities.BacteriaStorage.ITEM);
        storage.setBacteria(0, BacteriaInstance.roll(NTBacterias.LITHOPHILES, helper.getLevel().registryAccess()).copyWithSize(100));
        return dish;
    }

    private static int insert(ResourceHandler<ItemResource> handler, ItemStack stack) {
        try (Transaction tx = Transaction.openRoot()) {
            int inserted = 0;
            for (int slot = 0; slot < handler.size() && inserted == 0; slot++) {
                inserted = handler.insert(slot, ItemResource.of(stack), stack.getCount(), tx);
            }
            tx.commit();
            return inserted;
        }
    }
}
