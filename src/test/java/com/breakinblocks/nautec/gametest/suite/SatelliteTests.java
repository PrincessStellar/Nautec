package com.breakinblocks.nautec.gametest.suite;

import com.breakinblocks.nautec.api.blockentities.LaserBlockEntity;
import com.breakinblocks.nautec.content.resonance.ResonanceNodeBlockEntity;
import org.jetbrains.annotations.Nullable;
import com.breakinblocks.nautec.content.resonance.ResonanceNodeBlock;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.minecraft.world.item.ItemStack;
import com.breakinblocks.nautec.content.resonance.ResonanceCharmItem;
import com.breakinblocks.nautec.content.items.MachineSettings;
import com.breakinblocks.nautec.content.items.ConfigurationCardItem;
import com.breakinblocks.nautec.capabilities.power.IPowerStorage;
import com.breakinblocks.nautec.capabilities.NTCapabilities;
import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.content.blockentities.MixerBlockEntity;
import com.breakinblocks.nautec.content.resonance.ResonanceNetwork;
import com.breakinblocks.nautec.content.resonance.ResonanceNetworks;
import com.breakinblocks.nautec.content.resonance.SatelliteArrayBlockEntity;
import com.breakinblocks.nautec.content.resonance.SatelliteGrid;
import com.breakinblocks.nautec.registries.NTBlocks;
import com.breakinblocks.nautec.registries.NTItems;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;

import java.util.List;
import java.util.UUID;

public final class SatelliteTests {
    private static final int FEED = 1000;
    private static final float FEED_PURITY = 0.8F;

    private SatelliteTests() {
    }

    private static ServerPlayer player(GameTestHelper helper, String name) {
        return new ServerPlayer(helper.getLevel().getServer(), helper.getLevel(), new GameProfile(UUID.randomUUID(), name),
                ClientInformation.createDefault());
    }

    private static void openSky(GameTestHelper helper, BlockPos absolute) {
        ServerLevel level = helper.getLevel();
        for (int dy = 2; dy < 24; dy++) {
            BlockPos pos = absolute.above(dy);
            if (level.getBlockState(pos).is(Blocks.BARRIER)) {
                level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
            }
        }
    }

    private static SatelliteArrayBlockEntity uplink(GameTestHelper helper, BlockPos relative, @Nullable ResonanceNetwork network) {
        BlockPos pos = helper.absolutePos(relative);
        helper.getLevel().setBlock(pos, NTBlocks.UPLINK_ARRAY.get().defaultBlockState(), Block.UPDATE_ALL);
        openSky(helper, pos);
        SatelliteArrayBlockEntity array = (SatelliteArrayBlockEntity) helper.getLevel().getBlockEntity(pos);
        array.setNetwork(network);
        return array;
    }

    private static ResonanceNodeBlockEntity node(GameTestHelper helper, BlockPos relative, @Nullable ResonanceNetwork network, boolean output) {
        return node(helper, relative, network, output, Direction.UP);
    }

    private static ResonanceNodeBlockEntity node(GameTestHelper helper, BlockPos relative, @Nullable ResonanceNetwork network, boolean output,
                                                 Direction facing) {
        BlockPos pos = helper.absolutePos(relative);
        helper.getLevel().setBlock(pos, NTBlocks.RESONANCE_NODE.get().defaultBlockState().setValue(ResonanceNodeBlock.FACING, facing), Block.UPDATE_ALL);
        ResonanceNodeBlockEntity node = (ResonanceNodeBlockEntity) helper.getLevel().getBlockEntity(pos);
        node.setNetwork(network);
        node.setOutput(output);
        return node;
    }

    private static void feed(GameTestHelper helper, SatelliteArrayBlockEntity uplink) {
        helper.onEachTick(() -> {
            uplink.receivePower(FEED, Direction.NORTH, uplink.getBlockPos().north());
            uplink.receiveNewPurity(FEED_PURITY, Direction.NORTH, uplink.getBlockPos().north());
        });
    }

    private static int share(int outputs) {
        return (int) Math.floor(FEED * (1.0 - NTConfig.satelliteLoss) / outputs);
    }

    public static void register(NTTestRegistrar r) {
        r.add("satellite/relays_ap_to_an_output_node_with_loss", 200, helper -> {
            ServerPlayer owner = player(helper, "SatOwner");
            ResonanceNetworks networks = ResonanceNetworks.get(helper.getLevel().getServer());
            ResonanceNetwork network = networks.create(owner, "Sat relay").network();
            SatelliteArrayBlockEntity uplink = uplink(helper, new BlockPos(1, 1, 1), network);
            ResonanceNodeBlockEntity output = node(helper, new BlockPos(1, 1, 6), network, true, Direction.EAST);
            BlockPos mixerPos = new BlockPos(5, 1, 6);
            helper.setBlock(mixerPos, NTBlocks.MIXER.get());
            helper.assertTrue(uplink.launch(), "an uplink takes a satellite");
            feed(helper, uplink);

            int expected = share(1);
            helper.succeedWhen(() -> {
                MixerBlockEntity mixer = helper.getBlockEntity(mixerPos, MixerBlockEntity.class);
                helper.assertValueEqual(uplink.getStatus(), SatelliteArrayBlockEntity.STATUS_ONLINE, "uplink status");
                helper.assertValueEqual(output.getStatus(), ResonanceNodeBlockEntity.STATUS_ONLINE, "output node status");
                helper.assertValueEqual(mixer.getPower(), expected, "the output node fires the uplink's AP less the loss");
                helper.assertTrue(Math.abs(mixer.getPurity() - FEED_PURITY) < 0.001F, "purity carries through, got " + mixer.getPurity());
                networks.delete(owner, network.id());
            });
        });

        r.add("satellite/no_satellite_no_relay", 80, helper -> {
            ServerPlayer owner = player(helper, "SatNone");
            ResonanceNetworks networks = ResonanceNetworks.get(helper.getLevel().getServer());
            ResonanceNetwork network = networks.create(owner, "Sat empty").network();
            SatelliteArrayBlockEntity uplink = uplink(helper, new BlockPos(1, 1, 1), network);
            ResonanceNodeBlockEntity output = node(helper, new BlockPos(6, 1, 6), network, true);
            feed(helper, uplink);
            helper.runAfterDelay(40, () -> {
                helper.assertValueEqual(uplink.getStatus(), SatelliteArrayBlockEntity.STATUS_NO_SATELLITE, "uplink without a satellite");
                helper.assertValueEqual(output.getStatus(), ResonanceNodeBlockEntity.STATUS_NO_CORE, "output node with no live uplink");
                helper.assertValueEqual(output.getApStored(), 0, "nothing relayed without a satellite");
                networks.delete(owner, network.id());
                helper.succeed();
            });
        });

        r.add("satellite/blocked_sky_stops_the_uplink", 200, helper -> {
            ServerPlayer owner = player(helper, "SatSky");
            ResonanceNetworks networks = ResonanceNetworks.get(helper.getLevel().getServer());
            ResonanceNetwork network = networks.create(owner, "Sat sky").network();
            SatelliteArrayBlockEntity uplink = uplink(helper, new BlockPos(1, 1, 1), network);
            ResonanceNodeBlockEntity output = node(helper, new BlockPos(6, 1, 6), network, true);
            uplink.launch();
            feed(helper, uplink);
            helper.runAfterDelay(40, () -> {
                helper.assertValueEqual(output.getStatus(), ResonanceNodeBlockEntity.STATUS_ONLINE, "the output node is online before the sky is blocked");
                helper.assertTrue(output.getApStored() > 0, "the output node receives AP before the sky is blocked");
                helper.getLevel().setBlock(uplink.getBlockPos().above(4), Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);
            });
            helper.runAfterDelay(41, () -> helper.succeedWhen(() -> {
                helper.assertValueEqual(uplink.getStatus(), SatelliteArrayBlockEntity.STATUS_SKY_BLOCKED, "uplink under stone");
                helper.assertValueEqual(output.getStatus(), ResonanceNodeBlockEntity.STATUS_NO_CORE, "a covered uplink stops being the core");
                networks.delete(owner, network.id());
            }));
        });

        r.add("satellite/only_solid_blocks_block_the_sky", 60, helper -> {
            BlockPos base = helper.absolutePos(new BlockPos(4, 1, 4));
            helper.getLevel().setBlock(base, NTBlocks.UPLINK_ARRAY.get().defaultBlockState(), Block.UPDATE_ALL);
            openSky(helper, base);
            BlockPos top = base.above();
            helper.assertTrue(SatelliteGrid.clearSky(helper.getLevel(), top), "open air is clear sky");
            for (int dy = 1; dy <= 3; dy++) {
                helper.getLevel().setBlock(top.above(dy), Blocks.WATER.defaultBlockState(), Block.UPDATE_ALL);
            }
            helper.assertTrue(SatelliteGrid.clearSky(helper.getLevel(), top), "water above the dish still counts as clear");
            helper.getLevel().setBlock(top.above(5), Blocks.GLASS.defaultBlockState(), Block.UPDATE_ALL);
            helper.getLevel().setBlock(top.above(6), Blocks.OAK_LEAVES.defaultBlockState(), Block.UPDATE_ALL);
            helper.assertTrue(SatelliteGrid.clearSky(helper.getLevel(), top), "glass and leaves above the dish still count as clear");
            helper.getLevel().setBlock(top.above(7), Blocks.STONE_SLAB.defaultBlockState(), Block.UPDATE_ALL);
            helper.assertFalse(SatelliteGrid.clearSky(helper.getLevel(), top), "a slab above the dish blocks it");
            helper.getLevel().setBlock(top.above(7), Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);
            helper.assertFalse(SatelliteGrid.clearSky(helper.getLevel(), top), "a solid block above the dish blocks it");
            helper.succeed();
        });

        r.add("satellite/output_nodes_share_the_uplink", 200, helper -> {
            ServerPlayer owner = player(helper, "SatSplit");
            ResonanceNetworks networks = ResonanceNetworks.get(helper.getLevel().getServer());
            ResonanceNetwork network = networks.create(owner, "Sat split").network();
            SatelliteArrayBlockEntity uplink = uplink(helper, new BlockPos(1, 1, 1), network);
            ResonanceNodeBlockEntity first = node(helper, new BlockPos(6, 1, 1), network, true);
            ResonanceNodeBlockEntity second = node(helper, new BlockPos(6, 1, 6), network, true);
            uplink.launch();
            feed(helper, uplink);
            helper.succeedWhen(() -> {
                helper.assertTrue(first.getApStored() > 0, "the first output node receives AP");
                helper.assertValueEqual(first.getApStored(), second.getApStored(), "equal priorities split the uplink evenly");
                networks.delete(owner, network.id());
            });
        });

        r.add("satellite/ap_crosses_dimensions_with_purity_loss", 200, helper -> {
            ServerPlayer owner = player(helper, "SatDim");
            ResonanceNetworks networks = ResonanceNetworks.get(helper.getLevel().getServer());
            ResonanceNetwork network = networks.create(owner, "Sat dimension").network();
            SatelliteArrayBlockEntity uplink = uplink(helper, new BlockPos(1, 1, 1), network);
            uplink.launch();
            feed(helper, uplink);
            ServerLevel nether = helper.getLevel().getServer().getLevel(Level.NETHER);
            BlockPos arena = helper.absolutePos(new BlockPos(4, 1, 4));
            BlockPos remote = new BlockPos(arena.getX(), 200, arena.getZ());
            nether.setBlock(remote, NTBlocks.RESONANCE_NODE.get().defaultBlockState(), Block.UPDATE_ALL);
            ResonanceNodeBlockEntity node = (ResonanceNodeBlockEntity) nether.getBlockEntity(remote);
            node.setNetwork(network);
            node.setOutput(true);
            node.setChunkLoading(true);
            float expected = FEED_PURITY * (float) (1.0 - NTConfig.satelliteCrossDimensionPurityLoss);
            helper.succeedWhen(() -> {
                helper.assertValueEqual(node.getStatus(), ResonanceNodeBlockEntity.STATUS_ONLINE, "the Nether node finds the Overworld core");
                helper.assertTrue(node.getApStored() > 0, "AP reaches the Nether");
                helper.assertTrue(Math.abs(node.getApPurity() - expected) < 0.001F,
                        "purity drops by the cross-dimension loss, expected " + expected + " got " + node.getApPurity());
                nether.setBlock(remote, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                networks.delete(owner, network.id());
            });
        });

        r.add("resonance_node/input_feeds_the_core_and_output_takes_from_it", 200, helper -> {
            ServerPlayer owner = player(helper, "NodeOwner");
            ResonanceNetworks networks = ResonanceNetworks.get(helper.getLevel().getServer());
            ResonanceNetwork network = networks.create(owner, "Node relay").network();
            SatelliteArrayBlockEntity core = uplink(helper, new BlockPos(1, 1, 1), network);
            core.launch();
            ResonanceNodeBlockEntity input = node(helper, new BlockPos(4, 1, 2), network, false);
            ResonanceNodeBlockEntity output = node(helper, new BlockPos(6, 1, 6), network, true);
            helper.onEachTick(() -> {
                input.receivePower(FEED, Direction.NORTH, input.getBlockPos().north());
                input.receiveNewPurity(FEED_PURITY, Direction.NORTH, input.getBlockPos().north());
            });
            try (Transaction tx = Transaction.openRoot()) {
                helper.assertValueEqual(input.getPort().insert(5_000, tx), 5_000, "an input node takes FE from cables");
                helper.assertValueEqual(output.getPort().insert(5_000, tx), 0, "an output node refuses FE from cables");
                tx.commit();
            }
            helper.succeedWhen(() -> {
                helper.assertValueEqual(input.getStatus(), ResonanceNodeBlockEntity.STATUS_ONLINE, "input node status");
                helper.assertValueEqual(output.getStatus(), ResonanceNodeBlockEntity.STATUS_ONLINE, "output node status");
                helper.assertTrue(output.getApStored() > 0, "AP from the input node reaches the output node through the core");
                helper.assertTrue(Math.abs(output.getApPurity() - FEED_PURITY) < 0.001F, "purity carries through in one dimension, got " + output.getApPurity());
                helper.assertTrue(output.getEnergy().getAmountAsInt() > 0, "FE from the input node reaches the output node");
                networks.delete(owner, network.id());
            });
        });

        r.add("resonance_node/needs_a_core", 60, helper -> {
            ServerPlayer owner = player(helper, "NodeCore");
            ResonanceNetworks networks = ResonanceNetworks.get(helper.getLevel().getServer());
            ResonanceNetwork network = networks.create(owner, "Node core").network();
            ResonanceNodeBlockEntity output = node(helper, new BlockPos(4, 1, 4), network, true);
            helper.succeedWhen(() -> {
                helper.assertValueEqual(output.getStatus(), ResonanceNodeBlockEntity.STATUS_NO_CORE, "a node without a core");
                helper.assertValueEqual(output.apDemand(), 0, "it asks for nothing");
                networks.delete(owner, network.id());
            });
        });

        r.add("resonance_node/merged_purity_favours_the_purer_source", 20, helper -> {
            float merged = LaserBlockEntity.mergedPurity(1_000, 3.0F, 1_000, 1.0F);
            float expected = 3.0F - (3.0F - 2.0F) * (float) NTConfig.beamMergePurityDrop;
            helper.assertTrue(Math.abs(merged - expected) < 0.001F, "equal amounts keep close to the purer one, got " + merged);
            helper.assertTrue(LaserBlockEntity.mergedPurity(0, 0F, 500, 2.5F) == 2.5F, "an empty store takes the incoming purity");
            helper.assertTrue(LaserBlockEntity.mergedPurity(9_000, 1.0F, 1_000, 3.0F) > 2.0F, "a small pure input still lifts a large impure store");
            helper.succeed();
        });

        r.add("satellite/uplinks_buffer_ap_and_fe", 80, helper -> {
            ServerPlayer owner = player(helper, "SatBuffer");
            ResonanceNetworks networks = ResonanceNetworks.get(helper.getLevel().getServer());
            ResonanceNetwork network = networks.create(owner, "Sat buffer").network();
            SatelliteArrayBlockEntity uplink = uplink(helper, new BlockPos(1, 1, 1), network);
            uplink.launch();
            feed(helper, uplink);
            try (Transaction tx = Transaction.openRoot()) {
                helper.assertValueEqual(uplink.getPort().insert(40_000, tx), 40_000, "an uplink takes FE from cables");
                helper.assertValueEqual(uplink.getPort().extract(40_000, tx), 0, "an uplink gives no FE back to cables");
                tx.commit();
            }
            helper.runAfterDelay(20, () -> {
                helper.assertTrue(uplink.getApStored() >= FEED * 10, "an uplink with no output node stores its beam, has " + uplink.getApStored());
                helper.assertTrue(Math.abs(uplink.getApPurity() - FEED_PURITY) < 0.001F, "the stored AP keeps its purity");
                helper.assertValueEqual(uplink.getEnergy().getAmountAsInt(), 40_000, "the FE stays until an output node wants it");
                helper.assertValueEqual(uplink.getStatus(), SatelliteArrayBlockEntity.STATUS_NO_OUTPUT, "an uplink with no output node says so");
                networks.delete(owner, network.id());
                helper.succeed();
            });
        });

        r.add("satellite/priority_fills_higher_output_nodes_first", 80, helper -> {
            ServerPlayer owner = player(helper, "SatPriority");
            ResonanceNetworks networks = ResonanceNetworks.get(helper.getLevel().getServer());
            ResonanceNetwork network = networks.create(owner, "Sat priority").network();
            SatelliteArrayBlockEntity uplink = uplink(helper, new BlockPos(1, 1, 1), network);
            ResonanceNodeBlockEntity first = node(helper, new BlockPos(6, 1, 1), network, true);
            ResonanceNodeBlockEntity second = node(helper, new BlockPos(6, 1, 6), network, true);
            ResonanceNodeBlockEntity third = node(helper, new BlockPos(1, 1, 6), network, true);
            uplink.launch();
            first.setPriority(5);
            first.setLimit(3_000);
            helper.runAfterDelay(5, () -> uplink.getEnergy().set(5_000));
            helper.runAfterDelay(15, () -> {
                int deliverable = (int) Math.floor(5_000 * (1.0 - NTConfig.satelliteLoss));
                int rest = (deliverable - 3_000) / 2;
                helper.assertValueEqual(first.getEnergy().getAmountAsInt(), 3_000, "the higher priority node fills to its limit first");
                helper.assertValueEqual(second.getEnergy().getAmountAsInt(), rest, "equal priorities share what is left");
                helper.assertValueEqual(third.getEnergy().getAmountAsInt(), rest, "equal priorities share what is left");
                helper.assertTrue(uplink.getEnergy().getAmountAsInt() <= 1, "the uplink paid for it, has " + uplink.getEnergy().getAmountAsInt());
                networks.delete(owner, network.id());
                helper.succeed();
            });
        });

        r.add("resonance_node/settings_copy_and_clamp", 20, helper -> {
            ServerPlayer owner = player(helper, "NodeCopy");
            ResonanceNetworks networks = ResonanceNetworks.get(helper.getLevel().getServer());
            ResonanceNetwork network = networks.create(owner, "Node copy").network();
            ResonanceNodeBlockEntity source = node(helper, new BlockPos(1, 1, 1), network, true);
            ResonanceNodeBlockEntity target = node(helper, new BlockPos(6, 1, 6), null, false);
            source.setPriority(500);
            helper.assertValueEqual(source.getPriority(), ResonanceNodeBlockEntity.MAX_PRIORITY, "priority clamps");
            source.setLimit(Integer.MAX_VALUE);
            helper.assertValueEqual(source.effectiveLimit(), NTConfig.satelliteTransferLimit, "the limit clamps to the config");
            source.setLimit(2_500);
            MachineSettings card = ConfigurationCardItem.copy(source, helper.getLevel());
            helper.assertTrue(ConfigurationCardItem.paste(card, target, owner), "the card pastes onto another node");
            helper.assertTrue(target.isOutput(), "pasted mode");
            helper.assertValueEqual(target.getPriority(), ResonanceNodeBlockEntity.MAX_PRIORITY, "pasted priority");
            helper.assertValueEqual(target.getLimit(), 2_500, "pasted limit");
            helper.assertValueEqual(target.getNetworkId(), network.id(), "pasted network");
            networks.delete(owner, network.id());
            helper.succeed();
        });

        r.add("satellite/charm_demand_and_delivery", 20, helper -> {
            ServerPlayer owner = player(helper, "SatCharm");
            ItemStack battery = new ItemStack(NTItems.PRISMATIC_BATTERY.get());
            owner.getInventory().setItem(0, battery);
            ResonanceCharmItem.Tuning charm = new ResonanceCharmItem.Tuning(new ItemStack(NTItems.RESONANCE_CHARM.get()), UUID.randomUUID(), 0);
            int demand = ResonanceCharmItem.demand(owner, charm, 5_000);
            helper.assertTrue(demand > 0 && demand <= 5_000, "an empty battery wants power, wants " + demand);
            int used = ResonanceCharmItem.deliver(owner, charm, demand);
            helper.assertValueEqual(used, demand, "everything offered is used");
            IPowerStorage power = owner.getInventory().getItem(0).getCapability(NTCapabilities.PowerStorage.ITEM);
            helper.assertValueEqual(power.getPowerStored(), used, "the battery holds what was delivered");
            helper.succeed();
        });

        r.add("satellite/breaking_the_uplink_returns_the_satellite", 60, helper -> {
            ServerPlayer owner = player(helper, "SatBreak");
            ResonanceNetworks networks = ResonanceNetworks.get(helper.getLevel().getServer());
            ResonanceNetwork network = networks.create(owner, "Sat break").network();
            SatelliteArrayBlockEntity uplink = uplink(helper, new BlockPos(4, 1, 4), network);
            BlockPos pos = uplink.getBlockPos();
            helper.assertTrue(helper.getLevel().getBlockState(pos.above()).is(NTBlocks.SATELLITE_ARRAY_TOP.get()), "placing the array adds its top half");
            helper.assertTrue(uplink.launch(), "the first satellite launches");
            helper.assertFalse(uplink.launch(), "a second satellite does not");
            helper.getLevel().destroyBlock(pos, false);
            helper.assertTrue(helper.getLevel().getBlockState(pos.above()).isAir(), "the top half goes with the base");
            AABB around = new AABB(pos).inflate(3);
            boolean dropped = false;
            for (ItemEntity item : helper.getLevel().getEntitiesOfClass(ItemEntity.class, around)) {
                if (item.getItem().is(NTItems.PRISM_SATELLITE.get())) {
                    dropped = true;
                    item.discard();
                }
            }
            helper.assertTrue(dropped, "the launched satellite drops when the uplink breaks");
            networks.delete(owner, network.id());
            helper.succeed();
        });
    }
}
