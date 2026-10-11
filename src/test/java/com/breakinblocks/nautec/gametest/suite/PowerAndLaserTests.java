package com.breakinblocks.nautec.gametest.suite;

import com.breakinblocks.nautec.transfer.TransferCapabilities;
import com.breakinblocks.nautec.NTConfig;
import it.unimi.dsi.fastutil.floats.FloatList;
import com.breakinblocks.nautec.capabilities.item.ItemStackHandler;
import com.breakinblocks.nautec.api.blockentities.LaserBlockEntity;
import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.capabilities.NTCapabilities;
import com.breakinblocks.nautec.capabilities.power.IPowerStorage;
import com.breakinblocks.nautec.content.blockentities.AquaticCatalystBlockEntity;
import com.breakinblocks.nautec.content.blockentities.CreativePowerSourceBlockEntity;
import com.breakinblocks.nautec.content.blockentities.EnergyConverterBlockEntity;
import com.breakinblocks.nautec.content.blockentities.LaserJunctionBlockEntity;
import com.breakinblocks.nautec.content.blockentities.PrismarineLaserRelayBlockEntity;
import com.breakinblocks.nautec.content.blockentities.MixerBlockEntity;
import com.breakinblocks.nautec.content.blockentities.ResonanceChamberBlockEntity;
import com.breakinblocks.nautec.content.blockentities.multiblock.semi.PrismarineCrystalBlockEntity;
import com.breakinblocks.nautec.content.blockentities.multiblock.semi.PrismarineCrystalPartBlockEntity;
import com.breakinblocks.nautec.content.blocks.AquaticCatalystBlock;
import com.breakinblocks.nautec.content.blocks.OpticsBlock;
import com.breakinblocks.nautec.content.blocks.PrismarineLaserRelayBlock;
import com.breakinblocks.nautec.content.blocks.multiblock.semi.PrismarineCrystalPartBlock;
import com.breakinblocks.nautec.registries.NTBlocks;
import com.breakinblocks.nautec.registries.NTItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import java.util.List;
import java.util.Set;
import com.breakinblocks.nautec.transfer.transaction.Transaction;
import com.breakinblocks.nautec.transfer.energy.EnergyHandler;


public final class PowerAndLaserTests {
    private static final BlockPos SOURCE_POS = new BlockPos(2, 1, 4);

    private PowerAndLaserTests() {
    }

    private static void placeShieldedSource(NTGameTestHelper helper, BlockPos pos, Direction... openDirections) {
        helper.setBlock(pos, NTBlocks.CREATIVE_POWER_SOURCE.get().defaultBlockState());
        Set<Direction> open = Set.of(openDirections);
        for (Direction direction : Direction.values()) {
            if (direction == Direction.DOWN || open.contains(direction)) {
                continue;
            }
            helper.setBlock(pos.relative(direction, 2), Blocks.STONE.defaultBlockState());
        }
    }

    private static void assertPurityNear(NTGameTestHelper helper, float expected, float actual, String what) {
        if (Math.abs(expected - actual) > 1.0e-3f) {
            helper.fail(what + ": expected " + expected + " but was " + actual);
        }
    }

    private static MixerBlockEntity mixer(NTGameTestHelper helper, BlockPos pos) {
        MixerBlockEntity mixer = helper.getBlockEntity(pos, MixerBlockEntity.class);
        if (mixer == null) {
            throw helper.assertionException("Expected MixerBlockEntity at " + pos);
        }
        return mixer;
    }

    private static void placeJunction(NTGameTestHelper helper, BlockPos pos, Direction output, Direction... inputs) {
        helper.setBlock(pos, NTBlocks.LASER_JUNCTION.get().defaultBlockState());
        LaserJunctionBlockEntity junction = junction(helper, pos);
        junction.getLaserOutputs().add(output);
        for (Direction input : inputs) {
            junction.getLaserInputs().add(input);
        }
    }

    private static void placeJunctionLoop(NTGameTestHelper helper, BlockPos entry) {
        placeJunction(helper, entry, Direction.SOUTH, Direction.WEST, Direction.EAST);
        placeJunction(helper, entry.south(3), Direction.EAST, Direction.NORTH);
        placeJunction(helper, entry.south(3).east(3), Direction.NORTH, Direction.WEST);
        placeJunction(helper, entry.east(3), Direction.WEST, Direction.SOUTH);
    }

    private static LaserJunctionBlockEntity junction(NTGameTestHelper helper, BlockPos pos) {
        LaserJunctionBlockEntity junction = helper.getBlockEntity(pos, LaserJunctionBlockEntity.class);
        if (junction == null) {
            throw helper.assertionException("Expected LaserJunctionBlockEntity at " + pos);
        }
        return junction;
    }

    private static IPowerStorage batteryStorage(NTGameTestHelper helper, ItemStack stack) {
        IPowerStorage storage = stack.getCapability(NTCapabilities.PowerStorage.ITEM);
        if (storage == null) {
            throw helper.assertionException("Prismatic battery did not expose item power capability");
        }
        return storage;
    }

    public static void register(NTTestRegistrar r) {
        r.add("power/item_fill_respects_max_input_and_capacity", 20, helper -> {
            ItemStack stack = new ItemStack(NTItems.PRISMATIC_BATTERY.get());
            IPowerStorage storage = batteryStorage(helper, stack);

            helper.assertValueEqual(0, storage.getPowerStored(), "initial stored power");
            helper.assertValueEqual(10000, storage.getPowerCapacity(), "battery capacity");
            helper.assertValueEqual(128, storage.getMaxInput(), "battery max input");

            helper.assertValueEqual(128, storage.tryFillPower(500, false), "fill clamped to max input");
            helper.assertValueEqual(128, storage.getPowerStored(), "stored after clamped fill");

            helper.assertValueEqual(0, storage.tryFillPower(0, false), "fill of zero");
            helper.assertValueEqual(0, storage.tryFillPower(-5, false), "fill of negative");

            storage.setPowerStored(9990);
            helper.assertValueEqual(10, storage.tryFillPower(500, false), "fill clamped to remaining capacity");
            helper.assertValueEqual(10000, storage.getPowerStored(), "stored at capacity");
            helper.assertValueEqual(0, storage.tryFillPower(1, false), "fill when full");

            helper.succeed();
        });

        r.add("power/item_drain_respects_max_output", 20, helper -> {
            ItemStack stack = new ItemStack(NTItems.PRISMATIC_BATTERY.get());
            IPowerStorage storage = batteryStorage(helper, stack);
            helper.assertValueEqual(100, storage.getMaxOutput(), "battery max output");

            storage.setPowerStored(500);
            helper.assertValueEqual(40, storage.tryDrainPower(40, false), "partial drain");
            helper.assertValueEqual(460, storage.getPowerStored(), "stored after partial drain");

            helper.assertValueEqual(100, storage.tryDrainPower(500, false), "drain clamped to max output");
            helper.assertValueEqual(360, storage.getPowerStored(), "stored after clamped drain");

            storage.setPowerStored(30);
            helper.assertValueEqual(30, storage.tryDrainPower(500, false), "drain clamped to stored power");
            helper.assertValueEqual(0, storage.getPowerStored(), "stored after emptying");
            helper.assertValueEqual(0, storage.tryDrainPower(5, false), "drain when empty");

            helper.succeed();
        });

        r.add("power/simulate_does_not_mutate", 20, helper -> {
            ItemStack stack = new ItemStack(NTItems.PRISMATIC_BATTERY.get());
            IPowerStorage storage = batteryStorage(helper, stack);

            helper.assertValueEqual(128, storage.tryFillPower(500, true), "simulated fill result");
            helper.assertValueEqual(0, storage.getPowerStored(), "stored unchanged after simulated fill");

            storage.setPowerStored(250);
            helper.assertValueEqual(100, storage.tryDrainPower(500, true), "simulated drain result");
            helper.assertValueEqual(250, storage.getPowerStored(), "stored unchanged after simulated drain");

            helper.succeed();
        });

        r.add("power/purity_get_set_persists", 20, helper -> {
            ItemStack stack = new ItemStack(NTItems.PRISMATIC_BATTERY.get());
            IPowerStorage storage = batteryStorage(helper, stack);

            helper.assertValueEqual(0.0f, storage.getPurity(), "initial purity");
            storage.setPurity(0.5f);
            helper.assertValueEqual(0.5f, storage.getPurity(), "purity after set");

            storage.setPowerStored(64);
            helper.assertValueEqual(0.5f, storage.getPurity(), "purity preserved after power change");
            helper.assertValueEqual(64, storage.getPowerStored(), "stored preserved after purity set");

            IPowerStorage freshWrapper = batteryStorage(helper, stack);
            helper.assertValueEqual(0.5f, freshWrapper.getPurity(), "purity persisted in data component");

            helper.succeed();
        });

        r.add("power/item_capability_exposure", 20, helper -> {
            ItemStack battery = new ItemStack(NTItems.PRISMATIC_BATTERY.get());
            helper.assertTrue(battery.getCapability(NTCapabilities.PowerStorage.ITEM) != null,
                    "Prismatic battery should expose the item power capability");

            ItemStack stick = new ItemStack(Items.STICK);
            helper.assertTrue(stick.getCapability(NTCapabilities.PowerStorage.ITEM) == null,
                    "Plain stick should not expose the item power capability");

            helper.succeed();
        });

        r.add("laser/source_transmits_power_to_mixer", 100, helper -> {
            BlockPos mixerPos = new BlockPos(5, 1, 4);
            placeShieldedSource(helper, SOURCE_POS, Direction.EAST);
            helper.setBlock(mixerPos, NTBlocks.MIXER.get().defaultBlockState());

            helper.runAfterDelay(60, () -> {
                CreativePowerSourceBlockEntity source = helper.getBlockEntity(SOURCE_POS, CreativePowerSourceBlockEntity.class);
                if (source == null) {
                    helper.fail("Expected CreativePowerSourceBlockEntity");
                    return;
                }
                helper.assertValueEqual(3, source.getLaserDistances().getInt(Direction.EAST), "laser distance to mixer");
                helper.assertValueEqual(100, mixer(helper, mixerPos).getPower(), "mixer received power");
                helper.succeed();
            });
        });

        r.add("laser/beam_blocked_by_obstruction", 140, helper -> {
            BlockPos mixerPos = new BlockPos(6, 1, 4);
            BlockPos obstructionPos = new BlockPos(4, 1, 4);
            placeShieldedSource(helper, SOURCE_POS, Direction.EAST);
            helper.setBlock(mixerPos, NTBlocks.MIXER.get().defaultBlockState());

            helper.runAfterDelay(50, () -> {
                helper.assertValueEqual(100, mixer(helper, mixerPos).getPower(), "mixer powered before obstruction");
                helper.setBlock(obstructionPos, Blocks.STONE.defaultBlockState());
            });

            helper.runAfterDelay(110, () -> {
                CreativePowerSourceBlockEntity source = helper.getBlockEntity(SOURCE_POS, CreativePowerSourceBlockEntity.class);
                if (source == null) {
                    helper.fail("Expected CreativePowerSourceBlockEntity");
                    return;
                }
                helper.assertValueEqual(0, source.getLaserDistances().getInt(Direction.EAST), "laser distance after obstruction");
                helper.assertValueEqual(0, mixer(helper, mixerPos).getPower(), "mixer power after obstruction");
                helper.succeed();
            });
        });

        r.add("laser/relay_passes_power_through", 100, helper -> {
            BlockPos relayPos = new BlockPos(4, 1, 4);
            BlockPos mixerPos = new BlockPos(6, 1, 4);
            placeShieldedSource(helper, SOURCE_POS, Direction.EAST);
            helper.setBlock(relayPos, NTBlocks.PRISMARINE_RELAY.get().defaultBlockState()
                    .setValue(PrismarineLaserRelayBlock.FACING, Direction.EAST));
            helper.setBlock(mixerPos, NTBlocks.MIXER.get().defaultBlockState());

            helper.runAfterDelay(60, () -> {
                helper.assertValueEqual(100, mixer(helper, mixerPos).getPower(), "mixer power through relay");
                helper.succeed();
            });
        });

        r.add("laser/relay_facing_gates_input", 100, helper -> {
            BlockPos relayPos = new BlockPos(4, 1, 4);
            BlockPos mixerPos = new BlockPos(6, 1, 4);
            placeShieldedSource(helper, SOURCE_POS, Direction.EAST);
            helper.setBlock(relayPos, NTBlocks.PRISMARINE_RELAY.get().defaultBlockState()
                    .setValue(PrismarineLaserRelayBlock.FACING, Direction.NORTH));
            helper.setBlock(new BlockPos(4, 1, 2), Blocks.STONE.defaultBlockState());
            helper.setBlock(mixerPos, NTBlocks.MIXER.get().defaultBlockState());

            helper.runAfterDelay(60, () -> {
                CreativePowerSourceBlockEntity source = helper.getBlockEntity(SOURCE_POS, CreativePowerSourceBlockEntity.class);
                if (source == null) {
                    helper.fail("Expected CreativePowerSourceBlockEntity");
                    return;
                }
                helper.assertValueEqual(0, source.getLaserDistances().getInt(Direction.EAST), "no connection into wrongly-faced relay");
                helper.assertValueEqual(0, mixer(helper, mixerPos).getPower(), "mixer unpowered behind wrongly-faced relay");
                helper.succeed();
            });
        });

        r.add("laser/lens_raises_purity", 120, helper -> {
            BlockPos lensPos = new BlockPos(4, 1, 4);
            BlockPos mixerPos = new BlockPos(6, 1, 4);
            placeShieldedSource(helper, SOURCE_POS, Direction.EAST);
            helper.setBlock(lensPos, NTBlocks.FOCUSING_LENS.get().defaultBlockState()
                    .setValue(OpticsBlock.FACING, Direction.EAST));
            helper.setBlock(mixerPos, NTBlocks.MIXER.get().defaultBlockState());

            CreativePowerSourceBlockEntity source = helper.getBlockEntity(SOURCE_POS, CreativePowerSourceBlockEntity.class);
            if (source == null) {
                helper.fail("Expected CreativePowerSourceBlockEntity");
                return;
            }
            source.setPurity(1.0f);

            helper.runAfterDelay(80, () -> {
                MixerBlockEntity mixer = mixer(helper, mixerPos);
                helper.assertValueEqual(100, mixer.getPower(), "power straight through a lens");
                assertPurityNear(helper, 1.0f + (float) NTConfig.lensPurityBonus, mixer.getPurity(),
                        "purity after a focusing lens");
                helper.succeed();
            });
        });

        r.add("laser/purity_clears_when_source_is_removed", 160, helper -> {
            BlockPos mixerPos = new BlockPos(6, 1, 4);
            placeShieldedSource(helper, SOURCE_POS, Direction.EAST);
            helper.setBlock(new BlockPos(4, 1, 4), NTBlocks.PRISMARINE_RELAY.get().defaultBlockState()
                    .setValue(PrismarineLaserRelayBlock.FACING, Direction.EAST));
            helper.setBlock(mixerPos, NTBlocks.MIXER.get().defaultBlockState());

            CreativePowerSourceBlockEntity source = helper.getBlockEntity(SOURCE_POS, CreativePowerSourceBlockEntity.class);
            if (source == null) {
                helper.fail("Expected CreativePowerSourceBlockEntity");
                return;
            }
            source.setPurity(2.0f);

            helper.runAfterDelay(60, () -> {
                assertPurityNear(helper, 2.0f, mixer(helper, mixerPos).getPurity(), "purity while the source runs");
                helper.setBlock(SOURCE_POS, Blocks.AIR.defaultBlockState());
            });

            helper.runAfterDelay(140, () -> {
                MixerBlockEntity mixer = mixer(helper, mixerPos);
                helper.assertValueEqual(0, mixer.getPower(), "power after the source is gone");
                assertPurityNear(helper, 0f, mixer.getPurity(),
                        "purity should fall to zero once the source is removed, not stick at the old value");
                helper.succeed();
            });
        });

        r.add("laser/junction_loop_does_not_amplify", 200, helper -> {
            BlockPos entry = new BlockPos(4, 1, 4);
            BlockPos last = new BlockPos(7, 1, 4);
            placeShieldedSource(helper, SOURCE_POS, Direction.EAST);
            placeJunctionLoop(helper, entry);

            helper.runAfterDelay(80, () -> {
                helper.assertValueEqual(100, junction(helper, entry).getPower(), "entry junction carries only the source");
                helper.assertValueEqual(100, junction(helper, last).getPower(), "last junction in the loop");
                helper.assertTrue(junction(helper, last).loopsBack(Direction.WEST), "beam back into the entry junction is cut");
                helper.assertFalse(junction(helper, last).shouldRender(Direction.WEST), "cut beam is not drawn");
            });

            helper.runAfterDelay(160, () -> {
                helper.assertValueEqual(100, junction(helper, entry).getPower(), "entry junction after many more laps");
                helper.assertValueEqual(100, junction(helper, last).getPower(), "last junction after many more laps");
                helper.succeed();
            });
        });

        r.add("laser/junction_loop_empties_without_its_source", 200, helper -> {
            BlockPos entry = new BlockPos(4, 1, 4);
            placeShieldedSource(helper, SOURCE_POS, Direction.EAST);
            placeJunctionLoop(helper, entry);

            helper.runAfterDelay(80, () -> {
                helper.assertValueEqual(100, junction(helper, entry).getPower(), "loop is powered while the source runs");
                helper.setBlock(SOURCE_POS, Blocks.AIR.defaultBlockState());
            });

            helper.runAfterDelay(160, () -> {
                for (BlockPos pos : List.of(entry, new BlockPos(4, 1, 7), new BlockPos(7, 1, 7), new BlockPos(7, 1, 4))) {
                    helper.assertValueEqual(0, junction(helper, pos).getPower(), "junction at " + pos + " after the source is gone");
                }
                helper.succeed();
            });
        });

        r.add("laser/junction_splits_without_purity_loss", 120, helper -> {
            BlockPos northMixer = new BlockPos(4, 1, 2);
            BlockPos southMixer = new BlockPos(4, 1, 6);
            placeShieldedSource(helper, SOURCE_POS, Direction.EAST);
            placeJunction(helper, new BlockPos(4, 1, 4), Direction.NORTH, Direction.WEST);
            junction(helper, new BlockPos(4, 1, 4)).getLaserOutputs().add(Direction.SOUTH);
            helper.setBlock(northMixer, NTBlocks.MIXER.get().defaultBlockState());
            helper.setBlock(southMixer, NTBlocks.MIXER.get().defaultBlockState());

            CreativePowerSourceBlockEntity source = helper.getBlockEntity(SOURCE_POS, CreativePowerSourceBlockEntity.class);
            source.setPurity(2.0f);

            helper.runAfterDelay(80, () -> {
                helper.assertValueEqual(50, mixer(helper, northMixer).getPower(), "north branch power");
                helper.assertValueEqual(50, mixer(helper, southMixer).getPower(), "south branch power");
                assertPurityNear(helper, 2.0f, mixer(helper, northMixer).getPurity(), "purity on a split branch");
                helper.succeed();
            });
        });

        r.add("resonance/ceiling_scales_with_purity", 20, helper -> {
            float base = (float) NTConfig.resonanceBaseCeiling;
            assertPurityNear(helper, base, ResonanceChamberBlockEntity.stabilityCeiling(0f), "ceiling at zero purity");
            assertPurityNear(helper, base * 3f, ResonanceChamberBlockEntity.stabilityCeiling(2f), "ceiling at purity 2");
            assertPurityNear(helper, base, ResonanceChamberBlockEntity.stabilityCeiling(-1f), "negative purity clamps to base");
            helper.succeed();
        });

        r.add("resonance/crafts_in_the_critical_band", 400, helper -> {
            BlockPos chamberPos = new BlockPos(4, 1, 4);
            placeShieldedSource(helper, SOURCE_POS, Direction.EAST);
            helper.setBlock(chamberPos, NTBlocks.RESONANCE_CHAMBER.get().defaultBlockState());

            CreativePowerSourceBlockEntity source = helper.getBlockEntity(SOURCE_POS, CreativePowerSourceBlockEntity.class);
            ResonanceChamberBlockEntity chamber = helper.getBlockEntity(chamberPos, ResonanceChamberBlockEntity.class);
            if (source == null || chamber == null) {
                helper.fail("Expected a power source and a resonance chamber");
                return;
            }
            source.setPurity(3.0f);
            chamber.getItemStackHandler().setStackInSlot(0, new ItemStack(NTItems.PRISMARINE_CRYSTAL_SHARD.get()));

            helper.succeedWhen(() -> {
                ItemStack result = chamber.getItemStackHandler().getStackInSlot(1);
                helper.assertTrue(result.is(NTItems.RESONANT_SHARD.get()),
                        "Chamber should have produced a Resonant Shard, slot held " + result);
                helper.assertTrue(chamber.getItemStackHandler().getStackInSlot(0).isEmpty(),
                        "The crystal shard should have been consumed");
                helper.assertFalse(chamber.isVenting(), "A successful craft should not vent");
            });
        });

        r.add("resonance/low_purity_will_not_craft_tier_three", 300, helper -> {
            BlockPos chamberPos = new BlockPos(4, 1, 4);
            placeShieldedSource(helper, SOURCE_POS, Direction.EAST);
            helper.setBlock(chamberPos, NTBlocks.RESONANCE_CHAMBER.get().defaultBlockState());

            CreativePowerSourceBlockEntity source = helper.getBlockEntity(SOURCE_POS, CreativePowerSourceBlockEntity.class);
            ResonanceChamberBlockEntity chamber = helper.getBlockEntity(chamberPos, ResonanceChamberBlockEntity.class);
            if (source == null || chamber == null) {
                helper.fail("Expected a power source and a resonance chamber");
                return;
            }
            source.setPurity(1.0f);
            chamber.getItemStackHandler().setStackInSlot(0, new ItemStack(NTItems.PRISMARINE_CRYSTAL_SHARD.get()));

            helper.runAfterDelay(280, () -> {
                helper.assertTrue(chamber.getItemStackHandler().getStackInSlot(1).isEmpty(),
                        "A purity 1 beam should never make a Resonant Shard");
                helper.assertTrue(chamber.getItemStackHandler().getStackInSlot(0).is(NTItems.PRISMARINE_CRYSTAL_SHARD.get()),
                        "The crystal shard should still be sitting there uncrafted");
                helper.succeed();
            });
        });

        r.add("resonance/vent_resets_charge_and_locks_out", 300, helper -> {
            BlockPos chamberPos = new BlockPos(4, 1, 4);
            placeShieldedSource(helper, SOURCE_POS, Direction.EAST);
            helper.setBlock(chamberPos, NTBlocks.RESONANCE_CHAMBER.get().defaultBlockState());

            CreativePowerSourceBlockEntity source = helper.getBlockEntity(SOURCE_POS, CreativePowerSourceBlockEntity.class);
            ResonanceChamberBlockEntity chamber = helper.getBlockEntity(chamberPos, ResonanceChamberBlockEntity.class);
            if (source == null || chamber == null) {
                helper.fail("Expected a power source and a resonance chamber");
                return;
            }
            source.setPurity(0f);

            helper.runAfterDelay(260, () -> {
                helper.assertTrue(chamber.isVenting(), "An empty chamber left charging should vent");
                helper.assertValueEqual(0.0f, chamber.getCharge(), "charge after venting");
                helper.succeed();
            });
        });

        r.add("laser/junction_routes_power", 100, helper -> {
            BlockPos junctionPos = new BlockPos(4, 1, 4);
            BlockPos mixerPos = new BlockPos(6, 1, 4);
            placeShieldedSource(helper, SOURCE_POS, Direction.EAST);
            helper.setBlock(junctionPos, NTBlocks.LASER_JUNCTION.get().defaultBlockState());
            helper.setBlock(mixerPos, NTBlocks.MIXER.get().defaultBlockState());

            LaserJunctionBlockEntity junction = helper.getBlockEntity(junctionPos, LaserJunctionBlockEntity.class);
            if (junction == null) {
                helper.fail("Expected LaserJunctionBlockEntity");
                return;
            }
            junction.getLaserInputs().add(Direction.WEST);
            junction.getLaserOutputs().add(Direction.EAST);

            helper.runAfterDelay(60, () -> {
                helper.assertValueEqual(100, junction.getPower(), "junction received power");
                helper.assertValueEqual(100, mixer(helper, mixerPos).getPower(), "mixer power through junction");
                helper.succeed();
            });
        });

        r.add("laser/relay_reaches_long_range", 100, helper -> {
            BlockPos relayPos = new BlockPos(4, 1, 4);
            BlockPos mixerPos = new BlockPos(8, 1, 4);
            placeShieldedSource(helper, SOURCE_POS, Direction.EAST);
            helper.setBlock(relayPos, NTBlocks.PRISMARINE_RELAY.get().defaultBlockState()
                    .setValue(PrismarineLaserRelayBlock.FACING, Direction.EAST));
            helper.setBlock(mixerPos, NTBlocks.MIXER.get().defaultBlockState());

            helper.runAfterDelay(60, () -> {
                PrismarineLaserRelayBlockEntity relay = helper.getBlockEntity(relayPos, PrismarineLaserRelayBlockEntity.class);
                helper.assertValueEqual(64, relay.getMaxLaserDistance(), "relay range");
                helper.assertValueEqual(100, mixer(helper, mixerPos).getPower(), "mixer power through the relay");
                helper.succeed();
            });
        });

        registerCrystal(r);
        registerEnergyBridge(r);
    }

    private static void registerCrystal(NTTestRegistrar r) {
        r.add("crystal/geode_template_places_part_indexes", 40, helper -> {
            ServerLevel level = helper.getLevel();
            StructureTemplate template = level.getStructureManager()
                    .get(Nautec.rl("stone_crystal_geode"))
                    .orElseThrow(() -> helper.assertionException("stone_crystal_geode template missing"));
            BoundingBox arena = BoundingBox.fromCorners(helper.absolutePos(BlockPos.ZERO), helper.absolutePos(new BlockPos(8, 8, 8)));
            StructurePlaceSettings settings = new StructurePlaceSettings()
                    .setBoundingBox(arena)
                    .setLiquidSettings(LiquidSettings.APPLY_WATERLOGGING);
            template.placeInWorld(level, helper.absolutePos(new BlockPos(-6, -2, -5)), BlockPos.ZERO, settings, level.getRandom(), 18);

            helper.runAfterDelay(5, () -> {
                int[] expected = {5, 4, 3, -1, 1, 0};
                for (int i = 0; i < expected.length; i++) {
                    BlockPos pos = new BlockPos(4, 1 + i, 4);
                    if (expected[i] < 0) {
                        helper.assertBlockPresent(NTBlocks.PRISMARINE_CRYSTAL.get(), pos);
                        continue;
                    }
                    helper.assertBlockPresent(NTBlocks.PRISMARINE_CRYSTAL_PART.get(), pos);
                    helper.assertValueEqual(expected[i], helper.getBlockState(pos).getValue(PrismarineCrystalPartBlock.INDEX), "index at " + pos);
                    helper.assertTrue(helper.getBlockEntity(pos, PrismarineCrystalPartBlockEntity.class).getCrystalBE() != null,
                            "part at " + pos + " should find the crystal core");
                }
                helper.succeed();
            });
        });

        r.add("crystal/catalyst_beam_reaches_core", 100, helper -> {
            BlockPos corePos = new BlockPos(4, 5, 4);
            placeCrystal(helper, corePos);
            BlockPos catalystPos = new BlockPos(1, 5, 4);
            helper.setBlock(catalystPos, NTBlocks.AQUATIC_CATALYST.get().defaultBlockState()
                    .setValue(BlockStateProperties.FACING, Direction.WEST));
            AquaticCatalystBlockEntity catalyst = helper.getBlockEntity(catalystPos, AquaticCatalystBlockEntity.class);
            helper.runAfterDelay(1, () ->
                    catalyst.getItemStackHandler().setStackInSlot(0, new ItemStack(Items.PRISMARINE_SHARD, 4)));

            helper.runAfterDelay(40, () -> {
                helper.assertTrue(catalyst.isActive(), "catalyst burning fuel");
                helper.assertValueEqual(3, catalyst.getLaserDistances().getInt(Direction.EAST), "catalyst beam length to the core");
                helper.assertTrue(helper.getBlockEntity(corePos, PrismarineCrystalBlockEntity.class).getPower() > 0, "core received catalyst power");
                helper.assertTrue(helper.getBlockState(catalystPos).getValue(AquaticCatalystBlock.ACTIVE), "active blockstate set");
                helper.succeed();
            });
        });

        r.add("crystal/bottom_beam_transforms_item_on_floor", 400, helper -> {
            BlockPos corePos = new BlockPos(4, 6, 4);
            placeCrystal(helper, corePos);
            helper.setBlock(new BlockPos(4, 1, 4), Blocks.STONE.defaultBlockState());
            BlockPos catalystPos = new BlockPos(1, 6, 4);
            helper.setBlock(catalystPos, NTBlocks.AQUATIC_CATALYST.get().defaultBlockState()
                    .setValue(BlockStateProperties.FACING, Direction.WEST));
            AquaticCatalystBlockEntity catalyst = helper.getBlockEntity(catalystPos, AquaticCatalystBlockEntity.class);
            helper.runAfterDelay(1, () ->
                    catalyst.getItemStackHandler().setStackInSlot(0, new ItemStack(Items.PRISMARINE_SHARD, 16)));
            helper.runAfterDelay(20, () -> {
                BlockPos drop = helper.absolutePos(new BlockPos(4, 2, 4));
                ItemEntity coil = new ItemEntity(helper.getLevel(), drop.getX() + 0.15, drop.getY() + 0.1, drop.getZ() + 0.8,
                        new ItemStack(NTItems.BURNT_COIL.get()));
                coil.setDeltaMovement(0, 0, 0);
                helper.getLevel().addFreshEntity(coil);
            });

            helper.succeedWhen(() -> {
                AABB area = new AABB(helper.absolutePos(new BlockPos(3, 1, 3)).getCenter(), helper.absolutePos(new BlockPos(5, 4, 5)).getCenter());
                boolean made = helper.getLevel().getEntitiesOfClass(ItemEntity.class, area).stream()
                        .anyMatch(e -> e.getItem().is(NTItems.LASER_CHANNELING_COIL.get()));
                helper.assertTrue(made, "burnt coil under the crystal should become a laser channeling coil");
            });
        });

        r.add("crystal/powered_core_emits_from_top_and_bottom", 100, helper -> {
            BlockPos corePos = new BlockPos(4, 5, 4);
            BlockPos topPos = corePos.above(2);
            BlockPos bottomPos = corePos.below(3);
            for (int i = 0; i < 6; i++) {
                BlockPos pos = topPos.below(i);
                if (i == 2) {
                    helper.setBlock(pos, NTBlocks.PRISMARINE_CRYSTAL.get().defaultBlockState());
                } else {
                    helper.setBlock(pos, NTBlocks.PRISMARINE_CRYSTAL_PART.get().defaultBlockState()
                            .setValue(PrismarineCrystalPartBlock.INDEX, i));
                }
            }
            placeShieldedSource(helper, new BlockPos(2, 5, 4), Direction.EAST);

            helper.runAfterDelay(60, () -> {
                helper.assertValueEqual(100, helper.getBlockEntity(corePos, PrismarineCrystalBlockEntity.class).getPower(), "core received power");
                PrismarineCrystalPartBlockEntity top = helper.getBlockEntity(topPos, PrismarineCrystalPartBlockEntity.class);
                PrismarineCrystalPartBlockEntity bottom = helper.getBlockEntity(bottomPos, PrismarineCrystalPartBlockEntity.class);
                helper.assertValueEqual(100, top.getPowerToTransfer(), "top part output");
                helper.assertValueEqual(100, bottom.getPowerToTransfer(), "bottom part output");
                helper.assertTrue(top.getLaserDistances().getInt(Direction.UP) > 0, "top beam has a length");
                helper.assertTrue(bottom.getLaserDistances().getInt(Direction.DOWN) > 0, "bottom beam has a length");
                helper.assertTrue(top.shouldRender(Direction.UP), "top beam should render");
                helper.assertTrue(bottom.shouldRender(Direction.DOWN), "bottom beam should render");
                helper.succeed();
            });
        });
    }

    private static void placeCrystal(NTGameTestHelper helper, BlockPos corePos) {
        BlockPos topPos = corePos.above(2);
        for (int i = 0; i < 6; i++) {
            BlockPos pos = topPos.below(i);
            if (i == 2) {
                helper.setBlock(pos, NTBlocks.PRISMARINE_CRYSTAL.get().defaultBlockState());
            } else {
                helper.setBlock(pos, NTBlocks.PRISMARINE_CRYSTAL_PART.get().defaultBlockState()
                        .setValue(PrismarineCrystalPartBlock.INDEX, i));
            }
        }
    }

    private static final int FE_BUFFER = 1_000_000;
    private static final int AP_PER_TICK = 150;

    private static void registerEnergyBridge(NTTestRegistrar r) {
        r.add("power/creative_energy_source_exposes_energy_capability", 20, helper -> {
            BlockPos pos = new BlockPos(4, 1, 4);
            helper.setBlock(pos, NTBlocks.CREATIVE_ENERGY_SOURCE.get().defaultBlockState());

            EnergyHandler handler = energyAt(helper, pos);
            if (handler == null) {
                helper.fail("Creative Energy Source should expose the energy capability");
                return;
            }

            try (Transaction tx = Transaction.openRoot()) {
                helper.assertValueEqual(500, handler.extract(500, tx), "creative source extraction");
                helper.assertValueEqual(0, handler.insert(500, tx), "creative source rejects insertion");
                tx.commit();
            }
            helper.succeed();
        });

        r.add("power/energy_converter_accepts_fe_and_converts_it", 120, helper -> {
            BlockPos converterPos = new BlockPos(2, 1, 4);
            BlockPos mixerPos = new BlockPos(5, 1, 4);
            helper.setBlock(converterPos, NTBlocks.ENERGY_CONVERTER.get().defaultBlockState());
            helper.setBlock(mixerPos, NTBlocks.MIXER.get().defaultBlockState());

            EnergyHandler handler = energyAt(helper, converterPos);
            if (handler == null) {
                helper.fail("Energy Converter should expose the energy capability");
                return;
            }

            int accepted;
            try (Transaction tx = Transaction.openRoot()) {
                accepted = handler.insert(FE_BUFFER, tx);
                helper.assertValueEqual(0, handler.extract(100, tx), "converter refuses to give FE back");
                tx.commit();
            }
            helper.assertValueEqual(FE_BUFFER, accepted, "FE accepted by converter");
            helper.assertValueEqual(FE_BUFFER, handler.getAmountAsInt(), "FE buffered by converter");

            helper.runAfterDelay(60, () -> {
                helper.assertTrue(handler.getAmountAsInt() < FE_BUFFER, "converter should drain its FE buffer");
                helper.assertValueEqual(AP_PER_TICK, mixer(helper, mixerPos).getPower(), "converted power reaching the mixer");
                helper.succeed();
            });
        });

        r.add("power/energy_conversion_upgrades_raise_the_max_rate", 20, helper -> {
            BlockPos converterPos = new BlockPos(2, 1, 4);
            helper.setBlock(converterPos, NTBlocks.ENERGY_CONVERTER.get().defaultBlockState());
            EnergyConverterBlockEntity converter = helper.getBlockEntity(converterPos, EnergyConverterBlockEntity.class);
            ItemStackHandler upgrades = converter.getItemStackHandler();

            ItemStack basic = new ItemStack(NTItems.ENERGY_CONVERSION_UPGRADE.get(), 8);
            ItemStack advanced = new ItemStack(NTItems.ADVANCED_ENERGY_CONVERSION_UPGRADE.get(), 8);
            ItemStack ultimate = new ItemStack(NTItems.ULTIMATE_ENERGY_CONVERSION_UPGRADE.get(), 8);
            helper.assertValueEqual(8, upgrades.insertItem(0, advanced.copy(), false).getCount(), "the basic slot refuses an advanced upgrade");
            helper.assertValueEqual(8, upgrades.insertItem(1, ultimate.copy(), false).getCount(), "the advanced slot refuses an ultimate upgrade");
            helper.assertValueEqual(8, upgrades.insertItem(2, basic.copy(), false).getCount(), "the ultimate slot refuses a basic upgrade");

            helper.assertTrue(upgrades.insertItem(0, basic.copy(), false).isEmpty(), "the basic slot takes 8 basic upgrades");
            helper.assertValueEqual(1, upgrades.insertItem(0, basic.copyWithCount(1), false).getCount(), "the basic slot holds no more than 8");
            int withBasic = NTConfig.energyConverterBaseAp + 8 * NTConfig.energyConversionUpgradeAp;
            helper.assertValueEqual(withBasic, converter.getMaxRate(), "8 basic upgrades raise the max rate");

            upgrades.insertItem(1, advanced.copy(), false);
            upgrades.insertItem(2, ultimate.copy(), false);
            int full = withBasic + 8 * NTConfig.advancedEnergyConversionUpgradeAp + 8 * NTConfig.ultimateEnergyConversionUpgradeAp;
            helper.assertValueEqual(full, converter.getMaxRate(), "all three tiers add up");
            helper.assertValueEqual(full, converter.getRate(), "an unset rate follows the max");

            converter.setRate(full);
            upgrades.setStackInSlot(2, ItemStack.EMPTY);
            int withoutUltimate = full - 8 * NTConfig.ultimateEnergyConversionUpgradeAp;
            helper.assertValueEqual(withoutUltimate, converter.getRate(), "removing upgrades pulls the rate down to the new max");
            helper.succeed();
        });

        r.add("power/merged_beams_keep_close_to_the_purest", 20, helper -> {
            float drop = (float) NTConfig.beamMergePurityDrop;
            helper.assertValueEqual(0F, LaserBlockEntity.mergedPurity(FloatList.of()), "no beams give no purity");
            helper.assertValueEqual(1.2F, LaserBlockEntity.mergedPurity(FloatList.of(1.2F)), "one beam keeps its purity");
            helper.assertValueEqual(3F - 1.5F * drop, LaserBlockEntity.mergedPurity(FloatList.of(3F, 0F)),
                    "a crystal beam merged with a converter beam keeps most of its purity");
            helper.assertTrue(LaserBlockEntity.mergedPurity(FloatList.of(3F, 0F)) > 2.5F,
                    "a 3.0 beam joined by a 0 beam still runs Deep Steel Plating");
            helper.succeed();
        });

        r.add("power/energy_converter_rate_caps_output_and_spends_fe_per_ap", 120, helper -> {
            BlockPos converterPos = new BlockPos(2, 1, 4);
            BlockPos mixerPos = new BlockPos(5, 1, 4);
            helper.setBlock(converterPos, NTBlocks.ENERGY_CONVERTER.get().defaultBlockState());
            helper.setBlock(mixerPos, NTBlocks.MIXER.get().defaultBlockState());
            EnergyConverterBlockEntity converter = helper.getBlockEntity(converterPos, EnergyConverterBlockEntity.class);
            helper.assertValueEqual(NTConfig.energyConverterBaseAp, converter.getRate(), "a new converter runs at the base max rate");
            converter.setRate(NTConfig.energyConverterBaseAp + 50);
            helper.assertValueEqual(NTConfig.energyConverterBaseAp, converter.getRate(), "the rate is capped at the base max");
            converter.setRate(25);
            helper.assertValueEqual(25, converter.getRate(), "the rate takes a lower setting");

            try (Transaction tx = Transaction.openRoot()) {
                converter.getFeBuffer().insert(FE_BUFFER, tx);
                tx.commit();
            }

            int[] before = new int[1];
            helper.runAfterDelay(40, () -> before[0] = converter.getFeStored());
            helper.runAfterDelay(50, () -> {
                helper.assertValueEqual(25, converter.getSending(), "the converter sends the set rate");
                helper.assertValueEqual(25, mixer(helper, mixerPos).getPower(), "the mixer receives the set rate");
                helper.assertValueEqual(10 * 25 * NTConfig.energyConverterFePerAp, before[0] - converter.getFeStored(),
                        "ten ticks at 25 AP spend energyConverterFePerAp FE for each AP");
                converter.setRate(0);
            });
            helper.runAfterDelay(60, () -> {
                helper.assertValueEqual(0, converter.getSending(), "a rate of 0 stops the beam");
                int stored = converter.getFeStored();
                helper.runAfterDelay(5, () -> {
                    helper.assertValueEqual(stored, converter.getFeStored(), "a stopped converter keeps its FE");
                    helper.succeed();
                });
            });
        });
    }

    private static EnergyHandler energyAt(NTGameTestHelper helper, BlockPos pos) {
        BlockPos absolute = helper.absolutePos(pos);
        return helper.getLevel().getCapability(TransferCapabilities.Energy.BLOCK, absolute, null);
    }
}
