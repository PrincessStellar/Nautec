package com.breakinblocks.nautec.datagen;

import com.breakinblocks.nautec.registries.NTBlocks;
import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.registries.NTItems;
import com.breakinblocks.nautec.tags.NTTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.IntrinsicHolderTagsProvider;
import net.minecraft.data.tags.IntrinsicHolderTagsProvider.IntrinsicTagAppender;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import java.util.concurrent.CompletableFuture;

public class ItemTagProvider extends IntrinsicHolderTagsProvider<Item> {

    public ItemTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, ExistingFileHelper existingFileHelper) {
        super(output, Registries.ITEM, lookupProvider, item -> item.builtInRegistryHolder().key(), Nautec.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(NTTags.Items.AQUATIC_CATALYST, Items.HEART_OF_THE_SEA);
        tag(NTTags.Items.AQUARINE_STEEL, NTItems.AQUARINE_STEEL_INGOT);
        tag(NTTags.Items.INGOTS_AQUARINE_COPPER, NTItems.AQUARINE_COPPER_INGOT);
        tag(Tags.Items.INGOTS, NTTags.Items.INGOTS_AQUARINE_COPPER);
        tag(NTTags.Items.NUGGETS_AQUARINE_COPPER, NTItems.AQUARINE_COPPER_NUGGET);
        tag(Tags.Items.NUGGETS, NTTags.Items.NUGGETS_AQUARINE_COPPER);
        tag(NTTags.Items.STORAGE_BLOCKS_AQUARINE_COPPER, NTBlocks.AQUARINE_COPPER_BLOCK.asItem());
        tag(Tags.Items.STORAGE_BLOCKS, NTTags.Items.STORAGE_BLOCKS_AQUARINE_COPPER);
        tag(NTTags.Items.DUSTS_SALT, NTItems.SALT);
        tag(Tags.Items.DUSTS, NTTags.Items.DUSTS_SALT);
        tag(ItemTags.DOORS, NTBlocks.PRESSURE_HATCH.asItem());
        tag(ItemTags.AXES, NTItems.AQUARINE_AXE);
        tag(ItemTags.PICKAXES, NTItems.AQUARINE_PICKAXE);
        tag(ItemTags.SWORDS, NTItems.AQUARINE_SWORD);
        tag(ItemTags.SHOVELS, NTItems.AQUARINE_SHOVEL);
        tag(ItemTags.HOES, NTItems.AQUARINE_HOE);
        tag(ItemTags.HEAD_ARMOR_ENCHANTABLE, NTItems.AQUARINE_HELMET);
        tag(ItemTags.BOW_ENCHANTABLE, NTItems.ATLANTEAN_RIFLE);
        tag(ItemTags.VANISHING_ENCHANTABLE, NTItems.ATLANTEAN_RIFLE, NTItems.NEPTUNES_TRIDENT);
        tag(ItemTags.TRIDENT_ENCHANTABLE, NTItems.NEPTUNES_TRIDENT);
        tag(ItemTags.SWORD_ENCHANTABLE, NTItems.NEPTUNES_TRIDENT);
        tag(ItemTags.SHARP_WEAPON_ENCHANTABLE, NTItems.NEPTUNES_TRIDENT);
        tag(ItemTags.CHEST_ARMOR_ENCHANTABLE, NTItems.AQUARINE_CHESTPLATE);
        tag(ItemTags.LEG_ARMOR_ENCHANTABLE, NTItems.AQUARINE_LEGGINGS);
        tag(ItemTags.FOOT_ARMOR_ENCHANTABLE, NTItems.AQUARINE_BOOTS);

        tag(Tags.Items.TOOLS_WRENCH, NTItems.AQUARINE_WRENCH);

        IntrinsicTagAppender<Item> modules = tag(NTTags.Items.SUBMARINE_MODULE);
        for (var module : NTItems.SUBMARINE_MODULES) {
            modules.add(module.get());
        }

        tag(NTTags.Items.REPAIRS_AQUARINE_TOOLS, NTItems.AQUARINE_STEEL_INGOT);
        tag(NTTags.Items.REPAIRS_AQUARINE_ARMOR, NTItems.AQUARINE_STEEL_INGOT);
        tag(NTTags.Items.REPAIRS_DIVING_SUIT, Items.COPPER_INGOT);
        tag(NTTags.Items.REPAIRS_PRISMARINE_ARMOR, Items.PRISMARINE);

        this.tag(NTTags.Items.CORALS,
                Items.TUBE_CORAL,
                Items.BRAIN_CORAL,
                Items.BUBBLE_CORAL,
                Items.FIRE_CORAL,
                Items.HORN_CORAL,
                Items.TUBE_CORAL_FAN,
                Items.BRAIN_CORAL_FAN,
                Items.BUBBLE_CORAL_FAN,
                Items.FIRE_CORAL_FAN,
                Items.HORN_CORAL_FAN);

        tag(NTTags.Items.BIOME_TANK_PLANTS,
                NTBlocks.GLOW_POLYP,
                NTBlocks.LUMINESCENT_ALGAE,
                NTBlocks.DEEP_KELP,
                NTBlocks.PRISMARINE_FROND,
                NTBlocks.ABYSSAL_CORAL,
                NTBlocks.VENT_TUBEWORM,
                Items.KELP,
                Items.SEAGRASS,
                Items.SEA_PICKLE,
                Items.TUBE_CORAL,
                Items.BRAIN_CORAL,
                Items.BUBBLE_CORAL,
                Items.FIRE_CORAL,
                Items.HORN_CORAL,
                Items.CACTUS,
                Items.SUGAR_CANE,
                Items.BAMBOO,
                Items.SWEET_BERRIES,
                Items.GLOW_BERRIES,
                Items.RED_MUSHROOM,
                Items.BROWN_MUSHROOM,
                Items.CRIMSON_FUNGUS,
                Items.WARPED_FUNGUS,
                Items.CHORUS_FRUIT,
                Items.COCOA_BEANS);

    }

    private void tag(TagKey<Item> itemTagKey, ItemLike... items) {
        IntrinsicTagAppender<Item> tag = tag(itemTagKey);
        for (ItemLike item : items) {
            tag.add(item.asItem());
        }
    }

    @SafeVarargs
    private void tag(TagKey<Item> itemTagKey, TagKey<Item>... items) {
        IntrinsicTagAppender<Item> tag = tag(itemTagKey);
        for (TagKey<Item> item : items) {
            tag.addTag(item);
        }
    }
}
