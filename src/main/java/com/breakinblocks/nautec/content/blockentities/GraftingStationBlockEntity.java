package com.breakinblocks.nautec.content.blockentities;

import com.breakinblocks.nautec.utils.BeamOverclock;
import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.api.bacteria.BacteriaInstance;
import com.breakinblocks.nautec.api.sides.SlotRoles;
import com.breakinblocks.nautec.api.blockentities.LaserBlockEntity;
import com.breakinblocks.nautec.capabilities.IOActions;
import com.breakinblocks.nautec.capabilities.NTCapabilities;
import com.breakinblocks.nautec.capabilities.bacteria.IBacteriaStorage;
import com.breakinblocks.nautec.content.blocks.GraftingStationBlock;
import com.breakinblocks.nautec.content.items.GraftingAnchorItem;
import com.breakinblocks.nautec.content.items.PetriDishItem;
import com.breakinblocks.nautec.content.menus.GraftingStationMenu;
import com.breakinblocks.nautec.data.NTDataMaps;
import com.breakinblocks.nautec.data.maps.BacteriaObtainValue;
import com.breakinblocks.nautec.registries.NTBlockEntityTypes;
import com.breakinblocks.nautec.registries.NTFluids;
import com.breakinblocks.nautec.utils.DataSlots;
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
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.Capabilities;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

public class GraftingStationBlockEntity extends LaserBlockEntity implements MenuProvider {
    private final BeamOverclock overclock = new BeamOverclock();

    public static final int DISH_SLOT = 0;
    public static final int SAMPLE_SLOT = 1;
    public static final int OUTPUT_SLOT = 2;
    public static final int ANCHOR_SLOT = 3;

    public static final int STATUS_RUNNING = 0;
    public static final int STATUS_NO_DISH = 1;
    public static final int STATUS_NO_SAMPLE = 2;
    public static final int STATUS_NO_WATER = 3;
    public static final int STATUS_OUTPUT_FULL = 4;
    public static final int STATUS_LOW_POWER = 5;
    public static final int STATUS_LOW_PURITY = 6;

    public static final int DATA_PROGRESS = 0;
    public static final int DATA_DURATION = 2;
    public static final int DATA_STATUS = 4;
    public static final int DATA_POWER = 5;
    public static final int DATA_COUNT = 7;

    private static final int SYNC_INTERVAL = 10;

    private int progress;
    private int status = STATUS_NO_DISH;
    private boolean syncPending;

    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case DATA_PROGRESS -> DataSlots.low(progress);
                case DATA_PROGRESS + 1 -> DataSlots.high(progress);
                case DATA_DURATION -> DataSlots.low(getDuration());
                case DATA_DURATION + 1 -> DataSlots.high(getDuration());
                case DATA_STATUS -> status;
                case DATA_POWER -> DataSlots.low(getRequiredPower());
                case DATA_POWER + 1 -> DataSlots.high(getRequiredPower());
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

    private static final SlotRoles ITEM_ROLES = SlotRoles.of(new int[]{DISH_SLOT, SAMPLE_SLOT}, new int[]{OUTPUT_SLOT});
    private static final SlotRoles FLUID_ROLES = SlotRoles.of(new int[]{0}, new int[0]);

    public GraftingStationBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(NTBlockEntityTypes.GRAFTING_STATION.get(), blockPos, blockState);
        addItemHandler(4, (slot, stack) -> switch (slot) {
            case DISH_SLOT -> isEmptyDish(stack);
            case SAMPLE_SLOT -> sample(stack) != null;
            case ANCHOR_SLOT -> isAnchor(stack);
            default -> false;
        });
        addFluidTank(NTConfig.graftingStationCapacity, fluidStack -> fluidStack.getFluid() == NTFluids.SALT_WATER.getStillFluid());
    }

    public static boolean isEmptyDish(ItemStack stack) {
        if (!(stack.getItem() instanceof PetriDishItem)) {
            return false;
        }
        IBacteriaStorage storage = stack.getCapability(NTCapabilities.BacteriaStorage.ITEM);
        return storage != null && storage.getBacteria(0).isEmpty();
    }

    public static @Nullable BacteriaObtainValue sample(ItemStack stack) {
        if (!(stack.getItem() instanceof BlockItem blockItem)) {
            return null;
        }
        return blockItem.getBlock().defaultBlockState().typeHolder().getData(NTDataMaps.BACTERIA_OBTAINING);
    }

    public static boolean isAnchor(ItemStack stack) {
        return stack.getItem() instanceof GraftingAnchorItem;
    }

    public boolean hasAnchor() {
        return isAnchor(getItemStackHandler().getStackInSlot(ANCHOR_SLOT));
    }

    public boolean hasAdvancedAnchor() {
        return getItemStackHandler().getStackInSlot(ANCHOR_SLOT).getItem() instanceof GraftingAnchorItem anchor && anchor.isAdvanced();
    }

    public int getDuration() {
        if (!hasAdvancedAnchor()) {
            return NTConfig.graftingStationDuration;
        }
        return Math.max(1, (int) Math.round(NTConfig.graftingStationDuration / NTConfig.advancedGraftingAnchorSpeed));
    }

    @Override
    public int getRequiredPower() {
        if (!hasAdvancedAnchor()) {
            return NTConfig.graftingStationPowerUsage;
        }
        return (int) Math.ceil(NTConfig.graftingStationPowerUsage * NTConfig.advancedGraftingAnchorPowerMultiplier);
    }

    public ContainerData getData() {
        return data;
    }

    public int getProgress() {
        return progress;
    }

    public int getStatus() {
        return status;
    }

    @Override
    protected void onFluidChanged() {
        super.onFluidChanged();
        this.syncPending = true;
    }

    @Override
    public void commonTick() {
        super.commonTick();
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        int newStatus = checkStatus();
        if (newStatus == STATUS_RUNNING) {
            progress += overclock.advance(beamSpeed());
            if (progress >= getDuration()) {
                graft(serverLevel);
                progress = 0;
            }
        } else if (newStatus != STATUS_LOW_POWER && newStatus != STATUS_LOW_PURITY) {
            progress = 0;
        }

        if (newStatus != status) {
            status = newStatus;
            syncPending = false;
            update();
        }
        boolean active = status == STATUS_RUNNING;
        if (getBlockState().getValue(GraftingStationBlock.ACTIVE) != active) {
            level.setBlock(worldPosition, getBlockState().setValue(GraftingStationBlock.ACTIVE, active), Block.UPDATE_CLIENTS);
        }
        if (syncPending && serverLevel.getGameTime() % SYNC_INTERVAL == 0) {
            syncPending = false;
            update();
        }
    }

    private int checkStatus() {
        if (!isEmptyDish(getItemStackHandler().getStackInSlot(DISH_SLOT))) {
            return STATUS_NO_DISH;
        }
        if (sample(getItemStackHandler().getStackInSlot(SAMPLE_SLOT)) == null) {
            return STATUS_NO_SAMPLE;
        }
        if (getFluidTank().getFluidAmount() < NTConfig.graftingStationSaltWaterUsage) {
            return STATUS_NO_WATER;
        }
        if (!getItemStackHandler().getStackInSlot(OUTPUT_SLOT).isEmpty()) {
            return STATUS_OUTPUT_FULL;
        }
        if (getPower() < getRequiredPower()) {
            return STATUS_LOW_POWER;
        }
        if (getPurity() < NTConfig.graftingStationPurity) {
            return STATUS_LOW_PURITY;
        }
        return STATUS_RUNNING;
    }

    private void graft(ServerLevel serverLevel) {
        BacteriaObtainValue sample = sample(getItemStackHandler().getStackInSlot(SAMPLE_SLOT));
        ItemStack result = getItemStackHandler().getStackInSlot(DISH_SLOT).copyWithCount(1);
        IBacteriaStorage storage = result.getCapability(NTCapabilities.BacteriaStorage.ITEM);
        if (sample == null || storage == null) {
            return;
        }
        storage.setBacteria(0, BacteriaInstance.roll(sample.bacteria(), serverLevel.registryAccess()));
        getItemStackHandler().extractItem(DISH_SLOT, 1, false);
        if (!hasAnchor()) {
            getItemStackHandler().extractItem(SAMPLE_SLOT, 1, false);
        }
        getFluidTank().drain(NTConfig.graftingStationSaltWaterUsage);
        forceInsertItem(OUTPUT_SLOT, result, false);
    }

    @Override
    public Set<Direction> getLaserInputs() {
        Set<Direction> inputs = EnumSet.allOf(Direction.class);
        inputs.remove(getBlockState().getValue(GraftingStationBlock.FACING));
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
    public SlotRoles fluidRoles() {
        return FLUID_ROLES;
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
        return new GraftingStationMenu(containerId, inventory, this);
    }

    @Override
    protected void loadData(ValueInput in) {
        super.loadData(in);
        getItemStackHandler().ensureSize(4);
        this.progress = in.getIntOr("progress", 0);
        this.status = in.getIntOr("status", STATUS_NO_DISH);
    }

    @Override
    protected void saveData(ValueOutput out) {
        super.saveData(out);
        out.putInt("progress", this.progress);
        out.putInt("status", this.status);
    }
}
