package com.breakinblocks.nautec.content.menus;

import com.breakinblocks.nautec.api.menu.NTMachineMenu;
import com.breakinblocks.nautec.api.menu.slots.SlotBacteriaStorage;
import com.breakinblocks.nautec.content.blockentities.ColonyReplicatorBlockEntity;
import com.breakinblocks.nautec.utils.DataSlots;
import com.breakinblocks.nautec.registries.NTMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import com.breakinblocks.nautec.transfer.item.ResourceHandlerSlot;
import org.jetbrains.annotations.NotNull;

public class ColonyReplicatorMenu extends NTMachineMenu<ColonyReplicatorBlockEntity> {
    public static final int TEMPLATE_X = 26;
    public static final int TEMPLATE_Y = 18;
    public static final int PARTNER_X = 26;
    public static final int PARTNER_Y = 40;
    public static final int FODDER_X = 64;
    public static final int FODDER_Y = 40;
    public static final int FODDER_ITEM_X = 64;
    public static final int FODDER_ITEM_Y = 18;
    public static final int RESULT_X = 134;
    public static final int RESULT_Y = 27;

    private final ContainerData data;

    public ColonyReplicatorMenu(int containerId, Inventory inv, FriendlyByteBuf extraData) {
        this(containerId, inv, (ColonyReplicatorBlockEntity) inv.player.level().getBlockEntity(extraData.readBlockPos()),
                new SimpleContainerData(ColonyReplicatorBlockEntity.DATA_COUNT));
    }

    public ColonyReplicatorMenu(int containerId, @NotNull Inventory inv, @NotNull ColonyReplicatorBlockEntity blockEntity) {
        this(containerId, inv, blockEntity, blockEntity.getData());
    }

    private ColonyReplicatorMenu(int containerId, Inventory inv, ColonyReplicatorBlockEntity blockEntity, ContainerData data) {
        super(NTMenuTypes.COLONY_REPLICATOR.get(), containerId, inv, blockEntity);
        this.data = data;
        addDataSlots(data);
        addBacteriaStorageSlot(new SlotBacteriaStorage(blockEntity.getBacteriaStorage(), ColonyReplicatorBlockEntity.TEMPLATE, TEMPLATE_X, TEMPLATE_Y));
        addBacteriaStorageSlot(new SlotBacteriaStorage(blockEntity.getBacteriaStorage(), ColonyReplicatorBlockEntity.PARTNER, PARTNER_X, PARTNER_Y));
        addBacteriaStorageSlot(new SlotBacteriaStorage(blockEntity.getBacteriaStorage(), ColonyReplicatorBlockEntity.FODDER, FODDER_X, FODDER_Y));
        addBacteriaStorageSlot(new SlotBacteriaStorage(blockEntity.getBacteriaStorage(), ColonyReplicatorBlockEntity.RESULT, RESULT_X, RESULT_Y));
        addSlot(new ResourceHandlerSlot(blockEntity.getItemStackHandler(), blockEntity.getItemStackHandler()::set,
                ColonyReplicatorBlockEntity.FODDER_ITEM, FODDER_ITEM_X, FODDER_ITEM_Y));
        addDishPort(ColonyReplicatorBlockEntity.DISH_IN, 7, 61, ColonyReplicatorBlockEntity.DISH_OUT, 27, 61, ColonyReplicatorBlockEntity.DISH_EMPTY_OUT, 47, 61);
    }

    @Override
    protected int getMergeableSlotCount() {
        return 2;
    }

    public int getProgress() {
        return DataSlots.join(data.get(ColonyReplicatorBlockEntity.DATA_PROGRESS), data.get(ColonyReplicatorBlockEntity.DATA_PROGRESS + 1));
    }

    public int getDuration() {
        return Math.max(1, DataSlots.join(data.get(ColonyReplicatorBlockEntity.DATA_DURATION),
                data.get(ColonyReplicatorBlockEntity.DATA_DURATION + 1)));
    }

    public int getStatus() {
        return data.get(ColonyReplicatorBlockEntity.DATA_STATUS);
    }

    public boolean isSplice() {
        return data.get(ColonyReplicatorBlockEntity.DATA_SPLICE) == 1;
    }

    public int getBiomass() {
        return DataSlots.join(data.get(ColonyReplicatorBlockEntity.DATA_BIOMASS), data.get(ColonyReplicatorBlockEntity.DATA_BIOMASS + 1));
    }
}
