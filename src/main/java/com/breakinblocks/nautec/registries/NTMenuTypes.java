package com.breakinblocks.nautec.registries;

import com.breakinblocks.nautec.content.menus.ConduitTapMenu;
import com.breakinblocks.nautec.content.menus.PressureForgeMenu;
import com.breakinblocks.nautec.content.menus.LaserPurificationSmelterMenu;
import com.breakinblocks.nautec.content.menus.ResonantCisternMenu;
import com.breakinblocks.nautec.content.menus.ResonantVaultMenu;
import com.breakinblocks.nautec.content.dishstorage.DishStorageMenu;
import com.breakinblocks.nautec.content.menus.ResonanceNodeMenu;
import com.breakinblocks.nautec.content.menus.ColonyReplicatorMenu;
import com.breakinblocks.nautec.content.menus.BubbleAnchorMenu;
import com.breakinblocks.nautec.content.menus.EnergyConverterMenu;
import com.breakinblocks.nautec.content.menus.DistributorMenu;
import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.content.menus.AugmentMenu;
import com.breakinblocks.nautec.content.menus.AugmentationStationExtensionMenu;
import com.breakinblocks.nautec.content.menus.AdvancedBacterialAnalyzerMenu;
import com.breakinblocks.nautec.content.menus.BacterialAnalyzerMenu;
import com.breakinblocks.nautec.content.menus.GraftingStationMenu;
import com.breakinblocks.nautec.content.menus.BioReactorMenu;
import com.breakinblocks.nautec.content.menus.ConfinedSpawnerMenu;
import com.breakinblocks.nautec.content.menus.CrateMenu;
import com.breakinblocks.nautec.content.menus.FishingStationMenu;
import com.breakinblocks.nautec.content.menus.CombustionDynamoMenu;
import com.breakinblocks.nautec.content.menus.FusionControllerMenu;
import com.breakinblocks.nautec.content.menus.SatelliteArrayMenu;
import com.breakinblocks.nautec.content.menus.IncubatorMenu;
import com.breakinblocks.nautec.content.menus.IndustrialBioReactorMenu;
import com.breakinblocks.nautec.content.menus.LaserCraftingMatrixMenu;
import com.breakinblocks.nautec.content.menus.MixerMenu;
import com.breakinblocks.nautec.content.menus.MutatorMenu;
import com.breakinblocks.nautec.content.menus.SubmarineModuleMenu;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.network.IContainerFactory;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public final class NTMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(BuiltInRegistries.MENU, Nautec.MODID);

    public static final Supplier<MenuType<CrateMenu>> CRATE = registerMenuType(CrateMenu::new, "crate");
    public static final Supplier<MenuType<FishingStationMenu>> FISHING_STATION = registerMenuType(FishingStationMenu::new, "fishing_station");
    public static final Supplier<MenuType<MixerMenu>> MIXER = registerMenuType(MixerMenu::new, "mixer");
    public static final Supplier<MenuType<LaserCraftingMatrixMenu>> LASER_CRAFTING_MATRIX = registerMenuType(LaserCraftingMatrixMenu::new, "laser_crafting_matrix");
    public static final Supplier<MenuType<AugmentMenu>> AUGMENTS = registerMenuType(AugmentMenu::new, "augments");
    public static final Supplier<MenuType<AugmentationStationExtensionMenu>> AUGMENT_STATION_EXTENSION = registerMenuType(AugmentationStationExtensionMenu::new, "augment_station_extension");
    public static final Supplier<MenuType<BioReactorMenu>> BIO_REACTOR = registerMenuType(BioReactorMenu::new, "bio_reactor");
    public static final Supplier<MenuType<IndustrialBioReactorMenu>> INDUSTRIAL_BIO_REACTOR = registerMenuType(IndustrialBioReactorMenu::new, "industrial_bio_reactor");
    public static final Supplier<MenuType<IncubatorMenu>> INCUBATOR = registerMenuType(IncubatorMenu::new, "incubator");
    public static final Supplier<MenuType<MutatorMenu>> MUTATOR = registerMenuType(MutatorMenu::new, "mutator");
    public static final Supplier<MenuType<BacterialAnalyzerMenu>> BACTERIAL_ANALYZER = registerMenuType(BacterialAnalyzerMenu::new, "bacterial_analyzer");
    public static final Supplier<MenuType<ColonyReplicatorMenu>> COLONY_REPLICATOR = registerMenuType(ColonyReplicatorMenu::new, "colony_replicator");
    public static final Supplier<MenuType<BubbleAnchorMenu>> BUBBLE_ANCHOR = registerMenuType(BubbleAnchorMenu::new, "bubble_anchor");
    public static final Supplier<MenuType<EnergyConverterMenu>> ENERGY_CONVERTER = registerMenuType(EnergyConverterMenu::new, "energy_converter");
    public static final Supplier<MenuType<PressureForgeMenu>> PRESSURE_FORGE = registerMenuType(PressureForgeMenu::new, "pressure_forge");
    public static final Supplier<MenuType<LaserPurificationSmelterMenu>> LASER_PURIFICATION_SMELTER = registerMenuType(LaserPurificationSmelterMenu::new, "laser_purification_smelter");
    public static final Supplier<MenuType<DistributorMenu>> DISTRIBUTOR = registerMenuType(DistributorMenu::new, "nautechnical_distributor");
    public static final Supplier<MenuType<AdvancedBacterialAnalyzerMenu>> ADVANCED_BACTERIAL_ANALYZER = registerMenuType(AdvancedBacterialAnalyzerMenu::new, "advanced_bacterial_analyzer");
    public static final Supplier<MenuType<GraftingStationMenu>> GRAFTING_STATION = registerMenuType(GraftingStationMenu::new, "grafting_station");
    public static final Supplier<MenuType<SubmarineModuleMenu>> SUBMARINE_MODULES = registerMenuType(SubmarineModuleMenu::new, "submarine_modules");
    public static final Supplier<MenuType<ConfinedSpawnerMenu>> CONFINED_SPAWNER = registerMenuType(ConfinedSpawnerMenu::new, "confined_spawner");
    public static final Supplier<MenuType<FusionControllerMenu>> FUSION_CONTROLLER = registerMenuType(FusionControllerMenu::new, "fusion_controller");
    public static final Supplier<MenuType<CombustionDynamoMenu>> COMBUSTION_DYNAMO = registerMenuType(CombustionDynamoMenu::new, "combustion_dynamo");
    public static final Supplier<MenuType<SatelliteArrayMenu>> SATELLITE_ARRAY = registerMenuType(SatelliteArrayMenu::new, "satellite_array");
    public static final Supplier<MenuType<ResonanceNodeMenu>> RESONANCE_NODE = registerMenuType(ResonanceNodeMenu::new, "resonance_node");
    public static final Supplier<MenuType<DishStorageMenu>> DISH_STORAGE = registerMenuType(DishStorageMenu::new, "dish_storage");
    public static final Supplier<MenuType<ConduitTapMenu>> CONDUIT_TAP = registerMenuType(ConduitTapMenu::new, "conduit_tap");
    public static final Supplier<MenuType<ResonantVaultMenu>> RESONANT_VAULT = registerMenuType(ResonantVaultMenu::new, "resonant_vault");
    public static final Supplier<MenuType<ResonantCisternMenu>> RESONANT_CISTERN = registerMenuType(ResonantCisternMenu::new, "resonant_cistern");


    private static <T extends AbstractContainerMenu> Supplier<MenuType<T>> registerMenuType(IContainerFactory<T> factory, String name) {
        return MENUS.register(name, () -> IMenuTypeExtension.create(factory));
    }
}
