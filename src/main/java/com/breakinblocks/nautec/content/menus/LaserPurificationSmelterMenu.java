package com.breakinblocks.nautec.content.menus;

import com.breakinblocks.nautec.api.menu.NTMachineMenu;
import com.breakinblocks.nautec.capabilities.item.ItemStackHandler;
import com.breakinblocks.nautec.content.blockentities.LaserPurificationSmelterBlockEntity;
import com.breakinblocks.nautec.registries.NTMenuTypes;
import com.breakinblocks.nautec.utils.DataSlots;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.item.ResourceHandlerSlot;
import org.jetbrains.annotations.NotNull;

public class LaserPurificationSmelterMenu extends NTMachineMenu<LaserPurificationSmelterBlockEntity> {
    public static final int INVENTORY_Y = 136;
    public static final int INPUT_X = 8;
    public static final int OUTPUT_X = 94;
    public static final int GRID_Y = 18;
    public static final int UPGRADE_X = 152;
    public static final int UPGRADE_Y = 36;

    private final ContainerData data;

    public LaserPurificationSmelterMenu(int containerId, Inventory inv, FriendlyByteBuf extraData) {
        this(containerId, inv, (LaserPurificationSmelterBlockEntity) inv.player.level().getBlockEntity(extraData.readBlockPos()),
                new SimpleContainerData(LaserPurificationSmelterBlockEntity.DATA_COUNT));
    }

    public LaserPurificationSmelterMenu(int containerId, @NotNull Inventory inv, @NotNull LaserPurificationSmelterBlockEntity blockEntity) {
        this(containerId, inv, blockEntity, blockEntity.getData());
    }

    private LaserPurificationSmelterMenu(int containerId, Inventory inv, LaserPurificationSmelterBlockEntity blockEntity, ContainerData data) {
        super(NTMenuTypes.LASER_PURIFICATION_SMELTER.get(), containerId, inv, blockEntity, INVENTORY_Y);
        this.data = data;
        addDataSlots(data);
        ItemStackHandler handler = blockEntity.getItemStackHandler();
        for (int lane = 0; lane < LaserPurificationSmelterBlockEntity.LANES; lane++) {
            addSlot(new ResourceHandlerSlot(handler, handler::set, LaserPurificationSmelterBlockEntity.INPUT_START + lane, inputX(lane), slotY(lane)));
        }
        addSlot(new ResourceHandlerSlot(handler, handler::set, LaserPurificationSmelterBlockEntity.UPGRADE_SLOT, UPGRADE_X, UPGRADE_Y));
        for (int lane = 0; lane < LaserPurificationSmelterBlockEntity.LANES; lane++) {
            addSlot(new OutputSlot(handler, LaserPurificationSmelterBlockEntity.OUTPUT_START + lane, outputX(lane), slotY(lane)));
        }
    }

    public static int inputX(int lane) {
        return INPUT_X + (lane % 3) * 18;
    }

    public static int outputX(int lane) {
        return OUTPUT_X + (lane % 3) * 18;
    }

    public static int slotY(int lane) {
        return GRID_Y + (lane / 3) * 18;
    }

    @Override
    protected int getMergeableSlotCount() {
        return LaserPurificationSmelterBlockEntity.LANES + 1;
    }

    private int wide(int index) {
        return DataSlots.join(data.get(index), data.get(index + 1));
    }

    public int getProgress() {
        return data.get(LaserPurificationSmelterBlockEntity.DATA_PROGRESS);
    }

    public int getInterval() {
        return Math.max(1, data.get(LaserPurificationSmelterBlockEntity.DATA_INTERVAL));
    }

    public int getItemsPerInterval() {
        return data.get(LaserPurificationSmelterBlockEntity.DATA_ITEMS_PER_INTERVAL);
    }

    public int getPower() {
        return wide(LaserPurificationSmelterBlockEntity.DATA_POWER);
    }

    public int getCost() {
        return wide(LaserPurificationSmelterBlockEntity.DATA_COST);
    }

    public float getPurity() {
        return data.get(LaserPurificationSmelterBlockEntity.DATA_PURITY) / 100F;
    }

    public int getStatus() {
        return data.get(LaserPurificationSmelterBlockEntity.DATA_STATUS);
    }

    public int getUnlocked() {
        return data.get(LaserPurificationSmelterBlockEntity.DATA_UNLOCKED);
    }

    public int getTier() {
        return data.get(LaserPurificationSmelterBlockEntity.DATA_TIER);
    }

    public boolean isAutoDistribute() {
        return data.get(LaserPurificationSmelterBlockEntity.DATA_AUTO) == 1;
    }

    public int getBusy() {
        return data.get(LaserPurificationSmelterBlockEntity.DATA_BUSY);
    }

    private static final class OutputSlot extends ResourceHandlerSlot {
        private OutputSlot(ItemStackHandler handler, int slot, int x, int y) {
            super(handler, handler::set, slot, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }
    }
}
