package com.breakinblocks.nautec;

import net.neoforged.neoforge.common.world.chunk.RegisterTicketControllersEvent;
import com.breakinblocks.nautec.content.resonance.ResonanceChunkLoading;
import com.breakinblocks.nautec.registries.NTFeatures;
import com.mojang.logging.LogUtils;
import com.breakinblocks.nautec.compat.starcatcher.StarcatcherCompat;
import com.breakinblocks.nautec.api.augments.AugmentSlot;
import com.breakinblocks.nautec.api.augments.AugmentType;
import com.breakinblocks.nautec.api.bacteria.Bacteria;
import com.breakinblocks.nautec.api.blockentities.ContainerBlockEntity;
import com.breakinblocks.nautec.api.blockentities.LaserBlockEntity;
import com.breakinblocks.nautec.api.items.IBacteriaItem;
import com.breakinblocks.nautec.api.items.ICurioItem;
import com.breakinblocks.nautec.api.items.IFluidItem;
import com.breakinblocks.nautec.api.items.IPowerItem;
import com.breakinblocks.nautec.capabilities.NTCapabilities;
import com.breakinblocks.nautec.capabilities.bacteria.ItemBacteriaWrapper;
import com.breakinblocks.nautec.capabilities.fluid.DivingSuitAirHandler;
import com.breakinblocks.nautec.capabilities.power.ItemPowerWrapper;
import com.breakinblocks.nautec.capabilities.power.LaserPowerView;
import com.breakinblocks.nautec.content.commands.arguments.AugmentSlotArgumentType;
import com.breakinblocks.nautec.content.commands.arguments.AugmentTypeArgumentType;
import com.breakinblocks.nautec.data.NTDataAttachments;
import com.breakinblocks.nautec.data.NTDataComponents;
import com.breakinblocks.nautec.data.NTDataMaps;
import com.breakinblocks.nautec.data.generated.GeneratedPackFinder;
import com.breakinblocks.nautec.registries.NTAliases;
import com.breakinblocks.nautec.registries.NTArgumentTypes;
import com.breakinblocks.nautec.registries.NTAttachmentTypes;
import com.breakinblocks.nautec.registries.NTAugmentSlots;
import com.breakinblocks.nautec.registries.NTAugments;
import com.breakinblocks.nautec.registries.NTBacteriaSerializers;
import com.breakinblocks.nautec.registries.NTBacteriaStatsSerializers;
import com.breakinblocks.nautec.registries.NTBlockEntityTypes;
import com.breakinblocks.nautec.registries.NTBlocks;
import com.breakinblocks.nautec.registries.NTConditions;
import com.breakinblocks.nautec.registries.NTCreativeTabs;
import com.breakinblocks.nautec.registries.NTCriteriaTriggers;
import com.breakinblocks.nautec.registries.NTEntities;
import com.breakinblocks.nautec.registries.NTFluids;
import com.breakinblocks.nautec.registries.NTItems;
import com.breakinblocks.nautec.registries.NTLootConditions;
import com.breakinblocks.nautec.registries.NTLootFunctions;
import com.breakinblocks.nautec.registries.NTLootModifier;
import com.breakinblocks.nautec.registries.NTMenuTypes;
import com.breakinblocks.nautec.registries.NTMobEffects;
import com.breakinblocks.nautec.registries.NTMultiblocks;
import com.breakinblocks.nautec.registries.NTParticles;
import com.breakinblocks.nautec.registries.NTRecipes;
import com.breakinblocks.nautec.registries.NTSounds;
import com.breakinblocks.nautec.registries.NTStructures;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.event.AddPackFindersEvent;
import net.neoforged.neoforge.transfer.fluid.ItemAccessFluidHandler;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.neoforged.neoforge.registries.RegisterEvent;
import net.neoforged.neoforge.registries.datamaps.RegisterDataMapTypesEvent;
import org.slf4j.Logger;
import top.theillusivec4.curios.api.CuriosCapability;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurio;

import java.util.Objects;
import java.nio.file.Files;
import java.nio.file.Path;
import java.io.IOException;
import net.neoforged.fml.loading.FMLPaths;
import java.util.stream.Collectors;

@Mod(Nautec.MODID)
public final class Nautec {
    public static final String MODID = "nautec";
    public static final String MODNAME = "NauTec";
    public static final Logger LOGGER = LogUtils.getLogger();

    public Nautec(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(NewRegistryEvent.class, event -> {
            event.register(NTRegistries.MULTIBLOCK);
            event.register(NTRegistries.AUGMENT_SLOT);
            event.register(NTRegistries.AUGMENT_TYPE);
            event.register(NTRegistries.BACTERIA_SERIALIZER);
            event.register(NTRegistries.BACTERIA_STATS_SERIALIZER);
        });

        modEventBus.addListener(DataPackRegistryEvent.NewRegistry.class, event -> {
            event.dataPackRegistry(NTRegistries.BACTERIA_KEY, Bacteria.CODEC, Bacteria.CODEC);
        });

        modEventBus.addListener(AddPackFindersEvent.class, GeneratedPackFinder::onAddPackFinders);
        modEventBus.addListener(RegisterTicketControllersEvent.class, ResonanceChunkLoading::register);

        NTAliases.register();
        NTEntities.ENTITIES.register(modEventBus);
        NTItems.ITEMS.register(modEventBus);
        NTBlocks.BLOCKS.register(modEventBus);
        NTParticles.PARTICLE_TYPES.register(modEventBus);
        NTCriteriaTriggers.TRIGGERS.register(modEventBus);
        NTRecipes.SERIALIZERS.register(modEventBus);
        NTRecipes.TYPES.register(modEventBus);
        NTDataAttachments.ATTACHMENTS.register(modEventBus);
        NTArgumentTypes.ARGUMENT_TYPES.register(modEventBus);
        NTBlockEntityTypes.BLOCK_ENTITIES.register(modEventBus);
        NTCreativeTabs.CREATIVE_MODE_TABS.register(modEventBus);
        NTDataComponents.DATA_COMPONENT_TYPES.register(modEventBus);
        NTAttachmentTypes.ATTACHMENT_TYPES.register(modEventBus);
        NTMultiblocks.MULTIBLOCKS.register(modEventBus);
        NTAugments.AUGMENTS.register(modEventBus);
        NTAugmentSlots.AUGMENT_SLOTS.register(modEventBus);
        NTMenuTypes.MENUS.register(modEventBus);
        NTMobEffects.MOB_EFFECTS.register(modEventBus);
        NTSounds.SOUNDS.register(modEventBus);
        NTStructures.STRUCTURES.register(modEventBus);
        NTStructures.STRUCTURE_PIECES.register(modEventBus);
        NTFeatures.FEATURES.register(modEventBus);
        NTLootModifier.LOOT_MODIFIERS.register(modEventBus);
        NTLootConditions.LOOT_CONDITIONS.register(modEventBus);
        NTConditions.CONDITION_CODECS.register(modEventBus);
        NTLootFunctions.LOOT_FUNCTIONS.register(modEventBus);
        NTBacteriaSerializers.SERIALIZERS.register(modEventBus);
        NTBacteriaStatsSerializers.SERIALIZERS.register(modEventBus);

        NTFluids.HELPER.register(modEventBus);

        modEventBus.addListener(this::registerDataMaps);
        modEventBus.addListener(this::onRegisterAugments);
        modEventBus.addListener(this::registerCapabilities);

        preserveWorldgenConfig();
        StarcatcherCompat.init();
        modContainer.registerConfig(ModConfig.Type.COMMON, NTConfig.WORLDGEN_SPEC, "nautec-worldgen.toml");
        modContainer.registerConfig(ModConfig.Type.SERVER, NTConfig.SPEC, "nautec-common.toml");
    }

    private static void preserveWorldgenConfig() {
        Path config = FMLPaths.CONFIGDIR.get();
        Path previous = config.resolve("nautec-common.toml");
        Path worldgen = config.resolve("nautec-worldgen.toml");
        if (Files.isRegularFile(previous) && !Files.exists(worldgen)) {
            try {
                Files.copy(previous, worldgen);
            } catch (IOException exception) {
                throw new IllegalStateException("Cannot preserve Nautec worldgen settings", exception);
            }
        }
    }

    private void registerDataMaps(RegisterDataMapTypesEvent event) {
        event.register(NTDataMaps.BACTERIA_OBTAINING);
        event.register(NTDataMaps.STORAGE_BLOCKS);
    }

    private void onRegisterAugments(RegisterEvent event) {
        Registry<AugmentSlot> slotRegistry = event.getRegistry(NTRegistries.AUGMENT_SLOT.key());
        if (slotRegistry != null) {
            AugmentSlotArgumentType.suggestions = slotRegistry.keySet().stream().map(Objects::toString).collect(Collectors.toSet());
        }

        Registry<AugmentType<?>> augmentRegistry = event.getRegistry(NTRegistries.AUGMENT_TYPE.key());
        if (augmentRegistry != null) {
            AugmentTypeArgumentType.suggestions = augmentRegistry.keySet().stream().map(Objects::toString).collect(Collectors.toSet());
        }
    }

    private void registerCapabilities(RegisterCapabilitiesEvent event) {
        registerItemCaps(event);
        registerBECaps(event);
        registerEntityCaps(event);
    }

    private static void registerEntityCaps(RegisterCapabilitiesEvent event) {
        event.registerEntity(NTCapabilities.PowerStorage.ENTITY, NTEntities.SUBMARINE.get(),
                (submarine, dir) -> submarine.getPowerStorage());
    }

    private static void registerItemCaps(RegisterCapabilitiesEvent event) {
        event.registerItem(Capabilities.Fluid.ITEM, (stack, access) -> new DivingSuitAirHandler(access), NTItems.DIVING_CHESTPLATE.get());

        for (Item item : BuiltInRegistries.ITEM) {
            if (item instanceof IPowerItem powerItem) {
                event.registerItem(NTCapabilities.PowerStorage.ITEM, (stack, ctx) -> new ItemPowerWrapper(stack, powerItem), item);
            }

            if (item instanceof IFluidItem fluidItem) {
                event.registerItem(Capabilities.Fluid.ITEM, (stack, access) -> new ItemAccessFluidHandler(access, NTDataComponents.FLUID.get(), fluidItem.getFluidCapacity()), item);
            }

            if (item instanceof IBacteriaItem) {
                event.registerItem(NTCapabilities.BacteriaStorage.ITEM, (stack, ctx) -> new ItemBacteriaWrapper(NTDataComponents.BACTERIA, stack), item);
            }

            if (item instanceof ICurioItem curioItem) {
                event.registerItem(CuriosCapability.ITEM,
                        (stack, context) -> new ICurio() {
                            @Override
                            public ItemStack getStack() {
                                return stack;
                            }

                            @Override
                            public void curioTick(SlotContext slotContext) {
                                curioItem.curioTick(stack, slotContext);
                            }
                        }, item);
            }
        }
    }

    private static void registerBECaps(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.Energy.BLOCK, NTBlockEntityTypes.ENERGY_CONVERTER.get(),
                (blockEntity, dir) -> blockEntity.getFeBuffer());
        event.registerBlockEntity(Capabilities.Energy.BLOCK, NTBlockEntityTypes.CREATIVE_ENERGY_SOURCE.get(),
                (blockEntity, dir) -> blockEntity);
        event.registerBlockEntity(Capabilities.Energy.BLOCK, NTBlockEntityTypes.PRISMATIC_EMITTER.get(),
                (blockEntity, dir) -> blockEntity.getPort());
        event.registerBlockEntity(Capabilities.Energy.BLOCK, NTBlockEntityTypes.SATELLITE_ARRAY.get(),
                (blockEntity, dir) -> blockEntity.getPort());
        event.registerBlockEntity(Capabilities.Energy.BLOCK, NTBlockEntityTypes.RESONANCE_NODE.get(),
                (blockEntity, dir) -> blockEntity.getPort());
        event.registerBlockEntity(Capabilities.Energy.BLOCK, NTBlockEntityTypes.TIDAL_ROTOR.get(),
                (blockEntity, dir) -> blockEntity.getEnergyOutput());
        event.registerBlockEntity(Capabilities.Energy.BLOCK, NTBlockEntityTypes.THERMAL_VENT_TAP.get(),
                (blockEntity, dir) -> blockEntity.getEnergyOutput());
        event.registerBlockEntity(Capabilities.Fluid.BLOCK, NTBlockEntityTypes.THERMAL_VENT_TAP.get(),
                (blockEntity, dir) -> blockEntity.getFuelInput());
        event.registerBlockEntity(Capabilities.Energy.BLOCK, NTBlockEntityTypes.COMBUSTION_DYNAMO.get(),
                (blockEntity, dir) -> blockEntity.getEnergyOutput());
        event.registerBlockEntity(Capabilities.Energy.BLOCK, NTBlockEntityTypes.FUSION_CONTROLLER.get(),
                (blockEntity, dir) -> blockEntity.getEnergyOutput());
        event.registerBlockEntity(Capabilities.Fluid.BLOCK, NTBlockEntityTypes.FUSION_CONTROLLER.get(),
                (blockEntity, dir) -> blockEntity.getFuelInput());
        event.registerBlockEntity(Capabilities.Energy.BLOCK, NTBlockEntityTypes.FUSION_PORT.get(),
                (blockEntity, dir) -> blockEntity.getEnergy());
        event.registerBlockEntity(Capabilities.Fluid.BLOCK, NTBlockEntityTypes.FUSION_PORT.get(),
                (blockEntity, dir) -> blockEntity.getFuel());
        event.registerBlockEntity(Capabilities.Item.BLOCK, NTBlockEntityTypes.RESONANT_VAULT.get(),
                (blockEntity, dir) -> blockEntity.itemHandler(dir));
        event.registerBlockEntity(Capabilities.Fluid.BLOCK, NTBlockEntityTypes.RESONANT_CISTERN.get(),
                (blockEntity, dir) -> blockEntity.fluidHandler(dir));
        event.registerBlockEntity(Capabilities.Fluid.BLOCK, NTBlockEntityTypes.CONDUIT_TAP.get(),
                (blockEntity, dir) -> blockEntity.fluidSink(dir));
        event.registerBlockEntity(Capabilities.Energy.BLOCK, NTBlockEntityTypes.CONDUIT_TAP.get(),
                (blockEntity, dir) -> blockEntity.energySink(dir));

        for (DeferredHolder<BlockEntityType<?>, ? extends BlockEntityType<?>> be : NTBlockEntityTypes.BLOCK_ENTITIES.getEntries()) {
            Block validBlock = be.get().getValidBlocks().stream().iterator().next();
            BlockEntity testBE = be.get().create(BlockPos.ZERO, validBlock.defaultBlockState());
            if (testBE instanceof ContainerBlockEntity containerBE) {
                if (containerBE instanceof LaserBlockEntity) {
                    event.registerBlockEntity(NTCapabilities.PowerStorage.BLOCK, be.get(), (blockEntity, dir) -> new LaserPowerView((LaserBlockEntity) blockEntity));
                } else if (containerBE.getPowerStorage() != null) {
                    event.registerBlockEntity(NTCapabilities.PowerStorage.BLOCK, be.get(), (blockEntity, dir) -> ((ContainerBlockEntity) blockEntity).getPowerStorage());
                }

                if (containerBE.getItemHandler() != null) {
                    event.registerBlockEntity(Capabilities.Item.BLOCK, be.get(), (blockEntity, dir) -> ((ContainerBlockEntity) blockEntity).getItemHandlerOnSide(dir));
                }

                if (containerBE.getFluidHandler() != null) {
                    event.registerBlockEntity(Capabilities.Fluid.BLOCK, be.get(), (blockEntity, dir) -> ((ContainerBlockEntity) blockEntity).getFluidHandlerOnSide(dir));
                }

                if (containerBE.getBacteriaStorage() != null){
                    event.registerBlockEntity(NTCapabilities.BacteriaStorage.BLOCK, be.get(), (blockEntity, ctx) -> ((ContainerBlockEntity) blockEntity).getBacteriaStorage());
                }
            }
        }
    }

    public static Identifier rl(String path) {
        return Identifier.fromNamespaceAndPath(MODID, path);
    }
}
