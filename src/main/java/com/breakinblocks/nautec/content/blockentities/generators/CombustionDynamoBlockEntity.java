package com.breakinblocks.nautec.content.blockentities.generators;

import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.api.sides.SlotRoles;
import com.breakinblocks.nautec.content.blocks.generators.CombustionDynamoBlock;
import com.breakinblocks.nautec.content.menus.CombustionDynamoMenu;
import com.breakinblocks.nautec.content.recipes.CombustionAdditiveRecipe;
import com.breakinblocks.nautec.utils.DataSlots;
import com.breakinblocks.nautec.registries.NTBlockEntityTypes;
import com.breakinblocks.nautec.tags.NTTags;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import com.breakinblocks.nautec.utils.valueio.ValueInput;
import com.breakinblocks.nautec.utils.valueio.ValueOutput;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.Optional;

public class CombustionDynamoBlockEntity extends FeGeneratorBlockEntity implements MenuProvider {
    public static final int BUFFER = 100_000;
    public static final int ADDITIVE_SLOT = 0;
    private static final int MILLI = 1000;

    public static final int DATA_FE = 0;
    public static final int DATA_CAPACITY = 2;
    public static final int DATA_RATE = 4;
    public static final int DATA_ADDITIVE_LEFT = 6;
    public static final int DATA_ADDITIVE_TOTAL = 8;
    public static final int DATA_STATUS = 10;
    public static final int DATA_FUEL_PERCENT = 11;
    public static final int DATA_COUNT = 12;

    private static final SlotRoles ITEM_ROLES = SlotRoles.of(new int[]{ADDITIVE_SLOT}, new int[0]);
    private static final SlotRoles FLUID_ROLES = SlotRoles.of(new int[]{0, 1}, new int[0]);

    private Status status = Status.NO_OIL;
    private int rate;
    private int oilDebt;
    private int waterDebt;
    private int additiveLeft;
    private int additiveTotal;
    private float outputMultiplier = 1F;
    private float fuelMultiplier = 1F;

    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case DATA_FE -> DataSlots.low(getEnergyStorage().getAmountAsInt());
                case DATA_FE + 1 -> DataSlots.high(getEnergyStorage().getAmountAsInt());
                case DATA_CAPACITY -> DataSlots.low(getEnergyStorage().getCapacityAsInt());
                case DATA_CAPACITY + 1 -> DataSlots.high(getEnergyStorage().getCapacityAsInt());
                case DATA_RATE -> DataSlots.low(rate);
                case DATA_RATE + 1 -> DataSlots.high(rate);
                case DATA_ADDITIVE_LEFT -> DataSlots.low(additiveLeft);
                case DATA_ADDITIVE_LEFT + 1 -> DataSlots.high(additiveLeft);
                case DATA_ADDITIVE_TOTAL -> DataSlots.low(additiveTotal);
                case DATA_ADDITIVE_TOTAL + 1 -> DataSlots.high(additiveTotal);
                case DATA_STATUS -> status.ordinal();
                case DATA_FUEL_PERCENT -> Math.round(fuelMultiplier * 100);
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

    public CombustionDynamoBlockEntity(BlockPos pos, BlockState state) {
        super(NTBlockEntityTypes.COMBUSTION_DYNAMO.get(), pos, state, BUFFER);
        addItemHandler(1, (slot, stack) -> isAdditive(stack));
        addFluidTank(NTConfig.combustionDynamoTankCapacity, stack -> stack.is(NTTags.Fluids.OIL));
        addSecondaryFluidTank(NTConfig.combustionDynamoTankCapacity, stack -> stack.is(FluidTags.WATER));
    }

    @Override
    public SlotRoles itemRoles() {
        return ITEM_ROLES;
    }

    @Override
    public SlotRoles fluidRoles() {
        return FLUID_ROLES;
    }

    public ContainerData getData() {
        return data;
    }

    public Status getStatus() {
        return status;
    }

    public int getRate() {
        return rate;
    }

    public int getAdditiveLeft() {
        return additiveLeft;
    }

    public float getOutputMultiplier() {
        return additiveLeft > 0 ? outputMultiplier : 1F;
    }

    public float getFuelMultiplier() {
        return additiveLeft > 0 ? fuelMultiplier : 1F;
    }

    public boolean isAdditive(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        if (!(level instanceof ServerLevel serverLevel)) {
            return true;
        }
        return findAdditive(serverLevel, stack).isPresent();
    }

    public static Optional<RecipeHolder<CombustionAdditiveRecipe>> findAdditive(ServerLevel level, ItemStack stack) {
        return level.getRecipeManager().getRecipeFor(CombustionAdditiveRecipe.Type.INSTANCE, new SingleRecipeInput(stack), level);
    }

    public static int baseRate() {
        return NTConfig.combustionDynamoOutput;
    }

    @Override
    protected void serverTick(ServerLevel level) {
        burn(level);
        boolean lit = status == Status.RUNNING;
        BlockState state = getBlockState();
        if (state.hasProperty(CombustionDynamoBlock.LIT) && state.getValue(CombustionDynamoBlock.LIT) != lit) {
            level.setBlock(worldPosition, state.setValue(CombustionDynamoBlock.LIT, lit), Block.UPDATE_CLIENTS);
        }
    }

    private void burn(ServerLevel level) {
        this.rate = 0;
        if (getFluidTank().isEmpty()) {
            status = Status.NO_OIL;
            return;
        }
        if (getSecondaryFluidTank().isEmpty() && NTConfig.combustionDynamoWaterPerTick > 0) {
            status = Status.NO_WATER;
            return;
        }
        if (additiveLeft <= 0) {
            loadAdditive(level);
        }
        int output = Math.round(baseRate() * getOutputMultiplier());
        if (space() < output) {
            status = Status.BUFFER_FULL;
            return;
        }
        float fuel = getFuelMultiplier();
        int nextOil = oilDebt + Math.round(fuel * MILLI / Math.max(1, NTConfig.combustionDynamoTicksPerOil));
        int nextWater = waterDebt + Math.round(fuel * MILLI * NTConfig.combustionDynamoWaterPerTick);
        if (getFluidTank().getFluidAmount() < ceilMb(nextOil)) {
            status = Status.NO_OIL;
            return;
        }
        if (getSecondaryFluidTank().getFluidAmount() < ceilMb(nextWater)) {
            status = Status.NO_WATER;
            return;
        }
        int oil = nextOil / MILLI;
        int water = nextWater / MILLI;
        if (oil > 0) {
            getFluidTank().drain(oil);
        }
        if (water > 0) {
            getSecondaryFluidTank().drain(water);
        }
        this.oilDebt = nextOil - oil * MILLI;
        this.waterDebt = nextWater - water * MILLI;
        generate(output);
        this.rate = output;
        status = Status.RUNNING;
        if (additiveLeft > 0) {
            additiveLeft--;
        }
        setChanged();
    }

    private static int ceilMb(int milli) {
        return (milli + MILLI - 1) / MILLI;
    }

    private void loadAdditive(ServerLevel level) {
        ItemStack stack = getItemStackHandler().getStackInSlot(ADDITIVE_SLOT);
        if (stack.isEmpty()) {
            return;
        }
        findAdditive(level, stack).ifPresent(holder -> {
            CombustionAdditiveRecipe recipe = holder.value();
            this.outputMultiplier = recipe.outputMultiplier();
            this.fuelMultiplier = recipe.fuelMultiplier();
            this.additiveTotal = recipe.duration();
            this.additiveLeft = recipe.duration();
            getItemStackHandler().setStackInSlot(ADDITIVE_SLOT, stack.copyWithCount(stack.getCount() - 1));
        });
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.nautec.combustion_dynamo");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new CombustionDynamoMenu(containerId, inventory, this);
    }

    @Override
    protected void saveData(ValueOutput out) {
        super.saveData(out);
        out.putInt("oilDebt", oilDebt);
        out.putInt("waterDebt", waterDebt);
        out.putInt("additiveLeft", additiveLeft);
        out.putInt("additiveTotal", additiveTotal);
        out.putFloat("outputMultiplier", outputMultiplier);
        out.putFloat("fuelMultiplier", fuelMultiplier);
    }

    @Override
    protected void loadData(ValueInput in) {
        super.loadData(in);
        this.oilDebt = in.getIntOr("oilDebt", 0);
        this.waterDebt = in.getIntOr("waterDebt", 0);
        this.additiveLeft = in.getIntOr("additiveLeft", 0);
        this.additiveTotal = in.getIntOr("additiveTotal", 0);
        this.outputMultiplier = in.getFloatOr("outputMultiplier", 1F);
        this.fuelMultiplier = in.getFloatOr("fuelMultiplier", 1F);
    }

    public enum Status {
        RUNNING,
        NO_OIL,
        NO_WATER,
        BUFFER_FULL;

        public String translationKey() {
            return "nautec.combustion_dynamo.status." + name().toLowerCase(Locale.ROOT);
        }

        public static Status byId(int id) {
            Status[] values = values();
            return id >= 0 && id < values.length ? values[id] : NO_OIL;
        }
    }
}
