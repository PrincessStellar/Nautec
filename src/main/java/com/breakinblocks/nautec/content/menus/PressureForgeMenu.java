package com.breakinblocks.nautec.content.menus;

import com.breakinblocks.nautec.api.menu.NTMachineMenu;
import com.breakinblocks.nautec.api.menu.slots.SlotFluidHandler;
import com.breakinblocks.nautec.capabilities.item.ItemStackHandler;
import com.breakinblocks.nautec.content.blockentities.PressureForgeBlockEntity;
import com.breakinblocks.nautec.content.items.PressureSynthesizerItem;
import com.breakinblocks.nautec.registries.NTMenuTypes;
import com.breakinblocks.nautec.utils.DataSlots;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.item.ResourceHandlerSlot;
import org.jetbrains.annotations.NotNull;

public class PressureForgeMenu extends NTMachineMenu<PressureForgeBlockEntity> {
    public static final int INVENTORY_Y = 120;
    public static final int TANK_X = 8;
    public static final int TANK_Y = 18;
    public static final int TANK_WIDTH = 18;
    public static final int TANK_HEIGHT = 34;
    public static final int INPUT_X = 44;
    public static final int OUTPUT_X = 102;
    public static final int SYNTHESIZER_X = 152;
    public static final int ROW_Y = 27;

    private final ContainerData data;

    public PressureForgeMenu(int containerId, Inventory inv, FriendlyByteBuf extraData) {
        this(containerId, inv, (PressureForgeBlockEntity) inv.player.level().getBlockEntity(extraData.readBlockPos()),
                new SimpleContainerData(PressureForgeBlockEntity.DATA_COUNT));
    }

    public PressureForgeMenu(int containerId, @NotNull Inventory inv, @NotNull PressureForgeBlockEntity blockEntity) {
        this(containerId, inv, blockEntity, blockEntity.getData());
    }

    private PressureForgeMenu(int containerId, Inventory inv, PressureForgeBlockEntity blockEntity, ContainerData data) {
        super(NTMenuTypes.PRESSURE_FORGE.get(), containerId, inv, blockEntity, INVENTORY_Y);
        this.data = data;
        addDataSlots(data);
        ItemStackHandler handler = blockEntity.getItemStackHandler();
        addSlot(new SynthesizerSlot(new SynthesizerContainer(blockEntity), SYNTHESIZER_X, ROW_Y));
        addSlot(new ResourceHandlerSlot(handler, handler::set, 0, INPUT_X, ROW_Y));
        addSlot(new OutputSlot(handler, 1, OUTPUT_X, ROW_Y));
        addFluidHandlerSlot(new SlotFluidHandler(blockEntity.getFluidTank(), 0, TANK_X, TANK_Y, TANK_WIDTH, TANK_HEIGHT));
    }

    @Override
    protected int getMergeableSlotCount() {
        return 2;
    }

    private int wide(int index) {
        return DataSlots.join(data.get(index), data.get(index + 1));
    }

    public int getProgress() {
        return data.get(PressureForgeBlockEntity.DATA_PROGRESS);
    }

    public int getDuration() {
        return data.get(PressureForgeBlockEntity.DATA_DURATION);
    }

    public int getPower() {
        return wide(PressureForgeBlockEntity.DATA_POWER);
    }

    public float getPurity() {
        return data.get(PressureForgeBlockEntity.DATA_PURITY) / 100F;
    }

    public boolean hasRecipe() {
        return data.get(PressureForgeBlockEntity.DATA_RECIPE_PURITY) != PressureForgeBlockEntity.NO_VALUE;
    }

    public float getRecipePurity() {
        return data.get(PressureForgeBlockEntity.DATA_RECIPE_PURITY) / 100F;
    }

    public int getDepthLimit() {
        return data.get(PressureForgeBlockEntity.DATA_DEPTH_LIMIT);
    }

    public int getY() {
        return data.get(PressureForgeBlockEntity.DATA_Y);
    }

    public int getWater() {
        return data.get(PressureForgeBlockEntity.DATA_WATER);
    }

    public PressureForgeBlockEntity.Synthesizer getSynthesizer() {
        int ordinal = data.get(PressureForgeBlockEntity.DATA_SYNTHESIZER);
        PressureForgeBlockEntity.Synthesizer[] values = PressureForgeBlockEntity.Synthesizer.values();
        return ordinal >= 0 && ordinal < values.length ? values[ordinal] : PressureForgeBlockEntity.Synthesizer.NONE;
    }

    public int getStatus() {
        return data.get(PressureForgeBlockEntity.DATA_STATUS);
    }

    public int getAcid() {
        return wide(PressureForgeBlockEntity.DATA_ACID);
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

    private static final class SynthesizerSlot extends Slot {
        private SynthesizerSlot(Container container, int x, int y) {
            super(container, 0, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return stack.getItem() instanceof PressureSynthesizerItem;
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }
    }

    private record SynthesizerContainer(PressureForgeBlockEntity forge) implements Container {
        @Override
        public int getContainerSize() {
            return 1;
        }

        @Override
        public boolean isEmpty() {
            return forge.getSynthesizer() == PressureForgeBlockEntity.Synthesizer.NONE;
        }

        @Override
        public ItemStack getItem(int slot) {
            return forge.getSynthesizer().stack();
        }

        @Override
        public ItemStack removeItem(int slot, int amount) {
            if (amount <= 0 || isEmpty()) {
                return ItemStack.EMPTY;
            }
            return forge.setSynthesizer(PressureForgeBlockEntity.Synthesizer.NONE);
        }

        @Override
        public ItemStack removeItemNoUpdate(int slot) {
            return removeItem(slot, 1);
        }

        @Override
        public void setItem(int slot, ItemStack stack) {
            PressureForgeBlockEntity.Synthesizer tier = stack.getItem() instanceof PressureSynthesizerItem synthesizer
                    ? synthesizer.tier() : PressureForgeBlockEntity.Synthesizer.NONE;
            if (tier != forge.getSynthesizer()) {
                forge.setSynthesizer(tier);
            }
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }

        @Override
        public void setChanged() {
            forge.setChanged();
        }

        @Override
        public boolean stillValid(Player player) {
            return !forge.isRemoved();
        }

        @Override
        public void clearContent() {
            setItem(0, ItemStack.EMPTY);
        }
    }
}
