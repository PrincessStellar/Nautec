package com.breakinblocks.nautec.registries;

import com.breakinblocks.nautec.Nautec;
import net.minecraft.resources.Identifier;

public final class NTAliases {
    private static final String[] BIOME_TANK_PLANTS = {"glow_polyp", "luminescent_algae", "deep_kelp", "prismarine_frond", "abyssal_coral",
            "vent_tubeworm", "kelp", "seagrass", "sea_pickle", "tube_coral", "brain_coral", "bubble_coral", "fire_coral", "horn_coral", "cactus",
            "sugar_cane", "bamboo", "sweet_berries", "glow_berries", "red_mushroom", "brown_mushroom", "crimson_fungus", "warped_fungus",
            "chorus_fruit", "cocoa_beans"};

    private NTAliases() {
    }

    public static void register() {
        alias("long_distance_laser", "prismarine_laser_relay", true);
        for (String plant : BIOME_TANK_PLANTS) {
            alias(plant + "_biome_tank", "biome_tank", false);
        }
    }

    private static void alias(String from, String to, boolean blockEntity) {
        Identifier source = Nautec.rl(from);
        Identifier target = Nautec.rl(to);
        NTBlocks.BLOCKS.addAlias(source, target);
        NTItems.ITEMS.addAlias(source, target);
        if (blockEntity) {
            NTBlockEntityTypes.BLOCK_ENTITIES.addAlias(source, target);
        }
    }
}
