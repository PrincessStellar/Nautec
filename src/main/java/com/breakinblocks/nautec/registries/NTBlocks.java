package com.breakinblocks.nautec.registries;

import com.breakinblocks.nautec.content.conduits.ConduitTapBlock;
import com.breakinblocks.nautec.content.conduits.CurrentConduitBlock;
import com.breakinblocks.nautec.content.resonantstorage.ResonantCisternBlock;
import com.breakinblocks.nautec.content.resonantstorage.ResonantStorageItem;
import com.breakinblocks.nautec.content.resonantstorage.ResonantVaultBlock;
import com.breakinblocks.nautec.content.resonantstorage.ResonantVaultItem;
import com.breakinblocks.nautec.content.biometank.BiomeTankBlock;
import com.breakinblocks.nautec.content.dishstorage.DishStorageBlock;
import com.breakinblocks.nautec.content.conduit.ConduitBeaconBlock;
import com.breakinblocks.nautec.content.resonance.ResonanceNodeBlock;
import com.breakinblocks.nautec.content.blocks.OxygenDiffuserBlock;
import com.breakinblocks.nautec.content.blocks.PressureHatchBlock;
import com.breakinblocks.nautec.content.blocks.HydrothermalVentBlock;
import com.breakinblocks.nautec.content.blocks.ColonyReplicatorBlock;
import com.breakinblocks.nautec.content.bubble.AirPocketBlock;
import com.breakinblocks.nautec.content.bubble.HeldWaterBlock;
import com.breakinblocks.nautec.content.bubble.BubbleAnchorBlock;
import com.breakinblocks.nautec.content.distributor.DistributorBlock;
import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.content.blocks.AnchorBlock;
import com.breakinblocks.nautec.content.blocks.AquaticCatalystBlock;
import com.breakinblocks.nautec.content.blocks.AdvancedBacterialAnalyzerBlock;
import com.breakinblocks.nautec.content.blocks.BacterialAnalyzerBlock;
import com.breakinblocks.nautec.content.blocks.GraftingStationBlock;
import com.breakinblocks.nautec.content.blocks.ConfinedSpawnerBlock;
import com.breakinblocks.nautec.content.blocks.CrystalCradleBlock;
import com.breakinblocks.nautec.content.blocks.BacterialAnalyzerTopBlock;
import com.breakinblocks.nautec.content.blocks.BacterialFuelCellBlock;
import com.breakinblocks.nautec.content.blocks.BuddingPrismarineBlock;
import com.breakinblocks.nautec.content.blocks.ChargerBlock;
import com.breakinblocks.nautec.content.blocks.CrateBlock;
import com.breakinblocks.nautec.content.blocks.CreativeEnergySourceBlock;
import com.breakinblocks.nautec.content.blocks.CreativePowerSourceBlock;
import com.breakinblocks.nautec.content.blocks.DecorativePrismarineCrystalBlock;
import com.breakinblocks.nautec.content.blocks.DecorativePrismarineCrystalPartBlock;
import com.breakinblocks.nautec.content.blocks.EnergyConverterBlock;
import com.breakinblocks.nautec.content.blocks.FishingStationBlock;
import com.breakinblocks.nautec.content.blocks.FocusingLensBlock;
import com.breakinblocks.nautec.content.blocks.GatewayBlock;
import com.breakinblocks.nautec.content.blocks.GatewayRingPartBlock;
import com.breakinblocks.nautec.content.blocks.IncubatorBlock;
import com.breakinblocks.nautec.content.blocks.LaserJunctionBlock;
import com.breakinblocks.nautec.content.blocks.LuckyFishingZoneBlock;
import com.breakinblocks.nautec.content.blocks.LaserCraftingMatrixBlock;
import com.breakinblocks.nautec.content.blocks.MixerBlock;
import com.breakinblocks.nautec.content.items.blocks.LaserCraftingMatrixItem;
import com.breakinblocks.nautec.content.blocks.MutatorBlock;
import com.breakinblocks.nautec.content.blocks.OilBarrelBlock;
import com.breakinblocks.nautec.content.blocks.PressureForgeBlock;
import com.breakinblocks.nautec.content.blocks.LaserPurificationSmelterBlock;
import com.breakinblocks.nautec.content.blocks.PrismarineLaserRelayBlock;
import com.breakinblocks.nautec.content.blocks.PrismarineSandBlock;
import com.breakinblocks.nautec.content.blocks.ResonanceChamberBlock;
import com.breakinblocks.nautec.content.blocks.SubmarineDockBlock;
import com.breakinblocks.nautec.content.blocks.flora.DeepKelpBlock;
import com.breakinblocks.nautec.content.blocks.fusion.FusionCollectorBlock;
import com.breakinblocks.nautec.content.blocks.generators.CombustionDynamoBlock;
import com.breakinblocks.nautec.content.blocks.generators.ThermalVentTapBlock;
import com.breakinblocks.nautec.content.resonance.PrismaticEmitterBlock;
import com.breakinblocks.nautec.content.resonance.SatelliteArrayBlock;
import com.breakinblocks.nautec.content.resonance.SatelliteArrayTopBlock;
import com.breakinblocks.nautec.content.blocks.generators.TidalRotorBlock;
import com.breakinblocks.nautec.content.blocks.fusion.FusionControllerBlock;
import com.breakinblocks.nautec.content.blocks.fusion.FusionPortBlock;
import com.breakinblocks.nautec.content.blocks.fusion.LaserInjectorBlock;
import com.breakinblocks.nautec.content.blocks.flora.DeepKelpPlantBlock;
import com.breakinblocks.nautec.content.blocks.flora.UnderwaterPlantBlock;
import com.breakinblocks.nautec.content.blocks.multiblock.controller.AugmentationStationBlock;
import com.breakinblocks.nautec.content.blocks.multiblock.controller.BioReactorBlock;
import com.breakinblocks.nautec.content.blocks.multiblock.controller.IndustrialBioReactorBlock;
import com.breakinblocks.nautec.content.blocks.multiblock.controller.DrainBlock;
import com.breakinblocks.nautec.content.blocks.multiblock.part.AugmentationStationExtensionBlock;
import com.breakinblocks.nautec.content.blocks.multiblock.part.AugmentationStationPartBlock;
import com.breakinblocks.nautec.content.blocks.multiblock.part.BioReactorPartBlock;
import com.breakinblocks.nautec.content.blocks.multiblock.part.IndustrialBioReactorPartBlock;
import com.breakinblocks.nautec.content.blocks.multiblock.part.DrainPartBlock;
import com.breakinblocks.nautec.content.blocks.multiblock.semi.PrismarineCrystalBlock;
import com.breakinblocks.nautec.content.blocks.multiblock.semi.PrismarineCrystalPartBlock;
import com.breakinblocks.nautec.content.items.blocks.CrystalCradleItem;
import com.breakinblocks.nautec.content.items.blocks.PrismarineCrystalItem;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.AmethystClusterBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.GlowLichenBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.TransparentBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.BiFunction;
import java.util.function.Function;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

public final class NTBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Nautec.MODID);

    public static final DeferredBlock<PrismarineSandBlock> PRISMARINE_SAND = registerBlockAndItem("prismarine_sand", props -> new PrismarineSandBlock(UniformInt.of(4, 6), props),
            BlockBehaviour.Properties.ofFullCopy(Blocks.SAND));

    public static final DeferredBlock<CrateBlock> CRATE = registerBlockAndItem("crate", CrateBlock::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.BARREL).noOcclusion());
    public static final DeferredBlock<CrateBlock> RUSTY_CRATE = registerBlockAndItem("rusty_crate", CrateBlock::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.BARREL).noOcclusion());
    public static final DeferredBlock<OilBarrelBlock> OIL_BARREL = registerBlockAndItem("oil_barrel", OilBarrelBlock::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK));
    public static final DeferredBlock<Block> BROWN_POLYMER_BLOCK = registerBlockAndItem("brown_polymer_block", Block::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.BROWN_WOOL));
    public static final DeferredBlock<RotatedPillarBlock> DARK_PRISMARINE_PILLAR = registerBlockAndItem("dark_prismarine_pillar", RotatedPillarBlock::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.DARK_PRISMARINE));
    public static final DeferredBlock<Block> CHISELED_DARK_PRISMARINE = registerBlockAndItem("chiseled_dark_prismarine", Block::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.DARK_PRISMARINE));
    public static final DeferredBlock<Block> POLISHED_PRISMARINE = registerBlockAndItem("polished_prismarine", Block::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.DARK_PRISMARINE));
    public static final DeferredBlock<Block> AQUARINE_STEEL_BLOCK = registerBlockAndItem("aquarine_steel_block", Block::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK));
    public static final DeferredBlock<Block> CAST_IRON_BLOCK = registerBlockAndItem("cast_iron_block", Block::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK));
    public static final DeferredBlock<AquaticCatalystBlock> AQUATIC_CATALYST = registerBlockAndItem("aquatic_catalyst", AquaticCatalystBlock::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.DARK_PRISMARINE));
    public static final DeferredBlock<PrismarineLaserRelayBlock> PRISMARINE_RELAY = registerBlockAndItem("prismarine_laser_relay", PrismarineLaserRelayBlock::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.PRISMARINE_BRICKS));
    public static final DeferredBlock<MixerBlock> MIXER = registerBlockAndItem("mixer", MixerBlock::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.PRISMARINE_BRICKS).noOcclusion());
    public static final DeferredBlock<LaserCraftingMatrixBlock> LASER_CRAFTING_MATRIX = registerBlockAndItem("laser_crafting_matrix", LaserCraftingMatrixBlock::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).noOcclusion(), LaserCraftingMatrixItem::new);
    public static final DeferredBlock<LaserJunctionBlock> LASER_JUNCTION = registerBlockAndItem("laser_junction", props -> new LaserJunctionBlock(props, 8),
            BlockBehaviour.Properties.ofFullCopy(Blocks.DARK_PRISMARINE).noOcclusion(), true, false);

    public static final DeferredBlock<PrismarineCrystalBlock> PRISMARINE_CRYSTAL = registerBlockAndItem("prismarine_crystal", PrismarineCrystalBlock::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.SEA_LANTERN).strength(-1, 1200).noOcclusion(), PrismarineCrystalItem::new);
    public static final DeferredBlock<PrismarineCrystalPartBlock> PRISMARINE_CRYSTAL_PART = BLOCKS.registerBlock("prismarine_crystal_part", PrismarineCrystalPartBlock::new,
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.SEA_LANTERN).strength(-1, 1200).noOcclusion());
    public static final DeferredBlock<DecorativePrismarineCrystalBlock> DECORATIVE_PRISMARINE_CRYSTAL = registerBlockAndItem("decorative_prismarine_crystal", DecorativePrismarineCrystalBlock::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.SEA_LANTERN).strength(2.0f, 6.0f).noOcclusion());
    public static final DeferredBlock<DecorativePrismarineCrystalPartBlock> DECORATIVE_PRISMARINE_CRYSTAL_PART = BLOCKS.registerBlock("decorative_prismarine_crystal_part", DecorativePrismarineCrystalPartBlock::new,
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.SEA_LANTERN).strength(2.0f, 6.0f).noOcclusion());
    public static final DeferredBlock<AnchorBlock> ANCHOR = registerBlockAndItem("anchor", AnchorBlock::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).sound(SoundType.ANVIL).noOcclusion());
    public static final DeferredBlock<ChargerBlock> CHARGER = registerBlockAndItem("charger", ChargerBlock::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).noOcclusion());
    public static final DeferredBlock<ConfinedSpawnerBlock> CONFINED_SPAWNER = registerBlockAndItem("confined_spawner", ConfinedSpawnerBlock::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.SPAWNER), false, true);
    public static final DeferredBlock<CrystalCradleBlock> CRYSTAL_CRADLE = registerBlockAndItem("crystal_cradle", CrystalCradleBlock::new,
            BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_CYAN).strength(4.0f, 1200.0f).sound(SoundType.METAL).noOcclusion(), CrystalCradleItem::new);
    public static final DeferredBlock<FishingStationBlock> FISHING_STATION = registerBlockAndItem("fishing_station", FishingStationBlock::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).noOcclusion());

    public static final DeferredBlock<SubmarineDockBlock> SUBMARINE_DOCK = registerBlockAndItem("submarine_dock", SubmarineDockBlock::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).strength(3.0f).noOcclusion());

    public static final DeferredBlock<PressureForgeBlock> PRESSURE_FORGE = registerBlockAndItem("pressure_forge", PressureForgeBlock::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).strength(4.0f).noOcclusion());

    public static final DeferredBlock<LaserPurificationSmelterBlock> LASER_PURIFICATION_SMELTER = registerBlockAndItem("laser_purification_smelter",
            LaserPurificationSmelterBlock::new, BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).strength(4.0f));

    public static final DeferredBlock<GatewayBlock> GATEWAY = registerBlockAndItem("gateway", GatewayBlock::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.DARK_PRISMARINE).strength(4.0f).noOcclusion());
    public static final DeferredBlock<Block> GATEWAY_RING = registerBlockAndItem("gateway_ring", Block::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.DARK_PRISMARINE).strength(4.0f));
    public static final DeferredBlock<GatewayRingPartBlock> GATEWAY_RING_PART = BLOCKS.registerBlock("gateway_ring_part", GatewayRingPartBlock::new,
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.DARK_PRISMARINE).strength(50.0f, 1200.0f).requiresCorrectToolForDrops().noOcclusion());

    public static final DeferredBlock<ResonanceChamberBlock> RESONANCE_CHAMBER = registerBlockAndItem("resonance_chamber", ResonanceChamberBlock::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.SEA_LANTERN).strength(3.0f).noOcclusion());

    public static final DeferredBlock<FocusingLensBlock> FOCUSING_LENS = registerBlockAndItem("focusing_lens", FocusingLensBlock::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.SEA_LANTERN).strength(1.5f).noOcclusion());

    public static final DeferredBlock<MutatorBlock> MUTATOR = bacteriaBlock(registerBlockAndItem("mutator", MutatorBlock::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).noOcclusion()));
    public static final DeferredBlock<ColonyReplicatorBlock> COLONY_REPLICATOR = bacteriaBlock(registerBlockAndItem("colony_replicator",
            ColonyReplicatorBlock::new, BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK)));
    public static final DeferredBlock<OxygenDiffuserBlock> OXYGEN_DIFFUSER = registerBlockAndItem("oxygen_diffuser", OxygenDiffuserBlock::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.COPPER_BLOCK).noOcclusion().lightLevel(state -> state.getValue(OxygenDiffuserBlock.ACTIVE) ? 8 : 0));
    public static final DeferredBlock<PressureHatchBlock> PRESSURE_HATCH = registerBlockAndItem("pressure_hatch", PressureHatchBlock::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_DOOR).strength(5.0F, 1200.0F).noOcclusion());
    public static final DeferredBlock<BubbleAnchorBlock> BUBBLE_ANCHOR = registerBlockAndItem("bubble_anchor", BubbleAnchorBlock::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.PRISMARINE).lightLevel(state -> state.getValue(BubbleAnchorBlock.ACTIVE) ? 12 : 4));
    public static final DeferredBlock<HeldWaterBlock> HELD_WATER = BLOCKS.registerBlock("held_water", HeldWaterBlock::new,
            () -> BlockBehaviour.Properties.of().strength(-1.0F, 3_600_000.0F));
    public static final DeferredBlock<AirPocketBlock> AIR_POCKET = BLOCKS.registerBlock("air_pocket", AirPocketBlock::new,
            () -> BlockBehaviour.Properties.of().strength(-1.0F, 3_600_000.0F));
    public static final DeferredBlock<DistributorBlock> DISTRIBUTOR = registerBlockAndItem("nautechnical_distributor", DistributorBlock::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK));
    public static final DeferredBlock<Block> AQUARINE_COPPER_BLOCK = registerBlockAndItem("aquarine_copper_block", Block::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.COPPER_BLOCK));
    public static final DeferredBlock<CurrentConduitBlock> CURRENT_CONDUIT = registerBlockAndItem("current_conduit", CurrentConduitBlock::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.COPPER_BLOCK).strength(1.0F, 6.0F).noOcclusion(), true, false);
    public static final DeferredBlock<ConduitTapBlock> CONDUIT_TAP = BLOCKS.registerBlock("conduit_tap", ConduitTapBlock::new,
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.COPPER_BLOCK).strength(1.5F, 6.0F).noOcclusion());
    public static final DeferredBlock<ResonantVaultBlock> RESONANT_VAULT = registerBlockAndItem("resonant_vault", ResonantVaultBlock::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).strength(3.0F, 1200.0F).noOcclusion(), ResonantVaultItem::new);
    public static final DeferredBlock<ResonantCisternBlock> RESONANT_CISTERN = registerBlockAndItem("resonant_cistern", ResonantCisternBlock::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).strength(3.0F, 1200.0F).noOcclusion(), ResonantStorageItem::new);
    public static final DeferredBlock<AdvancedBacterialAnalyzerBlock> ADVANCED_BACTERIAL_ANALYZER = bacteriaBlock(registerBlockAndItem("advanced_bacterial_analyzer",
            AdvancedBacterialAnalyzerBlock::new, BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK)));
    public static final DeferredBlock<GraftingStationBlock> GRAFTING_STATION = bacteriaBlock(registerBlockAndItem("grafting_station", GraftingStationBlock::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK)));
    public static final DeferredBlock<IncubatorBlock> INCUBATOR = bacteriaBlock(registerBlockAndItem("incubator", IncubatorBlock::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).noOcclusion()));
    public static final DeferredBlock<BioReactorBlock> BIO_REACTOR = bacteriaBlock(registerBlockAndItem("bio_reactor", BioReactorBlock::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK)));
    public static final DeferredBlock<BioReactorPartBlock> BIO_REACTOR_PART = BLOCKS.registerBlock("bio_reactor_part", BioReactorPartBlock::new,
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK));
    public static final DeferredBlock<IndustrialBioReactorBlock> INDUSTRIAL_BIO_REACTOR = bacteriaBlock(registerBlockAndItem("industrial_bio_reactor", IndustrialBioReactorBlock::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).noOcclusion()));
    public static final DeferredBlock<IndustrialBioReactorPartBlock> INDUSTRIAL_BIO_REACTOR_PART = BLOCKS.registerBlock("industrial_bio_reactor_part", IndustrialBioReactorPartBlock::new,
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).noOcclusion());
    public static final DeferredBlock<BacterialFuelCellBlock> BACTERIAL_FUEL_CELL = bacteriaBlock(registerBlockAndItem("bacterial_fuel_cell", BacterialFuelCellBlock::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).noOcclusion()));
    public static final DeferredBlock<BacterialAnalyzerBlock> BACTERIAL_ANALYZER = bacteriaBlock(registerBlockAndItem("bacterial_analyzer", BacterialAnalyzerBlock::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).noOcclusion(), true, false));
    public static final DeferredBlock<BacterialAnalyzerTopBlock> BACTERIAL_ANALYZER_TOP = BLOCKS.registerBlock("bacterial_analyzer_top", BacterialAnalyzerTopBlock::new,
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).noOcclusion());

    public static final DeferredBlock<DrainBlock> DRAIN = registerBlockAndItem("deep_sea_drain", DrainBlock::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).noOcclusion());
    public static final DeferredBlock<Block> DRAIN_WALL = registerBlockAndItem("deep_sea_drain_wall", Block::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK));
    public static final DeferredBlock<DrainPartBlock> DRAIN_PART = BLOCKS.registerBlock("deep_sea_drain_part", DrainPartBlock::new,
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).noOcclusion());

    public static final DeferredBlock<AugmentationStationBlock> AUGMENTATION_STATION = registerBlockAndItem("augmentation_station",
            AugmentationStationBlock::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.DARK_PRISMARINE).noOcclusion());
    public static final DeferredBlock<AugmentationStationPartBlock> AUGMENTATION_STATION_PART = BLOCKS.registerBlock("augmentation_station_part",
            AugmentationStationPartBlock::new,
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.DARK_PRISMARINE).noOcclusion());
    public static final DeferredBlock<AugmentationStationExtensionBlock> AUGMENTATION_STATION_EXTENSION = registerBlockAndItem("augmentation_station_extension",
            AugmentationStationExtensionBlock::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.DARK_PRISMARINE).noOcclusion());

    public static final DeferredBlock<Block> BACTERIAL_CONTAINMENT_SHIELD = bacteriaBlock(registerBlockAndItem("bacterial_containment_shield", Block::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.PRISMARINE)));

    public static final DeferredBlock<CreativePowerSourceBlock> CREATIVE_POWER_SOURCE = registerBlockAndItem("creative_power_source", CreativePowerSourceBlock::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK), false, true);
    public static final DeferredBlock<CreativeEnergySourceBlock> CREATIVE_ENERGY_SOURCE = registerBlockAndItem("creative_energy_source", CreativeEnergySourceBlock::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK), true, true);
    public static final DeferredBlock<EnergyConverterBlock> ENERGY_CONVERTER = registerBlockAndItem("energy_converter", EnergyConverterBlock::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK), true, true);

    public static final DeferredBlock<ResonanceNodeBlock> RESONANCE_NODE = registerBlockAndItem("resonance_node", ResonanceNodeBlock::new,
            BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_CYAN).strength(3.0f, 12.0f).sound(SoundType.METAL).noOcclusion()
                    .requiresCorrectToolForDrops().lightLevel(state -> 7), true, false);

    public static final DeferredBlock<ConduitBeaconBlock> CONDUIT_BEACON = registerBlockAndItem("conduit_beacon", ConduitBeaconBlock::new,
            BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_CYAN).strength(3.0f, 12.0f).sound(SoundType.METAL).noOcclusion()
                    .requiresCorrectToolForDrops().lightLevel(state -> state.getValue(ConduitBeaconBlock.ACTIVE) ? 15 : 6));

    public static final DeferredBlock<DishStorageBlock> AQUARINE_DISH_STORAGE = bacteriaBlock(registerBlockAndItem("aquarine_dish_storage",
            properties -> new DishStorageBlock(48, properties),
            BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).requiresCorrectToolForDrops()));
    public static final DeferredBlock<DishStorageBlock> DEEP_STEEL_DISH_STORAGE = bacteriaBlock(registerBlockAndItem("deep_steel_dish_storage",
            properties -> new DishStorageBlock(96, properties),
            BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).requiresCorrectToolForDrops()));
    public static final DeferredBlock<DishStorageBlock> ATLANTIC_GOLD_DISH_STORAGE = bacteriaBlock(registerBlockAndItem("atlantic_gold_dish_storage",
            properties -> new DishStorageBlock(192, properties),
            BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).requiresCorrectToolForDrops()));

    public static final DeferredBlock<BiomeTankBlock> BIOME_TANK = registerBlockAndItem("biome_tank", BiomeTankBlock::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS).strength(2.0f, 6.0f).sound(SoundType.GLASS).noOcclusion());

    public static final DeferredBlock<SatelliteArrayBlock> UPLINK_ARRAY = registerBlockAndItem("uplink_array",
            SatelliteArrayBlock::new,
            BlockBehaviour.Properties.of().mapColor(MapColor.GOLD).strength(5.0f, 12.0f).sound(SoundType.METAL).noOcclusion()
                    .requiresCorrectToolForDrops().pushReaction(PushReaction.BLOCK).lightLevel(state -> 6));
    public static final DeferredBlock<SatelliteArrayTopBlock> SATELLITE_ARRAY_TOP = BLOCKS.registerBlock("satellite_array_top", SatelliteArrayTopBlock::new,
            () -> BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(5.0f, 12.0f).sound(SoundType.METAL).noOcclusion()
                    .requiresCorrectToolForDrops().noLootTable());

    public static final DeferredBlock<PrismaticEmitterBlock> PRISMATIC_EMITTER = registerBlockAndItem("prismatic_emitter", PrismaticEmitterBlock::new,
            BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_CYAN).strength(4.0f, 12.0f).sound(SoundType.METAL).noOcclusion()
                    .requiresCorrectToolForDrops().lightLevel(state -> 5));

    public static final DeferredBlock<TidalRotorBlock> TIDAL_ROTOR = registerBlockAndItem("tidal_rotor", TidalRotorBlock::new,
            BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(3.5f, 6.0f).sound(SoundType.METAL).requiresCorrectToolForDrops());
    public static final DeferredBlock<ThermalVentTapBlock> THERMAL_VENT_TAP = registerBlockAndItem("thermal_vent_tap", ThermalVentTapBlock::new,
            BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_ORANGE).strength(4.5f, 12.0f).sound(SoundType.METAL).requiresCorrectToolForDrops()
                    .noOcclusion().lightLevel(state -> state.getValue(ThermalVentTapBlock.LIT) ? 9 : 0));
    public static final DeferredBlock<CombustionDynamoBlock> COMBUSTION_DYNAMO = registerBlockAndItem("combustion_dynamo", CombustionDynamoBlock::new,
            BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(4.0f, 8.0f).sound(SoundType.METAL).requiresCorrectToolForDrops()
                    .noOcclusion().lightLevel(state -> state.getValue(CombustionDynamoBlock.LIT) ? 8 : 0));

    public static final DeferredBlock<Block> FUSION_CASING = registerBlockAndItem("fusion_casing", Block::new,
            fusionProperties());
    public static final DeferredBlock<TransparentBlock> AQUAMARINE_STRUCTURAL_GLASS = registerBlockAndItem("aquamarine_structural_glass", TransparentBlock::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS).mapColor(MapColor.COLOR_CYAN).strength(4.0f, 1200.0f).requiresCorrectToolForDrops());
    public static final DeferredBlock<Block> CONTAINMENT_COIL = registerBlockAndItem("containment_coil", Block::new,
            fusionProperties().sound(SoundType.COPPER).lightLevel(state -> 6));
    public static final DeferredBlock<FusionControllerBlock> FUSION_CONTROLLER = registerBlockAndItem("fusion_controller", FusionControllerBlock::new,
            fusionProperties().lightLevel(state -> state.getValue(FusionControllerBlock.ACTIVE) ? 13 : 3));
    public static final DeferredBlock<LaserInjectorBlock> LASER_INJECTOR = registerBlockAndItem("laser_injector", LaserInjectorBlock::new,
            fusionProperties());
    public static final DeferredBlock<FusionCollectorBlock> FUSION_COLLECTOR = registerBlockAndItem("fusion_collector", FusionCollectorBlock::new,
            fusionProperties());
    public static final DeferredBlock<FusionPortBlock> FUSION_PORT = registerBlockAndItem("fusion_port", FusionPortBlock::new,
            fusionProperties());

    public static final DeferredBlock<BuddingPrismarineBlock> BUDDING_PRISMARINE = registerBlockAndItem("budding_prismarine", BuddingPrismarineBlock::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.PRISMARINE).randomTicks().strength(1.5f).requiresCorrectToolForDrops());
    public static final DeferredBlock<AmethystClusterBlock> SMALL_PRISMARINE_BUD = registerBlockAndItem("small_prismarine_bud",
            props -> new AmethystClusterBlock(3.0f, 4.0f, props), prismarineBudProperties(3));
    public static final DeferredBlock<AmethystClusterBlock> MEDIUM_PRISMARINE_BUD = registerBlockAndItem("medium_prismarine_bud",
            props -> new AmethystClusterBlock(4.0f, 3.0f, props), prismarineBudProperties(5));
    public static final DeferredBlock<AmethystClusterBlock> LARGE_PRISMARINE_BUD = registerBlockAndItem("large_prismarine_bud",
            props -> new AmethystClusterBlock(5.0f, 3.0f, props), prismarineBudProperties(7));
    public static final DeferredBlock<AmethystClusterBlock> PRISMARINE_CLUSTER = registerBlockAndItem("prismarine_cluster",
            props -> new AmethystClusterBlock(7.0f, 3.0f, props), prismarineBudProperties(9));

    public static final DeferredBlock<DeepKelpBlock> DEEP_KELP = registerBlockAndItem("deep_kelp", DeepKelpBlock::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.KELP));
    public static final DeferredBlock<DeepKelpPlantBlock> DEEP_KELP_PLANT = BLOCKS.registerBlock("deep_kelp_plant", DeepKelpPlantBlock::new,
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.KELP_PLANT));
    public static final DeferredBlock<UnderwaterPlantBlock> LUMINESCENT_ALGAE = registerBlockAndItem("luminescent_algae", UnderwaterPlantBlock::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.SEAGRASS).lightLevel(state -> 9));
    public static final DeferredBlock<UnderwaterPlantBlock> PRISMARINE_FROND = registerBlockAndItem("prismarine_frond", UnderwaterPlantBlock::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.SEAGRASS));
    public static final DeferredBlock<UnderwaterPlantBlock> VENT_TUBEWORM = registerBlockAndItem("vent_tubeworm", UnderwaterPlantBlock::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.SEAGRASS).lightLevel(state -> 3));
    public static final DeferredBlock<UnderwaterPlantBlock> ABYSSAL_CORAL = registerBlockAndItem("abyssal_coral", UnderwaterPlantBlock::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.SEAGRASS));
    public static final DeferredBlock<HydrothermalVentBlock> HYDROTHERMAL_VENT = registerBlockAndItem("hydrothermal_vent", HydrothermalVentBlock::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.BASALT).lightLevel(state -> 6).isValidSpawn((state, level, pos, type) -> false)
                    .noOcclusion());
    public static final DeferredBlock<GlowLichenBlock> GLOW_POLYP = registerBlockAndItem("glow_polyp", GlowLichenBlock::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.GLOW_LICHEN).lightLevel(GlowLichenBlock.emission(7)));

    public static final DeferredBlock<LuckyFishingZoneBlock> LUCKY_FISHING_ZONE = BLOCKS.registerBlock("lucky_fishing_zone",
            LuckyFishingZoneBlock::new, () -> BlockBehaviour.Properties.of().strength(-1.0f, 3600000.0f).noOcclusion());

    private static BlockBehaviour.Properties prismarineBudProperties(int light) {
        return BlockBehaviour.Properties.ofFullCopy(Blocks.SMALL_AMETHYST_BUD).lightLevel(state -> light);
    }

    private static BlockBehaviour.Properties fusionProperties() {
        return BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_CYAN).strength(6.0f, 1200.0f).sound(SoundType.NETHERITE_BLOCK).requiresCorrectToolForDrops();
    }

    private static <T extends Block> DeferredBlock<T> registerBlockAndItem(String name, Function<BlockBehaviour.Properties, T> blockConstructor, BlockBehaviour.Properties properties) {
        return registerBlockAndItem(name, blockConstructor, properties, true, true);
    }

    public static <T extends Block> DeferredBlock<T> bacteriaBlock(DeferredBlock<T> block) {
        NTItems.addBacteriaItem(block);
        return block;
    }

    private static <T extends Block> DeferredBlock<T> registerBlockAndItem(String name, Function<BlockBehaviour.Properties, T> blockConstructor, BlockBehaviour.Properties properties, boolean addToTab, boolean genItemModel) {
        DeferredBlock<T> block = BLOCKS.registerBlock(name, blockConstructor, () -> properties);
        DeferredItem<BlockItem> blockItem = NTItems.registerItem(name, props -> new BlockItem(block.get(), props), new Item.Properties().useBlockDescriptionPrefix(), addToTab);
        if (genItemModel) {
            NTItems.addBlockItem(blockItem);
        }
        return block;
    }

    private static <T extends Block> DeferredBlock<T> registerBlockAndItem(String name, Function<BlockBehaviour.Properties, T> blockConstructor, BlockBehaviour.Properties properties, BiFunction<T, Item.Properties, BlockItem> blockItemConstructor) {
        DeferredBlock<T> block = BLOCKS.registerBlock(name, blockConstructor, () -> properties);
        DeferredItem<BlockItem> blockItem = NTItems.registerItem(name, props -> blockItemConstructor.apply(block.get(), props), new Item.Properties().useBlockDescriptionPrefix());
        NTItems.addBlockItem(blockItem);
        return block;
    }
}
