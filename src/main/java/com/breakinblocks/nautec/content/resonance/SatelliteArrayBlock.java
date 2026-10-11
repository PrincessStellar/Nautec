package com.breakinblocks.nautec.content.resonance;

import com.breakinblocks.nautec.api.blockentities.ContainerBlockEntity;
import com.breakinblocks.nautec.api.blocks.blockentities.LaserBlock;
import com.breakinblocks.nautec.registries.NTBlockEntityTypes;
import com.breakinblocks.nautec.registries.NTBlocks;
import com.breakinblocks.nautec.registries.NTCriteriaTriggers;
import com.mojang.serialization.MapCodec;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import com.breakinblocks.nautec.utils.InteractionResults;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class SatelliteArrayBlock extends LaserBlock {
    private static final VoxelShape SHAPE = Shapes.or(Block.box(1, 0, 1, 15, 3, 15), Block.box(3, 3, 3, 13, 13, 13),
            Block.box(2, 13, 2, 14, 15, 14), Block.box(6, 15, 6, 10, 16, 10));

    public SatelliteArrayBlock(Properties properties) {
        super(properties);
    }

    @Override
    public boolean waterloggable() {
        return true;
    }

    @Override
    public BlockEntityType<? extends ContainerBlockEntity> getBlockEntityType() {
        return NTBlockEntityTypes.SATELLITE_ARRAY.get();
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockPos above = context.getClickedPos().above();
        Level level = context.getLevel();
        if (above.getY() >= level.getMaxBuildHeight() || !level.getBlockState(above).canBeReplaced(context)) {
            return null;
        }
        return super.getStateForPlacement(context);
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (!state.is(oldState.getBlock())) {
            BlockPos above = pos.above();
            boolean water = level.getFluidState(above).getType() == Fluids.WATER;
            level.setBlockAndUpdate(above, NTBlocks.SATELLITE_ARRAY_TOP.get().defaultBlockState().setValue(BlockStateProperties.WATERLOGGED, water));
        }
    }

    @Override
    protected void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean movedByPiston) {
        super.onRemove(state, world, pos, newState, movedByPiston);
        if (!state.is(newState.getBlock()) && world instanceof ServerLevel level) {
            BlockPos above = pos.above();
            BlockState top = level.getBlockState(above);
            if (top.getBlock() instanceof SatelliteArrayTopBlock) {
                level.setBlockAndUpdate(above, top.getValue(BlockStateProperties.WATERLOGGED) ? Fluids.WATER.defaultFluidState().createLegacyBlock()
                        : Blocks.AIR.defaultBlockState());
            }
        }
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (!(stack.getItem() instanceof PrismSatelliteItem)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        return InteractionResults.toItem(launch(stack, level, pos, player));
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        return open(level, pos, player);
    }

    public static InteractionResult launch(ItemStack stack, Level level, BlockPos pos, Player player) {
        if (!(level.getBlockEntity(pos) instanceof SatelliteArrayBlockEntity array)) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (!(player instanceof ServerPlayer serverPlayer) || !(level instanceof ServerLevel serverLevel)) {
            return InteractionResult.PASS;
        }
        String error = null;
        ResonanceNetwork network = array.getNetwork();
        if (network != null && !ResonanceNetworks.canUse(serverPlayer, network)) {
            error = "nautec.resonance.error.no_access";
        } else if (array.hasSatellite()) {
            error = "nautec.satellite.error.occupied";
        } else if (!SatelliteGrid.clearSky(serverLevel, pos.above())) {
            error = "nautec.satellite.error.sky";
        }
        if (error != null) {
            serverPlayer.displayClientMessage(Component.translatable(error).withStyle(ChatFormatting.RED), true);
            return InteractionResult.FAIL;
        }
        array.launch();
        stack.consume(1, player);
        NTCriteriaTriggers.SATELLITE_LAUNCHED.get().trigger(serverPlayer);
        serverPlayer.displayClientMessage(Component.translatable("nautec.satellite.launched").withStyle(ChatFormatting.AQUA), true);
        double x = pos.getX() + 0.5;
        double z = pos.getZ() + 0.5;
        serverLevel.playSound(null, pos, SoundEvents.FIREWORK_ROCKET_LAUNCH, SoundSource.BLOCKS, 1.4F, 0.8F);
        serverLevel.playSound(null, pos, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 1.0F, 1.5F);
        serverLevel.sendParticles(ParticleTypes.END_ROD, x, pos.getY() + 2.0, z, 30, 0.2, 0.6, 0.2, 0.08);
        serverLevel.sendParticles(ParticleTypes.CLOUD, x, pos.getY() + 1.6, z, 16, 0.4, 0.1, 0.4, 0.04);
        return InteractionResult.SUCCESS;
    }

    public static InteractionResult open(Level level, BlockPos pos, Player player) {
        if (!(level.getBlockEntity(pos) instanceof SatelliteArrayBlockEntity array)) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (player instanceof ServerPlayer serverPlayer) {
            ResonanceNetwork network = array.getNetwork();
            if (network != null && !ResonanceNetworks.canUse(serverPlayer, network)) {
                serverPlayer.displayClientMessage(Component.translatable("nautec.resonance.locked", network.name(), network.ownerName())
                        .withStyle(ChatFormatting.RED), true);
                return InteractionResult.FAIL;
            }
            serverPlayer.openMenu(array, pos);
            ResonanceSync.send(serverPlayer, array);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public List<Component> displayText(Level level, BlockPos blockPos, Player player) {
        List<Component> lines = new ArrayList<>(super.displayText(level, blockPos, player));
        if (level.getBlockEntity(blockPos) instanceof SatelliteArrayBlockEntity array) {
            boolean online = array.getStatus() == SatelliteArrayBlockEntity.STATUS_ONLINE;
            lines.add(Component.translatable("nautec.monocle.satellite",
                    Component.translatable("nautec.satellite.kind.uplink"),
                    Component.translatable(SatelliteArrayBlockEntity.statusKey(array.getStatus())))
                    .withStyle(online ? ChatFormatting.AQUA : ChatFormatting.RED));
        }
        return lines;
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return simpleCodec(SatelliteArrayBlock::new);
    }
}
