package com.breakinblocks.nautec.gametest.suite;

import com.breakinblocks.nautec.data.NTDataComponentsUtils;
import com.breakinblocks.nautec.content.blockentities.MixerBlockEntity;
import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.content.blockentities.multiblock.controller.AugmentationStationBlockEntity;
import com.breakinblocks.nautec.content.blockentities.multiblock.part.AugmentationStationExtensionBlockEntity;
import com.breakinblocks.nautec.content.blockentities.PressureForgeBlockEntity;
import com.breakinblocks.nautec.content.menus.PressureForgeMenu;
import com.breakinblocks.nautec.content.menus.IncubatorMenu;
import com.breakinblocks.nautec.network.StartAugmentationPayload;
import com.breakinblocks.nautec.network.ClearAugmentPayload;
import com.breakinblocks.nautec.network.KeyPressedPayload;
import com.breakinblocks.nautec.network.AugmentationStationSyncPayload;
import com.breakinblocks.nautec.data.NTDataAttachments;
import com.breakinblocks.nautec.events.NTEvents;
import com.breakinblocks.nautec.events.AugmentEvents;
import com.breakinblocks.nautec.utils.MultiblockHelper;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.arrow.ThrownTrident;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.network.RegistryFriendlyByteBuf;
import io.netty.buffer.Unpooled;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import java.util.Optional;
import java.util.List;
import com.breakinblocks.nautec.api.bacteria.BacteriaInstance;
import com.breakinblocks.nautec.capabilities.NTCapabilities;
import com.breakinblocks.nautec.content.blockentities.IncubatorBlockEntity;
import com.breakinblocks.nautec.content.blockentities.PrismarineLaserRelayBlockEntity;
import com.breakinblocks.nautec.content.blocks.PrismarineLaserRelayBlock;
import com.breakinblocks.nautec.content.items.BatteryItem;
import com.breakinblocks.nautec.data.NTDataComponents;
import com.breakinblocks.nautec.network.BacteriaSlotClickedPayload;
import com.breakinblocks.nautec.network.SyncAugmentPayload;
import com.breakinblocks.nautec.registries.*;
import com.breakinblocks.nautec.utils.AugmentHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import top.theillusivec4.curios.api.SlotContext;

import java.lang.reflect.Proxy;
import java.util.LinkedHashMap;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public final class ReviewRegressionTests {
    private static IPayloadContext context(Player player) {
        return (IPayloadContext) Proxy.newProxyInstance(IPayloadContext.class.getClassLoader(), new Class[]{IPayloadContext.class},
                (proxy, method, args) -> {
                    if (method.getName().equals("player")) return player;
                    if (method.getName().equals("enqueueWork")) {
                        ((Runnable) args[0]).run();
                        return CompletableFuture.completedFuture(null);
                    }
                    throw new UnsupportedOperationException(method.getName());
                });
    }

    private static AugmentationStationBlockEntity station(GameTestHelper helper) {
        BlockPos center = new BlockPos(4, 1, 4);
        helper.setBlock(center, NTBlocks.AUGMENTATION_STATION.get());
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            helper.setBlock(center.relative(direction), NTBlocks.POLISHED_PRISMARINE.get());
            helper.setBlock(center.relative(direction, 2), NTBlocks.AUGMENTATION_STATION_EXTENSION.get());
        }
        for (int x : new int[]{-1, 1}) {
            for (int z : new int[]{-1, 1}) {
                helper.setBlock(center.offset(x, 0, z), NTBlocks.AQUARINE_STEEL_BLOCK.get());
            }
        }
        helper.assertTrue(MultiblockHelper.form(NTMultiblocks.AUGMENTATION_STATION.get(), helper.absolutePos(center), helper.getLevel()), "station forms");
        var extension = helper.getBlockEntity(center.north(2), AugmentationStationExtensionBlockEntity.class);
        extension.getItemStackHandler().setStackInSlot(1, NTItems.CLAW_ROBOT_ARM.toStack());
        extension.getItemStackHandler().setStackInSlot(0, NTItems.DROWNED_LUNGS.toStack());
        extension.setPowerPerSide(Direction.DOWN, NTConfig.augmentationStationPower);
        extension.commonTick();
        var station = helper.getBlockEntity(center, AugmentationStationBlockEntity.class);
        helper.assertTrue(station.getRecipe().isPresent(), "fixture has a drowned lung recipe");
        return station;
    }

    private static Player recipient(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setPos(helper.absolutePos(new BlockPos(4, 2, 4)).getCenter());
        player.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.17);
        player.getAttribute(Attributes.JUMP_STRENGTH).setBaseValue(0.6);
        return player;
    }

    private static void assertReleased(GameTestHelper helper, Player player) {
        helper.assertValueEqual(0.17, player.getAttributeValue(Attributes.MOVEMENT_SPEED), "movement restored");
        helper.assertValueEqual(0.6, player.getAttributeValue(Attributes.JUMP_STRENGTH), "jump restored");
        helper.assertTrue(player.getData(NTDataAttachments.AUGMENTATION_STATION).isEmpty(), "operation attachment cleared");
    }

    public static void register(NTTestRegistrar registrar) {
        var tests = new LinkedHashMap<String, Consumer<GameTestHelper>>();
        tests.put("server_rejects_unearned_augment", helper -> {
            Player player = helper.makeMockPlayer(GameType.SURVIVAL);
            var slot = NTAugmentSlots.LUNG.get();
            SyncAugmentPayload.setAugmentDataAction(new SyncAugmentPayload(NTAugments.DROWNED_LUNG.get().create(slot), new CompoundTag()), context(player));
            helper.assertTrue(AugmentHelper.getAugmentBySlot(player, slot) == null, "Unauthenticated sync installed drowned lung on server");
            helper.succeed();
        });
        tests.put("bacteria_click_conserves_population", helper -> {
            BlockPos pos = new BlockPos(4, 1, 4);
            helper.setBlock(pos, NTBlocks.INCUBATOR.get().defaultBlockState());
            var machine = helper.getBlockEntity(pos, IncubatorBlockEntity.class);
            Player player = helper.makeMockPlayer(GameType.SURVIVAL);
            ItemStack dish = new ItemStack(NTItems.PETRI_DISH.get());
            var carried = dish.getCapability(NTCapabilities.BacteriaStorage.ITEM);
            BacteriaInstance bacteria = BacteriaInstance.roll(NTBacterias.LITHOPHILES, helper.getLevel().registryAccess()).copyWithSize(100);
            carried.setBacteria(0, bacteria.copy());
            machine.getBacteriaStorage().setBacteria(0, bacteria.copyWithSize(200));
            player.setPos(helper.absolutePos(pos).getCenter());
            player.containerMenu = new IncubatorMenu(1, player.getInventory(), machine);
            player.containerMenu.setCarried(dish);
            new BacteriaSlotClickedPayload(helper.absolutePos(pos), 1, 0).handle(context(player));
            long total = carried.getBacteria(0).getSize() + machine.getBacteriaStorage().getBacteria(0).getSize();
            helper.assertValueEqual(300L, total, "population after clicking compatible colony");
            helper.succeed();
        });
        tests.put("bacteria_uses_carried_population", helper -> {
            BlockPos pos = new BlockPos(4, 1, 4);
            helper.setBlock(pos, NTBlocks.INCUBATOR.get().defaultBlockState());
            var machine = helper.getBlockEntity(pos, IncubatorBlockEntity.class);
            Player player = helper.makeMockPlayer(GameType.SURVIVAL);
            ItemStack dish = new ItemStack(NTItems.PETRI_DISH.get());
            BacteriaInstance bacteria = BacteriaInstance.roll(NTBacterias.LITHOPHILES, helper.getLevel().registryAccess()).copyWithSize(100);
            dish.getCapability(NTCapabilities.BacteriaStorage.ITEM).setBacteria(0, bacteria);
            player.setPos(helper.absolutePos(pos).getCenter());
            player.containerMenu = new IncubatorMenu(1, player.getInventory(), machine);
            player.containerMenu.setCarried(dish);
            new BacteriaSlotClickedPayload(helper.absolutePos(pos), 1, 0).handle(context(player));
            helper.assertValueEqual(100L, machine.getBacteriaStorage().getBacteria(0).getSize(), "server carried population inserted");
            helper.succeed();
        });
        tests.put("mixer_preserves_unrelated_fluid_output", helper -> {
            BlockPos pos = new BlockPos(4, 1, 4);
            helper.setBlock(pos, NTBlocks.MIXER.get().defaultBlockState());
            var mixer = helper.getBlockEntity(pos, MixerBlockEntity.class);
            mixer.getSecondaryFluidTank().setFluid(new net.neoforged.neoforge.fluids.FluidStack(NTFluids.ETCHING_ACID.getStillFluid(), 1000));
            mixer.getFluidTank().setFluid(new net.neoforged.neoforge.fluids.FluidStack(NTFluids.SALT_WATER.getStillFluid(), 1000));
            mixer.getItemStackHandler().setStackInSlot(0, new ItemStack(Items.RAW_IRON, 2));
            mixer.getItemStackHandler().setStackInSlot(1, new ItemStack(Items.PRISMARINE_CRYSTALS));
            for (int tick = 0; tick < 1200; tick++) {
                mixer.setPowerPerSide(Direction.EAST, 1000);
                mixer.commonTick();
            }
            helper.assertTrue(mixer.getItemStackHandler().getStackInSlot(4).is(NTItems.AQUARINE_STEEL_COMPOUND), "Fixture must finish item recipe");
            helper.assertValueEqual(1000, mixer.getSecondaryFluidTank().getFluidAmount(), "stored acid after item-only output recipe");
            helper.succeed();
        });
        tests.put("battery_transfer_conserves_power", helper -> {
            Player player = helper.makeMockPlayer(GameType.SURVIVAL);
            ItemStack source = new ItemStack(NTItems.PRISMATIC_BATTERY.get());
            ItemStack target = new ItemStack(NTItems.PRISMATIC_BATTERY.get());
            var sourcePower = source.getCapability(NTCapabilities.PowerStorage.ITEM);
            var targetPower = target.getCapability(NTCapabilities.PowerStorage.ITEM);
            sourcePower.setPowerStored(1000);
            source.set(NTDataComponents.ABILITY_ENABLED, true);
            player.getInventory().setItem(0, target);
            ((BatteryItem) source.getItem()).curioTick(source, new SlotContext("battery", player, 0, false, true));
            helper.assertValueEqual(1000, sourcePower.getPowerStored() + targetPower.getPowerStored(), "total power after one battery transfer");
            helper.succeed();
        });
        tests.put("unpowered_relay_does_not_damage", helper -> {
            BlockPos pos = new BlockPos(2, 1, 4);
            helper.setBlock(pos, NTBlocks.PRISMARINE_RELAY.get().defaultBlockState().setValue(PrismarineLaserRelayBlock.FACING, Direction.EAST));
            helper.setBlock(new BlockPos(5, 1, 4), NTBlocks.MIXER.get().defaultBlockState());
            var relay = helper.getBlockEntity(pos, PrismarineLaserRelayBlockEntity.class);
            relay.getLaserDistances().put(Direction.EAST, 3);
            var cow = helper.spawn(EntityType.COW, new BlockPos(3, 1, 4));
            float before = cow.getHealth();
            relay.commonTick();
            helper.assertValueEqual(0, relay.getPower(), "relay is unpowered");
            helper.assertValueEqual(before, cow.getHealth(), "cow health after unpowered relay tick");
            helper.succeed();
        });
        tests.put("augment_replacement_removes_old_effect", helper -> {
            Player player = helper.makeMockPlayer(GameType.SURVIVAL);
            var slot = NTAugmentSlots.HEART.get();
            AugmentHelper.createAugment(NTAugments.BONUS_HEART_AUGMENT.get(), player, slot);
            AugmentHelper.createAugment(NTAugments.ELDRITCH_HEART.get(), player, slot);
            helper.assertValueEqual(20.0f, player.getMaxHealth(), "max health after replacing bonus hearts");
            helper.succeed();
        });
        tests.put("server_rejects_client_clear", helper -> {
            Player player = helper.makeMockPlayer(GameType.SURVIVAL);
            var slot = NTAugmentSlots.LUNG.get();
            AugmentHelper.createAugment(NTAugments.DROWNED_LUNG.get(), player, slot);
            ClearAugmentPayload.clearAugmentAction(new ClearAugmentPayload(slot), context(player));
            helper.assertTrue(AugmentHelper.getAugmentBySlot(player, slot) != null, "client cannot clear server augment state");
            helper.succeed();
        });
        for (String invalid : List.of("no_menu", "wrong_id", "wrong_position", "remote", "negative_slot", "large_slot")) {
            tests.put("bacteria_rejects_" + invalid, helper -> {
                BlockPos pos = new BlockPos(4, 1, 4);
                helper.setBlock(pos, NTBlocks.INCUBATOR.get());
                var machine = helper.getBlockEntity(pos, IncubatorBlockEntity.class);
                Player player = helper.makeMockPlayer(GameType.SURVIVAL);
                player.setPos(helper.absolutePos(pos).getCenter());
                if (!invalid.equals("no_menu")) player.containerMenu = new IncubatorMenu(1, player.getInventory(), machine);
                var dish = NTItems.PETRI_DISH.toStack();
                var carried = dish.getCapability(NTCapabilities.BacteriaStorage.ITEM);
                carried.setBacteria(0, BacteriaInstance.roll(NTBacterias.LITHOPHILES, helper.getLevel().registryAccess()).copyWithSize(100));
                player.containerMenu.setCarried(dish);
                if (invalid.equals("remote")) player.setPos(helper.absolutePos(pos).getCenter().add(100, 0, 0));
                new BacteriaSlotClickedPayload(helper.absolutePos(invalid.equals("wrong_position") ? pos.east() : pos),
                        invalid.equals("wrong_id") ? 2 : 1, invalid.equals("negative_slot") ? -1 : invalid.equals("large_slot") ? 1000 : 0).handle(context(player));
                helper.assertValueEqual(100L, carried.getBacteria(0).getSize(), "invalid request preserves carried bacteria");
                helper.assertTrue(machine.getBacteriaStorage().getBacteria(0).isEmpty(), "invalid request cannot populate machine");
                helper.succeed();
            });
        }
        tests.put("augment_activation_enforces_server_cooldown", helper -> {
            Player player = recipient(helper);
            var slot = NTAugmentSlots.LEFT_ARM.get();
            AugmentHelper.createAugment(NTAugments.THROWN_BOUNCING_TRIDENT_AUGMENT.get(), player, slot);
            KeyPressedPayload.keyPressedAction(new KeyPressedPayload(slot), context(player));
            KeyPressedPayload.keyPressedAction(new KeyPressedPayload(slot), context(player));
            helper.assertValueEqual(1, helper.getLevel().getEntitiesOfClass(ThrownTrident.class, player.getBoundingBox().inflate(3)).size(), "repeated packets launch one trident");
            helper.succeed();
        });
        tests.put("surgery_completes_and_preserves_base_attributes", helper -> {
            var station = station(helper);
            Player player = recipient(helper);
            StartAugmentationPayload.startAugmentation(new StartAugmentationPayload(station.getBlockPos(), NTAugmentSlots.LUNG.get()), context(player));
            helper.assertTrue(station.isOperatingOn(player), "valid surgery starts");
            helper.assertValueEqual(0.0, player.getAttributeValue(Attributes.MOVEMENT_SPEED), "movement temporarily locked");
            helper.assertValueEqual(0.17, player.getAttribute(Attributes.MOVEMENT_SPEED).getBaseValue(), "base movement unchanged during surgery");
            for (int tick = 0; tick < 80; tick++) station.commonTick();
            helper.assertTrue(AugmentHelper.getAugmentBySlot(player, NTAugmentSlots.LUNG.get()) != null, "surgery installs augment");
            helper.assertTrue(AugmentHelper.getAugmentsData(player).containsKey(NTAugmentSlots.LUNG.get()), "installation records state for synchronization");
            helper.assertTrue(helper.getBlockEntity(new BlockPos(4, 1, 2), AugmentationStationExtensionBlockEntity.class).getAugmentItem().isEmpty(), "successful surgery consumes ingredient");
            assertReleased(helper, player);
            helper.succeed();
        });
        for (String interruption : List.of("removed", "disconnected", "moved", "ingredient_changed", "power_lost", "extension_removed")) {
            tests.put("surgery_releases_on_" + interruption, helper -> {
                var station = station(helper);
                Player player = recipient(helper);
                station.startAugmentation(player, NTAugmentSlots.LUNG.get());
                helper.assertTrue(station.isOperatingOn(player), "valid surgery starts");
                var extension = helper.getBlockEntity(new BlockPos(4, 1, 2), AugmentationStationExtensionBlockEntity.class);
                switch (interruption) {
                    case "removed" -> helper.destroyBlock(new BlockPos(4, 1, 4));
                    case "disconnected" -> AugmentEvents.onPlayerLeave(new EntityLeaveLevelEvent(player, helper.getLevel()));
                    case "moved" -> player.setPos(player.position().add(4, 0, 0));
                    case "ingredient_changed" -> extension.getItemStackHandler().setStackInSlot(0, Items.DIRT.getDefaultInstance());
                    case "power_lost" -> { extension.setPowerPerSide(Direction.DOWN, 0); extension.commonTick(); }
                    case "extension_removed" -> helper.destroyBlock(new BlockPos(4, 1, 2));
                }
                station.commonTick();
                assertReleased(helper, player);
                helper.assertTrue(AugmentHelper.getAugmentBySlot(player, NTAugmentSlots.LUNG.get()) == null, "interruption does not install augment");
                helper.succeed();
            });
        }
        for (String invalid : List.of("remote", "incompatible_slot", "unformed", "unpowered")) {
            tests.put("surgery_rejects_" + invalid, helper -> {
                var station = station(helper);
                Player player = recipient(helper);
                if (invalid.equals("remote")) player.setPos(player.position().add(20, 0, 0));
                if (invalid.equals("unformed")) MultiblockHelper.unform(NTMultiblocks.AUGMENTATION_STATION.get(), station.getBlockPos(), helper.getLevel());
                if (invalid.equals("unpowered")) {
                    var extension = helper.getBlockEntity(new BlockPos(4, 1, 2), AugmentationStationExtensionBlockEntity.class);
                    extension.setPowerPerSide(Direction.DOWN, 0);
                    extension.commonTick();
                }
                StartAugmentationPayload.startAugmentation(new StartAugmentationPayload(station.getBlockPos(), invalid.equals("incompatible_slot") ? NTAugmentSlots.HEART.get() : NTAugmentSlots.LUNG.get()), context(player));
                helper.assertTrue(!station.isOperatingOn(player), "invalid surgery is rejected");
                assertReleased(helper, player);
                helper.succeed();
            });
        }
        tests.put("augmentation_screen_snapshot_round_trip", helper -> {
            var station = station(helper);
            var recipe = station.getRecipe().orElseThrow();
            var payload = new AugmentationStationSyncPayload(station.getBlockPos(), true, station.getStatus(), 0, Optional.of(recipe.resultAugment()),
                    recipe.augmentItem().getDefaultInstance(), recipe.desc(),
                    List.of(new AugmentationStationSyncPayload.Extension(Direction.NORTH, true, true, recipe.augmentItem().getDefaultInstance(), 30)));
            var buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), helper.getLevel().registryAccess());
            try {
                AugmentationStationSyncPayload.STREAM_CODEC.encode(buffer, payload);
                var decoded = AugmentationStationSyncPayload.STREAM_CODEC.decode(buffer);
                helper.assertTrue(decoded.result().orElseThrow().getAugmentSlots().equals(List.of(NTAugmentSlots.LUNG.get())), "screen receives only compatible slots");
                helper.assertTrue(decoded.preview().is(NTItems.DROWNED_LUNGS), "screen receives recipe preview");
                helper.assertValueEqual(recipe.desc(), decoded.description(), "screen receives the augment description");
                helper.assertValueEqual(30, decoded.extensions().getFirst().power(), "screen receives each extension's beam");
                helper.assertTrue(decoded.extensions().getFirst().part().is(NTItems.DROWNED_LUNGS), "screen receives each extension's part");
            } finally { buffer.release(); }
            helper.succeed();
        });
        tests.put("login_restores_transient_augment_effects", helper -> {
            Player player = recipient(helper);
            var slot = NTAugmentSlots.HEAD.get();
            AugmentHelper.createAugment(NTAugments.VENT_CARAPACE.get(), player, slot);
            AugmentHelper.getAugmentBySlot(player, slot).onRemoved(player);
            helper.assertValueEqual(0.0, player.getAttributeValue(Attributes.ARMOR), "transient effects absent before login");
            NTEvents.Game.onPlayerLoggedIn(new PlayerEvent.PlayerLoggedInEvent(player));
            helper.assertValueEqual(4.0, player.getAttributeValue(Attributes.ARMOR), "login reapplies armor");
            helper.assertValueEqual(0.5, player.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE), "login reapplies knockback resistance");
            helper.succeed();
        });
        tests.put("removal_preserves_shared_augment_effects", helper -> {
            Player player = recipient(helper);
            AugmentHelper.createAugment(NTAugments.VENT_CARAPACE.get(), player, NTAugmentSlots.HEAD.get());
            AugmentHelper.createAugment(NTAugments.VENT_CARAPACE.get(), player, NTAugmentSlots.BODY.get());
            AugmentHelper.removeAugment(player, NTAugmentSlots.HEAD.get());
            helper.assertValueEqual(4.0, player.getAttributeValue(Attributes.ARMOR), "remaining carapace retains armor");
            helper.succeed();
        });
        tests.put("pressure_forge_output_accessible_to_automation", helper -> {
            BlockPos pos = new BlockPos(4, 1, 4);
            helper.setBlock(pos, NTBlocks.PRESSURE_FORGE.get());
            var forge = helper.getBlockEntity(pos, PressureForgeBlockEntity.class);
            forge.getItemStackHandler().setStackInSlot(1, NTItems.AQUARINE_STEEL_INGOT.toStack());
            var handler = helper.getLevel().getCapability(Capabilities.Item.BLOCK, helper.absolutePos(pos), Direction.DOWN);
            helper.assertTrue(handler != null, "forge exposes output capability");
            try (var tx = Transaction.openRoot()) {
                helper.assertValueEqual(1, handler.extract(ItemResource.of(NTItems.AQUARINE_STEEL_INGOT.toStack()), 1, tx), "bottom extracts finished output");
                tx.commit();
            }
            helper.assertTrue(forge.getItemStackHandler().getStackInSlot(1).isEmpty(), "extraction removes output");
            helper.succeed();
        });
        tests.put("pressure_forge_output_taken_through_its_screen", helper -> {
            BlockPos pos = new BlockPos(4, 1, 4);
            helper.setBlock(pos, NTBlocks.PRESSURE_FORGE.get());
            var forge = helper.getBlockEntity(pos, PressureForgeBlockEntity.class);
            forge.getItemStackHandler().setStackInSlot(1, NTItems.AQUARINE_STEEL_INGOT.toStack());
            Player player = recipient(helper);
            BlockPos absolute = helper.absolutePos(pos);
            InteractionResult result = forge.getBlockState().useWithoutItem(helper.getLevel(), player, new BlockHitResult(absolute.getCenter(), Direction.UP, absolute, false));
            helper.assertTrue(result.consumesAction(), "an empty hand opens the forge");
            helper.assertFalse(forge.getItemStackHandler().getStackInSlot(1).isEmpty(), "opening the forge leaves the output in place");
            PressureForgeMenu menu = new PressureForgeMenu(0, player.getInventory(), forge);
            ItemStack taken = menu.getSlot(38).remove(64);
            helper.assertTrue(taken.is(NTItems.AQUARINE_STEEL_INGOT.get()), "the output slot hands the item over");
            helper.assertTrue(forge.getItemStackHandler().getStackInSlot(1).isEmpty(), "and empties the forge's output");
            helper.succeed();
        });
        for (String interruption : List.of("leaves_fluid", "removed")) {
            tests.put("infusion_cancels_when_" + interruption, helper -> {
                BlockPos pos = new BlockPos(4, 1, 4);
                helper.setBlock(pos, NTFluids.EAS.getStillFluid().defaultFluidState().createLegacyBlock());
                var item = new ItemEntity(helper.getLevel(), helper.absolutePos(pos).getX() + 0.5, helper.absolutePos(pos).getY() + 0.5, helper.absolutePos(pos).getZ() + 0.5, NTItems.AQUARINE_PICKAXE.toStack());
                NTEvents.Game.onItemEntityTick(new EntityTickEvent.Post(item));
                helper.assertTrue(item.getData(NTDataAttachments.ITEM_INFUSION).isPresent(), "infusion begins in EAS");
                if (interruption.equals("removed")) {
                    NTEvents.Game.onEntityLeaveLevel(new EntityLeaveLevelEvent(item, helper.getLevel()));
                } else {
                    item.setPos(item.position().add(2, 0, 0));
                    NTEvents.Game.onItemEntityTick(new EntityTickEvent.Post(item));
                }
                helper.assertTrue(item.getData(NTDataAttachments.ITEM_INFUSION).isEmpty(), "interruption releases infusion state");
                helper.succeed();
            });
        }
        record SaltWaterCase(String name, Item bucket, boolean collect, boolean expectSaltWater) {
        }
        for (SaltWaterCase saltCase : List.of(
                new SaltWaterCase("empty_bucket_collects_ocean_saltwater", Items.BUCKET, true, true),
                new SaltWaterCase("filled_bucket_not_replaced_by_saltwater", Items.LAVA_BUCKET, true, false))) {
            tests.put(saltCase.name(), helper -> {
                helper.setBiome(Biomes.OCEAN);
                BlockPos pos = new BlockPos(4, 3, 4);
                helper.setBlock(pos.below(), Blocks.STONE);
                helper.setBlock(pos, Blocks.WATER);
                Player player = helper.makeMockPlayer(GameType.SURVIVAL);
                player.setPos(helper.absolutePos(pos).getCenter().add(0, 1, 0));
                player.setXRot(90);
                player.setItemInHand(InteractionHand.MAIN_HAND, saltCase.bucket().getDefaultInstance());
                boolean previous = NTConfig.collectSaltWater;
                NTConfig.collectSaltWater = saltCase.collect();
                InteractionResult result;
                try {
                    result = saltCase.bucket().use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
                } finally {
                    NTConfig.collectSaltWater = previous;
                }
                ItemStack transformed = result instanceof InteractionResult.Success success && success.heldItemTransformedTo() != null
                        ? success.heldItemTransformedTo() : player.getMainHandItem();
                helper.assertTrue(transformed.is(NTFluids.SALT_WATER.getBucket()) == saltCase.expectSaltWater(),
                        "salt water collection with collectSaltWater=" + saltCase.collect());
                helper.succeed();
            });
        }
        tests.put("salt_water_collection_off_by_default", helper -> {
            helper.assertFalse(NTConfig.COLLECT_SALT_WATER.getDefault(), "collectSaltWater should default to false");
            helper.succeed();
        });
        tests.put("infusion_consumes_source_without_destroying_floor", helper -> {
            BlockPos pos = new BlockPos(4, 1, 4);
            helper.setBlock(pos.below(), Blocks.STONE);
            helper.setBlock(pos, NTFluids.EAS.getStillFluid().defaultFluidState().createLegacyBlock());
            Vec3 location = helper.absolutePos(pos).getCenter();
            var item = new ItemEntity(helper.getLevel(), location.x, location.y, location.z, NTItems.AQUARINE_PICKAXE.toStack());
            for (int tick = 0; tick < 152; tick++) NTEvents.Game.onItemEntityTick(new EntityTickEvent.Post(item));
            helper.assertTrue(NTDataComponentsUtils.isInfused(item.getItem()), "item finishes infusion");
            helper.assertTrue(helper.getBlockState(pos).isAir(), "infusion consumes original fluid source");
            helper.assertBlockPresent(Blocks.STONE, pos.below());
            helper.assertTrue(item.getData(NTDataAttachments.ITEM_INFUSION).isEmpty(), "completed infusion releases state");
            helper.succeed();
        });
        tests.forEach((name, body) -> registrar.add("regression/" + name, 40, body));
    }
}
