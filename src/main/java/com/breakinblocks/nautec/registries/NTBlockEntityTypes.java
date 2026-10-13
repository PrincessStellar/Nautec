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
import com.breakinblocks.nautec.content.blockentities.LaserPurificationSmelterBlockEntity;
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
            () -> new BlockEntityType<>(AquaticCatalystBlockEntity::new,
                    NTBlocks.AQUATIC_CATALYST.get()));
    public static final Supplier<BlockEntityType<PrismarineLaserRelayBlockEntity>> PRISMARINE_LASER_RELAY = BLOCK_ENTITIES.register("prismarine_laser_relay",
            () -> new BlockEntityType<>(PrismarineLaserRelayBlockEntity::new,
                    NTBlocks.PRISMARINE_RELAY.get()));
    public static final Supplier<BlockEntityType<LaserJunctionBlockEntity>> LASER_JUNCTION = BLOCK_ENTITIES.register("laser_junction",
            () -> new BlockEntityType<>(LaserJunctionBlockEntity::new,
                    NTBlocks.LASER_JUNCTION.get()));
    public static final Supplier<BlockEntityType<MixerBlockEntity>> MIXER = BLOCK_ENTITIES.register("mixer",
            () -> new BlockEntityType<>(MixerBlockEntity::new,
                    NTBlocks.MIXER.get()));
    public static final Supplier<BlockEntityType<LaserCraftingMatrixBlockEntity>> LASER_CRAFTING_MATRIX = BLOCK_ENTITIES.register("laser_crafting_matrix",
            () -> new BlockEntityType<>(LaserCraftingMatrixBlockEntity::new,
                    NTBlocks.LASER_CRAFTING_MATRIX.get()));
    public static final Supplier<BlockEntityType<LuckyFishingZoneBlockEntity>> LUCKY_FISHING_ZONE = BLOCK_ENTITIES.register("lucky_fishing_zone",
            () -> new BlockEntityType<>(LuckyFishingZoneBlockEntity::new,
                    NTBlocks.LUCKY_FISHING_ZONE.get()));
    public static final Supplier<BlockEntityType<CrateBlockEntity>> CRATE = BLOCK_ENTITIES.register("crate",
            () -> new BlockEntityType<>(CrateBlockEntity::new,
                    NTBlocks.CRATE.get(), NTBlocks.RUSTY_CRATE.get()));
    public static final Supplier<BlockEntityType<AnchorBlockEntity>> ANCHOR = BLOCK_ENTITIES.register("anchor",
            () -> new BlockEntityType<>(AnchorBlockEntity::new,
                    NTBlocks.ANCHOR.get()));
    public static final Supplier<BlockEntityType<FishingStationBlockEntity>> FISHING_STATION = BLOCK_ENTITIES.register("fishing_station",
            () -> new BlockEntityType<>(FishingStationBlockEntity::new,
                    NTBlocks.FISHING_STATION.get()));
    public static final Supplier<BlockEntityType<OilBarrelBlockEntity>> OIL_BARREL = BLOCK_ENTITIES.register("oil_barrel",
            () -> new BlockEntityType<>(OilBarrelBlockEntity::new,
                    NTBlocks.OIL_BARREL.get()));

    public static final Supplier<BlockEntityType<SubmarineDockBlockEntity>> SUBMARINE_DOCK = BLOCK_ENTITIES.register("submarine_dock",
            () -> new BlockEntityType<>(SubmarineDockBlockEntity::new,
                    NTBlocks.SUBMARINE_DOCK.get()));
    public static final Supplier<BlockEntityType<PressureForgeBlockEntity>> PRESSURE_FORGE = BLOCK_ENTITIES.register("pressure_forge",
            () -> new BlockEntityType<>(PressureForgeBlockEntity::new,
                    NTBlocks.PRESSURE_FORGE.get()));
    public static final Supplier<BlockEntityType<LaserPurificationSmelterBlockEntity>> LASER_PURIFICATION_SMELTER = BLOCK_ENTITIES.register("laser_purification_smelter",
            () -> new BlockEntityType<>(LaserPurificationSmelterBlockEntity::new,
                    NTBlocks.LASER_PURIFICATION_SMELTER.get()));
    public static final Supplier<BlockEntityType<GatewayBlockEntity>> GATEWAY = BLOCK_ENTITIES.register("gateway",
            () -> new BlockEntityType<>(GatewayBlockEntity::new,
                    NTBlocks.GATEWAY.get()));
    public static final Supplier<BlockEntityType<ResonanceChamberBlockEntity>> RESONANCE_CHAMBER = BLOCK_ENTITIES.register("resonance_chamber",
            () -> new BlockEntityType<>(ResonanceChamberBlockEntity::new,
                    NTBlocks.RESONANCE_CHAMBER.get()));
    public static final Supplier<BlockEntityType<FocusingLensBlockEntity>> FOCUSING_LENS = BLOCK_ENTITIES.register("focusing_lens",
            () -> new BlockEntityType<>(FocusingLensBlockEntity::new,
                    NTBlocks.FOCUSING_LENS.get()));

    public static final Supplier<BlockEntityType<MutatorBlockEntity>> MUTATOR = BLOCK_ENTITIES.register("mutator",
            () -> new BlockEntityType<>(MutatorBlockEntity::new,
                    NTBlocks.MUTATOR.get()));
    public static final Supplier<BlockEntityType<IncubatorBlockEntity>> INCUBATOR = BLOCK_ENTITIES.register("incubator",
            () -> new BlockEntityType<>(IncubatorBlockEntity::new,
                    NTBlocks.INCUBATOR.get()));
    public static final Supplier<BlockEntityType<BioReactorBlockEntity>> BIO_REACTOR = BLOCK_ENTITIES.register("bio_reactor",
            () -> new BlockEntityType<>(BioReactorBlockEntity::new,
                    NTBlocks.BIO_REACTOR.get()));
    public static final Supplier<BlockEntityType<BioReactorPartBlockEntity>> BIO_REACTOR_PART = BLOCK_ENTITIES.register("bio_reactor_part",
            () -> new BlockEntityType<>(BioReactorPartBlockEntity::new,
                    NTBlocks.BIO_REACTOR_PART.get()));
    public static final Supplier<BlockEntityType<IndustrialBioReactorBlockEntity>> INDUSTRIAL_BIO_REACTOR = BLOCK_ENTITIES.register("industrial_bio_reactor",
            () -> new BlockEntityType<>(IndustrialBioReactorBlockEntity::new,
                    NTBlocks.INDUSTRIAL_BIO_REACTOR.get()));
    public static final Supplier<BlockEntityType<IndustrialBioReactorPartBlockEntity>> INDUSTRIAL_BIO_REACTOR_PART = BLOCK_ENTITIES.register("industrial_bio_reactor_part",
            () -> new BlockEntityType<>(IndustrialBioReactorPartBlockEntity::new,
                    NTBlocks.INDUSTRIAL_BIO_REACTOR_PART.get()));
    public static final Supplier<BlockEntityType<BacterialFuelCellBlockEntity>> BACTERIAL_FUEL_CELL = BLOCK_ENTITIES.register("bacterial_fuel_cell",
            () -> new BlockEntityType<>(BacterialFuelCellBlockEntity::new,
                    NTBlocks.BACTERIAL_FUEL_CELL.get()));
    public static final Supplier<BlockEntityType<BacterialAnalyzerBlockEntity>> BACTERIAL_ANALYZER = BLOCK_ENTITIES.register("bacterial_analyzer",
            () -> new BlockEntityType<>(BacterialAnalyzerBlockEntity::new,
                    NTBlocks.BACTERIAL_ANALYZER.get()));
    public static final Supplier<BlockEntityType<OxygenDiffuserBlockEntity>> OXYGEN_DIFFUSER = BLOCK_ENTITIES.register("oxygen_diffuser",
            () -> new BlockEntityType<>(OxygenDiffuserBlockEntity::new,
                    NTBlocks.OXYGEN_DIFFUSER.get()));
    public static final Supplier<BlockEntityType<ColonyReplicatorBlockEntity>> COLONY_REPLICATOR = BLOCK_ENTITIES.register("colony_replicator",
            () -> new BlockEntityType<>(ColonyReplicatorBlockEntity::new,
                    NTBlocks.COLONY_REPLICATOR.get()));
    public static final Supplier<BlockEntityType<AirPocketBlockEntity>> AIR_POCKET = BLOCK_ENTITIES.register("air_pocket",
            () -> new BlockEntityType<>(AirPocketBlockEntity::new,
                    NTBlocks.AIR_POCKET.get()));
    public static final Supplier<BlockEntityType<BubbleAnchorBlockEntity>> BUBBLE_ANCHOR = BLOCK_ENTITIES.register("bubble_anchor",
            () -> new BlockEntityType<>(BubbleAnchorBlockEntity::new,
                    NTBlocks.BUBBLE_ANCHOR.get()));
    public static final Supplier<BlockEntityType<DistributorBlockEntity>> DISTRIBUTOR = BLOCK_ENTITIES.register("nautechnical_distributor",
            () -> new BlockEntityType<>(DistributorBlockEntity::new,
                    NTBlocks.DISTRIBUTOR.get()));
    public static final Supplier<BlockEntityType<ConduitTapBlockEntity>> CONDUIT_TAP = BLOCK_ENTITIES.register("conduit_tap",
            () -> new BlockEntityType<>(ConduitTapBlockEntity::new,
                    NTBlocks.CONDUIT_TAP.get()));
    public static final Supplier<BlockEntityType<ResonantVaultBlockEntity>> RESONANT_VAULT = BLOCK_ENTITIES.register("resonant_vault",
            () -> new BlockEntityType<>(ResonantVaultBlockEntity::new,
                    NTBlocks.RESONANT_VAULT.get()));
    public static final Supplier<BlockEntityType<ResonantCisternBlockEntity>> RESONANT_CISTERN = BLOCK_ENTITIES.register("resonant_cistern",
            () -> new BlockEntityType<>(ResonantCisternBlockEntity::new,
                    NTBlocks.RESONANT_CISTERN.get()));
    public static final Supplier<BlockEntityType<AdvancedBacterialAnalyzerBlockEntity>> ADVANCED_BACTERIAL_ANALYZER = BLOCK_ENTITIES.register("advanced_bacterial_analyzer",
            () -> new BlockEntityType<>(AdvancedBacterialAnalyzerBlockEntity::new,
                    NTBlocks.ADVANCED_BACTERIAL_ANALYZER.get()));
    public static final Supplier<BlockEntityType<GraftingStationBlockEntity>> GRAFTING_STATION = BLOCK_ENTITIES.register("grafting_station",
            () -> new BlockEntityType<>(GraftingStationBlockEntity::new,
                    NTBlocks.GRAFTING_STATION.get()));

    public static final Supplier<BlockEntityType<CreativePowerSourceBlockEntity>> CREATIVE_POWER_SOURCE = BLOCK_ENTITIES.register("creative_power_source",
            () -> new BlockEntityType<>(CreativePowerSourceBlockEntity::new,
                    NTBlocks.CREATIVE_POWER_SOURCE.get()));
    public static final Supplier<BlockEntityType<CreativeEnergySourceBlockEntity>> CREATIVE_ENERGY_SOURCE = BLOCK_ENTITIES.register("creative_energy_source",
            () -> new BlockEntityType<>(CreativeEnergySourceBlockEntity::new,
                    NTBlocks.CREATIVE_ENERGY_SOURCE.get()));
    public static final Supplier<BlockEntityType<EnergyConverterBlockEntity>> ENERGY_CONVERTER = BLOCK_ENTITIES.register("energy_converter",
            () -> new BlockEntityType<>(EnergyConverterBlockEntity::new,
                    NTBlocks.ENERGY_CONVERTER.get()));
    public static final Supplier<BlockEntityType<ChargerBlockEntity>> CHARGER = BLOCK_ENTITIES.register("charger",
            () -> new BlockEntityType<>(ChargerBlockEntity::new,
                    NTBlocks.CHARGER.get()));
    public static final Supplier<BlockEntityType<CrystalCradleBlockEntity>> CRYSTAL_CRADLE = BLOCK_ENTITIES.register("crystal_cradle",
            () -> new BlockEntityType<>(CrystalCradleBlockEntity::new,
                    NTBlocks.CRYSTAL_CRADLE.get()));
    public static final Supplier<BlockEntityType<ConduitBeaconBlockEntity>> CONDUIT_BEACON = BLOCK_ENTITIES.register("conduit_beacon",
            () -> new BlockEntityType<>(ConduitBeaconBlockEntity::new,
                    NTBlocks.CONDUIT_BEACON.get()));
    public static final Supplier<BlockEntityType<DishStorageBlockEntity>> DISH_STORAGE = BLOCK_ENTITIES.register("dish_storage",
            () -> new BlockEntityType<>(DishStorageBlockEntity::new,
                    NTBlocks.AQUARINE_DISH_STORAGE.get(), NTBlocks.DEEP_STEEL_DISH_STORAGE.get(), NTBlocks.ATLANTIC_GOLD_DISH_STORAGE.get()));
    public static final Supplier<BlockEntityType<BiomeTankBlockEntity>> BIOME_TANK = BLOCK_ENTITIES.register("biome_tank",
            () -> new BlockEntityType<>(BiomeTankBlockEntity::new,
                    NTBlocks.BIOME_TANK.get()));
    public static final Supplier<BlockEntityType<ResonanceNodeBlockEntity>> RESONANCE_NODE = BLOCK_ENTITIES.register("resonance_node",
            () -> new BlockEntityType<>(ResonanceNodeBlockEntity::new,
                    NTBlocks.RESONANCE_NODE.get()));
    public static final Supplier<BlockEntityType<SatelliteArrayBlockEntity>> SATELLITE_ARRAY = BLOCK_ENTITIES.register("satellite_array",
            () -> new BlockEntityType<>(SatelliteArrayBlockEntity::new,
                    NTBlocks.UPLINK_ARRAY.get()));
    public static final Supplier<BlockEntityType<PrismaticEmitterBlockEntity>> PRISMATIC_EMITTER = BLOCK_ENTITIES.register("prismatic_emitter",
            () -> new BlockEntityType<>(PrismaticEmitterBlockEntity::new,
                    NTBlocks.PRISMATIC_EMITTER.get()));
    public static final Supplier<BlockEntityType<TidalRotorBlockEntity>> TIDAL_ROTOR = BLOCK_ENTITIES.register("tidal_rotor",
            () -> new BlockEntityType<>(TidalRotorBlockEntity::new,
                    NTBlocks.TIDAL_ROTOR.get()));
    public static final Supplier<BlockEntityType<ThermalVentTapBlockEntity>> THERMAL_VENT_TAP = BLOCK_ENTITIES.register("thermal_vent_tap",
            () -> new BlockEntityType<>(ThermalVentTapBlockEntity::new,
                    NTBlocks.THERMAL_VENT_TAP.get()));
    public static final Supplier<BlockEntityType<CombustionDynamoBlockEntity>> COMBUSTION_DYNAMO = BLOCK_ENTITIES.register("combustion_dynamo",
            () -> new BlockEntityType<>(CombustionDynamoBlockEntity::new,
                    NTBlocks.COMBUSTION_DYNAMO.get()));
    public static final Supplier<BlockEntityType<FusionControllerBlockEntity>> FUSION_CONTROLLER = BLOCK_ENTITIES.register("fusion_controller",
            () -> new BlockEntityType<>(FusionControllerBlockEntity::new,
                    NTBlocks.FUSION_CONTROLLER.get()));
    public static final Supplier<BlockEntityType<LaserInjectorBlockEntity>> LASER_INJECTOR = BLOCK_ENTITIES.register("laser_injector",
            () -> new BlockEntityType<>(LaserInjectorBlockEntity::new,
                    NTBlocks.LASER_INJECTOR.get()));
    public static final Supplier<BlockEntityType<FusionCollectorBlockEntity>> FUSION_COLLECTOR = BLOCK_ENTITIES.register("fusion_collector",
            () -> new BlockEntityType<>(FusionCollectorBlockEntity::new,
                    NTBlocks.FUSION_COLLECTOR.get()));
    public static final Supplier<BlockEntityType<FusionPortBlockEntity>> FUSION_PORT = BLOCK_ENTITIES.register("fusion_port",
            () -> new BlockEntityType<>(FusionPortBlockEntity::new,
                    NTBlocks.FUSION_PORT.get()));

    public static final Supplier<BlockEntityType<ConfinedSpawnerBlockEntity>> CONFINED_SPAWNER = BLOCK_ENTITIES.register("confined_spawner",
            () -> new BlockEntityType<>(ConfinedSpawnerBlockEntity::new,
                    NTBlocks.CONFINED_SPAWNER.get()));

    public static final Supplier<BlockEntityType<DrainBlockEntity>> DRAIN = BLOCK_ENTITIES.register("drain",
            () -> new BlockEntityType<>(DrainBlockEntity::new,
                    NTBlocks.DRAIN.get()));
    public static final Supplier<BlockEntityType<DrainPartBlockEntity>> DRAIN_PART = BLOCK_ENTITIES.register("drain_part",
            () -> new BlockEntityType<>(DrainPartBlockEntity::new,
                    NTBlocks.DRAIN_PART.get()));

    public static final Supplier<BlockEntityType<PrismarineCrystalBlockEntity>> PRISMARINE_CRYSTAL = BLOCK_ENTITIES.register("prismarine_crystal",
            () -> new BlockEntityType<>(PrismarineCrystalBlockEntity::new,
                    NTBlocks.PRISMARINE_CRYSTAL.get()));
    public static final Supplier<BlockEntityType<PrismarineCrystalPartBlockEntity>> PRISMARINE_CRYSTAL_PART = BLOCK_ENTITIES.register("prismarine_crystal_part",
            () -> new BlockEntityType<>(PrismarineCrystalPartBlockEntity::new,
                    NTBlocks.PRISMARINE_CRYSTAL_PART.get()));

    public static final Supplier<BlockEntityType<DecorativePrismarineCrystalBlockEntity>> DECORATIVE_PRISMARINE_CRYSTAL = BLOCK_ENTITIES.register("decorative_prismarine_crystal",
            () -> new BlockEntityType<>(DecorativePrismarineCrystalBlockEntity::new,
                    NTBlocks.DECORATIVE_PRISMARINE_CRYSTAL.get()));

    public static final Supplier<BlockEntityType<AugmentationStationBlockEntity>> AUGMENTATION_STATION = BLOCK_ENTITIES.register("augmentation_station",
            () -> new BlockEntityType<>(AugmentationStationBlockEntity::new,
                    NTBlocks.AUGMENTATION_STATION.get()));
    public static final Supplier<BlockEntityType<AugmentationStationPartBlockEntity>> AUGMENTATION_STATION_PART = BLOCK_ENTITIES.register("augmentation_station_part",
            () -> new BlockEntityType<>(AugmentationStationPartBlockEntity::new,
                    NTBlocks.AUGMENTATION_STATION_PART.get()));
    public static final Supplier<BlockEntityType<AugmentationStationExtensionBlockEntity>> AUGMENTATION_STATION_EXTENSION = BLOCK_ENTITIES.register("augmentation_station_extension",
            () -> new BlockEntityType<>(AugmentationStationExtensionBlockEntity::new,
                    NTBlocks.AUGMENTATION_STATION_EXTENSION.get()));
}
