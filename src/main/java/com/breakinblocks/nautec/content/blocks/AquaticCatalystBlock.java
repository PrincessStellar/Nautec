package com.breakinblocks.nautec.content.blocks;

import com.mojang.serialization.MapCodec;
import com.breakinblocks.nautec.api.blockentities.ContainerBlockEntity;
import com.breakinblocks.nautec.api.blocks.DisplayBlock;
import com.breakinblocks.nautec.api.blocks.blockentities.LaserBlock;
import com.breakinblocks.nautec.content.blockentities.AquaticCatalystBlockEntity;
import com.breakinblocks.nautec.registries.NTBlockEntityTypes;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import com.breakinblocks.nautec.utils.ItemUtils;

public class AquaticCatalystBlock extends LaserBlock implements DisplayBlock {
    public static final IntegerProperty STAGE = IntegerProperty.create("stage", 0, 8);
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");
    public static final BooleanProperty LINKED = BooleanProperty.create("linked");

    public AquaticCatalystBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState()
                .setValue(BlockStateProperties.FACING, Direction.NORTH)
                .setValue(STAGE, 0)
                .setValue(ACTIVE, false)
                .setValue(LINKED, false)
        );
    }

    @Override
    public boolean waterloggable() {
        return false;
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return simpleCodec(AquaticCatalystBlock::new);
    }

    @Override
    public BlockEntityType<? extends ContainerBlockEntity> getBlockEntityType() {
        return NTBlockEntityTypes.AQUATIC_CATALYST.get();
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder.add(BlockStateProperties.FACING, STAGE, ACTIVE, LINKED));
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = super.getStateForPlacement(context);
        return state != null ? state.setValue(BlockStateProperties.FACING, context.getNearestLookingDirection().getOpposite()) : null;
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, @Nullable Orientation orientation, boolean movedByPiston) {
        super.neighborChanged(state, level, pos, block, orientation, movedByPiston);
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof AquaticCatalystBlockEntity catalyst) {
            catalyst.setPaused(level.hasNeighborSignal(pos));
        }
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        AquaticCatalystBlockEntity be = (AquaticCatalystBlockEntity) level.getBlockEntity(pos);
        var itemHandler = be.getItemStackHandler();
        if (!stack.isEmpty()) {
            boolean valid = itemHandler.isItemValid(0, stack);
            if (valid) {
                ItemStack remainder = itemHandler.insertItem(0, stack, false);
                player.setItemInHand(hand, remainder);

                return InteractionResult.SUCCESS;
            }
        } else if (hand != InteractionHand.MAIN_HAND) {
            return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
        } else if (player.isShiftKeyDown()) {
            if (!level.isClientSide()) {
                be.diagnosticLines().forEach(player::sendSystemMessage);
            }
            return InteractionResult.SUCCESS;
        } else {
            ItemStack extracted = itemHandler.extractItem(0, itemHandler.getSlotLimit(0), false);
            ItemUtils.giveItemToPlayer(player, extracted, player.getInventory().getSelectedSlot());
            return InteractionResult.SUCCESS;
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
    }

    @Override
    public List<Component> displayText(Level level, BlockPos blockPos, Player player) {
        BlockState blockState = level.getBlockState(blockPos);
        AquaticCatalystBlockEntity be = (AquaticCatalystBlockEntity) level.getBlockEntity(blockPos);
        return List.of(
                Component.translatable("nautec.monocle.duration", be.getDuration()).withStyle(ChatFormatting.WHITE)
        );
    }

}
