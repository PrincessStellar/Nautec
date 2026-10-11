package com.breakinblocks.nautec.content.blockentities;

import com.google.common.collect.ImmutableMap;
import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.api.blockentities.LaserBlockEntity;
import com.breakinblocks.nautec.capabilities.IOActions;
import com.breakinblocks.nautec.content.blocks.PrismarineLaserRelayBlock;
import com.breakinblocks.nautec.registries.NTBlockEntityTypes;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.BlockCapability;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public class PrismarineLaserRelayBlockEntity extends LaserBlockEntity {
    public PrismarineLaserRelayBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(NTBlockEntityTypes.PRISMARINE_LASER_RELAY.get(), blockPos, blockState);
    }

    @Override
    public Set<Direction> getLaserInputs() {
        return ObjectSet.of(
                getBlockState().getValue(PrismarineLaserRelayBlock.FACING).getOpposite()
        );
    }

    @Override
    public Set<Direction> getLaserOutputs() {
        return ObjectSet.of(
                getBlockState().getValue(PrismarineLaserRelayBlock.FACING)
        );
    }

    @Override
    public <T> ImmutableMap<Direction, Pair<IOActions, int[]>> getSidedInteractions(BlockCapability<T, @Nullable Direction> capability) {
        return ImmutableMap.of();
    }

    @Override
    public int getMaxLaserDistance() {
        return NTConfig.relayLaserDistance;
    }

    @Override
    public void commonTick() {
        super.commonTick();

        transmitPower(this.getPower());
    }
}
