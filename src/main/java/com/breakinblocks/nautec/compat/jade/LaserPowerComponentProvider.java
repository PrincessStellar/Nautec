package com.breakinblocks.nautec.compat.jade;

import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.api.blockentities.LaserBlockEntity;
import com.breakinblocks.nautec.content.blockentities.AquaticCatalystBlockEntity;
import com.breakinblocks.nautec.content.blockentities.EnergyConverterBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.StreamServerDataProvider;
import snownee.jade.api.config.IPluginConfig;

import java.util.Locale;

public enum LaserPowerComponentProvider implements StreamServerDataProvider<BlockAccessor, LaserPowerComponentProvider.Data> {
    INSTANCE;

    private static final ResourceLocation UID = Nautec.rl("laser_power");

    public record Data(boolean receives, int received, float receivedPurity, boolean sends, int sent, float sentPurity, int stored, int capacity) {
        public static final StreamCodec<RegistryFriendlyByteBuf, Data> STREAM_CODEC = StreamCodec.of(
                (buf, data) -> {
                    buf.writeBoolean(data.receives());
                    buf.writeVarInt(data.received());
                    buf.writeFloat(data.receivedPurity());
                    buf.writeBoolean(data.sends());
                    buf.writeVarInt(data.sent());
                    buf.writeFloat(data.sentPurity());
                    buf.writeVarInt(data.stored());
                    buf.writeVarInt(data.capacity());
                },
                buf -> new Data(buf.readBoolean(), buf.readVarInt(), buf.readFloat(), buf.readBoolean(), buf.readVarInt(), buf.readFloat(),
                        buf.readVarInt(), buf.readVarInt())
        );
    }

    @Override
    public @Nullable Data streamData(BlockAccessor accessor) {
        if (!(accessor.getBlockEntity() instanceof LaserBlockEntity laser)
                || laser instanceof EnergyConverterBlockEntity || laser instanceof AquaticCatalystBlockEntity) {
            return null;
        }
        boolean sends = laser.sendsAp();
        int capacity = laser.apCapacity();
        if (!sends && capacity <= 0) {
            return null;
        }
        boolean receives = laser.receivesAp() && laser.getRequiredPower() <= 0;
        return new Data(receives, Math.max(0, laser.getPower()), laser.getPurity(), sends, laser.apSent(), laser.apSentPurity(),
                laser.apStored(), capacity);
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, Data> streamCodec() {
        return Data.STREAM_CODEC;
    }

    @Override
    public ResourceLocation getUid() {
        return UID;
    }

    private static String purity(float purity) {
        return String.format(Locale.ROOT, "%.2f", purity);
    }

    public enum Client implements IBlockComponentProvider {
        INSTANCE;

        @Override
        public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
            LaserPowerComponentProvider.INSTANCE.decodeFromData(accessor).ifPresent(data -> {
                if (data.capacity() > 0) {
                    tooltip.add(Component.translatable("nautec.jade.ap.stored", String.format("%,d", data.stored()), String.format("%,d", data.capacity()))
                            .withStyle(ChatFormatting.GRAY));
                }
                if (data.receives()) {
                    tooltip.add(data.received() > 0
                            ? Component.translatable("nautec.jade.ap.receiving", String.format("%,d", data.received()), purity(data.receivedPurity()))
                            : Component.translatable("nautec.jade.ap.receiving.none").withStyle(ChatFormatting.GRAY));
                }
                if (data.sends()) {
                    tooltip.add(data.sent() > 0
                            ? Component.translatable("nautec.jade.ap.sending", String.format("%,d", data.sent()), purity(data.sentPurity()))
                            .withStyle(ChatFormatting.AQUA)
                            : Component.translatable("nautec.jade.ap.sending.none").withStyle(ChatFormatting.GRAY));
                }
            });
        }

        @Override
        public ResourceLocation getUid() {
            return UID;
        }
    }
}
