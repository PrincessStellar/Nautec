package com.breakinblocks.nautec.gametest.suite;

import com.breakinblocks.nautec.transfer.TransferCapabilities;
import net.minecraft.world.level.GameType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ClientInformation;
import com.mojang.authlib.GameProfile;
import com.breakinblocks.nautec.api.blockentities.ContainerBlockEntity;
import com.breakinblocks.nautec.api.sides.RelativeFace;
import com.breakinblocks.nautec.api.sides.SideKind;
import com.breakinblocks.nautec.api.sides.SideMode;
import com.breakinblocks.nautec.content.blockentities.ConfinedSpawnerBlockEntity;
import com.breakinblocks.nautec.content.blockentities.GraftingStationBlockEntity;
import com.breakinblocks.nautec.content.blockentities.LaserJunctionBlockEntity;
import com.breakinblocks.nautec.content.blockentities.MutatorBlockEntity;
import com.breakinblocks.nautec.content.blockentities.PressureForgeBlockEntity;
import com.breakinblocks.nautec.content.blocks.GraftingStationBlock;
import com.breakinblocks.nautec.content.blocks.LaserJunctionBlock;
import com.breakinblocks.nautec.content.items.ConfigurationCardItem;
import com.breakinblocks.nautec.content.items.MachineSettings;
import com.breakinblocks.nautec.content.items.SpawnerConfinementMatrixItem;
import com.breakinblocks.nautec.content.items.tools.WrenchMode;
import com.breakinblocks.nautec.content.resonance.ResonanceNetwork;
import com.breakinblocks.nautec.content.resonance.ResonanceNetworks;
import com.breakinblocks.nautec.content.resonance.ResonanceNodeBlockEntity;
import com.breakinblocks.nautec.content.spawner.SpawnerFilterEntry;
import com.breakinblocks.nautec.data.NTDataComponents;
import com.breakinblocks.nautec.registries.NTBlocks;
import com.breakinblocks.nautec.registries.NTItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import com.breakinblocks.nautec.transfer.ResourceHandler;
import com.breakinblocks.nautec.transfer.item.ItemResource;
import com.breakinblocks.nautec.transfer.transaction.Transaction;

import java.util.UUID;

public final class SideConfigTests {
    private static final BlockPos FIRST = new BlockPos(2, 1, 2);
    private static final BlockPos SECOND = new BlockPos(5, 1, 5);

    private SideConfigTests() {
    }

    private static ServerPlayer player(NTGameTestHelper helper, String name) {
        return new ServerPlayer(helper.getLevel().getServer(), helper.getLevel(), new GameProfile(UUID.randomUUID(), name),
                ClientInformation.createDefault());
    }

    private static ResourceHandler<ItemResource> items(NTGameTestHelper helper, BlockPos pos, Direction side) {
        return helper.getLevel().getCapability(TransferCapabilities.Item.BLOCK, helper.absolutePos(pos), side);
    }

    private static int insert(ResourceHandler<ItemResource> handler, int slot, ItemStack stack) {
        try (Transaction tx = Transaction.openRoot()) {
            return handler.insert(slot, ItemResource.of(stack), stack.getCount(), tx);
        }
    }

    private static int extract(ResourceHandler<ItemResource> handler, int slot, ItemStack stack) {
        try (Transaction tx = Transaction.openRoot()) {
            return handler.extract(slot, ItemResource.of(stack), stack.getCount(), tx);
        }
    }

    public static void register(NTTestRegistrar r) {
        r.add("side_config/modes_gate_each_face", 20, helper -> {
            helper.setBlock(FIRST, NTBlocks.PRESSURE_FORGE.get());
            PressureForgeBlockEntity forge = helper.getBlockEntity(FIRST, PressureForgeBlockEntity.class);
            forge.getItemStackHandler().setStackInSlot(1, new ItemStack(Items.DIAMOND, 4));
            ItemStack input = new ItemStack(Items.IRON_INGOT);
            ItemStack output = new ItemStack(Items.DIAMOND);
            for (Direction side : Direction.values()) {
                ResourceHandler<ItemResource> handler = items(helper, FIRST, side);
                helper.assertTrue(handler != null, "Every face should be open by default, " + side);
                helper.assertValueEqual(1, insert(handler, 0, input), "default insert via " + side);
                helper.assertValueEqual(1, extract(handler, 1, output), "default extract via " + side);
            }

            forge.setSideMode(SideKind.ITEMS, RelativeFace.FRONT, SideMode.INPUT);
            helper.assertValueEqual(1, insert(items(helper, FIRST, Direction.NORTH), 0, input), "insert via an input face");
            helper.assertValueEqual(0, extract(items(helper, FIRST, Direction.NORTH), 1, output), "extract via an input face");
            forge.setSideMode(SideKind.ITEMS, RelativeFace.FRONT, SideMode.OUTPUT);
            helper.assertValueEqual(0, insert(items(helper, FIRST, Direction.NORTH), 0, input), "insert via an output face");
            helper.assertValueEqual(1, extract(items(helper, FIRST, Direction.NORTH), 1, output), "extract via an output face");
            forge.setSideMode(SideKind.ITEMS, RelativeFace.FRONT, SideMode.NONE);
            helper.assertTrue(items(helper, FIRST, Direction.NORTH) == null, "An Off face exposes nothing");
            helper.assertValueEqual(0, extract(items(helper, FIRST, Direction.SOUTH), 0, input), "inputs never come back out");
            helper.succeed();
        });

        r.add("side_config/faces_follow_rotation_and_reload", 20, helper -> {
            helper.setBlock(FIRST, NTBlocks.GRAFTING_STATION.get().defaultBlockState().setValue(GraftingStationBlock.FACING, Direction.EAST));
            GraftingStationBlockEntity station = helper.getBlockEntity(FIRST, GraftingStationBlockEntity.class);
            station.setSideMode(SideKind.ITEMS, RelativeFace.FRONT, SideMode.NONE);
            station.setSideMode(SideKind.FLUIDS, RelativeFace.LEFT, SideMode.OUTPUT);
            helper.assertTrue(items(helper, FIRST, Direction.EAST) == null, "The front of an east-facing station is its east face");
            helper.assertTrue(items(helper, FIRST, Direction.NORTH) != null, "Other faces stay open");

            CompoundTag saved = station.saveWithFullMetadata(helper.getLevel().registryAccess());
            BlockEntity loaded = BlockEntity.loadStatic(helper.absolutePos(FIRST), station.getBlockState(), saved, helper.getLevel().registryAccess());
            helper.assertTrue(loaded instanceof GraftingStationBlockEntity, "reloads as a grafting station");
            ContainerBlockEntity copy = (ContainerBlockEntity) loaded;
            helper.assertValueEqual(SideMode.NONE, copy.getSideConfig().get(SideKind.ITEMS, RelativeFace.FRONT), "item front after reload");
            helper.assertValueEqual(SideMode.OUTPUT, copy.getSideConfig().get(SideKind.FLUIDS, RelativeFace.LEFT), "fluid left after reload");
            helper.succeed();
        });

        r.add("side_config/wrench_cycles_the_clicked_face", 20, helper -> {
            helper.setBlock(FIRST, NTBlocks.PRESSURE_FORGE.get());
            PressureForgeBlockEntity forge = helper.getBlockEntity(FIRST, PressureForgeBlockEntity.class);
            Player player = helper.makeMockPlayer(GameType.SURVIVAL);
            ItemStack wrench = new ItemStack(NTItems.AQUARINE_WRENCH.get());
            wrench.set(NTDataComponents.WRENCH_MODE.get(), WrenchMode.FLUID_SIDES.ordinal());
            player.setItemInHand(InteractionHand.MAIN_HAND, wrench);
            BlockPos abs = helper.absolutePos(FIRST);
            UseOnContext click = new UseOnContext(player, InteractionHand.MAIN_HAND, new BlockHitResult(abs.getCenter(), Direction.UP, abs, false));
            wrench.useOn(click);
            helper.assertValueEqual(SideMode.INPUT, forge.getSideConfig().get(SideKind.FLUIDS, RelativeFace.TOP), "fluid top after one click");
            helper.assertValueEqual(SideMode.BOTH, forge.getSideConfig().get(SideKind.ITEMS, RelativeFace.TOP), "items untouched in fluid mode");
            player.setShiftKeyDown(true);
            wrench.useOn(click);
            helper.assertValueEqual(SideMode.BOTH, forge.getSideConfig().get(SideKind.FLUIDS, RelativeFace.TOP), "sneaking steps back");
            helper.succeed();
        });

        r.add("configuration_card/side_config_pastes_across_machines", 20, helper -> {
            helper.setBlock(FIRST, NTBlocks.PRESSURE_FORGE.get());
            helper.setBlock(SECOND, NTBlocks.MUTATOR.get());
            PressureForgeBlockEntity forge = helper.getBlockEntity(FIRST, PressureForgeBlockEntity.class);
            MutatorBlockEntity mutator = helper.getBlockEntity(SECOND, MutatorBlockEntity.class);
            forge.setSideMode(SideKind.ITEMS, RelativeFace.BOTTOM, SideMode.OUTPUT);
            forge.setSideMode(SideKind.ITEMS, RelativeFace.TOP, SideMode.INPUT);
            MachineSettings card = ConfigurationCardItem.copy(forge, helper.getLevel());
            helper.assertTrue(card != null, "A forge has settings to copy");
            helper.assertTrue(ConfigurationCardItem.paste(card, mutator, player(helper, "Paster")), "Side config should paste onto a mutator");
            helper.assertValueEqual(SideMode.OUTPUT, mutator.getSideConfig().get(SideKind.ITEMS, RelativeFace.BOTTOM), "pasted bottom");
            helper.assertValueEqual(SideMode.INPUT, mutator.getSideConfig().get(SideKind.ITEMS, RelativeFace.TOP), "pasted top");
            helper.assertTrue(items(helper, SECOND, Direction.DOWN) != null
                    && insert(items(helper, SECOND, Direction.DOWN), 0, new ItemStack(Items.STONE)) == 0, "The pasted mode takes effect");
            helper.succeed();
        });

        r.add("configuration_card/spawner_filter_copies", 40, helper -> {
            ConfinedSpawnerBlockEntity source = confined(helper, FIRST);
            ConfinedSpawnerBlockEntity target = confined(helper, SECOND);
            source.toggleWhitelist();
            source.setFilterEntry(2, SpawnerFilterEntry.of(new ItemStack(Items.FEATHER)));
            MachineSettings card = ConfigurationCardItem.copy(source, helper.getLevel());
            helper.assertTrue(ConfigurationCardItem.paste(card, target, player(helper, "Filterer")), "The filter should paste");
            helper.assertTrue(target.getFilter().isWhitelist(), "pasted whitelist mode");
            helper.assertTrue(target.getFilter().get(2) != null && target.getFilter().get(2).matches(new ItemStack(Items.FEATHER)), "pasted entry");
            helper.succeed();
        });

        r.add("configuration_card/junction_faces_copy", 20, helper -> {
            helper.setBlock(FIRST, NTBlocks.LASER_JUNCTION.get());
            helper.setBlock(SECOND, NTBlocks.LASER_JUNCTION.get());
            BlockState state = helper.getBlockState(FIRST)
                    .setValue(LaserJunctionBlock.CONNECTION[Direction.UP.get3DDataValue()], LaserJunctionBlock.ConnectionType.INPUT)
                    .setValue(LaserJunctionBlock.CONNECTION[Direction.EAST.get3DDataValue()], LaserJunctionBlock.ConnectionType.OUTPUT);
            helper.setBlock(FIRST, state);
            LaserJunctionBlockEntity source = helper.getBlockEntity(FIRST, LaserJunctionBlockEntity.class);
            MachineSettings card = ConfigurationCardItem.copy(source, helper.getLevel());
            LaserJunctionBlockEntity target = helper.getBlockEntity(SECOND, LaserJunctionBlockEntity.class);
            helper.assertTrue(ConfigurationCardItem.paste(card, target, player(helper, "Junctioner")), "The faces should paste");
            BlockState pasted = helper.getBlockState(SECOND);
            helper.assertValueEqual(LaserJunctionBlock.ConnectionType.INPUT, pasted.getValue(LaserJunctionBlock.CONNECTION[Direction.UP.get3DDataValue()]), "pasted top");
            helper.assertValueEqual(LaserJunctionBlock.ConnectionType.OUTPUT, pasted.getValue(LaserJunctionBlock.CONNECTION[Direction.EAST.get3DDataValue()]), "pasted east");
            LaserJunctionBlockEntity reloaded = helper.getBlockEntity(SECOND, LaserJunctionBlockEntity.class);
            helper.assertTrue(reloaded.getLaserInputs().contains(Direction.UP) && reloaded.getLaserOutputs().contains(Direction.EAST), "beam sets follow the paste");
            helper.succeed();
        });

        r.add("configuration_card/resonance_network_respects_access", 20, helper -> {
            helper.setBlock(FIRST, NTBlocks.RESONANCE_NODE.get());
            helper.setBlock(SECOND, NTBlocks.RESONANCE_NODE.get());
            ResonanceNodeBlockEntity source = helper.getBlockEntity(FIRST, ResonanceNodeBlockEntity.class);
            ResonanceNodeBlockEntity target = helper.getBlockEntity(SECOND, ResonanceNodeBlockEntity.class);
            ServerPlayer owner = player(helper, "GridOwner");
            ResonanceNetworks networks = ResonanceNetworks.get(helper.getLevel().getServer());
            ResonanceNetwork network = networks.create(owner, "Card grid " + UUID.randomUUID().toString().substring(0, 6)).network();
            source.setNetwork(network);
            source.setOutput(true);
            MachineSettings card = ConfigurationCardItem.copy(source, helper.getLevel());

            boolean refused = false;
            try {
                ConfigurationCardItem.paste(card, target, player(helper, "Stranger"));
            } catch (NullPointerException expected) {
                refused = true;
            }
            helper.assertTrue(refused, "The stranger should be told the network is off limits");
            helper.assertTrue(target.getNetworkId() == null, "A stranger cannot paste a private network");
            ConfigurationCardItem.paste(card, target, owner);
            helper.assertValueEqual(network.id(), target.getNetworkId(), "the owner pastes the network");
            helper.assertTrue(target.isOutput(), "the output mode pastes too");
            networks.delete(owner, network.id());
            helper.succeed();
        });
    }

    private static ConfinedSpawnerBlockEntity confined(NTGameTestHelper helper, BlockPos pos) {
        helper.setBlock(pos, Blocks.SPAWNER.defaultBlockState());
        helper.getBlockEntity(pos, SpawnerBlockEntity.class).setEntityId(EntityType.CHICKEN, helper.getLevel().getRandom());
        helper.assertTrue(SpawnerConfinementMatrixItem.confine(helper.getLevel(), helper.absolutePos(pos)), "matrix should confine a spawner");
        return helper.getBlockEntity(pos, ConfinedSpawnerBlockEntity.class);
    }
}
