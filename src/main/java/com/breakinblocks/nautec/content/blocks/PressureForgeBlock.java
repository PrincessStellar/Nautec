package com.breakinblocks.nautec.content.blocks;

import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.api.blockentities.ContainerBlockEntity;
import com.breakinblocks.nautec.api.blocks.blockentities.LaserBlock;
import com.breakinblocks.nautec.content.blockentities.PressureForgeBlockEntity;
import com.breakinblocks.nautec.content.items.PressureSynthesizerItem;
import com.breakinblocks.nautec.registries.NTBlockEntityTypes;
import com.mojang.serialization.MapCodec;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import com.breakinblocks.nautec.utils.ItemUtils;
import net.neoforged.neoforge.transfer.fluid.FluidUtil;

import java.util.ArrayList;
import java.util.List;

public class PressureForgeBlock extends LaserBlock {
    public PressureForgeBlock(Properties properties) {
        super(properties);
    }

    @Override
    public boolean waterloggable() {
        return false;
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return simpleCodec(PressureForgeBlock::new);
    }

    @Override
    public BlockEntityType<? extends ContainerBlockEntity> getBlockEntityType() {
        return NTBlockEntityTypes.PRESSURE_FORGE.get();
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player,
                                          InteractionHand hand, BlockHitResult hitResult) {
        if (!(level.getBlockEntity(pos) instanceof PressureForgeBlockEntity forge)) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (stack.getItem() instanceof PressureSynthesizerItem synthesizer) {
            return fitSynthesizer(forge, synthesizer, stack, level, pos, player);
        }
        if (FluidUtil.interactWithFluidHandler(player, hand, pos, forge.getFluidTank(), null)) {
            return InteractionResult.SUCCESS;
        }
        if (level instanceof ServerLevel serverLevel && PressureForgeBlockEntity.isInput(serverLevel, stack)) {
            return ItemUtils.insertHeldItem(forge.getItemHandler(), 0, stack, player, hand);
        }
        return InteractionResult.TRY_WITH_EMPTY_HAND;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!(level.getBlockEntity(pos) instanceof PressureForgeBlockEntity forge)) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (player.isShiftKeyDown() && forge.getSynthesizer() != PressureForgeBlockEntity.Synthesizer.NONE) {
            player.getInventory().placeItemBackInInventory(forge.setSynthesizer(PressureForgeBlockEntity.Synthesizer.NONE));
            level.playSound(null, pos, SoundEvents.ITEM_FRAME_REMOVE_ITEM, SoundSource.BLOCKS, 1.0F, 0.8F);
            return InteractionResult.SUCCESS;
        }
        player.openMenu(forge, pos);
        return InteractionResult.SUCCESS;
    }

    private static InteractionResult fitSynthesizer(PressureForgeBlockEntity forge, PressureSynthesizerItem synthesizer, ItemStack stack,
                                                    Level level, BlockPos pos, Player player) {
        if (forge.getSynthesizer() == synthesizer.tier()) {
            player.sendOverlayMessage(Component.translatable("nautec.pressure_synthesizer.already_fitted").withStyle(ChatFormatting.RED));
            return InteractionResult.FAIL;
        }
        ItemStack previous = forge.setSynthesizer(synthesizer.tier());
        stack.consume(1, player);
        if (!previous.isEmpty()) {
            player.getInventory().placeItemBackInInventory(previous);
        }
        level.playSound(null, pos, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 0.8F, 1.6F);
        player.sendOverlayMessage(Component.translatable("nautec.pressure_synthesizer.fitted", synthesizer.tier().stack().getHoverName()).withStyle(ChatFormatting.AQUA));
        return InteractionResult.SUCCESS;
    }

    @Override
    public List<Component> displayText(Level level, BlockPos blockPos, Player player) {
        if (!(level.getBlockEntity(blockPos) instanceof PressureForgeBlockEntity forge)) {
            return List.of();
        }

        List<Component> lines = new ArrayList<>();
        PressureForgeBlockEntity.Synthesizer synthesizer = forge.getSynthesizer();
        if (synthesizer != PressureForgeBlockEntity.Synthesizer.NONE) {
            lines.add(Component.translatable("nautec.monocle.synthesizer", synthesizer.stack().getHoverName()).withStyle(ChatFormatting.LIGHT_PURPLE));
        }

        if (!forge.isPressurised()) {
            lines.add(synthesizer == PressureForgeBlockEntity.Synthesizer.BASIC
                    ? Component.translatable("nautec.monocle.not_pressurised.depth", NTConfig.pressureForgeDepth).withStyle(ChatFormatting.RED)
                    : Component.translatable("nautec.monocle.not_pressurised", NTConfig.pressureForgeDepth, NTConfig.pressureForgeWaterColumn)
                    .withStyle(ChatFormatting.RED));
            return lines;
        }

        lines.add(Component.translatable("nautec.monocle.pressurised").withStyle(ChatFormatting.AQUA));
        lines.add(Component.translatable("nautec.monocle.acid", forge.getFluidTank().getFluidAmount()).withStyle(ChatFormatting.WHITE));
        lines.add(Component.translatable("nautec.monocle.purity", String.format("%.2f", forge.getPurity())).withStyle(ChatFormatting.WHITE));
        return lines;
    }
}
