package com.breakinblocks.nautec.content.menus;

import com.breakinblocks.nautec.api.menu.NTMachineMenu;
import com.breakinblocks.nautec.capabilities.item.ItemStackHandler;
import com.breakinblocks.nautec.content.blockentities.EnergyConverterBlockEntity;
import com.breakinblocks.nautec.utils.DataSlots;
import com.breakinblocks.nautec.registries.NTMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import com.breakinblocks.nautec.transfer.item.ResourceHandlerSlot;
import org.jetbrains.annotations.NotNull;

public class EnergyConverterMenu extends NTMachineMenu<EnergyConverterBlockEntity> {
    public static final int SLOT_X = 8;
    public static final int SLOT_Y = 52;

    private final ContainerData data;

    public EnergyConverterMenu(int containerId, Inventory inv, FriendlyByteBuf extraData) {
        this(containerId, inv, (EnergyConverterBlockEntity) inv.player.level().getBlockEntity(extraData.readBlockPos()),
                new SimpleContainerData(EnergyConverterBlockEntity.DATA_COUNT));
    }

    public EnergyConverterMenu(int containerId, @NotNull Inventory inv, @NotNull EnergyConverterBlockEntity blockEntity) {
        this(containerId, inv, blockEntity, blockEntity.getData());
    }

    private EnergyConverterMenu(int containerId, Inventory inv, EnergyConverterBlockEntity blockEntity, ContainerData data) {
        super(NTMenuTypes.ENERGY_CONVERTER.get(), containerId, inv, blockEntity);
        this.data = data;
        addDataSlots(data);
        ItemStackHandler handler = blockEntity.getItemStackHandler();
        for (int slot = 0; slot < EnergyConverterBlockEntity.UPGRADE_SLOTS; slot++) {
            addSlot(new ResourceHandlerSlot(handler, handler::set, slot, SLOT_X + slot * 18, SLOT_Y));
        }
    }

    @Override
    protected int getMergeableSlotCount() {
        return EnergyConverterBlockEntity.UPGRADE_SLOTS;
    }

    private int wide(int index) {
        return DataSlots.join(data.get(index), data.get(index + 1));
    }

    public int getRate() {
        return wide(EnergyConverterBlockEntity.DATA_RATE);
    }

    public int getMaxRate() {
        return wide(EnergyConverterBlockEntity.DATA_MAX);
    }

    public int getSending() {
        return wide(EnergyConverterBlockEntity.DATA_SENDING);
    }

    public int getFeStored() {
        return wide(EnergyConverterBlockEntity.DATA_FE);
    }

    public int getCapacity() {
        return wide(EnergyConverterBlockEntity.DATA_CAPACITY);
    }

    public int getFePerAp() {
        return wide(EnergyConverterBlockEntity.DATA_FE_PER_AP);
    }

    public int getBeams() {
        return data.get(EnergyConverterBlockEntity.DATA_BEAMS);
    }
}
