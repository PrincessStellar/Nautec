package com.breakinblocks.nautec.content.menus;

import com.breakinblocks.nautec.api.menu.NTAbstractContainerMenu;
import com.breakinblocks.nautec.content.resonance.ResonanceNodeBlockEntity;
import com.breakinblocks.nautec.utils.DataSlots;
import com.breakinblocks.nautec.registries.NTMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ResonanceNodeMenu extends NTAbstractContainerMenu<ResonanceNodeBlockEntity> {
    private final ContainerData data;

    public ResonanceNodeMenu(int containerId, Inventory inv, FriendlyByteBuf extraData) {
        this(containerId, inv, (ResonanceNodeBlockEntity) inv.player.level().getBlockEntity(extraData.readBlockPos()),
                new SimpleContainerData(ResonanceNodeBlockEntity.DATA_COUNT));
    }

    public ResonanceNodeMenu(int containerId, @NotNull Inventory inv, @NotNull ResonanceNodeBlockEntity blockEntity) {
        this(containerId, inv, blockEntity, blockEntity.getData());
    }

    private ResonanceNodeMenu(int containerId, Inventory inv, ResonanceNodeBlockEntity blockEntity, ContainerData data) {
        super(NTMenuTypes.RESONANCE_NODE.get(), containerId, inv, blockEntity);
        this.data = data;
        addDataSlots(data);
    }

    @Override
    protected int getMergeableSlotCount() {
        return 0;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    private int wide(int index) {
        return DataSlots.join(data.get(index), data.get(index + 1));
    }

    public int getFlow() {
        return wide(ResonanceNodeBlockEntity.DATA_FLOW);
    }

    public float getPurity() {
        return data.get(ResonanceNodeBlockEntity.DATA_PURITY) / 1000F;
    }

    public int getStatus() {
        return data.get(ResonanceNodeBlockEntity.DATA_STATUS);
    }

    public int getCores() {
        return data.get(ResonanceNodeBlockEntity.DATA_CORES);
    }

    public boolean isOutput() {
        return data.get(ResonanceNodeBlockEntity.DATA_OUTPUT) == 1;
    }

    public int getAp() {
        return wide(ResonanceNodeBlockEntity.DATA_AP);
    }

    public int getFe() {
        return wide(ResonanceNodeBlockEntity.DATA_FE);
    }

    public int getPriority() {
        return (short) data.get(ResonanceNodeBlockEntity.DATA_PRIORITY);
    }

    public int getLimit() {
        return wide(ResonanceNodeBlockEntity.DATA_LIMIT);
    }

    public int getChunkState() {
        return data.get(ResonanceNodeBlockEntity.DATA_CHUNK);
    }
}
