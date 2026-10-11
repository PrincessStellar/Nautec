package com.breakinblocks.nautec.data;

import com.breakinblocks.nautec.content.resonantstorage.ResonantLink;
import net.minecraft.world.item.component.ItemContainerContents;
import com.breakinblocks.nautec.content.items.MachineSettings;
import com.mojang.serialization.Codec;
import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.api.gateways.GatewayAddress;
import com.breakinblocks.nautec.api.gateways.PackedGateway;
import com.breakinblocks.nautec.content.items.SeaEyeTarget;
import com.breakinblocks.nautec.data.components.ComponentBacteriaStorage;
import com.breakinblocks.nautec.data.components.ComponentPowerStorage;
import com.breakinblocks.nautec.data.components.ShockwaveCooldown;
import com.breakinblocks.nautec.data.components.TeleportAnchor;
import com.breakinblocks.nautec.content.resonance.ResonanceBinding;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import com.breakinblocks.nautec.data.components.SubmarineModuleState;
import net.minecraft.core.Holder;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.fluids.SimpleFluidContent;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public final class NTDataComponents {
    public static final DeferredRegister.DataComponents DATA_COMPONENT_TYPES = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, Nautec.MODID);

    public static final Supplier<DataComponentType<ComponentPowerStorage>> POWER = registerDataComponentType("power",
            () -> builder -> builder.persistent(ComponentPowerStorage.CODEC).networkSynchronized(ComponentPowerStorage.STREAM_CODEC));
    public static final Supplier<DataComponentType<SimpleFluidContent>> FLUID = registerDataComponentType("fluid",
            () -> builder -> builder.persistent(SimpleFluidContent.CODEC).networkSynchronized(SimpleFluidContent.STREAM_CODEC));
    public static final Supplier<DataComponentType<ComponentBacteriaStorage>> BACTERIA = registerDataComponentType("bacteria",
            () -> builder -> builder
                    .persistent(ComponentBacteriaStorage.CODEC)
                    .networkSynchronized(ComponentBacteriaStorage.STREAM_CODEC));

    public static final Supplier<DataComponentType<Boolean>> ABILITY_ENABLED = registerDataComponentType("ability_enabled",
            () -> builder -> builder.persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL));

    public static final Supplier<DataComponentType<Boolean>> IS_INFUSED = registerDataComponentType("is_infused",
            () -> builder -> builder.persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL));

    public static final Supplier<DataComponentType<ResonanceBinding>> RESONANCE_BINDING = registerDataComponentType("resonance_binding",
            () -> builder -> builder.persistent(ResonanceBinding.CODEC).networkSynchronized(ResonanceBinding.STREAM_CODEC));

    public static final Supplier<DataComponentType<Integer>> WRENCH_MODE = registerDataComponentType("wrench_mode",
            () -> builder -> builder.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.VAR_INT));

    public static final Supplier<DataComponentType<MachineSettings>> MACHINE_SETTINGS = registerDataComponentType("machine_settings",
            () -> builder -> builder.persistent(MachineSettings.CODEC).networkSynchronized(MachineSettings.STREAM_CODEC));

    public static final Supplier<DataComponentType<Integer>> RESONANCE_PRIORITY = registerDataComponentType("resonance_priority",
            () -> builder -> builder.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.VAR_INT));
    public static final Supplier<DataComponentType<Holder<Item>>> BIOME_TANK_PLANT = registerDataComponentType("biome_tank_plant",
            () -> builder -> builder.persistent(Item.CODEC).networkSynchronized(Item.STREAM_CODEC));

    public static final Supplier<DataComponentType<GlobalPos>> TUNED_EMITTER = registerDataComponentType("tuned_emitter",
            () -> builder -> builder.persistent(GlobalPos.CODEC).networkSynchronized(GlobalPos.STREAM_CODEC));

    public static final Supplier<DataComponentType<Boolean>> CULTIVATED = registerDataComponentType("cultivated",
            () -> builder -> builder.persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL));

    public static final Supplier<DataComponentType<Long>> CRADLE_GROWTH = registerDataComponentType("cradle_growth",
            () -> builder -> builder.persistent(Codec.LONG).networkSynchronized(ByteBufCodecs.VAR_LONG));

    public static final Supplier<DataComponentType<Boolean>> CRADLE_SEEDED = registerDataComponentType("cradle_seeded",
            () -> builder -> builder.persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL));

    public static final Supplier<DataComponentType<SubmarineModuleState>> SUBMARINE_MODULE_STATE = registerDataComponentType("submarine_module_state",
            () -> builder -> builder.persistent(SubmarineModuleState.CODEC).networkSynchronized(ByteBufCodecs.fromCodec(SubmarineModuleState.CODEC)));

    public static final Supplier<DataComponentType<ItemContainerContents>> SUBMARINE_CARGO = registerDataComponentType("submarine_cargo",
            () -> builder -> builder.persistent(ItemContainerContents.CODEC).networkSynchronized(ItemContainerContents.STREAM_CODEC));

    public static final Supplier<DataComponentType<Float>> SUBMARINE_HEALTH = registerDataComponentType("submarine_health",
            () -> builder -> builder.persistent(Codec.FLOAT).networkSynchronized(ByteBufCodecs.FLOAT));

    public static final Supplier<DataComponentType<TeleportAnchor>> TELEPORT_ANCHOR = registerDataComponentType("teleport_anchor",
            () -> builder -> builder.persistent(TeleportAnchor.CODEC).networkSynchronized(TeleportAnchor.STREAM_CODEC));

    public static final Supplier<DataComponentType<GatewayAddress>> GATEWAY_ADDRESS = registerDataComponentType("gateway_address",
            () -> builder -> builder.persistent(GatewayAddress.CODEC).networkSynchronized(GatewayAddress.STREAM_CODEC));

    public static final Supplier<DataComponentType<ResonantLink>> RESONANT_LINK = registerDataComponentType("resonant_link",
            () -> builder -> builder.persistent(ResonantLink.CODEC).networkSynchronized(ResonantLink.STREAM_CODEC));

    public static final Supplier<DataComponentType<PackedGateway>> GATEWAY_PACKED = registerDataComponentType("gateway_packed",
            () -> builder -> builder.persistent(PackedGateway.CODEC).networkSynchronized(PackedGateway.STREAM_CODEC));

    public static final Supplier<DataComponentType<ShockwaveCooldown>> SHOCKWAVE_COOLDOWN = registerDataComponentType("shockwave_cooldown",
            () -> builder -> builder.persistent(ShockwaveCooldown.CODEC).networkSynchronized(ShockwaveCooldown.STREAM_CODEC));

    public static final Supplier<DataComponentType<Integer>> OXYGEN = registerDataComponentType("oxygen",
            () -> builder -> builder.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.INT));

    public static final Supplier<DataComponentType<EntityType<?>>> CATCH_ENTITY = registerDataComponentType("catch_entity",
            () -> builder -> builder
                    .persistent(BuiltInRegistries.ENTITY_TYPE.byNameCodec())
                    .networkSynchronized(ByteBufCodecs.registry(Registries.ENTITY_TYPE)));

    public static final Supplier<DataComponentType<SeaEyeTarget>> SEA_EYE_TARGET = registerDataComponentType("sea_eye_target",
            () -> builder -> builder.persistent(SeaEyeTarget.CODEC).networkSynchronized(SeaEyeTarget.STREAM_CODEC));

    public static <T> Supplier<DataComponentType<T>> registerDataComponentType(
            String name, Supplier<UnaryOperator<DataComponentType.Builder<T>>> builderOperator) {
        return DATA_COMPONENT_TYPES.register(name, () -> builderOperator.get().apply(DataComponentType.builder()).build());
    }
}
