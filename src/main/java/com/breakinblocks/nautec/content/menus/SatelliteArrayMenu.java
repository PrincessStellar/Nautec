package com.breakinblocks.nautec.content.menus;

import com.breakinblocks.nautec.api.menu.NTAbstractContainerMenu;
import com.breakinblocks.nautec.utils.DataSlots;
import com.breakinblocks.nautec.content.resonance.SatelliteArrayBlockEntity;
import com.breakinblocks.nautec.registries.NTMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class SatelliteArrayMenu extends NTAbstractContainerMenu<SatelliteArrayBlockEntity> {
    private final ContainerData data;

    public SatelliteArrayMenu(int containerId, Inventory inv, FriendlyByteBuf extraData) {
        this(containerId, inv, (SatelliteArrayBlockEntity) inv.player.level().getBlockEntity(extraData.readBlockPos()),
                new SimpleContainerData(SatelliteArrayBlockEntity.DATA_COUNT));
    }

    public SatelliteArrayMenu(int containerId, @NotNull Inventory inv, @NotNull SatelliteArrayBlockEntity blockEntity) {
        this(containerId, inv, blockEntity, blockEntity.getData());
    }

    private SatelliteArrayMenu(int containerId, Inventory inv, SatelliteArrayBlockEntity blockEntity, ContainerData data) {
        super(NTMenuTypes.SATELLITE_ARRAY.get(), containerId, inv, blockEntity);
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

    public int getPower() {
        return DataSlots.join(data.get(SatelliteArrayBlockEntity.DATA_POWER), data.get(SatelliteArrayBlockEntity.DATA_POWER + 1));
    }

    public float getPurity() {
        return data.get(SatelliteArrayBlockEntity.DATA_PURITY) / 1000F;
    }

    public int getStatus() {
        return data.get(SatelliteArrayBlockEntity.DATA_STATUS);
    }

    public int getUplinks() {
        return data.get(SatelliteArrayBlockEntity.DATA_UPLINKS);
    }

    public int getAp() {
        return DataSlots.join(data.get(SatelliteArrayBlockEntity.DATA_AP), data.get(SatelliteArrayBlockEntity.DATA_AP + 1));
    }

    public int getFe() {
        return DataSlots.join(data.get(SatelliteArrayBlockEntity.DATA_FE), data.get(SatelliteArrayBlockEntity.DATA_FE + 1));
    }

    public int getChunkState() {
        return data.get(SatelliteArrayBlockEntity.DATA_CHUNK);
    }

    public int getNodes() {
        return data.get(SatelliteArrayBlockEntity.DATA_NODES);
    }

}
