package com.breakinblocks.nautec.registries;

import com.breakinblocks.nautec.content.conduits.ConduitTapBlockEntity;
import com.breakinblocks.nautec.content.resonantstorage.ResonantCisternBlockEntity;
import com.breakinblocks.nautec.content.resonantstorage.ResonantVaultBlockEntity;
import com.breakinblocks.nautec.content.biometank.BiomeTankBlockEntity;
import com.breakinblocks.nautec.content.dishstorage.DishStorageBlockEntity;
import com.breakinblocks.nautec.content.conduit.ConduitBeaconBlockEntity;
import com.breakinblocks.nautec.content.resonance.ResonanceNodeBlockEntity;
import com.breakinblocks.nautec.content.blockentities.OxygenDiffuserBlockEntity;
import com.breakinblocks.nautec.content.blockentities.ColonyReplicatorBlockEntity;
import com.breakinblocks.nautec.content.bubble.AirPocketBlockEntity;
import com.breakinblocks.nautec.content.bubble.BubbleAnchorBlockEntity;
import com.breakinblocks.nautec.content.distributor.DistributorBlockEntity;
import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.content.blockentities.AnchorBlockEntity;
import com.breakinblocks.nautec.content.blockentities.AquaticCatalystBlockEntity;
import com.breakinblocks.nautec.content.blockentities.AdvancedBacterialAnalyzerBlockEntity;
import com.breakinblocks.nautec.content.blockentities.BacterialAnalyzerBlockEntity;
import com.breakinblocks.nautec.content.blockentities.GraftingStationBlockEntity;
import com.breakinblocks.nautec.content.blockentities.BacterialFuelCellBlockEntity;
import com.breakinblocks.nautec.content.blockentities.ChargerBlockEntity;
import com.breakinblocks.nautec.content.blockentities.ConfinedSpawnerBlockEntity;
import com.breakinblocks.nautec.content.blockentities.CrystalCradleBlockEntity;
import com.breakinblocks.nautec.content.blockentities.CrateBlockEntity;
import com.breakinblocks.nautec.content.blockentities.CreativeEnergySourceBlockEntity;
import com.breakinblocks.nautec.content.blockentities.CreativePowerSourceBlockEntity;
import com.breakinblocks.nautec.content.blockentities.DecorativePrismarineCrystalBlockEntity;
import com.breakinblocks.nautec.content.blockentities.EnergyConverterBlockEntity;
import com.breakinblocks.nautec.content.blockentities.FishingStationBlockEntity;
import com.breakinblocks.nautec.content.blockentities.FocusingLensBlockEntity;
import com.breakinblocks.nautec.content.blockentities.GatewayBlockEntity;
import com.breakinblocks.nautec.content.blockentities.IncubatorBlockEntity;
import com.breakinblocks.nautec.content.blockentities.LaserJunctionBlockEntity;
import com.breakinblocks.nautec.content.blockentities.LuckyFishingZoneBlockEntity;
import com.breakinblocks.nautec.content.blockentities.LaserCraftingMatrixBlockEntity;
import com.breakinblocks.nautec.content.blockentities.MixerBlockEntity;
import com.breakinblocks.nautec.content.blockentities.MutatorBlockEntity;
import com.breakinblocks.nautec.content.blockentities.OilBarrelBlockEntity;
import com.breakinblocks.nautec.content.blockentities.PressureForgeBlockEntity;
import com.breakinblocks.nautec.content.blockentities.PrismarineLaserRelayBlockEntity;
import com.breakinblocks.nautec.content.blockentities.ResonanceChamberBlockEntity;
import com.breakinblocks.nautec.content.blockentities.SubmarineDockBlockEntity;
import com.breakinblocks.nautec.content.blockentities.multiblock.controller.AugmentationStationBlockEntity;
import com.breakinblocks.nautec.content.blockentities.multiblock.controller.BioReactorBlockEntity;
import com.breakinblocks.nautec.content.blockentities.multiblock.controller.IndustrialBioReactorBlockEntity;
import com.breakinblocks.nautec.content.blockentities.multiblock.controller.DrainBlockEntity;
import com.breakinblocks.nautec.content.blockentities.multiblock.part.AugmentationStationExtensionBlockEntity;
import com.breakinblocks.nautec.content.blockentities.multiblock.part.AugmentationStationPartBlockEntity;
import com.breakinblocks.nautec.content.blockentities.multiblock.part.BioReactorPartBlockEntity;
import com.breakinblocks.nautec.content.blockentities.multiblock.part.IndustrialBioReactorPartBlockEntity;
import com.breakinblocks.nautec.content.blockentities.multiblock.part.DrainPartBlockEntity;
import com.breakinblocks.nautec.content.blockentities.multiblock.semi.PrismarineCrystalBlockEntity;
import com.breakinblocks.nautec.content.blockentities.multiblock.semi.PrismarineCrystalPartBlockEntity;
import com.breakinblocks.nautec.content.blockentities.fusion.FusionCollectorBlockEntity;
import com.breakinblocks.nautec.content.blockentities.generators.CombustionDynamoBlockEntity;
import com.breakinblocks.nautec.content.blockentities.generators.ThermalVentTapBlockEntity;
import com.breakinblocks.nautec.content.resonance.PrismaticEmitterBlockEntity;
import com.breakinblocks.nautec.content.resonance.SatelliteArrayBlockEntity;
import com.breakinblocks.nautec.content.blockentities.generators.TidalRotorBlockEntity;
import com.breakinblocks.nautec.content.blockentities.fusion.FusionControllerBlockEntity;
import com.breakinblocks.nautec.content.blockentities.fusion.FusionPortBlockEntity;
import com.breakinblocks.nautec.content.blockentities.fusion.LaserInjectorBlockEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public final class NTBlockEntityTypes {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, Nautec.MODID);

    public static final Supplier<BlockEntityType<AquaticCatalystBlockEntity>> AQUATIC_CATALYST = BLOCK_ENTITIES.register("aquatic_catalyst",
            () -> BlockEntityType.Builder.of(AquaticCatalystBlockEntity::new,
                    NTBlocks.AQUATIC_CATALYST.get()).build(null));
    public static final Supplier<BlockEntityType<PrismarineLaserRelayBlockEntity>> PRISMARINE_LASER_RELAY = BLOCK_ENTITIES.register("prismarine_laser_relay",
            () -> BlockEntityType.Builder.of(PrismarineLaserRelayBlockEntity::new,
                    NTBlocks.PRISMARINE_RELAY.get()).build(null));
    public static final Supplier<BlockEntityType<LaserJunctionBlockEntity>> LASER_JUNCTION = BLOCK_ENTITIES.register("laser_junction",
            () -> BlockEntityType.Builder.of(LaserJunctionBlockEntity::new,
                    NTBlocks.LASER_JUNCTION.get()).build(null));
    public static final Supplier<BlockEntityType<MixerBlockEntity>> MIXER = BLOCK_ENTITIES.register("mixer",
            () -> BlockEntityType.Builder.of(MixerBlockEntity::new,
                    NTBlocks.MIXER.get()).build(null));
    public static final Supplier<BlockEntityType<LaserCraftingMatrixBlockEntity>> LASER_CRAFTING_MATRIX = BLOCK_ENTITIES.register("laser_crafting_matrix",
            () -> BlockEntityType.Builder.of(LaserCraftingMatrixBlockEntity::new,
                    NTBlocks.LASER_CRAFTING_MATRIX.get()).build(null));
    public static final Supplier<BlockEntityType<LuckyFishingZoneBlockEntity>> LUCKY_FISHING_ZONE = BLOCK_ENTITIES.register("lucky_fishing_zone",
            () -> BlockEntityType.Builder.of(LuckyFishingZoneBlockEntity::new,
                    NTBlocks.LUCKY_FISHING_ZONE.get()).build(null));
    public static final Supplier<BlockEntityType<CrateBlockEntity>> CRATE = BLOCK_ENTITIES.register("crate",
            () -> BlockEntityType.Builder.of(CrateBlockEntity::new,
                    NTBlocks.CRATE.get(), NTBlocks.RUSTY_CRATE.get()).build(null));
    public static final Supplier<BlockEntityType<AnchorBlockEntity>> ANCHOR = BLOCK_ENTITIES.register("anchor",
            () -> BlockEntityType.Builder.of(AnchorBlockEntity::new,
                    NTBlocks.ANCHOR.get()).build(null));
    public static final Supplier<BlockEntityType<FishingStationBlockEntity>> FISHING_STATION = BLOCK_ENTITIES.register("fishing_station",
            () -> BlockEntityType.Builder.of(FishingStationBlockEntity::new,
                    NTBlocks.FISHING_STATION.get()).build(null));
    public static final Supplier<BlockEntityType<OilBarrelBlockEntity>> OIL_BARREL = BLOCK_ENTITIES.register("oil_barrel",
            () -> BlockEntityType.Builder.of(OilBarrelBlockEntity::new,
                    NTBlocks.OIL_BARREL.get()).build(null));

    public static final Supplier<BlockEntityType<SubmarineDockBlockEntity>> SUBMARINE_DOCK = BLOCK_ENTITIES.register("submarine_dock",
            () -> BlockEntityType.Builder.of(SubmarineDockBlockEntity::new,
                    NTBlocks.SUBMARINE_DOCK.get()).build(null));
    public static final Supplier<BlockEntityType<PressureForgeBlockEntity>> PRESSURE_FORGE = BLOCK_ENTITIES.register("pressure_forge",
            () -> BlockEntityType.Builder.of(PressureForgeBlockEntity::new,
                    NTBlocks.PRESSURE_FORGE.get()).build(null));
    public static final Supplier<BlockEntityType<GatewayBlockEntity>> GATEWAY = BLOCK_ENTITIES.register("gateway",
            () -> BlockEntityType.Builder.of(GatewayBlockEntity::new,
                    NTBlocks.GATEWAY.get()).build(null));
    public static final Supplier<BlockEntityType<ResonanceChamberBlockEntity>> RESONANCE_CHAMBER = BLOCK_ENTITIES.register("resonance_chamber",
            () -> BlockEntityType.Builder.of(ResonanceChamberBlockEntity::new,
                    NTBlocks.RESONANCE_CHAMBER.get()).build(null));
    public static final Supplier<BlockEntityType<FocusingLensBlockEntity>> FOCUSING_LENS = BLOCK_ENTITIES.register("focusing_lens",
            () -> BlockEntityType.Builder.of(FocusingLensBlockEntity::new,
                    NTBlocks.FOCUSING_LENS.get()).build(null));

    public static final Supplier<BlockEntityType<MutatorBlockEntity>> MUTATOR = BLOCK_ENTITIES.register("mutator",
            () -> BlockEntityType.Builder.of(MutatorBlockEntity::new,
                    NTBlocks.MUTATOR.get()).build(null));
    public static final Supplier<BlockEntityType<IncubatorBlockEntity>> INCUBATOR = BLOCK_ENTITIES.register("incubator",
            () -> BlockEntityType.Builder.of(IncubatorBlockEntity::new,
                    NTBlocks.INCUBATOR.get()).build(null));
    public static final Supplier<BlockEntityType<BioReactorBlockEntity>> BIO_REACTOR = BLOCK_ENTITIES.register("bio_reactor",
            () -> BlockEntityType.Builder.of(BioReactorBlockEntity::new,
                    NTBlocks.BIO_REACTOR.get()).build(null));
    public static final Supplier<BlockEntityType<BioReactorPartBlockEntity>> BIO_REACTOR_PART = BLOCK_ENTITIES.register("bio_reactor_part",
            () -> BlockEntityType.Builder.of(BioReactorPartBlockEntity::new,
                    NTBlocks.BIO_REACTOR_PART.get()).build(null));
    public static final Supplier<BlockEntityType<IndustrialBioReactorBlockEntity>> INDUSTRIAL_BIO_REACTOR = BLOCK_ENTITIES.register("industrial_bio_reactor",
            () -> BlockEntityType.Builder.of(IndustrialBioReactorBlockEntity::new,
                    NTBlocks.INDUSTRIAL_BIO_REACTOR.get()).build(null));
    public static final Supplier<BlockEntityType<IndustrialBioReactorPartBlockEntity>> INDUSTRIAL_BIO_REACTOR_PART = BLOCK_ENTITIES.register("industrial_bio_reactor_part",
            () -> BlockEntityType.Builder.of(IndustrialBioReactorPartBlockEntity::new,
                    NTBlocks.INDUSTRIAL_BIO_REACTOR_PART.get()).build(null));
    public static final Supplier<BlockEntityType<BacterialFuelCellBlockEntity>> BACTERIAL_FUEL_CELL = BLOCK_ENTITIES.register("bacterial_fuel_cell",
            () -> BlockEntityType.Builder.of(BacterialFuelCellBlockEntity::new,
                    NTBlocks.BACTERIAL_FUEL_CELL.get()).build(null));
    public static final Supplier<BlockEntityType<BacterialAnalyzerBlockEntity>> BACTERIAL_ANALYZER = BLOCK_ENTITIES.register("bacterial_analyzer",
            () -> BlockEntityType.Builder.of(BacterialAnalyzerBlockEntity::new,
                    NTBlocks.BACTERIAL_ANALYZER.get()).build(null));
    public static final Supplier<BlockEntityType<OxygenDiffuserBlockEntity>> OXYGEN_DIFFUSER = BLOCK_ENTITIES.register("oxygen_diffuser",
            () -> BlockEntityType.Builder.of(OxygenDiffuserBlockEntity::new,
                    NTBlocks.OXYGEN_DIFFUSER.get()).build(null));
    public static final Supplier<BlockEntityType<ColonyReplicatorBlockEntity>> COLONY_REPLICATOR = BLOCK_ENTITIES.register("colony_replicator",
            () -> BlockEntityType.Builder.of(ColonyReplicatorBlockEntity::new,
                    NTBlocks.COLONY_REPLICATOR.get()).build(null));
    public static final Supplier<BlockEntityType<AirPocketBlockEntity>> AIR_POCKET = BLOCK_ENTITIES.register("air_pocket",
            () -> BlockEntityType.Builder.of(AirPocketBlockEntity::new,
                    NTBlocks.AIR_POCKET.get()).build(null));
    public static final Supplier<BlockEntityType<BubbleAnchorBlockEntity>> BUBBLE_ANCHOR = BLOCK_ENTITIES.register("bubble_anchor",
            () -> BlockEntityType.Builder.of(BubbleAnchorBlockEntity::new,
                    NTBlocks.BUBBLE_ANCHOR.get()).build(null));
    public static final Supplier<BlockEntityType<DistributorBlockEntity>> DISTRIBUTOR = BLOCK_ENTITIES.register("nautechnical_distributor",
            () -> BlockEntityType.Builder.of(DistributorBlockEntity::new,
                    NTBlocks.DISTRIBUTOR.get()).build(null));
    public static final Supplier<BlockEntityType<ConduitTapBlockEntity>> CONDUIT_TAP = BLOCK_ENTITIES.register("conduit_tap",
            () -> BlockEntityType.Builder.of(ConduitTapBlockEntity::new,
                    NTBlocks.CONDUIT_TAP.get()).build(null));
    public static final Supplier<BlockEntityType<ResonantVaultBlockEntity>> RESONANT_VAULT = BLOCK_ENTITIES.register("resonant_vault",
            () -> BlockEntityType.Builder.of(ResonantVaultBlockEntity::new,
                    NTBlocks.RESONANT_VAULT.get()).build(null));
    public static final Supplier<BlockEntityType<ResonantCisternBlockEntity>> RESONANT_CISTERN = BLOCK_ENTITIES.register("resonant_cistern",
            () -> BlockEntityType.Builder.of(ResonantCisternBlockEntity::new,
                    NTBlocks.RESONANT_CISTERN.get()).build(null));
    public static final Supplier<BlockEntityType<AdvancedBacterialAnalyzerBlockEntity>> ADVANCED_BACTERIAL_ANALYZER = BLOCK_ENTITIES.register("advanced_bacterial_analyzer",
            () -> BlockEntityType.Builder.of(AdvancedBacterialAnalyzerBlockEntity::new,
                    NTBlocks.ADVANCED_BACTERIAL_ANALYZER.get()).build(null));
    public static final Supplier<BlockEntityType<GraftingStationBlockEntity>> GRAFTING_STATION = BLOCK_ENTITIES.register("grafting_station",
            () -> BlockEntityType.Builder.of(GraftingStationBlockEntity::new,
                    NTBlocks.GRAFTING_STATION.get()).build(null));

    public static final Supplier<BlockEntityType<CreativePowerSourceBlockEntity>> CREATIVE_POWER_SOURCE = BLOCK_ENTITIES.register("creative_power_source",
            () -> BlockEntityType.Builder.of(CreativePowerSourceBlockEntity::new,
                    NTBlocks.CREATIVE_POWER_SOURCE.get()).build(null));
    public static final Supplier<BlockEntityType<CreativeEnergySourceBlockEntity>> CREATIVE_ENERGY_SOURCE = BLOCK_ENTITIES.register("creative_energy_source",
            () -> BlockEntityType.Builder.of(CreativeEnergySourceBlockEntity::new,
                    NTBlocks.CREATIVE_ENERGY_SOURCE.get()).build(null));
    public static final Supplier<BlockEntityType<EnergyConverterBlockEntity>> ENERGY_CONVERTER = BLOCK_ENTITIES.register("energy_converter",
            () -> BlockEntityType.Builder.of(EnergyConverterBlockEntity::new,
                    NTBlocks.ENERGY_CONVERTER.get()).build(null));
    public static final Supplier<BlockEntityType<ChargerBlockEntity>> CHARGER = BLOCK_ENTITIES.register("charger",
            () -> BlockEntityType.Builder.of(ChargerBlockEntity::new,
                    NTBlocks.CHARGER.get()).build(null));
    public static final Supplier<BlockEntityType<CrystalCradleBlockEntity>> CRYSTAL_CRADLE = BLOCK_ENTITIES.register("crystal_cradle",
            () -> BlockEntityType.Builder.of(CrystalCradleBlockEntity::new,
                    NTBlocks.CRYSTAL_CRADLE.get()).build(null));
    public static final Supplier<BlockEntityType<ConduitBeaconBlockEntity>> CONDUIT_BEACON = BLOCK_ENTITIES.register("conduit_beacon",
            () -> BlockEntityType.Builder.of(ConduitBeaconBlockEntity::new,
                    NTBlocks.CONDUIT_BEACON.get()).build(null));
    public static final Supplier<BlockEntityType<DishStorageBlockEntity>> DISH_STORAGE = BLOCK_ENTITIES.register("dish_storage",
            () -> BlockEntityType.Builder.of(DishStorageBlockEntity::new,
                    NTBlocks.AQUARINE_DISH_STORAGE.get(), NTBlocks.DEEP_STEEL_DISH_STORAGE.get(), NTBlocks.ATLANTIC_GOLD_DISH_STORAGE.get()).build(null));
    public static final Supplier<BlockEntityType<BiomeTankBlockEntity>> BIOME_TANK = BLOCK_ENTITIES.register("biome_tank",
            () -> BlockEntityType.Builder.of(BiomeTankBlockEntity::new,
                    NTBlocks.BIOME_TANK.get()).build(null));
    public static final Supplier<BlockEntityType<ResonanceNodeBlockEntity>> RESONANCE_NODE = BLOCK_ENTITIES.register("resonance_node",
            () -> BlockEntityType.Builder.of(ResonanceNodeBlockEntity::new,
                    NTBlocks.RESONANCE_NODE.get()).build(null));
    public static final Supplier<BlockEntityType<SatelliteArrayBlockEntity>> SATELLITE_ARRAY = BLOCK_ENTITIES.register("satellite_array",
            () -> BlockEntityType.Builder.of(SatelliteArrayBlockEntity::new,
                    NTBlocks.UPLINK_ARRAY.get()).build(null));
    public static final Supplier<BlockEntityType<PrismaticEmitterBlockEntity>> PRISMATIC_EMITTER = BLOCK_ENTITIES.register("prismatic_emitter",
            () -> BlockEntityType.Builder.of(PrismaticEmitterBlockEntity::new,
                    NTBlocks.PRISMATIC_EMITTER.get()).build(null));
    public static final Supplier<BlockEntityType<TidalRotorBlockEntity>> TIDAL_ROTOR = BLOCK_ENTITIES.register("tidal_rotor",
            () -> BlockEntityType.Builder.of(TidalRotorBlockEntity::new,
                    NTBlocks.TIDAL_ROTOR.get()).build(null));
    public static final Supplier<BlockEntityType<ThermalVentTapBlockEntity>> THERMAL_VENT_TAP = BLOCK_ENTITIES.register("thermal_vent_tap",
            () -> BlockEntityType.Builder.of(ThermalVentTapBlockEntity::new,
                    NTBlocks.THERMAL_VENT_TAP.get()).build(null));
    public static final Supplier<BlockEntityType<CombustionDynamoBlockEntity>> COMBUSTION_DYNAMO = BLOCK_ENTITIES.register("combustion_dynamo",
            () -> BlockEntityType.Builder.of(CombustionDynamoBlockEntity::new,
                    NTBlocks.COMBUSTION_DYNAMO.get()).build(null));
    public static final Supplier<BlockEntityType<FusionControllerBlockEntity>> FUSION_CONTROLLER = BLOCK_ENTITIES.register("fusion_controller",
            () -> BlockEntityType.Builder.of(FusionControllerBlockEntity::new,
                    NTBlocks.FUSION_CONTROLLER.get()).build(null));
    public static final Supplier<BlockEntityType<LaserInjectorBlockEntity>> LASER_INJECTOR = BLOCK_ENTITIES.register("laser_injector",
            () -> BlockEntityType.Builder.of(LaserInjectorBlockEntity::new,
                    NTBlocks.LASER_INJECTOR.get()).build(null));
    public static final Supplier<BlockEntityType<FusionCollectorBlockEntity>> FUSION_COLLECTOR = BLOCK_ENTITIES.register("fusion_collector",
            () -> BlockEntityType.Builder.of(FusionCollectorBlockEntity::new,
                    NTBlocks.FUSION_COLLECTOR.get()).build(null));
    public static final Supplier<BlockEntityType<FusionPortBlockEntity>> FUSION_PORT = BLOCK_ENTITIES.register("fusion_port",
            () -> BlockEntityType.Builder.of(FusionPortBlockEntity::new,
                    NTBlocks.FUSION_PORT.get()).build(null));

    public static final Supplier<BlockEntityType<ConfinedSpawnerBlockEntity>> CONFINED_SPAWNER = BLOCK_ENTITIES.register("confined_spawner",
            () -> BlockEntityType.Builder.of(ConfinedSpawnerBlockEntity::new,
                    NTBlocks.CONFINED_SPAWNER.get()).build(null));

    public static final Supplier<BlockEntityType<DrainBlockEntity>> DRAIN = BLOCK_ENTITIES.register("drain",
            () -> BlockEntityType.Builder.of(DrainBlockEntity::new,
                    NTBlocks.DRAIN.get()).build(null));
    public static final Supplier<BlockEntityType<DrainPartBlockEntity>> DRAIN_PART = BLOCK_ENTITIES.register("drain_part",
            () -> BlockEntityType.Builder.of(DrainPartBlockEntity::new,
                    NTBlocks.DRAIN_PART.get()).build(null));

    public static final Supplier<BlockEntityType<PrismarineCrystalBlockEntity>> PRISMARINE_CRYSTAL = BLOCK_ENTITIES.register("prismarine_crystal",
            () -> BlockEntityType.Builder.of(PrismarineCrystalBlockEntity::new,
                    NTBlocks.PRISMARINE_CRYSTAL.get()).build(null));
    public static final Supplier<BlockEntityType<PrismarineCrystalPartBlockEntity>> PRISMARINE_CRYSTAL_PART = BLOCK_ENTITIES.register("prismarine_crystal_part",
            () -> BlockEntityType.Builder.of(PrismarineCrystalPartBlockEntity::new,
                    NTBlocks.PRISMARINE_CRYSTAL_PART.get()).build(null));

    public static final Supplier<BlockEntityType<DecorativePrismarineCrystalBlockEntity>> DECORATIVE_PRISMARINE_CRYSTAL = BLOCK_ENTITIES.register("decorative_prismarine_crystal",
            () -> BlockEntityType.Builder.of(DecorativePrismarineCrystalBlockEntity::new,
                    NTBlocks.DECORATIVE_PRISMARINE_CRYSTAL.get()).build(null));

    public static final Supplier<BlockEntityType<AugmentationStationBlockEntity>> AUGMENTATION_STATION = BLOCK_ENTITIES.register("augmentation_station",
            () -> BlockEntityType.Builder.of(AugmentationStationBlockEntity::new,
                    NTBlocks.AUGMENTATION_STATION.get()).build(null));
    public static final Supplier<BlockEntityType<AugmentationStationPartBlockEntity>> AUGMENTATION_STATION_PART = BLOCK_ENTITIES.register("augmentation_station_part",
            () -> BlockEntityType.Builder.of(AugmentationStationPartBlockEntity::new,
                    NTBlocks.AUGMENTATION_STATION_PART.get()).build(null));
    public static final Supplier<BlockEntityType<AugmentationStationExtensionBlockEntity>> AUGMENTATION_STATION_EXTENSION = BLOCK_ENTITIES.register("augmentation_station_extension",
            () -> BlockEntityType.Builder.of(AugmentationStationExtensionBlockEntity::new,
                    NTBlocks.AUGMENTATION_STATION_EXTENSION.get()).build(null));
}
