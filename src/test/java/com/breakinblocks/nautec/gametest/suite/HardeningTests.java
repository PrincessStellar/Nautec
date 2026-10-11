package com.breakinblocks.nautec.gametest.suite;

import com.breakinblocks.nautec.api.augments.AugmentSlot;
import com.breakinblocks.nautec.content.augments.EnderMagnetAugment;
import com.breakinblocks.nautec.content.distributor.DistributorLink;
import com.breakinblocks.nautec.content.menus.ResonanceNodeMenu;
import com.breakinblocks.nautec.content.resonance.ResonanceActions;
import com.breakinblocks.nautec.content.resonance.ResonanceNodeBlockEntity;
import com.breakinblocks.nautec.network.ResonanceActionPayload;
import com.breakinblocks.nautec.registries.NTAugments;
import com.breakinblocks.nautec.registries.NTBlocks;
import com.breakinblocks.nautec.utils.TemplateSanitizer;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.Optional;
import java.util.UUID;

public final class HardeningTests {
    private static final BlockPos NODE = new BlockPos(4, 1, 4);

    private HardeningTests() {
    }

    private static ServerPlayer player(NTGameTestHelper helper, String name) {
        return new ServerPlayer(helper.getLevel().getServer(), helper.getLevel(), new GameProfile(UUID.randomUUID(), name),
                ClientInformation.createDefault());
    }

    private static ResonanceNodeBlockEntity node(NTGameTestHelper helper) {
        helper.setBlock(NODE, NTBlocks.RESONANCE_NODE.get());
        return helper.getBlockEntity(NODE, ResonanceNodeBlockEntity.class);
    }

    private static ServerPlayer playerNear(NTGameTestHelper helper, String name) {
        ServerPlayer player = player(helper, name);
        player.setPos(helper.absoluteVec(new Vec3(4.5, 1.0, 2.5)));
        return player;
    }

    private static ResonanceActionPayload mode(BlockPos pos) {
        return new ResonanceActionPayload(pos, ResonanceActionPayload.MODE, Optional.empty(), "");
    }

    public static void register(NTTestRegistrar r) {
        r.add("hardening/resonance_action_ignores_unloaded_positions", 20, helper -> {
            ServerLevel level = helper.getLevel();
            ServerPlayer player = playerNear(helper, "far_resonance");
            BlockPos far = helper.absolutePos(NODE).offset(200_000, 0, 200_000);
            int chunkX = far.getX() >> 4;
            int chunkZ = far.getZ() >> 4;
            helper.assertFalse(level.getChunkSource().hasChunk(chunkX, chunkZ), "the far chunk starts unloaded");

            ResonanceActions.apply(player, mode(far));

            helper.assertFalse(level.getChunkSource().hasChunk(chunkX, chunkZ), "a resonance action loaded a chunk 200k blocks away");
            helper.succeed();
        });

        r.add("hardening/resonance_action_needs_the_open_menu", 20, helper -> {
            ResonanceNodeBlockEntity node = node(helper);
            ServerPlayer player = playerNear(helper, "menu_resonance");
            boolean before = node.isOutput();

            ResonanceActions.apply(player, mode(helper.absolutePos(NODE)));
            helper.assertValueEqual(node.isOutput(), before, "output mode without the node menu open");

            player.containerMenu = new ResonanceNodeMenu(1, player.getInventory(), node);
            try {
                ResonanceActions.apply(player, mode(helper.absolutePos(NODE)));
            } catch (NullPointerException expected) {
            }
            helper.assertValueEqual(node.isOutput(), !before, "output mode with the node menu open");
            helper.succeed();
        });

        r.add("hardening/ender_magnet_keeps_what_does_not_fit", 20, helper -> {
            Player player = helper.makeMockPlayer(GameType.SURVIVAL);
            player.moveTo(helper.absoluteVec(new Vec3(4.5, 1.0, 4.5)), 0.0f, 0.0f);
            Inventory inventory = player.getInventory();
            for (int i = 0; i < Inventory.INVENTORY_SIZE; i++) {
                inventory.setItem(i, new ItemStack(Items.STONE, 64));
            }
            inventory.setItem(0, new ItemStack(Items.DIRT, 60));

            ItemEntity drop = new ItemEntity(helper.getLevel(), player.getX() + 1.0, player.getY(), player.getZ(),
                    new ItemStack(Items.DIRT, 10));
            drop.setNoPickUpDelay();
            helper.getLevel().addFreshEntity(drop);

            AugmentSlot slot = NTAugments.ENDER_MAGNET_AUGMENT.get().getAugmentSlots().iterator().next();
            EnderMagnetAugment augment = new EnderMagnetAugment(slot);
            augment.setPlayer(player);
            augment.serverTick(null);

            helper.assertValueEqual(inventory.getItem(0).getCount(), 64, "dirt in the inventory after the magnet pull");
            helper.assertFalse(drop.isRemoved(), "the magnet deleted a drop that only partly fit");
            helper.assertValueEqual(drop.getItem().getCount(), 6, "dirt left on the ground");
            drop.discard();
            helper.succeed();
        });

        r.add("hardening/distributor_templates_drop_custom_data", 20, helper -> {
            DistributorLink link = new DistributorLink(helper.absolutePos(NODE), Direction.UP);
            ItemStack padded = new ItemStack(Items.DIAMOND, 12);
            CompoundTag tag = new CompoundTag();
            tag.putString("padding", "x".repeat(4096));
            padded.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));

            link.setItem(0, padded);
            helper.assertTrue(link.item(0).is(Items.DIAMOND), "the template keeps its item");
            helper.assertTrue(link.item(0).get(DataComponents.CUSTOM_DATA) == null, "the template kept client custom_data");
            helper.assertValueEqual(link.itemAmount(0), 12, "requested amount");

            ItemStack named = new ItemStack(Items.DIAMOND, 3);
            named.set(DataComponents.CUSTOM_NAME, Component.literal("y".repeat(4096)));
            ItemStack trimmed = TemplateSanitizer.item(named, helper.getLevel().registryAccess());
            helper.assertTrue(trimmed.is(Items.DIAMOND), "an oversized template keeps its item");
            helper.assertTrue(trimmed.get(DataComponents.CUSTOM_NAME) == null, "an oversized template was stored whole");
            helper.assertValueEqual(trimmed.getCount(), 3, "an oversized template keeps its count");

            ItemStack small = new ItemStack(Items.DIAMOND);
            small.set(DataComponents.CUSTOM_NAME, Component.literal("Shiny"));
            helper.assertTrue(TemplateSanitizer.item(small, helper.getLevel().registryAccess()).get(DataComponents.CUSTOM_NAME) != null,
                    "a small template should keep its components");
            helper.succeed();
        });

        r.add("hardening/distributor_fluid_amount_is_capped", 20, helper -> {
            DistributorLink link = new DistributorLink(helper.absolutePos(NODE), Direction.UP);
            link.setFluid(0, new FluidStack(Fluids.WATER, 50_000_000));
            helper.assertValueEqual(link.fluidAmount(0), DistributorLink.MAX_FLUID_AMOUNT, "fluid request amount");
            helper.assertValueEqual(link.fluid(0).getAmount(), 1, "the fluid template is a single unit");
            helper.succeed();
        });
    }
}
