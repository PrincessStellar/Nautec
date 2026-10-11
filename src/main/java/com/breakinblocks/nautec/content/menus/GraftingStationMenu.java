package com.breakinblocks.nautec.content.menus;

import net.minecraft.world.item.ItemStack;
import com.breakinblocks.nautec.api.menu.NTMachineMenu;
import com.breakinblocks.nautec.api.menu.slots.SlotFluidHandler;
import com.breakinblocks.nautec.content.blockentities.GraftingStationBlockEntity;
import com.breakinblocks.nautec.registries.NTMenuTypes;
import com.breakinblocks.nautec.utils.DataSlots;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.neoforged.neoforge.transfer.item.ResourceHandlerSlot;
import org.jetbrains.annotations.NotNull;

public class GraftingStationMenu extends NTMachineMenu<GraftingStationBlockEntity> {
    private final ContainerData data;

    public GraftingStationMenu(int containerId, Inventory inv, FriendlyByteBuf extraData) {
        this(containerId, inv, (GraftingStationBlockEntity) inv.player.level().getBlockEntity(extraData.readBlockPos()),
                new SimpleContainerData(GraftingStationBlockEntity.DATA_COUNT));
    }

    public GraftingStationMenu(int containerId, @NotNull Inventory inv, @NotNull GraftingStationBlockEntity blockEntity) {
        this(containerId, inv, blockEntity, blockEntity.getData());
    }

    private GraftingStationMenu(int containerId, Inventory inv, GraftingStationBlockEntity blockEntity, ContainerData data) {
        super(NTMenuTypes.GRAFTING_STATION.get(), containerId, inv, blockEntity);
        this.data = data;
        addDataSlots(data);

        addSlot(new ResourceHandlerSlot(blockEntity.getItemStackHandler(), blockEntity.getItemStackHandler()::set,
                GraftingStationBlockEntity.DISH_SLOT, 53, 22));
        addSlot(new ResourceHandlerSlot(blockEntity.getItemStackHandler(), blockEntity.getItemStackHandler()::set,
                GraftingStationBlockEntity.SAMPLE_SLOT, 53, 46));
        addSlot(new ResourceHandlerSlot(blockEntity.getItemStackHandler(), blockEntity.getItemStackHandler()::set,
                GraftingStationBlockEntity.OUTPUT_SLOT, 107, 34));
        addSlot(new ResourceHandlerSlot(blockEntity.getItemStackHandler(), blockEntity.getItemStackHandler()::set,
                GraftingStationBlockEntity.ANCHOR_SLOT, 8, 60));

        addFluidHandlerSlot(new SlotFluidHandler(blockEntity.getFluidTank(), 0, 28, 34, 18, 18));
    }

    @Override
    protected int getMergeableSlotCount() {
        return 2;
    }

    @Override
    protected boolean performMerge(int index, ItemStack stack) {
        int playerEnd = playerSlotStart + 36;
        if (index >= playerSlotStart && index < playerEnd && GraftingStationBlockEntity.isAnchor(stack)) {
            int machineStart = playerSlotStart == 0 ? playerEnd : 0;
            return moveItemStackTo(stack, machineStart + GraftingStationBlockEntity.ANCHOR_SLOT,
                    machineStart + GraftingStationBlockEntity.ANCHOR_SLOT + 1, false);
        }
        return super.performMerge(index, stack);
    }

    public int getProgress() {
        return DataSlots.join(data.get(GraftingStationBlockEntity.DATA_PROGRESS), data.get(GraftingStationBlockEntity.DATA_PROGRESS + 1));
    }

    public int getDuration() {
        return Math.max(1, DataSlots.join(data.get(GraftingStationBlockEntity.DATA_DURATION),
                data.get(GraftingStationBlockEntity.DATA_DURATION + 1)));
    }

    public int getStatus() {
        return data.get(GraftingStationBlockEntity.DATA_STATUS);
    }

    public int getRequiredPower() {
        return DataSlots.join(data.get(GraftingStationBlockEntity.DATA_POWER), data.get(GraftingStationBlockEntity.DATA_POWER + 1));
    }
}
