package com.breakinblocks.nautec.content.biometank;

import com.breakinblocks.nautec.api.blockentities.ContainerBlockEntity;
import com.breakinblocks.nautec.api.blocks.blockentities.ContainerBlock;
import com.breakinblocks.nautec.registries.NTBlockEntityTypes;
import com.mojang.serialization.MapCodec;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class BiomeTankBlock extends ContainerBlock {
    public static final MapCodec<BiomeTankBlock> CODEC = simpleCodec(BiomeTankBlock::new);

    public BiomeTankBlock(Properties properties) {
        super(properties);
    }

    @Override
    public boolean tickingEnabled() {
        return true;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (!BiomeTankBlockEntity.canGrow(stack) || !(level.getBlockEntity(pos) instanceof BiomeTankBlockEntity tank)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        Holder<Item> plant = stack.getItemHolder();
        if (tank.getPlant() != null && tank.getPlant().value() == plant.value()) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (!level.isClientSide()) {
            ItemStack grown = tank.takeOutput();
            if (!grown.isEmpty()) {
                player.getInventory().placeItemBackInInventory(grown);
            }
            tank.setPlant(plant);
            stack.consume(1, player);
            level.playSound(null, pos, SoundEvents.BIG_DRIPLEAF_PLACE, SoundSource.BLOCKS, 1.0F, 1.0F);
            player.displayClientMessage(Component.translatable("nautec.biome_tank.seeded", plant.value().getName(new ItemStack(plant)))
                    .withStyle(ChatFormatting.AQUA), true);
        }
        return ItemInteractionResult.SUCCESS;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!(level.getBlockEntity(pos) instanceof BiomeTankBlockEntity tank)) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide()) {
            ItemStack taken = tank.takeOutput();
            if (!taken.isEmpty()) {
                player.getInventory().placeItemBackInInventory(taken);
            } else if (tank.getPlant() == null) {
                player.displayClientMessage(Component.translatable("nautec.biome_tank.empty").withStyle(ChatFormatting.GRAY), true);
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public BlockEntityType<? extends ContainerBlockEntity> getBlockEntityType() {
        return NTBlockEntityTypes.BIOME_TANK.get();
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }
}
