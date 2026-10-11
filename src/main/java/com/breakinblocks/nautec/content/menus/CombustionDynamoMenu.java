package com.breakinblocks.nautec.content.menus;

import com.breakinblocks.nautec.api.menu.NTMachineMenu;
import com.breakinblocks.nautec.api.menu.slots.SlotFluidHandler;
import com.breakinblocks.nautec.capabilities.item.ItemStackHandler;
import com.breakinblocks.nautec.content.blockentities.generators.CombustionDynamoBlockEntity;
import com.breakinblocks.nautec.utils.DataSlots;
import com.breakinblocks.nautec.registries.NTMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import com.breakinblocks.nautec.transfer.item.ResourceHandlerSlot;
import org.jetbrains.annotations.NotNull;

public class CombustionDynamoMenu extends NTMachineMenu<CombustionDynamoBlockEntity> {
    public static final int OIL_X = 26;
    public static final int WATER_X = 50;
    public static final int TANK_Y = 18;
    public static final int TANK_WIDTH = 18;
    public static final int TANK_HEIGHT = 52;
    public static final int ADDITIVE_X = 80;
    public static final int ADDITIVE_Y = 35;

    private final ContainerData data;

    public CombustionDynamoMenu(int containerId, Inventory inv, FriendlyByteBuf extraData) {
        this(containerId, inv, (CombustionDynamoBlockEntity) inv.player.level().getBlockEntity(extraData.readBlockPos()),
                new SimpleContainerData(CombustionDynamoBlockEntity.DATA_COUNT));
    }

    public CombustionDynamoMenu(int containerId, @NotNull Inventory inv, @NotNull CombustionDynamoBlockEntity blockEntity) {
        this(containerId, inv, blockEntity, blockEntity.getData());
    }

    private CombustionDynamoMenu(int containerId, Inventory inv, CombustionDynamoBlockEntity blockEntity, ContainerData data) {
        super(NTMenuTypes.COMBUSTION_DYNAMO.get(), containerId, inv, blockEntity);
        this.data = data;
        addDataSlots(data);
        ItemStackHandler handler = blockEntity.getItemStackHandler();
        addSlot(new ResourceHandlerSlot(handler, handler::set, CombustionDynamoBlockEntity.ADDITIVE_SLOT, ADDITIVE_X, ADDITIVE_Y));
        addFluidHandlerSlot(new SlotFluidHandler(blockEntity.getFluidTank(), 0, OIL_X, TANK_Y, TANK_WIDTH, TANK_HEIGHT));
        addFluidHandlerSlot(new SlotFluidHandler(blockEntity.getSecondaryFluidTank(), 0, WATER_X, TANK_Y, TANK_WIDTH, TANK_HEIGHT));
    }

    @Override
    protected int getMergeableSlotCount() {
        return 1;
    }

    private int wide(int index) {
        return DataSlots.join(data.get(index), data.get(index + 1));
    }

    public int getFeStored() {
        return wide(CombustionDynamoBlockEntity.DATA_FE);
    }

    public int getCapacity() {
        return wide(CombustionDynamoBlockEntity.DATA_CAPACITY);
    }

    public int getRate() {
        return wide(CombustionDynamoBlockEntity.DATA_RATE);
    }

    public int getAdditiveLeft() {
        return wide(CombustionDynamoBlockEntity.DATA_ADDITIVE_LEFT);
    }

    public int getAdditiveTotal() {
        return wide(CombustionDynamoBlockEntity.DATA_ADDITIVE_TOTAL);
    }

    public CombustionDynamoBlockEntity.Status getStatus() {
        return CombustionDynamoBlockEntity.Status.byId(data.get(CombustionDynamoBlockEntity.DATA_STATUS));
    }

    public int getFuelPercent() {
        return data.get(CombustionDynamoBlockEntity.DATA_FUEL_PERCENT);
    }
}
