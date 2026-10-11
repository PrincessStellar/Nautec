package com.breakinblocks.nautec.network;

import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.client.ClientScreenHooks;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record OpenCharmScreenPayload(int hand, Info info) implements CustomPacketPayload {
    public static final int AUGMENT = -1;
    public static final Type<OpenCharmScreenPayload> TYPE = new Type<>(Nautec.rl("open_charm_screen"));

    public record Info(boolean bound, String network, String owner, boolean access, int uplinks, int nodes, int stored, int priority) {
        public static final StreamCodec<ByteBuf, Info> STREAM_CODEC = new StreamCodec<>() {
            @Override
            public Info decode(ByteBuf buf) {
                return new Info(ByteBufCodecs.BOOL.decode(buf), ByteBufCodecs.STRING_UTF8.decode(buf), ByteBufCodecs.STRING_UTF8.decode(buf),
                        ByteBufCodecs.BOOL.decode(buf), ByteBufCodecs.VAR_INT.decode(buf), ByteBufCodecs.VAR_INT.decode(buf),
                        ByteBufCodecs.VAR_INT.decode(buf), ByteBufCodecs.VAR_INT.decode(buf));
            }

            @Override
            public void encode(ByteBuf buf, Info info) {
                ByteBufCodecs.BOOL.encode(buf, info.bound());
                ByteBufCodecs.STRING_UTF8.encode(buf, info.network());
                ByteBufCodecs.STRING_UTF8.encode(buf, info.owner());
                ByteBufCodecs.BOOL.encode(buf, info.access());
                ByteBufCodecs.VAR_INT.encode(buf, info.uplinks());
                ByteBufCodecs.VAR_INT.encode(buf, info.nodes());
                ByteBufCodecs.VAR_INT.encode(buf, info.stored());
                ByteBufCodecs.VAR_INT.encode(buf, info.priority());
            }
        };
    }

    public static final StreamCodec<RegistryFriendlyByteBuf, OpenCharmScreenPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, OpenCharmScreenPayload::hand,
            Info.STREAM_CODEC, OpenCharmScreenPayload::info,
            OpenCharmScreenPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(OpenCharmScreenPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> ClientScreenHooks.openCharmScreen(payload));
    }
}
