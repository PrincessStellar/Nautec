package com.breakinblocks.nautec.tags;

import com.breakinblocks.nautec.Nautec;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;

public final class NTTags {
    public static final class DamageTypes {
        public static final TagKey<DamageType> ATLANTEAN_RIFLE = TagKey.create(Registries.DAMAGE_TYPE, Nautec.rl("atlantean_rifle"));
    }

    public static final class Fluids {
        public static final TagKey<Fluid> OXYGEN = TagKey.create(Registries.FLUID, Identifier.fromNamespaceAndPath("c", "oxygen"));
        public static final TagKey<Fluid> OIL = TagKey.create(Registries.FLUID, Identifier.fromNamespaceAndPath("c", "oil"));
    }

    public static final class Blocks {
        public static final TagKey<Block> GATEWAY_RING_CLEARABLE = ntTag("gateway_ring_clearable");
        public static final TagKey<Block> VENT_HEAT_SOURCES = ntTag("vent_heat_sources");

        private static TagKey<Block> ntTag(String name) {
            return TagKey.create(Registries.BLOCK, Nautec.rl(name));
        }
    }

    public static final class Items {
        public static final TagKey<Item> AQUATIC_CATALYST = ntTag("aquatic_catalyst");
        public static final TagKey<Item> AQUARINE_STEEL = ntTag("aquarine_steel");
        public static final TagKey<Item> CORALS = ntTag("corals");
        public static final TagKey<Item> REPAIRS_AQUARINE_TOOLS = ntTag("repairs_aquarine_tools");
        public static final TagKey<Item> REPAIRS_AQUARINE_ARMOR = ntTag("repairs_aquarine_armor");
        public static final TagKey<Item> REPAIRS_DIVING_SUIT = ntTag("repairs_diving_suit");
        public static final TagKey<Item> REPAIRS_PRISMARINE_ARMOR = ntTag("repairs_prismarine_armor");
        public static final TagKey<Item> SUBMARINE_MODULE = ntTag("submarine_module");
        public static final TagKey<Item> BIOME_TANK_PLANTS = ntTag("biome_tank_plants");
        public static final TagKey<Item> DUSTS_SALT = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("c", "dusts/salt"));
        public static final TagKey<Item> INGOTS_AQUARINE_COPPER = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("c", "ingots/aquarine_copper"));
        public static final TagKey<Item> NUGGETS_AQUARINE_COPPER = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("c", "nuggets/aquarine_copper"));
        public static final TagKey<Item> STORAGE_BLOCKS_AQUARINE_COPPER = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("c", "storage_blocks/aquarine_copper"));

        private static TagKey<Item> ntTag(String name) {
            return TagKey.create(Registries.ITEM, Nautec.rl(name));
        }
    }
}
