package com.breakinblocks.nautec;

import com.breakinblocks.nautec.client.renderer.blockentities.ResonantCisternRenderer;
import com.breakinblocks.nautec.client.renderer.blockentities.ResonantVaultRenderer;
import com.breakinblocks.nautec.client.screen.ResonantCisternScreen;
import com.breakinblocks.nautec.client.screen.ResonantVaultScreen;
import com.breakinblocks.nautec.client.screen.ConduitTapScreen;
import com.breakinblocks.nautec.client.screen.DishStorageScreen;
import com.breakinblocks.nautec.client.renderer.blockentities.ResonanceNodeBERenderer;
import com.breakinblocks.nautec.client.screen.ResonanceNodeScreen;
import com.breakinblocks.nautec.client.screen.ColonyReplicatorScreen;
import com.breakinblocks.nautec.client.screen.BubbleAnchorScreen;
import com.breakinblocks.nautec.client.screen.EnergyConverterScreen;
import com.breakinblocks.nautec.client.screen.PressureForgeScreen;
import com.breakinblocks.nautec.client.screen.DistributorScreen;
import com.breakinblocks.nautec.api.client.renderer.blockentities.LaserBlockEntityRenderer;
import com.breakinblocks.nautec.api.client.renderer.items.AnchorItemRenderer;
import com.breakinblocks.nautec.api.client.renderer.items.PrismarineCrystalItemRenderer;
import com.breakinblocks.nautec.api.fluids.BaseFluidType;
import com.breakinblocks.nautec.api.fluids.NTFluid;
import com.breakinblocks.nautec.client.render.ConduitTapTint;
import com.breakinblocks.nautec.client.render.BioReactorRenderer;
import com.breakinblocks.nautec.client.render.IndustrialBioReactorRenderer;
import com.breakinblocks.nautec.client.render.JsonMesh;
import com.breakinblocks.nautec.client.render.RifleArmPose;
import com.breakinblocks.nautec.client.render.WaveJetClientExtensions;
import com.breakinblocks.nautec.client.teleport.TeleportFadeRenderer;
import com.breakinblocks.nautec.client.hud.DivingSuitOverlay;
import com.breakinblocks.nautec.client.hud.SubmarineAbilityBarOverlay;
import com.breakinblocks.nautec.client.hud.SubmarineHudOverlay;
import com.breakinblocks.nautec.client.hud.PrismMonocleOverlay;
import com.breakinblocks.nautec.client.item.AbilityEnabledProperty;
import com.breakinblocks.nautec.client.particle.DriftingMoteParticle;
import com.breakinblocks.nautec.client.particle.ShockwaveRingParticle;
import com.breakinblocks.nautec.client.particle.SparkParticle;
import com.breakinblocks.nautec.client.particle.SwirlParticle;
import com.breakinblocks.nautec.client.item.BacteriaColorTintSource;
import com.breakinblocks.nautec.client.item.HasBacteriaProperty;
import com.breakinblocks.nautec.client.model.augment.DolphinFinModel;
import com.breakinblocks.nautec.client.model.augment.GuardianEyeModel;
import com.breakinblocks.nautec.client.model.block.AnchorModel;
import com.breakinblocks.nautec.client.model.block.DrainTopModel;
import com.breakinblocks.nautec.client.model.block.FishingNetModel;
import com.breakinblocks.nautec.client.model.block.RobotArmModel;
import com.breakinblocks.nautec.client.model.block.WhiskModel;
import com.breakinblocks.nautec.client.model.entity.AbyssalMawModel;
import com.breakinblocks.nautec.client.model.entity.LanternJellyModel;
import com.breakinblocks.nautec.client.model.entity.SiltSkipperModel;
import com.breakinblocks.nautec.client.model.entity.VentCrawlerModel;
import com.breakinblocks.nautec.client.renderer.entity.NTMobRenderers;
import com.breakinblocks.nautec.client.renderer.entity.NeptunesTridentRenderer;
import com.breakinblocks.nautec.client.shockwave.ShockwaveCooldownDecorator;
import net.neoforged.neoforge.client.event.RegisterItemDecorationsEvent;
import com.breakinblocks.nautec.client.renderer.entity.SubmarineRenderer;
import com.breakinblocks.nautec.client.renderer.augments.GuardianEyeRenderer;
import com.breakinblocks.nautec.client.renderer.augments.SimpleAugmentRenderer;
import com.breakinblocks.nautec.client.renderer.blockentities.AnchorBERenderer;
import com.breakinblocks.nautec.client.renderer.blockentities.AugmentStationExtensionBERenderer;
import com.breakinblocks.nautec.client.renderer.blockentities.BacterialAnalyzerBERenderer;
import com.breakinblocks.nautec.client.renderer.blockentities.BiomeTankBERenderer;
import com.breakinblocks.nautec.client.renderer.blockentities.ChargerBERenderer;
import com.breakinblocks.nautec.client.renderer.blockentities.ConfinedSpawnerBERenderer;
import com.breakinblocks.nautec.client.renderer.blockentities.CrystalCradleBERenderer;
import com.breakinblocks.nautec.client.renderer.blockentities.DecorativePrismarineCrystalBERenderer;
import com.breakinblocks.nautec.client.renderer.blockentities.FusionControllerBERenderer;
import com.breakinblocks.nautec.client.renderer.blockentities.PrismaticEmitterBERenderer;
import com.breakinblocks.nautec.client.renderer.blockentities.SatelliteArrayBERenderer;
import com.breakinblocks.nautec.client.renderer.blockentities.GatewayBERenderer;
import com.breakinblocks.nautec.client.renderer.blockentities.DrainBERenderer;
import com.breakinblocks.nautec.client.renderer.blockentities.FishingStationBERenderer;
import com.breakinblocks.nautec.client.renderer.blockentities.LaserCraftingMatrixRenderer;
import com.breakinblocks.nautec.client.renderer.blockentities.MixerBERenderer;
import com.breakinblocks.nautec.client.renderer.blockentities.PrismarineCrystalBERenderer;
import com.breakinblocks.nautec.client.renderer.robotArms.ClawRobotArmRenderer;
import com.breakinblocks.nautec.client.screen.AugmentationStationExtensionScreen;
import com.breakinblocks.nautec.client.screen.AdvancedBacterialAnalyzerScreen;
import com.breakinblocks.nautec.client.screen.BacterialAnalyzerScreen;
import com.breakinblocks.nautec.client.screen.GraftingStationScreen;
import com.breakinblocks.nautec.client.screen.BioReactorScreen;
import com.breakinblocks.nautec.client.screen.IndustrialBioReactorScreen;
import com.breakinblocks.nautec.client.screen.ConfinedSpawnerScreen;
import com.breakinblocks.nautec.client.screen.CombustionDynamoScreen;
import com.breakinblocks.nautec.client.screen.FusionControllerScreen;
import com.breakinblocks.nautec.client.screen.SatelliteArrayScreen;
import com.breakinblocks.nautec.client.screen.CrateScreen;
import com.breakinblocks.nautec.client.screen.FishingStationScreen;
import com.breakinblocks.nautec.client.screen.IncubatorScreen;
import com.breakinblocks.nautec.client.screen.LaserCraftingMatrixScreen;
import com.breakinblocks.nautec.client.screen.MixerScreen;
import com.breakinblocks.nautec.client.screen.MutatorScreen;
import com.breakinblocks.nautec.client.screen.SubmarineModuleScreen;
import com.breakinblocks.nautec.client.renderer.augments.helper.AugmentLayerRenderer;
import com.breakinblocks.nautec.client.renderer.augments.helper.AugmentSlotsRenderer;
import com.breakinblocks.nautec.registries.NTAugmentSlots;
import com.breakinblocks.nautec.registries.NTAugments;
import com.breakinblocks.nautec.registries.NTBlockEntityTypes;
import com.breakinblocks.nautec.registries.NTBlocks;
import com.breakinblocks.nautec.registries.NTEntities;
import com.breakinblocks.nautec.registries.NTFluids;
import com.breakinblocks.nautec.registries.NTItems;
import com.breakinblocks.nautec.registries.NTMenuTypes;
import com.breakinblocks.nautec.registries.NTParticles;
import com.breakinblocks.nautec.client.ArmorModelsHandler;
import net.minecraft.client.Camera;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.client.renderer.entity.ThrownTridentRenderer;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.fog.environment.FogEnvironment;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.util.ARGB;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.PlayerModelType;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterConditionalItemModelPropertyEvent;
import net.neoforged.neoforge.client.event.RegisterFluidModelsEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.client.event.RegisterRenderPipelinesEvent;
import net.neoforged.neoforge.client.event.RegisterSpecialModelRendererEvent;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.client.fluid.FluidTintSources;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.fluids.FluidType;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector4f;
import org.joml.Vector4i;
import net.minecraft.client.renderer.block.FluidModel;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.client.event.RegisterRenderPipelinesEvent;
import net.minecraft.client.renderer.entity.FishingHookRenderer;
import com.breakinblocks.nautec.client.render.NTRenderPipelines;
import com.breakinblocks.nautec.client.render.ShaderPackOverlay;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.event.AddPackFindersEvent;

@Mod(value = NautecClient.MODID, dist = Dist.CLIENT)
public final class NautecClient {
    public static final String MODID = "nautec";

    public NautecClient(IEventBus modEventBus, ModContainer container) {
        container.registerConfig(ModConfig.Type.CLIENT, NTClientConfig.SPEC);
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);

        modEventBus.addListener(this::registerBERenderers);
        modEventBus.addListener(this::registerGuiOverlays);
        modEventBus.addListener(this::registerClientExtensions);
        modEventBus.addListener(this::registerClientReloadListeners);
        modEventBus.addListener(this::registerLayerDefinitions);
        modEventBus.addListener(this::registerMenus);
        modEventBus.addListener(this::registerColorHandlers);
        modEventBus.addListener(this::registerBlockTints);
        modEventBus.addListener(this::onLayersAdded);
        modEventBus.addListener(this::registerSpecialModelRenderers);
        modEventBus.addListener(this::registerConditionalItemModelProperties);
        modEventBus.addListener(this::registerFluidModels);
        modEventBus.addListener(this::registerParticleProviders);
        modEventBus.addListener(this::registerRenderPipelines);
        modEventBus.addListener(this::registerItemDecorations);
        modEventBus.addListener(this::addPackFinders);
        modEventBus.addListener(FMLClientSetupEvent.class, event -> event.enqueueWork(ShaderPackOverlay::init));
    }

    private void addPackFinders(AddPackFindersEvent event) {
        if (event.getPackType() == PackType.CLIENT_RESOURCES && ModList.get().isLoaded("fusion")) {
            event.addPackFinders(Nautec.rl("resourcepacks/fusion_connected"), PackType.CLIENT_RESOURCES,
                    Component.literal("NauTec Connected Textures"), PackSource.BUILT_IN, true, Pack.Position.TOP);
        }
    }

    private void registerRenderPipelines(RegisterRenderPipelinesEvent event) {
        event.registerPipeline(NTRenderPipelines.SONAR_HIGHLIGHT);
        event.registerPipeline(NTRenderPipelines.SONAR_WAVE);
        event.registerPipeline(NTRenderPipelines.TELEPORT_BLUR);
        event.registerPipeline(NTRenderPipelines.TIDAL_SHOCKWAVE);
        event.registerPipeline(NTRenderPipelines.REACTOR_GLOW);
        event.registerPipeline(NTRenderPipelines.CRYSTAL_SHELL);
        event.registerPipeline(NTRenderPipelines.CRYSTAL_CORE);
        event.registerPipeline(NTRenderPipelines.CRYSTAL_HALO);
        event.registerPipeline(NTRenderPipelines.GATEWAY_GLOW);
        event.registerPipeline(NTRenderPipelines.GATEWAY_HORIZON);
        event.registerPipeline(NTRenderPipelines.FUSION_PLASMA);
        event.registerPipeline(NTRenderPipelines.FUSION_FIELD);
    }

    private void registerParticleProviders(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(NTParticles.VENT_BUBBLE.get(),
                sprites -> new DriftingMoteParticle.Provider(sprites, 0.85F, 0.86F, 0.88F, 0.055F, 0.09F, 24, 44));
        event.registerSpriteSet(NTParticles.GLOW_SPORE.get(),
                sprites -> new DriftingMoteParticle.Provider(sprites, 0.42F, 0.95F, 0.82F, 0.012F, 0.07F, 60, 110));
        event.registerSpriteSet(NTParticles.THRUSTER_WAKE.get(),
                sprites -> new DriftingMoteParticle.Provider(sprites, 0.72F, 0.92F, 0.98F, 0.02F, 0.12F, 14, 26));
        event.registerSpriteSet(NTParticles.BOOST_TRAIL.get(),
                sprites -> new DriftingMoteParticle.Provider(sprites, 0.24F, 0.99F, 1.0F, 0.01F, 0.16F, 10, 20));
        event.registerSpriteSet(NTParticles.CRYSTAL_MOTE.get(),
                sprites -> new DriftingMoteParticle.Provider(sprites, 0.45F, 0.93F, 1.0F, 0.018F, 0.06F, 50, 90));
        event.registerSpriteSet(NTParticles.SONAR_MOTE.get(),
                sprites -> new DriftingMoteParticle.Provider(sprites, 0.38F, 1.0F, 0.75F, 0.015F, 0.08F, 40, 70));
        event.registerSpriteSet(NTParticles.SHIELD_RING.get(),
                sprites -> new ShockwaveRingParticle.Provider(sprites, 0.43F, 0.75F, 1.0F, 0.5F, 6.0F, 12));
        event.registerSpriteSet(NTParticles.TELEPORT_SWIRL.get(),
                sprites -> new SwirlParticle.Provider(sprites, 0.80F, 0.48F, 1.0F, 3.0D, 0.03D, 0.35F, 0.12F, 20, 34));
        event.registerSpriteSet(NTParticles.LASER_SPARK.get(),
                sprites -> new SparkParticle.Provider(sprites, 0.6F, 0.94F, 1.0F, 0.12F, 0.11F, 5, 12));

        event.registerSpriteSet(NTParticles.ABYSSAL_MOTE.get(),
                sprites -> new DriftingMoteParticle.Provider(sprites, 0.30F, 0.42F, 0.58F, -0.004F, 0.06F, 70, 130));
    }

    private void registerGuiOverlays(RegisterGuiLayersEvent event) {
        event.registerAboveAll(Nautec.rl("scanner_info_overlay"), PrismMonocleOverlay.HUD);
        event.registerAboveAll(Nautec.rl("diving_suit_overlay"), DivingSuitOverlay::render);
        event.registerAboveAll(Nautec.rl("submarine_power_overlay"), SubmarineHudOverlay::render);
        event.registerAboveAll(Nautec.rl("submarine_ability_bar"), SubmarineAbilityBarOverlay::render);
        event.registerAboveAll(Nautec.rl("submarine_teleport_fade"), TeleportFadeRenderer::render);
    }

    private void registerClientExtensions(RegisterClientExtensionsEvent event) {
        for (NTFluid fluid : NTFluids.HELPER.getFluids()) {
            FluidType fluidType = fluid.getFluidType().get();
            if (fluidType instanceof BaseFluidType baseFluidType) {
                event.registerFluidType(new IClientFluidTypeExtensions() {
                    @Override
                    public void modifyFogColor(Camera camera, float partialTick, ClientLevel level, int renderDistance, float darkenWorldAmount, Vector4f fluidFogColor) {
                        Vector4i color = baseFluidType.getColor();
                        fluidFogColor.set(color.x / 255f, color.y / 255f, color.z / 255f, fluidFogColor.w);
                    }

                    @Override
                    public void modifyFogRender(Camera camera, @Nullable FogEnvironment environment, float renderDistance, float partialTick, FogData fogData) {
                        fogData.environmentalStart = 1f;
                        fogData.environmentalEnd = 6f;
                    }
                }, fluidType);
            }
        }

        event.registerItem(new IClientItemExtensions() {
            @Override
            public Model getHumanoidArmorModel(ItemStack itemStack, EquipmentClientInfo.LayerType layerType, Model original) {
                return ArmorModelsHandler.armorModel(ArmorModelsHandler.divingSuit, EquipmentSlot.HEAD);
            }
        }, NTItems.DIVING_HELMET);

        event.registerItem(new IClientItemExtensions() {
            @Override
            public HumanoidModel.ArmPose getArmPose(LivingEntity entity, InteractionHand hand, ItemStack stack) {
                return RifleArmPose.RIFLE.getValue();
            }
        }, NTItems.ATLANTEAN_RIFLE);
        event.registerItem(new WaveJetClientExtensions(), NTItems.WAVE_JET);
    }

    private void registerFluidModels(RegisterFluidModelsEvent event) {
        for (NTFluid fluid : NTFluids.HELPER.getFluids()) {
            FluidType fluidType = fluid.getFluidType().get();
            if (fluidType instanceof BaseFluidType baseFluidType) {
                Vector4i color = baseFluidType.getColor();
                int tint = ARGB.color(color.w, color.x, color.y, color.z);
                Identifier overlay = baseFluidType.getOverlayTexture();
                event.register(new FluidModel.Unbaked(
                        new Material(baseFluidType.getStillTexture()),
                        new Material(baseFluidType.getFlowingTexture()),
                        overlay != null ? new Material(overlay) : null,
                        FluidTintSources.constant(tint)
                ), fluid.stillFluid, fluid.flowingFluid);
            }
        }
    }

    private void registerItemDecorations(RegisterItemDecorationsEvent event) {
        event.register(NTItems.NEPTUNES_TRIDENT.get(), new ShockwaveCooldownDecorator());
    }

    private void registerSpecialModelRenderers(RegisterSpecialModelRendererEvent event) {
        event.register(Nautec.rl("prismarine_crystal"), PrismarineCrystalItemRenderer.Unbaked.MAP_CODEC);
        event.register(Nautec.rl("anchor"), AnchorItemRenderer.Unbaked.MAP_CODEC);
    }

    private void registerConditionalItemModelProperties(RegisterConditionalItemModelPropertyEvent event) {
        event.register(Nautec.rl("ability_enabled"), AbilityEnabledProperty.MAP_CODEC);
        event.register(Nautec.rl("has_bacteria"), HasBacteriaProperty.MAP_CODEC);
    }

    private void registerBERenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(NTEntities.THROWN_BOUNCING_TRIDENT.get(), ThrownTridentRenderer::new);
        event.registerEntityRenderer(NTEntities.THROWN_SPREADING_TRIDENT.get(), ThrownTridentRenderer::new);
        event.registerEntityRenderer(NTEntities.NEPTUNES_TRIDENT.get(), NeptunesTridentRenderer::new);
        event.registerEntityRenderer(NTEntities.NAUTEC_FISHING_HOOK.get(), FishingHookRenderer::new);
        event.registerEntityRenderer(NTEntities.SUBMARINE.get(), SubmarineRenderer::new);
        event.registerEntityRenderer(NTEntities.EYE_OF_THE_SEA.get(), context -> new ThrownItemRenderer<>(context, 1.0F, true));
        event.registerEntityRenderer(NTEntities.SILT_SKIPPER.get(), NTMobRenderers.SiltSkipperRenderer::new);
        event.registerEntityRenderer(NTEntities.LANTERN_JELLY.get(), NTMobRenderers.LanternJellyRenderer::new);
        event.registerEntityRenderer(NTEntities.VENT_CRAWLER.get(), NTMobRenderers.VentCrawlerRenderer::new);
        event.registerEntityRenderer(NTEntities.ABYSSAL_MAW.get(), NTMobRenderers.AbyssalMawRenderer::new);
        event.registerBlockEntityRenderer(NTBlockEntityTypes.AQUATIC_CATALYST.get(), LaserBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(NTBlockEntityTypes.PRISMARINE_LASER_RELAY.get(), LaserBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(NTBlockEntityTypes.CREATIVE_POWER_SOURCE.get(), LaserBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(NTBlockEntityTypes.LASER_JUNCTION.get(), LaserBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(NTBlockEntityTypes.ENERGY_CONVERTER.get(), LaserBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(NTBlockEntityTypes.BACTERIAL_FUEL_CELL.get(), LaserBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(NTBlockEntityTypes.FOCUSING_LENS.get(), LaserBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(NTBlockEntityTypes.PRISMARINE_CRYSTAL.get(), PrismarineCrystalBERenderer::new);
        event.registerBlockEntityRenderer(NTBlockEntityTypes.PRISMARINE_CRYSTAL_PART.get(), LaserBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(NTBlockEntityTypes.DECORATIVE_PRISMARINE_CRYSTAL.get(), DecorativePrismarineCrystalBERenderer::new);
        event.registerBlockEntityRenderer(NTBlockEntityTypes.GATEWAY.get(), GatewayBERenderer::new);
        event.registerBlockEntityRenderer(NTBlockEntityTypes.FUSION_CONTROLLER.get(), FusionControllerBERenderer::new);
        event.registerBlockEntityRenderer(NTBlockEntityTypes.PRISMATIC_EMITTER.get(), PrismaticEmitterBERenderer::new);
        event.registerBlockEntityRenderer(NTBlockEntityTypes.SATELLITE_ARRAY.get(), SatelliteArrayBERenderer::new);
        event.registerBlockEntityRenderer(NTBlockEntityTypes.RESONANCE_NODE.get(), ResonanceNodeBERenderer::new);
        event.registerBlockEntityRenderer(NTBlockEntityTypes.CONDUIT_BEACON.get(), LaserBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(NTBlockEntityTypes.LASER_INJECTOR.get(), LaserBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(NTBlockEntityTypes.MIXER.get(), MixerBERenderer::new);
        event.registerBlockEntityRenderer(NTBlockEntityTypes.LASER_CRAFTING_MATRIX.get(), LaserCraftingMatrixRenderer::new);
        event.registerBlockEntityRenderer(NTBlockEntityTypes.RESONANT_VAULT.get(), ResonantVaultRenderer::new);
        event.registerBlockEntityRenderer(NTBlockEntityTypes.RESONANT_CISTERN.get(), ResonantCisternRenderer::new);
        event.registerBlockEntityRenderer(NTBlockEntityTypes.CHARGER.get(), ChargerBERenderer::new);
        event.registerBlockEntityRenderer(NTBlockEntityTypes.BIOME_TANK.get(), BiomeTankBERenderer::new);
        event.registerBlockEntityRenderer(NTBlockEntityTypes.CONFINED_SPAWNER.get(), ConfinedSpawnerBERenderer::new);
        event.registerBlockEntityRenderer(NTBlockEntityTypes.CRYSTAL_CRADLE.get(), CrystalCradleBERenderer::new);
        event.registerBlockEntityRenderer(NTBlockEntityTypes.DRAIN.get(), DrainBERenderer::new);
        event.registerBlockEntityRenderer(NTBlockEntityTypes.DRAIN_PART.get(), LaserBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(NTBlockEntityTypes.AUGMENTATION_STATION_EXTENSION.get(), AugmentStationExtensionBERenderer::new);
        event.registerBlockEntityRenderer(NTBlockEntityTypes.ANCHOR.get(), AnchorBERenderer::new);
        event.registerBlockEntityRenderer(NTBlockEntityTypes.BACTERIAL_ANALYZER.get(), BacterialAnalyzerBERenderer::new);
        event.registerBlockEntityRenderer(NTBlockEntityTypes.FISHING_STATION.get(), FishingStationBERenderer::new);
        event.registerBlockEntityRenderer(NTBlockEntityTypes.BIO_REACTOR.get(), BioReactorRenderer::new);
        event.registerBlockEntityRenderer(NTBlockEntityTypes.INDUSTRIAL_BIO_REACTOR.get(), IndustrialBioReactorRenderer::new);

        AugmentLayerRenderer.registerRenderer(NTAugments.DOLPHIN_FIN.get(),
                ctx -> new SimpleAugmentRenderer<>(DolphinFinModel::new, DolphinFinModel.LAYER_LOCATION, DolphinFinModel.RENDER_TYPE, true, ctx));
        AugmentLayerRenderer.registerRenderer(NTAugments.GUARDIAN_EYE.get(), GuardianEyeRenderer::new);
        AugmentStationExtensionBERenderer.registerRenderer(NTItems.CLAW_ROBOT_ARM.get(), ClawRobotArmRenderer::new);

        AugmentSlotsRenderer.registerAugmentSlotModelPart(NTAugmentSlots.HEAD, model -> model.head);
        AugmentSlotsRenderer.registerAugmentSlotModelPart(NTAugmentSlots.EYES, model -> model.head);
        AugmentSlotsRenderer.registerAugmentSlotModelPart(NTAugmentSlots.LEFT_ARM, model -> model.leftArm);
        AugmentSlotsRenderer.registerAugmentSlotModelPart(NTAugmentSlots.RIGHT_ARM, model -> model.rightArm);
        AugmentSlotsRenderer.registerAugmentSlotModelPart(NTAugmentSlots.LEFT_LEG, model -> model.leftLeg);
        AugmentSlotsRenderer.registerAugmentSlotModelPart(NTAugmentSlots.RIGHT_LEG, model -> model.rightLeg);
        AugmentSlotsRenderer.registerAugmentSlotModelPart(NTAugmentSlots.BODY, model -> model.body);
    }

    private void registerClientReloadListeners(AddClientReloadListenersEvent event) {
        event.addListener(Nautec.rl("augment_renderers"), (ResourceManagerReloadListener) resourceManager -> {
            AugmentLayerRenderer.createRenderers();
            AugmentStationExtensionBERenderer.createRenderers();
        });
        event.addListener(Nautec.rl("meshes"), (ResourceManagerReloadListener) JsonMesh::reloadAll);
    }

    private void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(DrainTopModel.LAYER_LOCATION, DrainTopModel::createBodyLayer);
        event.registerLayerDefinition(AnchorModel.LAYER_LOCATION, AnchorModel::createBodyLayer);
        event.registerLayerDefinition(FishingNetModel.LAYER_LOCATION, FishingNetModel::createBodyLayer);
        event.registerLayerDefinition(WhiskModel.LAYER_LOCATION, WhiskModel::createBodyLayer);
        event.registerLayerDefinition(RobotArmModel.LAYER_LOCATION, RobotArmModel::createBodyLayer);
        event.registerLayerDefinition(DolphinFinModel.LAYER_LOCATION, DolphinFinModel::createBodyLayer);
        event.registerLayerDefinition(GuardianEyeModel.LAYER_LOCATION, GuardianEyeModel::createBodyLayer);
        event.registerLayerDefinition(NTMobRenderers.SILT_SKIPPER_LAYER, SiltSkipperModel::createBodyLayer);
        event.registerLayerDefinition(NTMobRenderers.LANTERN_JELLY_LAYER, LanternJellyModel::createBodyLayer);
        event.registerLayerDefinition(NTMobRenderers.VENT_CRAWLER_LAYER, VentCrawlerModel::createBodyLayer);
        event.registerLayerDefinition(NTMobRenderers.ABYSSAL_MAW_LAYER, AbyssalMawModel::createBodyLayer);
        ArmorModelsHandler.registerLayers(event);
    }

    private void onLayersAdded(EntityRenderersEvent.AddLayers event) {
        for (PlayerModelType skin : event.getSkins()) {
            AvatarRenderer<?> renderer = event.getPlayerRenderer(skin);
            if (renderer != null) {
                renderer.addLayer(new AugmentLayerRenderer<>(renderer));
            }
        }
    }

    private void registerMenus(RegisterMenuScreensEvent event) {
        event.register(NTMenuTypes.CRATE.get(), CrateScreen::new);
        event.register(NTMenuTypes.AUGMENT_STATION_EXTENSION.get(), AugmentationStationExtensionScreen::new);

        event.register(NTMenuTypes.FISHING_STATION.get(), FishingStationScreen::new);
        event.register(NTMenuTypes.INCUBATOR.get(), IncubatorScreen::new);
        event.register(NTMenuTypes.MUTATOR.get(), MutatorScreen::new);
        event.register(NTMenuTypes.BIO_REACTOR.get(), BioReactorScreen::new);
        event.register(NTMenuTypes.INDUSTRIAL_BIO_REACTOR.get(), IndustrialBioReactorScreen::new);
        event.register(NTMenuTypes.MIXER.get(), MixerScreen::new);
        event.register(NTMenuTypes.LASER_CRAFTING_MATRIX.get(), LaserCraftingMatrixScreen::new);
        event.register(NTMenuTypes.BACTERIAL_ANALYZER.get(), BacterialAnalyzerScreen::new);
        event.register(NTMenuTypes.GRAFTING_STATION.get(), GraftingStationScreen::new);
        event.register(NTMenuTypes.ADVANCED_BACTERIAL_ANALYZER.get(), AdvancedBacterialAnalyzerScreen::new);
        event.register(NTMenuTypes.DISTRIBUTOR.get(), DistributorScreen::new);
        event.register(NTMenuTypes.CONDUIT_TAP.get(), ConduitTapScreen::new);
        event.register(NTMenuTypes.RESONANT_VAULT.get(), ResonantVaultScreen::new);
        event.register(NTMenuTypes.RESONANT_CISTERN.get(), ResonantCisternScreen::new);
        event.register(NTMenuTypes.BUBBLE_ANCHOR.get(), BubbleAnchorScreen::new);
        event.register(NTMenuTypes.ENERGY_CONVERTER.get(), EnergyConverterScreen::new);
        event.register(NTMenuTypes.PRESSURE_FORGE.get(), PressureForgeScreen::new);
        event.register(NTMenuTypes.COLONY_REPLICATOR.get(), ColonyReplicatorScreen::new);
        event.register(NTMenuTypes.SUBMARINE_MODULES.get(), SubmarineModuleScreen::new);
        event.register(NTMenuTypes.CONFINED_SPAWNER.get(), ConfinedSpawnerScreen::new);
        event.register(NTMenuTypes.FUSION_CONTROLLER.get(), FusionControllerScreen::new);
        event.register(NTMenuTypes.COMBUSTION_DYNAMO.get(), CombustionDynamoScreen::new);
        event.register(NTMenuTypes.SATELLITE_ARRAY.get(), SatelliteArrayScreen::new);
        event.register(NTMenuTypes.RESONANCE_NODE.get(), ResonanceNodeScreen::new);
        event.register(NTMenuTypes.DISH_STORAGE.get(), DishStorageScreen::new);
    }

    private void registerColorHandlers(RegisterColorHandlersEvent.ItemTintSources event) {
        event.register(Nautec.rl("bacteria_color"), BacteriaColorTintSource.MAP_CODEC);
    }

    private void registerBlockTints(RegisterColorHandlersEvent.BlockTintSources event) {
        event.register(ConduitTapTint.sources(), NTBlocks.CONDUIT_TAP.get());
    }

}
