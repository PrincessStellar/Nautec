package com.breakinblocks.nautec.content.biometank;

import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.api.blockentities.ContainerBlockEntity;
import com.breakinblocks.nautec.capabilities.IOActions;
import com.breakinblocks.nautec.data.NTDataComponents;
import com.breakinblocks.nautec.registries.NTBlockEntityTypes;
import com.breakinblocks.nautec.tags.NTTags;
import it.unimi.dsi.fastutil.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import com.breakinblocks.nautec.utils.valueio.ValueInput;
import com.breakinblocks.nautec.utils.valueio.ValueOutput;
import net.neoforged.neoforge.capabilities.BlockCapability;
import com.breakinblocks.nautec.transfer.ResourceHandler;
import com.breakinblocks.nautec.transfer.item.ItemResource;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class BiomeTankBlockEntity extends ContainerBlockEntity {
    private @Nullable Holder<Item> plant;
    private int progress;

    public BiomeTankBlockEntity(BlockPos pos, BlockState state) {
        super(NTBlockEntityTypes.BIOME_TANK.get(), pos, state);
        addItemHandler(1, (slot, stack) -> false);
    }

    public @Nullable Holder<Item> getPlant() {
        return plant;
    }

    public void setPlant(@Nullable Holder<Item> plant) {
        this.plant = plant;
        this.progress = 0;
        setChanged();
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    public static boolean canGrow(ItemStack stack) {
        return !stack.isEmpty() && stack.is(NTTags.Items.BIOME_TANK_PLANTS);
    }

    public int getProgress() {
        return progress;
    }

    @Override
    public void commonTick() {
        super.commonTick();
        if (level == null || level.isClientSide() || plant == null || !plant.is(NTTags.Items.BIOME_TANK_PLANTS)) {
            return;
        }
        ItemStack output = getItemStackHandler().getStackInSlot(0);
        if (output.getCount() >= output.getMaxStackSize()) {
            return;
        }
        progress++;
        if (progress >= NTConfig.biomeTankTicks) {
            progress = 0;
            forceInsertItem(0, new ItemStack(plant), false);
        }
    }

    public ItemStack takeOutput() {
        return forceExtractItem(0, 64, false);
    }

    @Override
    public void update() {
        setChanged();
    }

    @Override
    public ResourceHandler<ItemResource> getItemHandlerOnSide(Direction direction) {
        return getItemHandler();
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        super.preRemoveSideEffects(pos, state);
        drop();
    }

    @Override
    public <T> Map<Direction, Pair<IOActions, int[]>> getSidedInteractions(BlockCapability<T, @Nullable Direction> capability) {
        return Map.of();
    }

    @Override
    protected void applyImplicitComponents(BlockEntity.DataComponentInput components) {
        super.applyImplicitComponents(components);
        Holder<Item> stored = components.get(NTDataComponents.BIOME_TANK_PLANT.get());
        if (stored != null) {
            this.plant = stored;
        }
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder components) {
        super.collectImplicitComponents(components);
        if (plant != null) {
            components.set(NTDataComponents.BIOME_TANK_PLANT.get(), plant);
        }
    }

    @Override
    public void removeComponentsFromTag(ValueOutput output) {
        super.removeComponentsFromTag(output);
        output.discard("plant");
    }

    @Override
    protected void saveData(ValueOutput out) {
        super.saveData(out);
        out.putInt("progress", progress);
        if (plant != null) {
            out.store("plant", BuiltInRegistries.ITEM.holderByNameCodec(), plant);
        }
    }

    @Override
    protected void loadData(ValueInput in) {
        super.loadData(in);
        this.progress = in.getIntOr("progress", 0);
        this.plant = in.read("plant", BuiltInRegistries.ITEM.holderByNameCodec()).orElse(null);
    }
}
