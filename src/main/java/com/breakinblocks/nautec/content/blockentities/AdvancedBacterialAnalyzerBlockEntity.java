package com.breakinblocks.nautec.content.blockentities;

import com.breakinblocks.nautec.utils.BeamOverclock;
import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.api.bacteria.BacteriaInstance;
import com.breakinblocks.nautec.api.blockentities.LaserBlockEntity;
import com.breakinblocks.nautec.api.sides.SlotRoles;
import com.breakinblocks.nautec.capabilities.IOActions;
import com.breakinblocks.nautec.capabilities.NTCapabilities;
import com.breakinblocks.nautec.capabilities.bacteria.DishPort;
import com.breakinblocks.nautec.capabilities.bacteria.IBacteriaStorage;
import com.breakinblocks.nautec.content.blocks.AdvancedBacterialAnalyzerBlock;
import com.breakinblocks.nautec.content.items.PetriDishItem;
import com.breakinblocks.nautec.content.menus.AdvancedBacterialAnalyzerMenu;
import com.breakinblocks.nautec.registries.NTBlockEntityTypes;
import com.breakinblocks.nautec.utils.DataSlots;
import com.breakinblocks.nautec.utils.ItemTemplates;
import it.unimi.dsi.fastutil.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

public class AdvancedBacterialAnalyzerBlockEntity extends LaserBlockEntity implements MenuProvider {
    private final BeamOverclock overclock = new BeamOverclock();

    @Override
    public int getRequiredPower() {
        return NTConfig.advancedAnalyzerPowerUsage;
    }

    public static final int DISHES = 9;
    public static final int FIRST_OUTPUT = DISHES;

    public static final int STATUS_RUNNING = 0;
    public static final int STATUS_IDLE = 1;
    public static final int STATUS_OUTPUT_FULL = 2;
    public static final int STATUS_LOW_POWER = 3;
    public static final int STATUS_LOW_PURITY = 4;

    public static final int DATA_DURATION = DISHES * 2;
    public static final int DATA_STATUS = DISHES * 2 + 2;
    public static final int DATA_COUNT = DISHES * 2 + 3;

    private static final SlotRoles ITEM_ROLES = SlotRoles.of(SlotRoles.range(0, DISHES), SlotRoles.range(FIRST_OUTPUT, FIRST_OUTPUT + DISHES));

    private final int[] progress = new int[DISHES];
    private int status = STATUS_IDLE;

    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            if (index < DATA_DURATION) {
                int value = progress[index / 2];
                return index % 2 == 0 ? DataSlots.low(value) : DataSlots.high(value);
            }
            return switch (index) {
                case DATA_DURATION -> DataSlots.low(NTConfig.advancedAnalyzerCraftingSpeed);
                case DATA_DURATION + 1 -> DataSlots.high(NTConfig.advancedAnalyzerCraftingSpeed);
                case DATA_STATUS -> status;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
        }

        @Override
        public int getCount() {
            return DATA_COUNT;
        }
    };

    public AdvancedBacterialAnalyzerBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(NTBlockEntityTypes.ADVANCED_BACTERIAL_ANALYZER.get(), blockPos, blockState);
        addItemHandler(DISHES * 2, 1, (slot, stack) -> slot < DISHES && needsAnalysis(stack));
    }

    public static boolean analysisGhostMatches(ItemStack ghost, ItemResource resource) {
        return DishPort.colonyOf(ghost).isEmpty() ? resource.getItem() instanceof PetriDishItem : ItemTemplates.matches(ghost, resource);
    }

    @Override
    public boolean ghostMatches(int slot, ItemStack ghost, ItemResource resource) {
        return analysisGhostMatches(ghost, resource);
    }

    @Override
    protected boolean acceptsGhost(int slot, ItemStack stack) {
        return slot < DISHES && DishPort.isDish(stack);
    }

    public static boolean needsAnalysis(ItemStack stack) {
        if (!(stack.getItem() instanceof PetriDishItem)) {
            return false;
        }
        IBacteriaStorage storage = stack.getCapability(NTCapabilities.BacteriaStorage.ITEM);
        if (storage == null) {
            return false;
        }
        BacteriaInstance bacteria = storage.getBacteria(0);
        return !bacteria.isEmpty() && !bacteria.isAnalyzed();
    }

    public ContainerData getData() {
        return data;
    }

    public int getStatus() {
        return status;
    }

    public int getProgress(int dish) {
        return progress[dish];
    }

    @Override
    public void commonTick() {
        super.commonTick();
        if (!(level instanceof ServerLevel)) {
            return;
        }

        boolean any = false;
        boolean blocked = false;
        boolean powered = getPower() >= NTConfig.advancedAnalyzerPowerUsage;
        boolean pure = getPurity() >= NTConfig.advancedAnalyzerPurity;
        int steps = powered && pure ? overclock.advance(beamSpeed()) : 0;
        for (int dish = 0; dish < DISHES; dish++) {
            if (!needsAnalysis(getItemStackHandler().getStackInSlot(dish))) {
                progress[dish] = 0;
                continue;
            }
            any = true;
            if (!powered || !pure) {
                continue;
            }
            if (progress[dish] < NTConfig.advancedAnalyzerCraftingSpeed) {
                progress[dish] = Math.min(NTConfig.advancedAnalyzerCraftingSpeed, progress[dish] + steps);
                continue;
            }
            int output = freeOutput();
            if (output < 0) {
                blocked = true;
                continue;
            }
            analyze(dish, output);
            progress[dish] = 0;
        }

        int newStatus = !any ? STATUS_IDLE : !powered ? STATUS_LOW_POWER : !pure ? STATUS_LOW_PURITY : blocked ? STATUS_OUTPUT_FULL : STATUS_RUNNING;
        if (newStatus != status) {
            status = newStatus;
            update();
        }
        boolean active = status == STATUS_RUNNING;
        if (getBlockState().getValue(AdvancedBacterialAnalyzerBlock.ACTIVE) != active) {
            level.setBlock(worldPosition, getBlockState().setValue(AdvancedBacterialAnalyzerBlock.ACTIVE, active), Block.UPDATE_CLIENTS);
        }
    }

    private int freeOutput() {
        for (int slot = FIRST_OUTPUT; slot < FIRST_OUTPUT + DISHES; slot++) {
            if (getItemStackHandler().getStackInSlot(slot).isEmpty()) {
                return slot;
            }
        }
        return -1;
    }

    private void analyze(int dish, int output) {
        ItemStack result = getItemStackHandler().getStackInSlot(dish).copyWithCount(1);
        IBacteriaStorage storage = result.getCapability(NTCapabilities.BacteriaStorage.ITEM);
        if (storage == null) {
            return;
        }
        BacteriaInstance analyzed = storage.getBacteria(0);
        analyzed.setAnalyzed(true);
        storage.setBacteria(0, analyzed);
        getItemStackHandler().extractItem(dish, 1, false);
        forceInsertItem(output, result, false);
    }

    @Override
    public Set<Direction> getLaserInputs() {
        Set<Direction> inputs = EnumSet.allOf(Direction.class);
        inputs.remove(getBlockState().getValue(AdvancedBacterialAnalyzerBlock.FACING));
        return inputs;
    }

    @Override
    public Set<Direction> getLaserOutputs() {
        return Set.of();
    }

    @Override
    public SlotRoles itemRoles() {
        return ITEM_ROLES;
    }

    @Override
    public <T> Map<Direction, Pair<IOActions, int[]>> getSidedInteractions(BlockCapability<T, @Nullable Direction> capability) {
        return Map.of();
    }

    @Override
    public Component getDisplayName() {
        return getBlockState().getBlock().getName();
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new AdvancedBacterialAnalyzerMenu(containerId, inventory, this);
    }

    @Override
    protected void loadData(ValueInput in) {
        super.loadData(in);
        Arrays.fill(progress, 0);
        int[] saved = in.getIntArray("progress").orElse(new int[0]);
        System.arraycopy(saved, 0, progress, 0, Math.min(saved.length, DISHES));
        this.status = in.getIntOr("status", STATUS_IDLE);
    }

    @Override
    protected void saveData(ValueOutput out) {
        super.saveData(out);
        out.putIntArray("progress", progress);
        out.putInt("status", status);
    }
}
