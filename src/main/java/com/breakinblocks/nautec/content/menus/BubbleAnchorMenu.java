package com.breakinblocks.nautec.content.menus;

import com.breakinblocks.nautec.api.menu.NTMachineMenu;
import com.breakinblocks.nautec.content.bubble.BubbleAnchorBlockEntity;
import com.breakinblocks.nautec.utils.DataSlots;
import com.breakinblocks.nautec.registries.NTMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.neoforged.neoforge.transfer.item.ResourceHandlerSlot;
import org.jetbrains.annotations.NotNull;

public class BubbleAnchorMenu extends NTMachineMenu<BubbleAnchorBlockEntity> {
    private final ContainerData data;

    public BubbleAnchorMenu(int containerId, Inventory inv, FriendlyByteBuf extraData) {
        this(containerId, inv, (BubbleAnchorBlockEntity) inv.player.level().getBlockEntity(extraData.readBlockPos()),
                new SimpleContainerData(BubbleAnchorBlockEntity.DATA_COUNT));
    }

    public BubbleAnchorMenu(int containerId, @NotNull Inventory inv, @NotNull BubbleAnchorBlockEntity blockEntity) {
        this(containerId, inv, blockEntity, blockEntity.getData());
    }

    private BubbleAnchorMenu(int containerId, Inventory inv, BubbleAnchorBlockEntity blockEntity, ContainerData data) {
        super(NTMenuTypes.BUBBLE_ANCHOR.get(), containerId, inv, blockEntity);
        this.data = data;
        addDataSlots(data);
        addSlot(new ResourceHandlerSlot(blockEntity.getItemStackHandler(), blockEntity.getItemStackHandler()::set, 0, 26, 52));
    }

    @Override
    protected int getMergeableSlotCount() {
        return 1;
    }

    public boolean isEnabled() {
        return data.get(BubbleAnchorBlockEntity.DATA_ENABLED) == 1;
    }

    public boolean fillsWater() {
        return data.get(BubbleAnchorBlockEntity.DATA_FILL) == 1;
    }

    public boolean isAbove() {
        return data.get(BubbleAnchorBlockEntity.DATA_ABOVE) == 1;
    }

    public int getBurn() {
        return DataSlots.join(data.get(BubbleAnchorBlockEntity.DATA_BURN), data.get(BubbleAnchorBlockEntity.DATA_BURN + 1));
    }

    public int getBurnTotal() {
        return DataSlots.join(data.get(BubbleAnchorBlockEntity.DATA_TOTAL), data.get(BubbleAnchorBlockEntity.DATA_TOTAL + 1));
    }

    public int getStatus() {
        return data.get(BubbleAnchorBlockEntity.DATA_STATUS);
    }

    public int getRadius() {
        return data.get(BubbleAnchorBlockEntity.DATA_RADIUS);
    }
}
