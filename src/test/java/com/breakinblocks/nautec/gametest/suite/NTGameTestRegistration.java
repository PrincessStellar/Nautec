package com.breakinblocks.nautec.gametest.suite;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.gametest.framework.TestFunction;

import java.util.List;

public final class NTGameTestRegistration {
    private NTGameTestRegistration() {
    }

    public static List<TestFunction> registerTests() {
        NTTestRegistrar r = new NTTestRegistrar();

        r.add("framework/arena_smoke", 20, helper -> {
            BlockPos pos = new BlockPos(4, 1, 4);
            helper.setBlock(pos, Blocks.STONE.defaultBlockState());
            helper.succeedWhenBlockPresent(Blocks.STONE, pos);
        });

        PowerAndLaserTests.register(r);
        MachineTests.register(r);
        MultiblockTests.register(r);
        CrateAndCapabilityTests.register(r);
        RecipeAndBacteriaTests.register(r);
        BacteriaMachineTests.register(r);
        GeneratedPackTests.register(r);
        PersistenceTests.register(r);
        WorldgenInjectionTests.register(r);
        ContentIntegrityTests.register(r);
        AquaticMovementTests.register(r);
        LootTableTests.register(r);
        LuckyZoneTests.register(r);
        FishingHookFlowTests.register(r);
        StarcatcherCompatTests.register(r);
        ConduitTests.register(r);
        ResonantStorageTests.register(r);
        WaveJetTests.register(r);
        AtlanteanRifleTests.register(r);
        NeptunesTridentTests.register(r);
        SubmarineTests.register(r);
        GatewayTests.register(r);
        ReviewRegressionTests.register(r);
        ReleaseRegressionTests.register(r);
        ClientAcceptanceRegressionTests.register(r);
        LaserFixTests.register(r);
        BiologyFixTests.register(r);
        DataFixTests.register(r);
        MachineFixTests.register(r);
        NavalFishingFixTests.register(r);
        ShowcaseTests.register(r);
        BioReactorOverhaulTests.register(r);
        CatalystFeedbackTests.register(r);
        EyeOfTheSeaTests.register(r);
        ConfinedSpawnerTests.register(r);
        DrainTests.register(r);
        CrystalCultivationTests.register(r);
        GuaranteedPartTests.register(r);
        FusionPlantTests.register(r);
        OceanGeneratorTests.register(r);
        ResonanceNetworkTests.register(r);
        PrismaticEmitterTests.register(r);
        ResonanceCharmTests.register(r);
        SatelliteTests.register(r);
        PressureSynthesizerTests.register(r);
        GraftingStationTests.register(r);
        DishPortTests.register(r);
        SideConfigTests.register(r);
        AdvancedAnalyzerTests.register(r);
        DistributorTests.register(r);
        BubbleAnchorTests.register(r);
        ColonyReplicatorTests.register(r);
        SeafloorFeatureTests.register(r);
        PressureHatchTests.register(r);
        CargoModuleTests.register(r);
        OxygenDiffuserTests.register(r);
        ResearchOutpostTests.register(r);
        ExpansionFeatureTests.register(r);
        HardeningTests.register(r);
        ArtPassTests.register(r);
        CombustionDynamoTests.register(r);
        BeamOverclockTests.register(r);
        LaserCraftingMatrixTests.register(r);
        SkyblockTests.register(r);
        if (r.registeredCount() != 684) {
            throw new IllegalStateException("Expected 684 Nautec suite tests, registered " + r.registeredCount());
        }
        return r.functions();
    }
}
