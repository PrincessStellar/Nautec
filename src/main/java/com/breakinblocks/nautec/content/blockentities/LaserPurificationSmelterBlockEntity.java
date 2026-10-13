package com.breakinblocks.nautec.content.blockentities;

import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.api.blockentities.LaserBlockEntity;
import com.breakinblocks.nautec.api.sides.SlotRoles;
import com.breakinblocks.nautec.capabilities.IOActions;
import com.breakinblocks.nautec.capabilities.item.ItemStackHandler;
import com.breakinblocks.nautec.content.blocks.LaserPurificationSmelterBlock;
import com.breakinblocks.nautec.content.conduits.ConduitUpgradeItem;
import com.breakinblocks.nautec.content.conduits.TapRates;
import com.breakinblocks.nautec.content.menus.LaserPurificationSmelterMenu;
import com.breakinblocks.nautec.registries.NTBlockEntityTypes;
import com.breakinblocks.nautec.utils.DataSlots;
import com.breakinblocks.nautec.utils.RecipeRevision;
import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipePropertySet;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class LaserPurificationSmelterBlockEntity extends LaserBlockEntity implements MenuProvider {
    public static final int LANES = 9;
    public static final int INPUT_START = 0;
    public static final int OUTPUT_START = LANES;
    public static final int UPGRADE_SLOT = LANES * 2;
    public static final int SLOTS = UPGRADE_SLOT + 1;
    public static final int MAX_TIER = 4;
    private static final int SLOT_LIMIT = 64;
    private static final int DISTRIBUTE_INTERVAL = 10;

    public static final int STATUS_IDLE = 0;
    public static final int STATUS_SMELTING = 1;
    public static final int STATUS_WAITING = 2;
    public static final int STATUS_OUTPUT_FULL = 3;

    public static final int DATA_PROGRESS = 0;
    public static final int DATA_INTERVAL = 1;
    public static final int DATA_ITEMS_PER_INTERVAL = 2;
    public static final int DATA_POWER = 3;
    public static final int DATA_COST = 5;
    public static final int DATA_PURITY = 7;
    public static final int DATA_STATUS = 8;
    public static final int DATA_UNLOCKED = 9;
    public static final int DATA_TIER = 10;
    public static final int DATA_AUTO = 11;
    public static final int DATA_BUSY = 12;
    public static final int DATA_COUNT = 13;

    private static final SlotRoles ITEM_ROLES = SlotRoles.of(range(INPUT_START, LANES), range(OUTPUT_START, LANES));
    private static final int[] CARD_SLOTS = {UPGRADE_SLOT};
    private static final int[] NO_GHOSTS = new int[0];
    private static final Set<Direction>[] LASER_INPUTS = laserInputs();

    private final RecipeManager.CachedCheck<SingleRecipeInput, SmeltingRecipe> check = RecipeManager.createCheck(RecipeType.SMELTING);
    private final RecipeRevision recipeRevision = new RecipeRevision();
    private final ItemStack[] recipeKeys = new ItemStack[LANES];
    private final ItemStack[] recipeResults = new ItemStack[LANES];
    private final int[] room = new int[LANES];
    private final int[] smelted = new int[LANES];

    private int progress;
    private int cost;
    private int busy;
    private int status = STATUS_IDLE;
    private int cursor;
    private int distributeTimer;
    private boolean autoDistribute;

    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case DATA_PROGRESS -> progress;
                case DATA_INTERVAL -> rates().itemInterval();
                case DATA_ITEMS_PER_INTERVAL -> rates().itemsPerOp();
                case DATA_POWER -> DataSlots.low(getPower());
                case DATA_POWER + 1 -> DataSlots.high(getPower());
                case DATA_COST -> DataSlots.low(cost);
                case DATA_COST + 1 -> DataSlots.high(cost);
                case DATA_PURITY -> Math.round(getPurity() * 100F);
                case DATA_STATUS -> status;
                case DATA_UNLOCKED -> unlockedSlots();
                case DATA_TIER -> tier();
                case DATA_AUTO -> autoDistribute ? 1 : 0;
                case DATA_BUSY -> busy;
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

    public LaserPurificationSmelterBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(NTBlockEntityTypes.LASER_PURIFICATION_SMELTER.get(), blockPos, blockState);
        addItemHandler(SLOTS, slot -> slot == UPGRADE_SLOT ? 1 : SLOT_LIMIT, LaserPurificationSmelterBlockEntity::validFor);
        Arrays.fill(recipeKeys, ItemStack.EMPTY);
        Arrays.fill(recipeResults, ItemStack.EMPTY);
    }

    private static int[] range(int start, int count) {
        int[] slots = new int[count];
        for (int i = 0; i < count; i++) {
            slots[i] = start + i;
        }
        return slots;
    }

    @SuppressWarnings("unchecked")
    private static Set<Direction>[] laserInputs() {
        Set<Direction>[] sets = new Set[4];
        for (Direction front : Direction.Plane.HORIZONTAL) {
            EnumSet<Direction> inputs = EnumSet.of(Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST, Direction.UP);
            inputs.remove(front);
            sets[front.get2DDataValue()] = Collections.unmodifiableSet(inputs);
        }
        return sets;
    }

    private static boolean validFor(int slot, ItemStack stack) {
        if (slot == UPGRADE_SLOT) {
            return stack.getItem() instanceof ConduitUpgradeItem;
        }
        return slot < OUTPUT_START;
    }

    @Override
    protected boolean acceptsNow(int slot, ItemResource resource) {
        if (slot >= OUTPUT_START) {
            return true;
        }
        return isUnlocked(slot) && isSmeltable(resource.toStack());
    }

    public boolean isSmeltable(ItemStack stack) {
        return level == null || level.recipeAccess().propertySet(RecipePropertySet.FURNACE_INPUT).test(stack);
    }

    public static int tierOf(ItemStack stack) {
        return stack.getItem() instanceof ConduitUpgradeItem upgrade ? Math.max(0, Math.min(MAX_TIER, upgrade.tier())) : 0;
    }

    public int tier() {
        return tierOf(getItemStackHandler().getStackInSlot(UPGRADE_SLOT));
    }

    public static int slotsForTier(int tier) {
        int[] slots = NTConfig.smelterSlots;
        if (slots.length == 0) {
            return 1;
        }
        return Math.max(1, Math.min(LANES, slots[Math.max(0, Math.min(slots.length - 1, tier))]));
    }

    public static int multiplierForTier(int tier) {
        int[] multipliers = NTConfig.smelterApMultipliers;
        if (multipliers.length == 0) {
            return 1;
        }
        return Math.max(0, multipliers[Math.max(0, Math.min(multipliers.length - 1, tier))]);
    }

    public static int tierUnlocking(int slot) {
        for (int tier = 0; tier <= MAX_TIER; tier++) {
            if (slotsForTier(tier) > slot) {
                return tier;
            }
        }
        return -1;
    }

    public static int cost(int busySlots, int multiplier, float purity) {
        if (busySlots <= 0 || multiplier <= 0) {
            return 0;
        }
        double raw = busySlots * (double) multiplier / Math.max(1.0, purity);
        return (int) Math.ceil(raw - 1.0E-6);
    }

    public int unlockedSlots() {
        return slotsForTier(tier());
    }

    public boolean isUnlocked(int slot) {
        return slot < unlockedSlots();
    }

    public TapRates rates() {
        return TapRates.of(tier());
    }

    public ContainerData getData() {
        return data;
    }

    public int getProgress() {
        return progress;
    }

    public int getStatus() {
        return status;
    }

    public int getBusySlots() {
        return busy;
    }

    public boolean isAutoDistribute() {
        return autoDistribute;
    }

    public void setAutoDistribute(boolean autoDistribute) {
        if (this.autoDistribute == autoDistribute) {
            return;
        }
        this.autoDistribute = autoDistribute;
        this.distributeTimer = DISTRIBUTE_INTERVAL;
        update();
    }

    @Override
    public int getRequiredPower() {
        return cost;
    }

    @Override
    public float beamSpeed() {
        return cost > 0 && getPower() >= cost ? 1F : 0F;
    }

    @Override
    public int[] ghostSlots() {
        return NO_GHOSTS;
    }

    @Override
    public int[] cardSlots() {
        return CARD_SLOTS;
    }

    @Override
    public void saveSettings(ValueOutput out) {
        out.putBoolean("auto_distribute", autoDistribute);
    }

    @Override
    public boolean loadSettings(ValueInput in, ServerPlayer player) {
        Optional<Boolean> auto = in.read("auto_distribute", Codec.BOOL);
        if (auto.isEmpty()) {
            return false;
        }
        setAutoDistribute(auto.get());
        return true;
    }

    @Override
    public void commonTick() {
        super.commonTick();
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        if (recipeRevision.changed(serverLevel)) {
            Arrays.fill(recipeKeys, ItemStack.EMPTY);
            Arrays.fill(recipeResults, ItemStack.EMPTY);
        }
        if (autoDistribute && ++distributeTimer >= DISTRIBUTE_INTERVAL) {
            distributeTimer = 0;
            distribute();
        }

        int tier = tier();
        int unlocked = slotsForTier(tier);
        boolean smeltable = plan(serverLevel, unlocked);
        cost = cost(busy, multiplierForTier(tier), getPurity());

        int newStatus;
        if (busy == 0) {
            newStatus = smeltable ? STATUS_OUTPUT_FULL : STATUS_IDLE;
            progress = 0;
        } else if (getPower() < cost) {
            newStatus = STATUS_WAITING;
        } else {
            newStatus = STATUS_SMELTING;
            TapRates rates = TapRates.of(tier);
            progress++;
            if (progress >= rates.itemInterval()) {
                progress = 0;
                smelt(unlocked, rates.itemsPerOp());
            }
        }

        if (newStatus != status) {
            status = newStatus;
            update();
        }
        boolean active = status == STATUS_SMELTING;
        BlockState state = getBlockState();
        if (state.hasProperty(LaserPurificationSmelterBlock.ACTIVE) && state.getValue(LaserPurificationSmelterBlock.ACTIVE) != active) {
            level.setBlock(worldPosition, state.setValue(LaserPurificationSmelterBlock.ACTIVE, active), Block.UPDATE_CLIENTS);
        }
    }

    private ItemStack result(ServerLevel level, int lane, ItemStack input) {
        if (input.isEmpty()) {
            return ItemStack.EMPTY;
        }
        if (!recipeKeys[lane].isEmpty() && ItemStack.isSameItemSameComponents(recipeKeys[lane], input)) {
            return recipeResults[lane];
        }
        SingleRecipeInput recipeInput = new SingleRecipeInput(input.copyWithCount(1));
        ItemStack result = check.getRecipeFor(recipeInput, level).map(holder -> holder.value().assemble(recipeInput)).orElse(ItemStack.EMPTY);
        recipeKeys[lane] = input.copyWithCount(1);
        recipeResults[lane] = result;
        return result;
    }

    private static int roomFor(ItemStack output, ItemStack result) {
        int limit = Math.min(SLOT_LIMIT, result.getMaxStackSize());
        if (output.isEmpty()) {
            return limit / result.getCount();
        }
        if (!ItemStack.isSameItemSameComponents(output, result)) {
            return 0;
        }
        return Math.max(0, limit - output.getCount()) / result.getCount();
    }

    private boolean plan(ServerLevel level, int unlocked) {
        ItemStackHandler handler = getItemStackHandler();
        boolean smeltable = false;
        int busySlots = 0;
        for (int lane = 0; lane < LANES; lane++) {
            room[lane] = 0;
            if (lane >= unlocked) {
                continue;
            }
            ItemStack input = handler.getStackInSlot(INPUT_START + lane);
            ItemStack result = result(level, lane, input);
            if (result.isEmpty()) {
                continue;
            }
            smeltable = true;
            int fits = Math.min(input.getCount(), roomFor(handler.getStackInSlot(OUTPUT_START + lane), result));
            room[lane] = fits;
            if (fits > 0) {
                busySlots++;
            }
        }
        busy = busySlots;
        return smeltable;
    }

    private void smelt(int unlocked, int budget) {
        if (unlocked <= 0) {
            return;
        }
        Arrays.fill(smelted, 0);
        cursor = Math.floorMod(cursor, unlocked);
        boolean progressed = true;
        while (budget > 0 && progressed) {
            progressed = false;
            for (int step = 0; step < unlocked && budget > 0; step++) {
                int lane = (cursor + step) % unlocked;
                if (room[lane] > 0) {
                    room[lane]--;
                    smelted[lane]++;
                    budget--;
                    progressed = true;
                    if (budget == 0) {
                        cursor = (lane + 1) % unlocked;
                    }
                }
            }
        }
        ItemStackHandler handler = getItemStackHandler();
        for (int lane = 0; lane < unlocked; lane++) {
            int count = smelted[lane];
            if (count <= 0) {
                continue;
            }
            ItemStack input = handler.getStackInSlot(INPUT_START + lane);
            ItemStack result = recipeResults[lane];
            ItemStack output = handler.getStackInSlot(OUTPUT_START + lane);
            int made = result.getCount() * count;
            ItemStack newOutput = output.isEmpty() ? result.copyWithCount(made) : output.copyWithCount(output.getCount() + made);
            ItemStack newInput = input.getCount() > count ? input.copyWithCount(input.getCount() - count) : ItemStack.EMPTY;
            handler.setStackInSlot(OUTPUT_START + lane, newOutput);
            handler.setStackInSlot(INPUT_START + lane, newInput);
        }
    }

    public void distribute() {
        ItemStackHandler handler = getItemStackHandler();
        int slots = unlockedSlots();
        if (slots <= 1) {
            return;
        }
        ItemStack[] types = new ItemStack[slots];
        int[] totals = new int[slots];
        int[] caps = new int[slots];
        int typeCount = 0;
        long grand = 0;
        for (int slot = 0; slot < slots; slot++) {
            ItemStack stack = handler.getStackInSlot(INPUT_START + slot);
            if (stack.isEmpty()) {
                continue;
            }
            int type = -1;
            for (int t = 0; t < typeCount; t++) {
                if (ItemStack.isSameItemSameComponents(types[t], stack)) {
                    type = t;
                    break;
                }
            }
            if (type < 0) {
                type = typeCount++;
                types[type] = stack;
                caps[type] = Math.max(1, Math.min(SLOT_LIMIT, stack.getMaxStackSize()));
            }
            totals[type] += stack.getCount();
            grand += stack.getCount();
        }
        if (typeCount == 0) {
            return;
        }

        int[] alloc = new int[typeCount];
        int used = 0;
        for (int t = 0; t < typeCount; t++) {
            alloc[t] = Math.max(1, (totals[t] + caps[t] - 1) / caps[t]);
            used += alloc[t];
        }
        if (used > slots) {
            return;
        }
        while (used < slots) {
            int best = -1;
            long bestDeficit = Long.MIN_VALUE;
            for (int t = 0; t < typeCount; t++) {
                if (alloc[t] >= totals[t]) {
                    continue;
                }
                long deficit = (long) totals[t] * slots - (long) alloc[t] * grand;
                if (deficit > bestDeficit) {
                    bestDeficit = deficit;
                    best = t;
                }
            }
            if (best < 0) {
                break;
            }
            alloc[best]++;
            used++;
        }

        ItemStack[] layout = new ItemStack[slots];
        Arrays.fill(layout, ItemStack.EMPTY);
        int slot = 0;
        for (int t = 0; t < typeCount; t++) {
            int base = totals[t] / alloc[t];
            int extra = totals[t] % alloc[t];
            for (int k = 0; k < alloc[t]; k++) {
                int count = base + (k < extra ? 1 : 0);
                if (count > caps[t]) {
                    return;
                }
                layout[slot++] = count > 0 ? types[t].copyWithCount(count) : ItemStack.EMPTY;
            }
        }
        for (int i = 0; i < slots; i++) {
            if (!ItemStack.matches(handler.getStackInSlot(INPUT_START + i), layout[i])) {
                handler.setStackInSlot(INPUT_START + i, layout[i]);
            }
        }
    }

    @Override
    public Set<Direction> getLaserInputs() {
        BlockState state = getBlockState();
        Direction front = state.hasProperty(LaserPurificationSmelterBlock.FACING) ? state.getValue(LaserPurificationSmelterBlock.FACING) : Direction.NORTH;
        return LASER_INPUTS[front.get2DDataValue()];
    }

    @Override
    public Set<Direction> getLaserOutputs() {
        return Set.of();
    }

    @Override
    public SlotRoles itemRoles() {
        return ITEM_ROLES;
    }

    @Override
    public <T> Map<Direction, Pair<IOActions, int[]>> getSidedInteractions(BlockCapability<T, @Nullable Direction> capability) {
        return Map.of();
    }

    @Override
    public Component getDisplayName() {
        return getBlockState().getBlock().getName();
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new LaserPurificationSmelterMenu(containerId, inventory, this);
    }

    @Override
    protected void loadData(ValueInput in) {
        super.loadData(in);
        getItemStackHandler().ensureSize(SLOTS);
        this.progress = in.getIntOr("progress", 0);
        this.status = in.getIntOr("status", STATUS_IDLE);
        this.cursor = in.getIntOr("cursor", 0);
        this.autoDistribute = in.getBooleanOr("auto_distribute", false);
    }

    @Override
    protected void saveData(ValueOutput out) {
        super.saveData(out);
        out.putInt("progress", this.progress);
        out.putInt("status", this.status);
        out.putInt("cursor", this.cursor);
        out.putBoolean("auto_distribute", this.autoDistribute);
    }
}
