package com.breakinblocks.nautec.compat.jade;

import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.api.blockentities.BeamScan;
import com.breakinblocks.nautec.content.blockentities.AquaticCatalystBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.StreamServerDataProvider;
import snownee.jade.api.config.IPluginConfig;

public enum AquaticCatalystComponentProvider implements StreamServerDataProvider<BlockAccessor, AquaticCatalystComponentProvider.Data> {
    INSTANCE;

    private static final Identifier UID = Nautec.rl("aquatic_catalyst");

    public record Data(boolean burning, boolean waiting, boolean paused, ItemStack queued, int remainingDuration, int transferring,
                       int scanStatus, int scanDistance, BlockState target, int maxDistance) {
        public static final StreamCodec<RegistryFriendlyByteBuf, Data> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.BOOL, Data::burning,
                ByteBufCodecs.BOOL, Data::waiting,
                ByteBufCodecs.BOOL, Data::paused,
                ItemStack.OPTIONAL_STREAM_CODEC, Data::queued,
                ByteBufCodecs.VAR_INT, Data::remainingDuration,
                ByteBufCodecs.VAR_INT, Data::transferring,
                ByteBufCodecs.VAR_INT, Data::scanStatus,
                ByteBufCodecs.VAR_INT, Data::scanDistance,
                ByteBufCodecs.idMapper(Block.BLOCK_STATE_REGISTRY), Data::target,
                ByteBufCodecs.VAR_INT, Data::maxDistance,
                Data::new
        );
    }

    @Override
    public Data streamData(BlockAccessor blockAccessor) {
        AquaticCatalystBlockEntity blockEntity = (AquaticCatalystBlockEntity) blockAccessor.getBlockEntity();
        BeamScan scan = blockEntity.getBeamScan();
        return new Data(
                blockEntity.isActive(),
                blockEntity.isWaiting(),
                blockEntity.isPaused(),
                blockEntity.getProcessingItem().copy(),
                blockEntity.getRemainingDuration(),
                blockEntity.getPowerToTransfer(),
                scan.status().ordinal(),
                scan.distance(),
                blockAccessor.getLevel().getBlockState(scan.targetPos(blockAccessor.getPosition())),
                blockEntity.getMaxLaserDistance());
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, Data> streamCodec() {
        return Data.STREAM_CODEC;
    }

    @Override
    public Identifier getUid() {
        return UID;
    }

    public enum Client implements IBlockComponentProvider {
        INSTANCE;

        @Override
        public void appendTooltip(ITooltip iTooltip, BlockAccessor blockAccessor, IPluginConfig iPluginConfig) {
            AquaticCatalystComponentProvider.INSTANCE.decodeFromData(blockAccessor).ifPresent(data -> {
                if (data.paused()) {
                    iTooltip.add(Component.translatable("nautec.jade.status.paused").withStyle(ChatFormatting.YELLOW));
                } else if (data.burning()) {
                    iTooltip.add(Component.translatable("nautec.jade.status.burning").withStyle(ChatFormatting.GREEN));
                } else if (data.waiting()) {
                    iTooltip.add(Component.translatable("nautec.jade.status.waiting").withStyle(ChatFormatting.RED));
                } else {
                    iTooltip.add(Component.translatable("nautec.jade.status.no_fuel").withStyle(ChatFormatting.YELLOW));
                }
                if (!data.queued().isEmpty()) {
                    iTooltip.add(Component.translatable("nautec.jade.processing",
                            data.queued().getCount(),
                            data.queued().getHoverName()));
                }
                if (data.burning() || data.waiting() || (data.paused() && data.remainingDuration() > 0)) {
                    iTooltip.add(Component.translatable("nautec.jade.remaining_duration", data.remainingDuration()));
                    iTooltip.add(Component.translatable("nautec.jade.transferring", data.transferring()));
                }

                BeamScan.Status status = BeamScan.Status.values()[data.scanStatus()];
                Component target = data.target().getBlock().getName();
                switch (status) {
                    case CONNECTED -> iTooltip.add(Component.translatable("nautec.jade.receiver.connected", target,
                            AquaticCatalystBlockEntity.distanceText(data.scanDistance(), emitter(blockAccessor)))
                            .withStyle(ChatFormatting.GREEN));
                    case WRONG_SIDE -> iTooltip.add(Component.translatable("nautec.jade.receiver.wrong_side", target)
                            .withStyle(ChatFormatting.RED));
                    case BLOCKED -> iTooltip.add(Component.translatable("nautec.jade.receiver.blocked", target)
                            .withStyle(ChatFormatting.RED));
                    case NO_TARGET -> iTooltip.add(Component.translatable("nautec.jade.receiver.no_target", data.maxDistance())
                            .withStyle(ChatFormatting.RED));
                }
                if (status != BeamScan.Status.CONNECTED) {
                    iTooltip.add(Component.translatable("nautec.jade.catalyst.details").withStyle(ChatFormatting.GRAY));
                }
            });
        }

        private static Direction emitter(BlockAccessor blockAccessor) {
            return blockAccessor.getBlockState().getValue(BlockStateProperties.FACING).getOpposite();
        }

        @Override
        public Identifier getUid() {
            return UID;
        }
    }
}
