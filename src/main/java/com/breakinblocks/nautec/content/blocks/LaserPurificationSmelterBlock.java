package com.breakinblocks.nautec.content.blocks;

import com.breakinblocks.nautec.api.blockentities.ContainerBlockEntity;
import com.breakinblocks.nautec.api.blocks.blockentities.LaserBlock;
import com.breakinblocks.nautec.content.blockentities.LaserPurificationSmelterBlockEntity;
import com.breakinblocks.nautec.registries.NTBlockEntityTypes;
import com.mojang.serialization.MapCodec;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;

public class LaserPurificationSmelterBlock extends LaserBlock {
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");

    public LaserPurificationSmelterBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(FACING, Direction.NORTH).setValue(ACTIVE, false));
    }

    @Override
    public boolean waterloggable() {
        return false;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder.add(FACING, ACTIVE));
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = super.getStateForPlacement(context);
        return state != null ? state.setValue(FACING, context.getHorizontalDirection().getOpposite()) : null;
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    public List<Component> displayText(Level level, BlockPos blockPos, Player player) {
        if (!(level.getBlockEntity(blockPos) instanceof LaserPurificationSmelterBlockEntity smelter)) {
            return List.of();
        }
        int status = smelter.getStatus();
        return List.of(
                Component.translatable(statusKey(status)).withStyle(status == LaserPurificationSmelterBlockEntity.STATUS_SMELTING
                        ? ChatFormatting.AQUA : status == LaserPurificationSmelterBlockEntity.STATUS_IDLE ? ChatFormatting.GRAY : ChatFormatting.RED),
                Component.translatable("nautec.monocle.power", smelter.getPower()).withStyle(ChatFormatting.WHITE),
                Component.translatable("nautec.monocle.purity", String.format(Locale.ROOT, "%.2f", smelter.getPurity())).withStyle(ChatFormatting.WHITE)
        );
    }

    public static String statusKey(int status) {
        return switch (status) {
            case LaserPurificationSmelterBlockEntity.STATUS_SMELTING -> "nautec.laser_purification_smelter.status.smelting";
            case LaserPurificationSmelterBlockEntity.STATUS_WAITING -> "nautec.laser_purification_smelter.status.waiting";
            case LaserPurificationSmelterBlockEntity.STATUS_OUTPUT_FULL -> "nautec.laser_purification_smelter.status.output_full";
            default -> "nautec.laser_purification_smelter.status.idle";
        };
    }

    @Override
    public BlockEntityType<? extends ContainerBlockEntity> getBlockEntityType() {
        return NTBlockEntityTypes.LASER_PURIFICATION_SMELTER.get();
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return simpleCodec(LaserPurificationSmelterBlock::new);
    }
}
