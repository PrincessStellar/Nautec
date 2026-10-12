package com.breakinblocks.nautec.gametest.suite;

import com.breakinblocks.nautec.transfer.TransferCapabilities;
import com.breakinblocks.nautec.content.blockentities.ConfinedSpawnerBlockEntity;
import com.breakinblocks.nautec.content.blockentities.EnergyConverterBlockEntity;
import com.breakinblocks.nautec.content.conduits.ConduitChannel;
import com.breakinblocks.nautec.content.conduits.ConduitNetwork;
import com.breakinblocks.nautec.content.conduits.ConduitNetworks;
import com.breakinblocks.nautec.content.conduits.ConduitTapBlock;
import com.breakinblocks.nautec.content.conduits.ConduitTapBlockEntity;
import com.breakinblocks.nautec.content.conduits.ConduitWrenching;
import com.breakinblocks.nautec.content.conduits.CurrentConduitBlock;
import com.breakinblocks.nautec.content.conduits.FlowMode;
import com.breakinblocks.nautec.content.conduits.RedstoneMode;
import com.breakinblocks.nautec.content.conduits.TapArm;
import com.breakinblocks.nautec.content.conduits.TapFace;
import com.breakinblocks.nautec.content.conduits.TapFilter;
import com.breakinblocks.nautec.content.conduits.TapFlow;
import com.breakinblocks.nautec.content.conduits.TapSide;
import com.breakinblocks.nautec.network.ConduitTapEditPayload;
import com.breakinblocks.nautec.content.items.ConfigurationCardItem;
import com.breakinblocks.nautec.content.items.MachineSettings;
import com.breakinblocks.nautec.data.NTDataComponents;
import com.breakinblocks.nautec.registries.NTBacterias;
import com.breakinblocks.nautec.registries.NTBlocks;
import com.breakinblocks.nautec.registries.NTItems;
import com.breakinblocks.nautec.utils.TemplateSanitizer;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.FurnaceBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import com.breakinblocks.nautec.transfer.ResourceHandler;
import com.breakinblocks.nautec.transfer.ResourceHandlerUtil;
import com.breakinblocks.nautec.transfer.item.ItemResource;
import com.breakinblocks.nautec.transfer.transaction.Transaction;

import java.util.Set;
import java.util.UUID;

public final class ConduitTests {
    private static final int Y = 1;

    private record Fork(ChestBlockEntity source, ChestBlockEntity north, ChestBlockEntity south, ConduitTapBlockEntity sourceTap,
                        ConduitTapBlockEntity targetTap) {
    }

    public static void register(NTTestRegistrar r) {
        r.add("conduit/plain_conduit_has_no_block_entity", 5, helper -> {
            BlockPos pos = helper.absolutePos(new BlockPos(4, Y, 4));
            helper.getLevel().setBlock(pos, NTBlocks.CURRENT_CONDUIT.get().defaultBlockState(), Block.UPDATE_ALL);
            if (helper.getLevel().getBlockEntity(pos) != null) {
                helper.fail("A Current Conduit created a block entity");
                return;
            }
            helper.succeed();
        });

        r.add("conduit/wrench_swaps_conduit_and_tap", 10, helper -> {
            ServerLevel level = helper.getLevel();
            line(helper, 2, 6);
            BlockPos middle = helper.absolutePos(new BlockPos(4, Y, 4));
            Player player = helper.makeMockPlayer(GameType.SURVIVAL);
            if (!ConduitWrenching.convert(level, middle, level.getBlockState(middle), player)) {
                helper.fail("Converting a conduit failed");
                return;
            }
            BlockState tapState = level.getBlockState(middle);
            helper.assertTrue(tapState.getBlock() instanceof ConduitTapBlock, "the conduit became a tap");
            helper.assertValueEqual(TapArm.CONDUIT, tapState.getValue(ConduitTapBlock.ARMS[Direction.WEST.ordinal()]), "west arm after the swap");
            helper.assertValueEqual(TapArm.CONDUIT, tapState.getValue(ConduitTapBlock.ARMS[Direction.EAST.ordinal()]), "east arm after the swap");
            ConduitTapBlockEntity tap = (ConduitTapBlockEntity) level.getBlockEntity(middle);
            tap.getItemStackHandler().setStackInSlot(ConduitTapBlockEntity.UPGRADE_SLOT, new ItemStack(NTItems.EDDY_UPGRADE.get()));
            ConduitWrenching.revert(level, middle, tapState, tap, player);
            helper.assertTrue(level.getBlockState(middle).getBlock() instanceof CurrentConduitBlock, "the tap became a conduit again");
            helper.assertValueEqual(1, player.getInventory().countItem(NTItems.EDDY_UPGRADE.get()), "upgrades given back");
            helper.succeed();
        });

        r.add("conduit/network_spans_the_line_and_splits_when_broken", 10, helper -> {
            ServerLevel level = helper.getLevel();
            line(helper, 1, 7);
            BlockPos left = tapAt(helper, 1);
            BlockPos right = tapAt(helper, 7);
            ConduitNetwork network = ConduitNetworks.get(level, left);
            helper.assertValueEqual(2, network.taps().length, "taps on the joined network");
            helper.assertTrue(ConduitNetworks.peek(level, right) == network, "the far tap is indexed on the joined network");
            level.removeBlock(helper.absolutePos(new BlockPos(4, Y, 4)), false);
            helper.assertTrue(network.dirty(), "breaking a conduit retired the network");
            helper.assertTrue(ConduitNetworks.peek(level, right) == null, "the retired network left an index entry behind");
            ConduitNetwork split = ConduitNetworks.get(level, left);
            helper.assertValueEqual(1, split.taps().length, "taps after breaking the middle conduit");
            helper.assertTrue(ConduitNetworks.peek(level, right) != split, "the far tap is still in the network");
            helper.succeed();
        });

        r.add("conduit/update_tag_carries_no_filters", 5, helper -> {
            line(helper, 3, 5);
            ConduitTapBlockEntity tap = tap(helper, 4);
            tap.getItemStackHandler().setStackInSlot(ConduitTapBlockEntity.FILTER_SLOT, new ItemStack(NTItems.INTRICATE_FILTER.get()));
            for (int slot = 0; slot < TapFilter.ITEM_SLOTS; slot++) {
                tap.face(Direction.NORTH).filter(TapSide.INPUT).setItem(slot, new ItemStack(Items.ENCHANTED_BOOK));
            }
            helper.assertValueEqual(Set.of("flow"), tap.getUpdateTag(helper.getLevel().registryAccess()).getAllKeys(), "the update tag only carries the flange colours");
            helper.succeed();
        });

        r.add("conduit/filter_sorts_dishes_by_strain", 5, helper -> {
            RegistryAccess registries = helper.getLevel().registryAccess();
            ItemStack lithophiles = DishPortTests.dish(DishPortTests.colony(helper, NTBacterias.LITHOPHILES, 40000));
            ItemStack calciophiles = DishPortTests.dish(DishPortTests.colony(helper, NTBacterias.CALCIOPHILES, 100));
            ItemStack empty = new ItemStack(NTItems.PETRI_DISH.get());
            TapFilter filter = new TapFilter();
            filter.setItem(0, TemplateSanitizer.item(lithophiles, registries));
            helper.assertTrue(filter.exact(0), "a colony dish matches by strain as soon as it is set");
            helper.assertTrue(filter.passes(ItemResource.of(DishPortTests.dish(DishPortTests.colony(helper, NTBacterias.LITHOPHILES, 400))), 9),
                    "a dish of the same strain at another size passes");
            helper.assertFalse(filter.passes(ItemResource.of(calciophiles), 9), "a dish of another strain is held back");
            helper.assertFalse(filter.passes(ItemResource.of(empty), 9), "an empty dish is held back");
            filter.setWhitelist(false);
            helper.assertTrue(filter.passes(ItemResource.of(calciophiles), 9), "a blacklist lets other strains through");
            helper.assertFalse(filter.passes(ItemResource.of(lithophiles), 9), "a blacklist holds back the listed strain");
            filter.setWhitelist(true);
            filter.setExact(0, false);
            helper.assertTrue(filter.passes(ItemResource.of(calciophiles), 9), "loose matching takes any dish");
            TapFilter emptyFilter = new TapFilter();
            emptyFilter.setItem(0, empty);
            helper.assertFalse(emptyFilter.exact(0), "an empty dish starts out loose");
            emptyFilter.setExact(0, true);
            helper.assertTrue(emptyFilter.passes(ItemResource.of(empty), 9), "an exact empty dish takes empty dishes");
            helper.assertFalse(emptyFilter.passes(ItemResource.of(calciophiles), 9), "an exact empty dish holds back colonies");
            helper.succeed();
        });

        r.add("conduit/flange_colour_follows_the_face_modes", 60, helper -> {
            chestLine(helper);
            ConduitTapBlockEntity tap = tap(helper, 1);
            helper.runAfterDelay(5, () -> {
                helper.assertValueEqual(TapFlow.OUTPUT, tap.flow(Direction.WEST), "a new face puts items into the chest");
                helper.assertValueEqual(TapFlow.IDLE, tap.flow(Direction.EAST), "the conduit side has no flange");
                setMode(tap, Direction.WEST, ConduitChannel.ITEMS, FlowMode.EXTRACT);
            });
            helper.runAfterDelay(10, () -> {
                helper.assertValueEqual(TapFlow.INPUT, tap.flow(Direction.WEST), "taking items from the chest");
                setMode(tap, Direction.WEST, ConduitChannel.ITEMS, FlowMode.BOTH);
            });
            helper.runAfterDelay(15, () -> {
                helper.assertValueEqual(TapFlow.BOTH, tap.flow(Direction.WEST), "taking and giving items");
                setMode(tap, Direction.WEST, ConduitChannel.ITEMS, FlowMode.OFF);
                setMode(tap, Direction.WEST, ConduitChannel.ENERGY, FlowMode.EXTRACT);
            });
            helper.runAfterDelay(20, () -> {
                helper.assertValueEqual(TapFlow.IDLE, tap.flow(Direction.WEST), "a chest has no energy, so the energy setting does not colour it");
                helper.succeed();
            });
        });

        r.add("conduit/edits_respect_the_filter_upgrade_on_the_server", 5, helper -> {
            line(helper, 3, 5);
            ConduitTapBlockEntity tap = tap(helper, 4);
            TapFace face = tap.face(Direction.WEST);
            RegistryAccess registries = helper.getLevel().registryAccess();
            ItemStack cobble = new ItemStack(Items.COBBLESTONE);
            helper.assertFalse(ConduitTapEditPayload.apply(tap, face, ConduitTapEditPayload.item(0, Direction.WEST, TapSide.INPUT, 0, cobble), registries),
                    "a filter was accepted with no Filter fitted");
            tap.getItemStackHandler().setStackInSlot(ConduitTapBlockEntity.FILTER_SLOT, new ItemStack(NTItems.FILTER.get()));
            helper.assertTrue(ConduitTapEditPayload.apply(tap, face, ConduitTapEditPayload.item(0, Direction.WEST, TapSide.INPUT, 0, cobble), registries),
                    "slot 1 accepts a filter with a Filter");
            helper.assertFalse(ConduitTapEditPayload.apply(tap, face, ConduitTapEditPayload.item(0, Direction.WEST, TapSide.OUTPUT, 9, cobble), registries),
                    "slot 10 was accepted without an Intricate Filter");
            helper.assertFalse(ConduitTapEditPayload.apply(tap, face, ConduitTapEditPayload.value(0, Direction.WEST, ConduitTapEditPayload.COMPONENTS, 5, 1),
                    registries), "an empty slot was set to match components");
            helper.assertFalse(ConduitTapEditPayload.apply(tap, face, ConduitTapEditPayload.value(0, Direction.WEST, ConduitTapEditPayload.MODE, 0, 99),
                    registries), "an out of range flow mode was accepted");
            tap.getItemStackHandler().setStackInSlot(ConduitTapBlockEntity.FILTER_SLOT, new ItemStack(NTItems.INTRICATE_FILTER.get()));
            helper.assertTrue(ConduitTapEditPayload.apply(tap, face, ConduitTapEditPayload.item(0, Direction.WEST, TapSide.OUTPUT, 20, cobble), registries),
                    "slot 21 accepts a filter with an Intricate Filter");
            helper.succeed();
        });

        r.add("conduit/null_side_hides_the_upgrade_slots", 5, helper -> {
            line(helper, 3, 5);
            ConduitTapBlockEntity tap = tap(helper, 4);
            tap.getItemStackHandler().setStackInSlot(ConduitTapBlockEntity.UPGRADE_SLOT, new ItemStack(NTItems.EDDY_UPGRADE.get()));
            helper.assertTrue(helper.getLevel().getCapability(TransferCapabilities.Item.BLOCK, tap.getBlockPos(), null) == null,
                    "the upgrade slots are reachable with a null side");
            helper.succeed();
        });

        r.add("conduit/items_move_chest_to_chest", 120, helper -> {
            ChestBlockEntity[] chests = chestLine(helper);
            ConduitTapBlockEntity source = tap(helper, 1);
            setMode(source, Direction.WEST, ConduitChannel.ITEMS, FlowMode.EXTRACT);
            chests[0].setItem(0, new ItemStack(Items.COBBLESTONE, 16));
            helper.succeedWhen(() -> helper.assertTrue(count(chests[1], Items.COBBLESTONE) >= 16, "two moves of cobblestone reached the far chest"));
        });

        r.add("conduit/items_move_out_of_a_confined_spawner", 120, helper -> {
            ServerLevel level = helper.getLevel();
            BlockPos spawnerPos = helper.absolutePos(new BlockPos(0, Y, 4));
            level.setBlock(spawnerPos, NTBlocks.CONFINED_SPAWNER.get().defaultBlockState(), Block.UPDATE_ALL);
            ConfinedSpawnerBlockEntity spawner = (ConfinedSpawnerBlockEntity) level.getBlockEntity(spawnerPos);
            spawner.getItemStackHandler().setStackInSlot(0, new ItemStack(Items.BONE, 16));
            ChestBlockEntity target = chest(helper, 8, 4);
            line(helper, 1, 7);
            ConduitTapBlockEntity source = tap(helper, 1);
            tapAt(helper, 7);
            setMode(source, Direction.WEST, ConduitChannel.ITEMS, FlowMode.EXTRACT);
            helper.succeedWhen(() -> helper.assertTrue(count(target, Items.BONE) >= 16, "bones from the confined spawner reached the chest"));
        });

        r.add("conduit/plain_containers_are_reached_through_their_faces", 5, helper -> {
            ServerLevel level = helper.getLevel();
            BlockPos furnacePos = helper.absolutePos(new BlockPos(4, Y, 4));
            level.setBlock(furnacePos, Blocks.FURNACE.defaultBlockState(), Block.UPDATE_ALL);
            FurnaceBlockEntity furnace = (FurnaceBlockEntity) level.getBlockEntity(furnacePos);
            furnace.setItem(0, new ItemStack(Items.RAW_IRON, 4));
            furnace.setItem(2, new ItemStack(Items.IRON_INGOT, 3));
            ResourceHandler<ItemResource> bottom = ConduitTapBlockEntity.containerAt(level, furnacePos, Direction.DOWN);
            ResourceHandler<ItemResource> top = ConduitTapBlockEntity.containerAt(level, furnacePos, Direction.UP);
            helper.assertTrue(bottom != null && top != null, "a furnace is found as a container");
            try (Transaction transaction = Transaction.openRoot()) {
                helper.assertValueEqual(3, ResourceHandlerUtil.extractFirst(bottom, resource -> true, 64, transaction).amount(), "ingots taken from below");
            }
            try (Transaction transaction = Transaction.openRoot()) {
                helper.assertTrue(ResourceHandlerUtil.extractFirst(top, resource -> resource.is(Items.IRON_INGOT), 64, transaction) == null,
                        "the top face cannot reach the result slot");
            }
            helper.assertTrue(ConduitTapBlockEntity.containerAt(level, helper.absolutePos(new BlockPos(5, Y, 4)), Direction.DOWN) == null,
                    "air is not a container");
            helper.succeed();
        });

        r.add("conduit/whitelist_only_passes_listed_items", 120, helper -> {
            ChestBlockEntity[] chests = chestLine(helper);
            ConduitTapBlockEntity source = tap(helper, 1);
            source.getItemStackHandler().setStackInSlot(ConduitTapBlockEntity.FILTER_SLOT, new ItemStack(NTItems.FILTER.get()));
            source.face(Direction.WEST).filter(TapSide.INPUT).setItem(0, new ItemStack(Items.COBBLESTONE));
            setMode(source, Direction.WEST, ConduitChannel.ITEMS, FlowMode.EXTRACT);
            chests[0].setItem(0, new ItemStack(Items.DIRT, 16));
            chests[0].setItem(1, new ItemStack(Items.COBBLESTONE, 16));
            helper.succeedWhen(() -> {
                helper.assertTrue(count(chests[1], Items.COBBLESTONE) >= 8, "cobblestone passed the whitelist");
                helper.assertValueEqual(0, count(chests[1], Items.DIRT), "dirt that got past the whitelist");
            });
        });

        r.add("conduit/redstone_high_waits_for_power", 140, helper -> {
            ChestBlockEntity[] chests = chestLine(helper);
            ConduitTapBlockEntity source = tap(helper, 1);
            source.face(Direction.WEST).setRedstone(TapSide.INPUT, RedstoneMode.HIGH);
            setMode(source, Direction.WEST, ConduitChannel.ITEMS, FlowMode.EXTRACT);
            chests[0].setItem(0, new ItemStack(Items.COBBLESTONE, 16));
            helper.runAfterDelay(45, () -> {
                helper.assertValueEqual(0, count(chests[1], Items.COBBLESTONE), "items moved without power");
                helper.getLevel().setBlock(helper.absolutePos(new BlockPos(1, Y + 1, 4)), Blocks.REDSTONE_BLOCK.defaultBlockState(), Block.UPDATE_ALL);
            });
            helper.runAfterDelay(50, () -> helper.succeedWhen(() -> helper.assertTrue(count(chests[1], Items.COBBLESTONE) > 0, "items moved once powered")));
        });

        r.add("conduit/output_filter_limits_what_enters_a_block", 120, helper -> {
            ChestBlockEntity[] chests = chestLine(helper);
            ConduitTapBlockEntity source = tap(helper, 1);
            ConduitTapBlockEntity target = tap(helper, 7);
            target.getItemStackHandler().setStackInSlot(ConduitTapBlockEntity.FILTER_SLOT, new ItemStack(NTItems.FILTER.get()));
            TapFilter output = target.face(Direction.EAST).filter(TapSide.OUTPUT);
            output.setWhitelist(false);
            output.setItem(0, new ItemStack(Items.DIRT));
            target.configChanged();
            setMode(source, Direction.WEST, ConduitChannel.ITEMS, FlowMode.EXTRACT);
            chests[0].setItem(0, new ItemStack(Items.DIRT, 16));
            chests[0].setItem(1, new ItemStack(Items.COBBLESTONE, 16));
            helper.succeedWhen(() -> {
                helper.assertTrue(count(chests[1], Items.COBBLESTONE) >= 8, "cobblestone passed the output filter");
                helper.assertValueEqual(0, count(chests[1], Items.DIRT), "dirt that got past the output blacklist");
                helper.assertTrue(count(chests[0], Items.DIRT) == 16, "the source input took dirt it had nowhere to send");
            });
        });

        r.add("conduit/output_redstone_holds_insertion_until_powered", 140, helper -> {
            ChestBlockEntity[] chests = chestLine(helper);
            ConduitTapBlockEntity source = tap(helper, 1);
            ConduitTapBlockEntity target = tap(helper, 7);
            target.face(Direction.EAST).setRedstone(TapSide.OUTPUT, RedstoneMode.HIGH);
            target.configChanged();
            setMode(source, Direction.WEST, ConduitChannel.ITEMS, FlowMode.EXTRACT);
            chests[0].setItem(0, new ItemStack(Items.COBBLESTONE, 16));
            helper.runAfterDelay(45, () -> {
                helper.assertValueEqual(0, count(chests[1], Items.COBBLESTONE), "items entered the block without output power");
                helper.getLevel().setBlock(helper.absolutePos(new BlockPos(7, Y + 1, 4)), Blocks.REDSTONE_BLOCK.defaultBlockState(), Block.UPDATE_ALL);
            });
            helper.runAfterDelay(50, () -> helper.succeedWhen(() -> helper.assertTrue(count(chests[1], Items.COBBLESTONE) > 0, "items entered once the output tap was powered")));
        });

        r.add("conduit/wrench_click_on_a_tap_reaches_the_wrench", 5, helper -> {
            line(helper, 3, 5);
            ConduitTapBlockEntity tap = tap(helper, 4);
            BlockPos pos = tap.getBlockPos();
            Player player = helper.makeMockPlayer(GameType.SURVIVAL);
            ItemStack wrench = new ItemStack(NTItems.AQUARINE_WRENCH.get());
            BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false);
            ItemInteractionResult result = helper.getLevel().getBlockState(pos).useItemOn(wrench, helper.getLevel(), player, InteractionHand.MAIN_HAND, hit);
            helper.assertTrue(result == ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION, "a wrench click on a tap was taken by the block, got " + result);
            helper.succeed();
        });

        r.add("conduit/card_moves_upgrades_from_the_inventory", 5, helper -> {
            line(helper, 2, 6);
            ConduitTapBlockEntity source = tap(helper, 2);
            ConduitTapBlockEntity target = tap(helper, 6);
            source.getItemStackHandler().setStackInSlot(ConduitTapBlockEntity.UPGRADE_SLOT, new ItemStack(NTItems.EDDY_UPGRADE.get()));
            source.getItemStackHandler().setStackInSlot(ConduitTapBlockEntity.FILTER_SLOT, new ItemStack(NTItems.FILTER.get()));
            setMode(source, Direction.NORTH, ConduitChannel.ITEMS, FlowMode.EXTRACT);
            target.getItemStackHandler().setStackInSlot(ConduitTapBlockEntity.UPGRADE_SLOT, new ItemStack(NTItems.SURGE_UPGRADE.get()));
            MachineSettings card = ConfigurationCardItem.copy(source, helper.getLevel());
            helper.assertTrue(card != null, "a tap has settings to copy");
            ServerPlayer player = serverPlayer(helper, "TapPaster");

            helper.assertValueEqual(2, ConfigurationCardItem.missingUpgrades(card, target, player).size(), "missing upgrade and filter");
            helper.assertTrue(!ConfigurationCardItem.paste(card, target, player), "nothing pastes while the items are missing");
            helper.assertValueEqual(FlowMode.INSERT, target.face(Direction.NORTH).mode(ConduitChannel.ITEMS), "faces untouched by a refused paste");
            helper.assertTrue(target.getItemStackHandler().getStackInSlot(ConduitTapBlockEntity.UPGRADE_SLOT).is(NTItems.SURGE_UPGRADE.get()), "upgrade untouched by a refused paste");

            player.getInventory().add(new ItemStack(NTItems.EDDY_UPGRADE.get()));
            player.getInventory().add(new ItemStack(NTItems.FILTER.get()));
            helper.assertTrue(ConfigurationCardItem.missingUpgrades(card, target, player).isEmpty(), "nothing missing once carried");
            helper.assertTrue(ConfigurationCardItem.paste(card, target, player), "the paste goes through");
            helper.assertTrue(target.getItemStackHandler().getStackInSlot(ConduitTapBlockEntity.UPGRADE_SLOT).is(NTItems.EDDY_UPGRADE.get()), "upgrade moved in");
            helper.assertTrue(target.getItemStackHandler().getStackInSlot(ConduitTapBlockEntity.FILTER_SLOT).is(NTItems.FILTER.get()), "filter moved in");
            helper.assertTrue(target.hasFilterUpgrade(), "the tap reads its new filter");
            helper.assertValueEqual(FlowMode.EXTRACT, target.face(Direction.NORTH).mode(ConduitChannel.ITEMS), "faces pasted");
            helper.assertValueEqual(0, player.getInventory().countItem(NTItems.EDDY_UPGRADE.get()), "upgrade taken from the inventory");
            helper.assertValueEqual(0, player.getInventory().countItem(NTItems.FILTER.get()), "filter taken from the inventory");
            helper.assertValueEqual(1, player.getInventory().countItem(NTItems.SURGE_UPGRADE.get()), "the replaced upgrade is handed back");
            helper.assertTrue(source.getItemStackHandler().getStackInSlot(ConduitTapBlockEntity.UPGRADE_SLOT).is(NTItems.EDDY_UPGRADE.get()), "the source keeps its upgrade");
            helper.succeed();
        });

        r.add("conduit/card_click_on_a_waterlogged_tap_pastes", 5, helper -> {
            line(helper, 2, 6);
            ConduitTapBlockEntity source = tap(helper, 2);
            BlockPos targetPos = tapAt(helper, 6);
            ServerLevel level = helper.getLevel();
            level.setBlock(targetPos, level.getBlockState(targetPos).setValue(BlockStateProperties.WATERLOGGED, true), Block.UPDATE_ALL);
            ConduitTapBlockEntity target = (ConduitTapBlockEntity) level.getBlockEntity(targetPos);
            setMode(source, Direction.SOUTH, ConduitChannel.FLUIDS, FlowMode.BOTH);
            ItemStack card = new ItemStack(NTItems.CONFIGURATION_CARD.get());
            card.set(NTDataComponents.MACHINE_SETTINGS.get(), ConfigurationCardItem.copy(source, level));
            ServerPlayer player = serverPlayer(helper, "WetPaster");
            player.setItemInHand(InteractionHand.MAIN_HAND, card);
            BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(targetPos), Direction.UP, targetPos, false);
            try {
                player.gameMode.useItemOn(player, level, card, InteractionHand.MAIN_HAND, hit);
            } catch (NullPointerException expected) {
            }
            helper.assertValueEqual(FlowMode.BOTH, target.face(Direction.SOUTH).mode(ConduitChannel.FLUIDS), "the click pasted instead of opening the tap");
            helper.succeed();
        });

        r.add("conduit/idle_face_resumes_within_an_interval", 300, helper -> {
            ChestBlockEntity[] chests = chestLine(helper);
            ConduitTapBlockEntity source = tap(helper, 1);
            setMode(source, Direction.WEST, ConduitChannel.ITEMS, FlowMode.EXTRACT);
            helper.runAfterDelay(200, () -> chests[0].setItem(0, new ItemStack(Items.COBBLESTONE, 8)));
            helper.runAfterDelay(245, () -> {
                helper.assertValueEqual(8, count(chests[1], Items.COBBLESTONE), "items moved within 45 ticks of arriving after a long idle");
                helper.succeed();
            });
        });

        r.add("conduit/higher_priority_fills_first", 120, helper -> {
            Fork fork = fork(helper);
            fork.targetTap().face(Direction.SOUTH).setPriority(5);
            fork.targetTap().configChanged();
            setMode(fork.sourceTap(), Direction.WEST, ConduitChannel.ITEMS, FlowMode.EXTRACT);
            fork.source().setItem(0, new ItemStack(Items.COBBLESTONE, 8));
            helper.succeedWhen(() -> {
                helper.assertValueEqual(8, count(fork.south(), Items.COBBLESTONE), "items in the priority 5 chest");
                helper.assertValueEqual(0, count(fork.north(), Items.COBBLESTONE), "items in the priority 0 chest");
            });
        });

        r.add("conduit/round_robin_reaches_every_destination", 20, helper -> {
            Fork fork = fork(helper);
            ConduitTapBlockEntity source = fork.sourceTap();
            source.face(Direction.WEST).setMode(ConduitChannel.ITEMS, FlowMode.EXTRACT);
            source.configChanged();
            source.serverTick(helper.getLevel());
            ResourceHandler<ItemResource> sink = helper.getLevel().getCapability(TransferCapabilities.Item.BLOCK, source.getBlockPos(), Direction.WEST);
            StringBuilder trace = new StringBuilder();
            for (int i = 0; i < 2; i++) {
                int moved;
                try (Transaction tx = Transaction.openRoot()) {
                    moved = sink == null ? -1 : sink.insert(ItemResource.of(Items.COBBLESTONE), 4, tx);
                    tx.commit();
                }
                trace.append(moved).append(':').append(count(fork.north(), Items.COBBLESTONE)).append('/')
                        .append(count(fork.south(), Items.COBBLESTONE)).append(' ');
            }
            helper.assertTrue(count(fork.north(), Items.COBBLESTONE) > 0, "items reached the north chest " + trace);
            helper.assertTrue(count(fork.south(), Items.COBBLESTONE) > 0, "items reached the south chest " + trace);
            helper.succeed();
        });

        r.add("conduit/round_robin_pulls_alternate", 160, helper -> {
            Fork fork = fork(helper);
            setMode(fork.sourceTap(), Direction.WEST, ConduitChannel.ITEMS, FlowMode.EXTRACT);
            fork.source().setItem(0, new ItemStack(Items.COBBLESTONE, 32));
            StringBuilder trace = new StringBuilder();
            for (int t = 5; t <= 150; t += 5) {
                int tick = t;
                helper.runAfterDelay(t, () -> trace.append(tick).append('=').append(count(fork.north(), Items.COBBLESTONE)).append('/')
                        .append(count(fork.south(), Items.COBBLESTONE)).append(' '));
            }
            helper.runAfterDelay(155, () -> {
                helper.assertTrue(count(fork.south(), Items.COBBLESTONE) > 0, "pull round robin " + trace);
                helper.assertTrue(count(fork.north(), Items.COBBLESTONE) > 0, "pull round robin " + trace);
                helper.succeed();
            });
        });

        r.add("conduit/fluids_move_with_an_upgrade", 120, helper -> {
            ServerLevel level = helper.getLevel();
            line(helper, 1, 7);
            BlockPos full = helper.absolutePos(new BlockPos(0, Y, 4));
            BlockPos empty = helper.absolutePos(new BlockPos(8, Y, 4));
            level.setBlock(full, Blocks.WATER_CAULDRON.defaultBlockState().setValue(LayeredCauldronBlock.LEVEL, 3), Block.UPDATE_ALL);
            level.setBlock(empty, Blocks.CAULDRON.defaultBlockState(), Block.UPDATE_ALL);
            ConduitTapBlockEntity source = tap(helper, 1);
            tap(helper, 7);
            source.getItemStackHandler().setStackInSlot(ConduitTapBlockEntity.UPGRADE_SLOT, new ItemStack(NTItems.EDDY_UPGRADE.get()));
            setMode(source, Direction.WEST, ConduitChannel.FLUIDS, FlowMode.EXTRACT);
            helper.succeedWhen(() -> {
                helper.assertTrue(level.getBlockState(empty).is(Blocks.WATER_CAULDRON), "water reached the empty cauldron");
                helper.assertTrue(level.getBlockState(full).is(Blocks.CAULDRON), "the full cauldron was emptied");
            });
        });

        r.add("conduit/energy_moves_within_the_rate", 60, helper -> {
            ServerLevel level = helper.getLevel();
            line(helper, 1, 7);
            BlockPos generator = helper.absolutePos(new BlockPos(0, Y, 4));
            BlockPos converter = helper.absolutePos(new BlockPos(8, Y, 4));
            level.setBlock(generator, NTBlocks.CREATIVE_ENERGY_SOURCE.get().defaultBlockState(), Block.UPDATE_ALL);
            level.setBlock(converter, NTBlocks.ENERGY_CONVERTER.get().defaultBlockState(), Block.UPDATE_ALL);
            ConduitTapBlockEntity source = tap(helper, 1);
            tap(helper, 7);
            long[] before = new long[1];
            helper.runAfterDelay(2, () -> {
                before[0] = ((EnergyConverterBlockEntity) level.getBlockEntity(converter)).getFeBuffer().getAmountAsLong();
                setMode(source, Direction.WEST, ConduitChannel.ENERGY, FlowMode.EXTRACT);
            });
            helper.runAfterDelay(5, () -> {
                long gained = ((EnergyConverterBlockEntity) level.getBlockEntity(converter)).getFeBuffer().getAmountAsLong() - before[0];
                helper.assertTrue(gained > 0, "the converter received FE through the network");
                helper.assertTrue(gained <= 4L * source.rates().energyRate(), "FE moved in 3 ticks stayed within the base rate, got " + gained);
                helper.succeed();
            });
        });
    }

    private static void line(NTGameTestHelper helper, int fromX, int toX) {
        ServerLevel level = helper.getLevel();
        for (int x = fromX; x <= toX; x++) {
            level.setBlock(helper.absolutePos(new BlockPos(x, Y, 4)), NTBlocks.CURRENT_CONDUIT.get().defaultBlockState(), Block.UPDATE_ALL);
        }
        for (int x = fromX; x <= toX; x++) {
            BlockPos pos = helper.absolutePos(new BlockPos(x, Y, 4));
            level.setBlock(pos, CurrentConduitBlock.refresh(level, pos, level.getBlockState(pos)), Block.UPDATE_ALL);
        }
    }

    private static BlockPos tapAt(NTGameTestHelper helper, int x) {
        ServerLevel level = helper.getLevel();
        BlockPos pos = helper.absolutePos(new BlockPos(x, Y, 4));
        if (!(level.getBlockState(pos).getBlock() instanceof ConduitTapBlock)) {
            ConduitWrenching.convert(level, pos, level.getBlockState(pos), null);
        }
        return pos;
    }

    private static ServerPlayer serverPlayer(NTGameTestHelper helper, String name) {
        return new ServerPlayer(helper.getLevel().getServer(), helper.getLevel(), new GameProfile(UUID.randomUUID(), name),
                ClientInformation.createDefault());
    }

    private static ConduitTapBlockEntity tap(NTGameTestHelper helper, int x) {
        return (ConduitTapBlockEntity) helper.getLevel().getBlockEntity(tapAt(helper, x));
    }

    private static ChestBlockEntity chest(NTGameTestHelper helper, int x, int z) {
        BlockPos pos = helper.absolutePos(new BlockPos(x, Y, z));
        helper.getLevel().setBlock(pos, Blocks.CHEST.defaultBlockState(), Block.UPDATE_ALL);
        return (ChestBlockEntity) helper.getLevel().getBlockEntity(pos);
    }

    private static ChestBlockEntity[] chestLine(NTGameTestHelper helper) {
        ChestBlockEntity left = chest(helper, 0, 4);
        ChestBlockEntity right = chest(helper, 8, 4);
        line(helper, 1, 7);
        tapAt(helper, 1);
        tapAt(helper, 7);
        return new ChestBlockEntity[]{left, right};
    }

    private static Fork fork(NTGameTestHelper helper) {
        line(helper, 1, 7);
        ChestBlockEntity source = chest(helper, 0, 4);
        ChestBlockEntity north = chest(helper, 7, 3);
        ChestBlockEntity south = chest(helper, 7, 5);
        return new Fork(source, north, south, tap(helper, 1), tap(helper, 7));
    }

    private static void setMode(ConduitTapBlockEntity tap, Direction face, ConduitChannel channel, FlowMode mode) {
        tap.face(face).setMode(channel, mode);
        tap.configChanged();
    }

    private static int count(ChestBlockEntity chest, Item item) {
        int total = 0;
        for (int slot = 0; slot < chest.getContainerSize(); slot++) {
            ItemStack stack = chest.getItem(slot);
            if (stack.is(item)) {
                total += stack.getCount();
            }
        }
        return total;
    }

    private ConduitTests() {
    }
}
