package com.breakinblocks.nautec.content.menus;

import com.breakinblocks.nautec.api.menu.NTMachineMenu;
import com.breakinblocks.nautec.capabilities.item.ItemStackHandler;
import com.breakinblocks.nautec.content.blockentities.AdvancedBacterialAnalyzerBlockEntity;
import com.breakinblocks.nautec.registries.NTMenuTypes;
import com.breakinblocks.nautec.utils.DataSlots;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.neoforged.neoforge.transfer.item.ResourceHandlerSlot;
import org.jetbrains.annotations.NotNull;

public class AdvancedBacterialAnalyzerMenu extends NTMachineMenu<AdvancedBacterialAnalyzerBlockEntity> {
    public static final int INPUT_X = 17;
    public static final int OUTPUT_X = 107;
    public static final int GRID_Y = 17;

    private final ContainerData data;

    public AdvancedBacterialAnalyzerMenu(int containerId, Inventory inv, FriendlyByteBuf extraData) {
        this(containerId, inv, (AdvancedBacterialAnalyzerBlockEntity) inv.player.level().getBlockEntity(extraData.readBlockPos()),
                new SimpleContainerData(AdvancedBacterialAnalyzerBlockEntity.DATA_COUNT));
    }

    public AdvancedBacterialAnalyzerMenu(int containerId, @NotNull Inventory inv, @NotNull AdvancedBacterialAnalyzerBlockEntity blockEntity) {
        this(containerId, inv, blockEntity, blockEntity.getData());
    }

    private AdvancedBacterialAnalyzerMenu(int containerId, Inventory inv, AdvancedBacterialAnalyzerBlockEntity blockEntity, ContainerData data) {
        super(NTMenuTypes.ADVANCED_BACTERIAL_ANALYZER.get(), containerId, inv, blockEntity);
        this.data = data;
        addDataSlots(data);

        ItemStackHandler handler = blockEntity.getItemStackHandler();
        for (int i = 0; i < AdvancedBacterialAnalyzerBlockEntity.DISHES; i++) {
            addSlot(new ResourceHandlerSlot(handler, handler::set, i, INPUT_X + (i % 3) * 18, GRID_Y + (i / 3) * 18));
        }
        for (int i = 0; i < AdvancedBacterialAnalyzerBlockEntity.DISHES; i++) {
            addSlot(new ResourceHandlerSlot(handler, handler::set, AdvancedBacterialAnalyzerBlockEntity.FIRST_OUTPUT + i,
                    OUTPUT_X + (i % 3) * 18, GRID_Y + (i / 3) * 18));
        }
    }

    @Override
    protected int getMergeableSlotCount() {
        return AdvancedBacterialAnalyzerBlockEntity.DISHES;
    }

    public int getProgress(int dish) {
        return DataSlots.join(data.get(dish * 2), data.get(dish * 2 + 1));
    }

    public int getDuration() {
        return Math.max(1, DataSlots.join(data.get(AdvancedBacterialAnalyzerBlockEntity.DATA_DURATION),
                data.get(AdvancedBacterialAnalyzerBlockEntity.DATA_DURATION + 1)));
    }

    public int getStatus() {
        return data.get(AdvancedBacterialAnalyzerBlockEntity.DATA_STATUS);
    }
}
