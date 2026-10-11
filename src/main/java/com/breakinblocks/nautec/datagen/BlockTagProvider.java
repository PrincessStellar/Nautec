package com.breakinblocks.nautec.datagen;

import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.registries.NTBlocks;
import com.breakinblocks.nautec.tags.NTTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.IntrinsicHolderTagsProvider.IntrinsicTagAppender;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredBlock;

import java.util.concurrent.CompletableFuture;

import static com.breakinblocks.nautec.registries.NTBlocks.*;

public class BlockTagProvider extends BlockTagsProvider {

    public BlockTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, Nautec.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(BlockTags.MINEABLE_WITH_AXE,
                NTBlocks.CRATE,
                RUSTY_CRATE);
        tag(BlockTags.MINEABLE_WITH_PICKAXE,
                CRATE,
                RUSTY_CRATE,
                DARK_PRISMARINE_PILLAR,
                CHISELED_DARK_PRISMARINE,
                POLISHED_PRISMARINE,
                AQUARINE_STEEL_BLOCK,
                AQUARINE_COPPER_BLOCK,
                CURRENT_CONDUIT,
                CONDUIT_TAP,
                RESONANT_VAULT,
                RESONANT_CISTERN,
                AQUATIC_CATALYST,
                SUBMARINE_DOCK,
                PRESSURE_FORGE,
                GATEWAY,
                GATEWAY_RING,
                RESONANCE_CHAMBER,
                FOCUSING_LENS,
                PRISMARINE_RELAY,
                MIXER,
                LASER_CRAFTING_MATRIX,
                CHARGER,
                CONFINED_SPAWNER,
                CRYSTAL_CRADLE,
                LASER_JUNCTION,
                DRAIN,
                DRAIN_WALL,
                DRAIN_PART,
                AUGMENTATION_STATION,
                AUGMENTATION_STATION_EXTENSION,
                AUGMENTATION_STATION_PART,
                BACTERIAL_ANALYZER,
                BACTERIAL_ANALYZER_TOP,
                BACTERIAL_CONTAINMENT_SHIELD,
                BACTERIAL_FUEL_CELL,
                BIO_REACTOR,
                BIO_REACTOR_PART,
                INDUSTRIAL_BIO_REACTOR,
                INDUSTRIAL_BIO_REACTOR_PART,
                MUTATOR,
                GRAFTING_STATION,
                DISTRIBUTOR,
                BUBBLE_ANCHOR,
                COLONY_REPLICATOR,
                HYDROTHERMAL_VENT,
                PRESSURE_HATCH,
                OXYGEN_DIFFUSER,
                ADVANCED_BACTERIAL_ANALYZER,
                INCUBATOR,
                FISHING_STATION,
                BUDDING_PRISMARINE,
                CAST_IRON_BLOCK,
                ANCHOR,
                OIL_BARREL,
                CREATIVE_POWER_SOURCE,
                CREATIVE_ENERGY_SOURCE,
                ENERGY_CONVERTER,
                TIDAL_ROTOR,
                THERMAL_VENT_TAP,
                COMBUSTION_DYNAMO,
                RESONANCE_NODE,
                CONDUIT_BEACON,
                AQUARINE_DISH_STORAGE,
                DEEP_STEEL_DISH_STORAGE,
                ATLANTIC_GOLD_DISH_STORAGE,
                PRISMATIC_EMITTER,
                UPLINK_ARRAY,
                SATELLITE_ARRAY_TOP,
                FUSION_CASING,
                AQUAMARINE_STRUCTURAL_GLASS,
                CONTAINMENT_COIL,
                FUSION_CONTROLLER,
                LASER_INJECTOR,
                FUSION_COLLECTOR,
                FUSION_PORT);
        tag(BlockTags.NEEDS_IRON_TOOL,
                THERMAL_VENT_TAP,
                COMBUSTION_DYNAMO,
                RESONANCE_NODE,
                PRISMATIC_EMITTER,
                UPLINK_ARRAY,
                SATELLITE_ARRAY_TOP);
        tag(BlockTags.MINEABLE_WITH_PICKAXE).add(NTBlocks.BIOME_TANK.get());
        tag(NTTags.Blocks.VENT_HEAT_SOURCES).add(Blocks.MAGMA_BLOCK, Blocks.LAVA, HYDROTHERMAL_VENT.get());
        tag(BlockTags.PREVENT_MOB_SPAWNING_INSIDE, HELD_WATER);
        tag(BlockTags.DOORS, PRESSURE_HATCH);
        tag(Tags.Blocks.BUDDING_BLOCKS, BUDDING_PRISMARINE);
        tag(Tags.Blocks.BUDS, SMALL_PRISMARINE_BUD, MEDIUM_PRISMARINE_BUD, LARGE_PRISMARINE_BUD);
        tag(Tags.Blocks.CLUSTERS, PRISMARINE_CLUSTER);
        tag(BlockTags.NEEDS_DIAMOND_TOOL,
                FUSION_CASING,
                AQUAMARINE_STRUCTURAL_GLASS,
                CONTAINMENT_COIL,
                FUSION_CONTROLLER,
                LASER_INJECTOR,
                FUSION_COLLECTOR,
                FUSION_PORT);
        tag(NTTags.Blocks.GATEWAY_RING_CLEARABLE)
                .add(Blocks.KELP, Blocks.KELP_PLANT, Blocks.SEAGRASS, Blocks.TALL_SEAGRASS, Blocks.SEA_PICKLE)
                .addTag(BlockTags.CORALS)
                .addTag(BlockTags.WALL_CORALS)
                .add(DEEP_KELP.get(), DEEP_KELP_PLANT.get(), LUMINESCENT_ALGAE.get(), PRISMARINE_FROND.get(),
                        VENT_TUBEWORM.get(), ABYSSAL_CORAL.get(), GLOW_POLYP.get());
    }

    private void tag(TagKey<Block> blockTagKey, Block... blocks) {
        tag(blockTagKey).add(blocks);
    }

    @SafeVarargs
    private void tag(TagKey<Block> blockTagKey, DeferredBlock<? extends Block>... blocks) {
        IntrinsicTagAppender<Block> tag = tag(blockTagKey);
        for (DeferredBlock<? extends Block> block : blocks) {
            tag.add(block.get());
        }
    }
}
