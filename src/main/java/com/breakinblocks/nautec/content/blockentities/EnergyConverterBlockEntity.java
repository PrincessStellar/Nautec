package com.breakinblocks.nautec.content.blockentities;

import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.api.blockentities.LaserBlockEntity;
import com.breakinblocks.nautec.capabilities.IOActions;
import com.breakinblocks.nautec.content.items.EnergyConversionUpgradeItem;
import com.breakinblocks.nautec.content.menus.EnergyConverterMenu;
import com.breakinblocks.nautec.utils.DataSlots;
import com.breakinblocks.nautec.registries.NTBlockEntityTypes;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.BlockCapability;
import com.breakinblocks.nautec.transfer.energy.EnergyHandler;
import com.breakinblocks.nautec.transfer.energy.SimpleEnergyHandler;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Set;
import com.breakinblocks.nautec.utils.valueio.ValueOutput;
import com.breakinblocks.nautec.utils.valueio.ValueInput;

public class EnergyConverterBlockEntity extends LaserBlockEntity implements MenuProvider {
    private static final String FE_BUFFER_KEY = "fe_buffer";
    private static final String SENDING_KEY = "sending";
    private static final String RATE_KEY = "rate";

    public static final int UPGRADE_SLOTS = 3;
    public static final int DATA_RATE = 0;
    public static final int DATA_MAX = 2;
    public static final int DATA_SENDING = 4;
    public static final int DATA_FE = 6;
    public static final int DATA_CAPACITY = 8;
    public static final int DATA_FE_PER_AP = 10;
    public static final int DATA_BEAMS = 12;
    public static final int DATA_COUNT = 13;

    private int sending;
    private int rate = -1;

    private final SimpleEnergyHandler feBuffer = new SimpleEnergyHandler(maxFe(), maxFe(), 0) {
        @Override
        protected void onEnergyChanged(int previousAmount) {
            setChanged();
        }
    };

    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case DATA_RATE -> DataSlots.low(getRate());
                case DATA_RATE + 1 -> DataSlots.high(getRate());
                case DATA_MAX -> DataSlots.low(getMaxRate());
                case DATA_MAX + 1 -> DataSlots.high(getMaxRate());
                case DATA_SENDING -> DataSlots.low(sending);
                case DATA_SENDING + 1 -> DataSlots.high(sending);
                case DATA_FE -> DataSlots.low(feBuffer.getAmountAsInt());
                case DATA_FE + 1 -> DataSlots.high(feBuffer.getAmountAsInt());
                case DATA_CAPACITY -> DataSlots.low(maxFe());
                case DATA_CAPACITY + 1 -> DataSlots.high(maxFe());
                case DATA_FE_PER_AP -> DataSlots.low(NTConfig.energyConverterFePerAp);
                case DATA_FE_PER_AP + 1 -> DataSlots.high(NTConfig.energyConverterFePerAp);
                case DATA_BEAMS -> connectedOutputs();
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
        }

        @Override
        public int getCount() {
            return DATA_COUNT;
        }
    };

    public EnergyConverterBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(NTBlockEntityTypes.ENERGY_CONVERTER.get(), blockPos, blockState);
        addItemHandler(UPGRADE_SLOTS, EnergyConversionUpgradeItem.MAX_PER_SLOT, EnergyConverterBlockEntity::acceptsUpgrade);
    }

    public static boolean acceptsUpgrade(int slot, ItemStack stack) {
        return stack.getItem() instanceof EnergyConversionUpgradeItem upgrade && upgrade.getTier().ordinal() == slot;
    }

    public int getMaxRate() {
        long max = NTConfig.energyConverterBaseAp;
        for (int slot = 0; slot < UPGRADE_SLOTS; slot++) {
            ItemStack stack = getItemStackHandler().getStackInSlot(slot);
            if (stack.getItem() instanceof EnergyConversionUpgradeItem upgrade) {
                max += (long) upgrade.getTier().ap() * stack.getCount();
            }
        }
        return (int) Math.min(Integer.MAX_VALUE, max);
    }

    public EnergyHandler getFeBuffer() {
        return feBuffer;
    }

    public ContainerData getData() {
        return data;
    }

    public int getRate() {
        int max = getMaxRate();
        return rate < 0 ? max : Math.min(rate, max);
    }

    public void setRate(int rate) {
        int clamped = Mth.clamp(rate, 0, getMaxRate());
        if (clamped != this.rate) {
            this.rate = clamped;
            setChanged();
        }
    }

    @Override
    public Set<Direction> getLaserInputs() {
        return ObjectSet.of();
    }

    @Override
    public Set<Direction> getLaserOutputs() {
        return ObjectSet.of(Direction.values());
    }

    @Override
    public <T> Map<Direction, Pair<IOActions, int[]>> getSidedInteractions(BlockCapability<T, @Nullable Direction> capability) {
        return Map.of();
    }

    @Override
    public void commonTick() {
        super.commonTick();

        if (level.isClientSide()) {
            transmitPower(connectedOutputs() > 0 ? sending : 0);
            return;
        }

        int fePerAp = Math.max(1, NTConfig.energyConverterFePerAp);
        int sent = 0;
        if (connectedOutputs() > 0) {
            sent = (int) Math.min(getRate(), feBuffer.getAmountAsInt() / (long) fePerAp);
            if (sent > 0) {
                feBuffer.set((int) (feBuffer.getAmountAsInt() - (long) sent * fePerAp));
            }
        }
        transmitPower(sent);
        if (sent != sending) {
            sending = sent;
            update();
        }
    }

    public int getSending() {
        return sending;
    }

    public int getBeams() {
        return connectedOutputs();
    }

    public int getFeStored() {
        return feBuffer.getAmountAsInt();
    }

    public static int maxFe() {
        return NTConfig.energyConverterFeCapacity;
    }

    @Override
    protected int outgoingPower(Direction direction) {
        return getPowerToTransfer() / Math.max(1, connectedOutputs());
    }

    @Override
    public Component getDisplayName() {
        return getBlockState().getBlock().getName();
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new EnergyConverterMenu(containerId, inventory, this);
    }

    @Override
    protected void saveData(ValueOutput out) {
        super.saveData(out);
        feBuffer.serialize(out.child(FE_BUFFER_KEY));
        out.putInt(SENDING_KEY, sending);
        out.putInt(RATE_KEY, rate);
    }

    @Override
    protected void loadData(ValueInput in) {
        super.loadData(in);
        feBuffer.deserialize(in.childOrEmpty(FE_BUFFER_KEY));
        sending = in.getIntOr(SENDING_KEY, 0);
        rate = in.getIntOr(RATE_KEY, -1);
    }
}
