package com.breakinblocks.nautec.network;

import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.content.menus.LaserPurificationSmelterMenu;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SmelterAutoDistributePayload(int containerId) implements CustomPacketPayload {
    public static final Type<SmelterAutoDistributePayload> TYPE = new Type<>(Nautec.rl("smelter_auto_distribute"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SmelterAutoDistributePayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, SmelterAutoDistributePayload::containerId,
            SmelterAutoDistributePayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SmelterAutoDistributePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player
                    && player.containerMenu instanceof LaserPurificationSmelterMenu menu
                    && menu.containerId == payload.containerId()
                    && menu.stillValid(player)) {
                menu.blockEntity.setAutoDistribute(!menu.blockEntity.isAutoDistribute());
            }
        });
    }
}
