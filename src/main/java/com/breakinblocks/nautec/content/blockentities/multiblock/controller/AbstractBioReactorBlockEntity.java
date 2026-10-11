package com.breakinblocks.nautec.content.blockentities.multiblock.controller;

import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.NTRegistries;
import com.breakinblocks.nautec.api.bacteria.Bacteria;
import com.breakinblocks.nautec.api.bacteria.BacteriaInstance;
import com.breakinblocks.nautec.api.blockentities.LaserBlockEntity;
import com.breakinblocks.nautec.api.blockentities.multiblock.MultiblockEntity;
import com.breakinblocks.nautec.api.multiblocks.Multiblock;
import com.breakinblocks.nautec.api.multiblocks.MultiblockData;
import com.breakinblocks.nautec.api.sides.RelativeFace;
import com.breakinblocks.nautec.api.sides.SideKind;
import com.breakinblocks.nautec.api.sides.SideMode;
import com.breakinblocks.nautec.api.sides.SlotRoles;
import com.breakinblocks.nautec.api.utils.HorizontalDirection;
import com.breakinblocks.nautec.capabilities.IOActions;
import com.breakinblocks.nautec.capabilities.bacteria.DishPort;
import com.breakinblocks.nautec.capabilities.bacteria.IBacteriaStorage;
import com.breakinblocks.nautec.content.items.ReactorUpgradeItem;
import com.breakinblocks.nautec.content.multiblocks.BioReactorMultiblock;
import com.breakinblocks.nautec.content.recipes.ColonyFeedingRecipe;
import com.breakinblocks.nautec.utils.MultiblockHelper;
import com.breakinblocks.nautec.utils.RecipeRevision;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.ints.IntIntPair;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.ints.IntSets;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import com.breakinblocks.nautec.utils.valueio.ValueInput;
import com.breakinblocks.nautec.utils.valueio.ValueOutput;
import net.neoforged.neoforge.capabilities.BlockCapability;
import com.breakinblocks.nautec.transfer.item.ItemResource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public abstract class AbstractBioReactorBlockEntity extends LaserBlockEntity implements MenuProvider, MultiblockEntity {
    public static final int WORK_TICKS_PER_DECAY = 20;
    public static final int ACTIVE_LINGER_TICKS = 20;
    private static final int FEED_RETRY_TICKS = 20;

    private final int colonies;
    private final int nutrientSlots;
    private final int upgradeSlots;
    private final float[] progress;
    private final float[] vitality;
    private final float[] vitalityCapacity;
    private final int[] workTicks;
    private final long[] nextFeedCheck;
    private final ProductCache[] productCache;
    private final SlotRoles slotRoles;
    private final RecipeRevision recipeRevision = new RecipeRevision();
    private MultiblockData multiblockData;
    private long lastProductiveTick = Long.MIN_VALUE;
    private int forwardedPower;

    protected AbstractBioReactorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, int colonies, int nutrientSlots, int upgradeSlots) {
        super(type, pos, state);
        this.colonies = colonies;
        this.nutrientSlots = nutrientSlots;
        this.upgradeSlots = upgradeSlots;
        this.progress = new float[colonies];
        this.vitality = new float[colonies];
        this.vitalityCapacity = new float[colonies];
        this.workTicks = new int[colonies];
        this.nextFeedCheck = new long[colonies];
        this.productCache = new ProductCache[colonies];
        this.multiblockData = MultiblockData.EMPTY;

        IntSet inputs = new IntOpenHashSet();
        for (int i = 0; i < nutrientSlots + upgradeSlots; i++) {
            inputs.add(colonies + i);
        }
        inputs.add(dishInSlot());
        IntSet outputs = new IntOpenHashSet();
        for (int i = 0; i < colonies; i++) {
            outputs.add(i);
        }
        outputs.add(dishOutSlot());
        outputs.add(dishEmptyOutSlot());
        this.slotRoles = new SlotRoles(IntSets.unmodifiable(inputs), IntSets.unmodifiable(outputs));

        addItemHandler(totalItemSlots(), slot -> isUpgradeSlot(slot) ? 1 : 64, this::isItemValid);
        addBacteriaStorage(colonies);
    }

    protected abstract int basePower();

    protected abstract int powerPerColony();

    protected abstract double baseSpeed();

    public abstract Multiblock multiblock();

    protected abstract Block partBlock();

    protected boolean canRun() {
        return true;
    }

    public int getColonySlots() {
        return colonies;
    }

    public int getNutrientSlotCount() {
        return nutrientSlots;
    }

    public int getUpgradeSlotCount() {
        return upgradeSlots;
    }

    public int totalItemSlots() {
        return colonies + nutrientSlots + upgradeSlots + 3;
    }

    public int dishInSlot() {
        return colonies + nutrientSlots + upgradeSlots;
    }

    public int dishOutSlot() {
        return dishInSlot() + 1;
    }

    public int dishEmptyOutSlot() {
        return dishInSlot() + 2;
    }

    public int outputSlot(int colony) {
        return colony;
    }

    public int nutrientSlot(int index) {
        return colonies + index;
    }

    public int upgradeSlot(int index) {
        return colonies + nutrientSlots + index;
    }

    public boolean isOutputSlot(int slot) {
        return slot >= 0 && slot < colonies;
    }

    public boolean isNutrientSlot(int slot) {
        return slot >= colonies && slot < colonies + nutrientSlots;
    }

    public boolean isUpgradeSlot(int slot) {
        return slot >= colonies + nutrientSlots && slot < dishInSlot();
    }

    protected boolean isItemValid(int slot, ItemStack stack) {
        if (isUpgradeSlot(slot)) {
            return stack.getItem() instanceof ReactorUpgradeItem;
        }
        if (isNutrientSlot(slot)) {
            return !(stack.getItem() instanceof ReactorUpgradeItem) && ColonyFeedingRecipe.isNutrient(level, stack);
        }
        return slot == dishInSlot() && DishPort.isDish(stack);
    }

    private int[] colonySlots() {
        int[] slots = new int[colonies];
        for (int i = 0; i < colonies; i++) {
            slots[i] = i;
        }
        return slots;
    }

    private int weakestColony() {
        IBacteriaStorage storage = getBacteriaStorage();
        int weakest = -1;
        for (int i = 0; i < colonies; i++) {
            if (!storage.getBacteria(i).isEmpty() && (weakest < 0 || this.vitality[i] < this.vitality[weakest])) {
                weakest = i;
            }
        }
        return weakest;
    }

    private int agedColony() {
        IBacteriaStorage storage = getBacteriaStorage();
        int aged = -1;
        float oldest = 0f;
        for (int i = 0; i < colonies; i++) {
            BacteriaInstance bacteria = storage.getBacteria(i);
            if (!bacteria.isEmpty() && bacteria.isSenescent()) {
                float age = (float) bacteria.getAge() / Math.max(1, bacteria.getStats().lifespan());
                if (aged < 0 || age > oldest) {
                    aged = i;
                    oldest = age;
                }
            }
        }
        return aged;
    }

    private void clearColony(int slot) {
        this.vitality[slot] = 0;
        this.vitalityCapacity[slot] = 0;
        this.progress[slot] = 0;
        this.workTicks[slot] = 0;
    }

    public int getActiveColonies() {
        IBacteriaStorage storage = getBacteriaStorage();
        int active = 0;
        for (int i = 0; i < colonies; i++) {
            if (!storage.getBacteria(i).isEmpty()) {
                active++;
            }
        }
        return active;
    }

    public int getUpgradeCount(ReactorUpgradeItem.Type type) {
        int count = 0;
        for (int i = 0; i < upgradeSlots; i++) {
            ItemStack stack = getItemStackHandler().getStackInSlot(upgradeSlot(i));
            if (stack.getItem() instanceof ReactorUpgradeItem upgrade && upgrade.getUpgradeType() == type) {
                count += stack.getCount();
            }
        }
        return count;
    }

    public int getEffectiveUpgradeCount(ReactorUpgradeItem.Type type) {
        int count = getUpgradeCount(type);
        if (ReactorUpgradeItem.Type.BASIC.contains(type)) {
            count += getUpgradeCount(ReactorUpgradeItem.Type.FUSION);
        }
        return count;
    }

    public boolean hasUpgrades() {
        for (ReactorUpgradeItem.Type type : ReactorUpgradeItem.Type.values()) {
            if (getUpgradeCount(type) > 0) {
                return true;
            }
        }
        return false;
    }

    public double getPowerMultiplier() {
        double multiplier = 1.0;
        for (ReactorUpgradeItem.Type type : ReactorUpgradeItem.Type.values()) {
            multiplier *= Math.pow(type.powerMultiplier(), getUpgradeCount(type));
        }
        return multiplier;
    }

    @Override
    public int getRequiredPower() {
        double raw = (basePower() + (double) powerPerColony() * getActiveColonies()) * getPowerMultiplier();
        return (int) Math.ceil(raw - 1.0e-6);
    }

    public float getSpeedMultiplier() {
        return (float) (1.0 + NTConfig.reactorSpeedUpgradeBonus * getEffectiveUpgradeCount(ReactorUpgradeItem.Type.SPEED));
    }

    public int getItemsPerCycle() {
        return 1 + NTConfig.reactorYieldUpgradeBonus * getEffectiveUpgradeCount(ReactorUpgradeItem.Type.YIELD);
    }

    public float getVitalityCost() {
        int upgrades = getEffectiveUpgradeCount(ReactorUpgradeItem.Type.EFFICIENCY);
        if (upgrades <= 0) {
            return 1.0f;
        }
        double cost = Math.pow(NTConfig.reactorEfficiencyUpgradeFactor, upgrades);
        return (float) Math.max(NTConfig.reactorEfficiencyUpgradeFloor, cost);
    }

    public void forwardPower(int amount) {
        this.forwardedPower += amount;
    }

    @Override
    public void commonTick() {
        super.commonTick();
        this.power += this.forwardedPower;
        this.forwardedPower = 0;

        boolean server = !level.isClientSide();
        if (server && level.getGameTime() % DishPort.INTERVAL == 0) {
            DishPort.tick(this, dishInSlot(), dishEmptyOutSlot(), dishOutSlot(), colonySlots(), this::weakestColony, this::clearColony, this::agedColony);
        }
        if (level instanceof ServerLevel serverLevel && recipeRevision.changed(serverLevel)) {
            Arrays.fill(this.productCache, null);
            Arrays.fill(this.nextFeedCheck, 0);
        }

        boolean productive = false;
        boolean worked = false;
        boolean starved = false;
        if (canRun() && getPower() >= getRequiredPower()) {
            IBacteriaStorage storage = getBacteriaStorage();
            float speed = getSpeedMultiplier() * beamSpeed();
            int perCycle = getItemsPerCycle();
            float cost = getVitalityCost();
            for (int i = 0; i < colonies; i++) {
                BacteriaInstance bacteria = storage.getBacteria(i);
                if (bacteria.isEmpty()) {
                    this.progress[i] = 0;
                    this.workTicks[i] = 0;
                    continue;
                }

                ProductCache cache = cacheFor(i, bacteria);
                ItemStack product = cache.product().isEmpty() ? ItemStack.EMPTY : cache.product().copyWithCount(perCycle);
                float next = this.progress[i] + productionPerTick(bacteria, cache.multiplier(), baseSpeed()) * speed;
                if (next >= 100 && !canOutput(i, product)) {
                    continue;
                }

                this.progress[i] = next;
                worked = true;
                if (!product.isEmpty()) {
                    productive = true;
                }

                boolean fed = spendVitality(i, bacteria, cost, server);
                if (++this.workTicks[i] >= WORK_TICKS_PER_DECAY) {
                    this.workTicks[i] = 0;
                    if (server && !fed) {
                        starved = true;
                        if (bacteria.isSenescent()) {
                            decay(i, bacteria, storage);
                        }
                    }
                }

                while (this.progress[i] >= 100) {
                    if (server && !product.isEmpty()) {
                        if (!canOutput(i, product)) {
                            this.progress[i] = 100;
                            break;
                        }
                        forceInsertItem(outputSlot(i), product, false);
                    }
                    this.progress[i] -= 100;
                }
            }
        } else {
            Arrays.fill(this.progress, 0);
        }

        if (server) {
            if (starved) {
                update();
            } else if (worked) {
                setChanged();
            }
            updateActive(productive);
        }
    }

    private boolean spendVitality(int slot, BacteriaInstance bacteria, float cost, boolean server) {
        if (this.vitality[slot] <= 0 && server) {
            tryFeed(slot, bacteria);
        }
        if (this.vitality[slot] > 0) {
            this.vitality[slot] = Math.max(0, this.vitality[slot] - cost);
            return true;
        }
        bacteria.addAge(1);
        return false;
    }

    private void tryFeed(int slot, BacteriaInstance bacteria) {
        long now = level.getGameTime();
        if (now < this.nextFeedCheck[slot]) {
            return;
        }
        for (int i = 0; i < nutrientSlots; i++) {
            int nutrient = nutrientSlot(i);
            ItemStack stack = getItemStackHandler().getStackInSlot(nutrient);
            if (stack.isEmpty()) {
                continue;
            }
            Optional<ColonyFeedingRecipe> recipe = ColonyFeedingRecipe.find(level, bacteria, stack);
            if (recipe.isPresent()) {
                this.vitality[slot] += recipe.get().vitalityTicks();
                this.vitalityCapacity[slot] = this.vitality[slot];
                forceExtractItem(nutrient, recipe.get().ingredient().count(), false);
                return;
            }
        }
        this.nextFeedCheck[slot] = now + FEED_RETRY_TICKS;
    }

    private void decay(int slot, BacteriaInstance bacteria, IBacteriaStorage storage) {
        double fraction = NTConfig.bioReactorDecayPerSecond;
        if (fraction <= 0) {
            return;
        }
        long amount = Math.max(1, (long) Math.ceil(bacteria.getSize() * fraction));
        storage.extractBacteria(slot, amount, false);
    }

    private ProductCache cacheFor(int slot, BacteriaInstance bacteria) {
        ProductCache cache = this.productCache[slot];
        if (cache != null && cache.bacteria().equals(bacteria.getBacteria())) {
            return cache;
        }
        Bacteria definition = definitionOf(bacteria.getBacteria());
        ItemStack product = ItemStack.EMPTY;
        float multiplier = 1.0f;
        if (definition != null) {
            Item item = definition.resource().resolve();
            if (item != null && item != Items.AIR) {
                product = item.getDefaultInstance();
            }
            multiplier = definition.productionMultiplier();
        }
        cache = new ProductCache(bacteria.getBacteria(), product, multiplier);
        this.productCache[slot] = cache;
        return cache;
    }

    public @Nullable Bacteria definitionOf(ResourceKey<Bacteria> key) {
        if (level == null) {
            return null;
        }
        return level.registryAccess().lookup(NTRegistries.BACTERIA_KEY)
                .flatMap(registry -> registry.get(key))
                .map(Holder::value)
                .orElse(null);
    }

    public boolean canOutput(int colony, ItemStack product) {
        return product.isEmpty() || forceInsertItem(outputSlot(colony), product, true).isEmpty();
    }

    public static float productionPerTick(BacteriaInstance bacteria, float strainMultiplier, double baseSpeed) {
        long cap = Math.max(1, NTConfig.bacteriaColonySizeCap);
        float sizeFactor = 0.5f + 0.5f * ((float) Math.min(bacteria.getSize(), cap) / cap);
        return (float) (bacteria.getStats().productionRate() * sizeFactor * baseSpeed * strainMultiplier);
    }

    private void updateActive(boolean productive) {
        long now = level.getGameTime();
        if (productive) {
            this.lastProductiveTick = now;
        }
        boolean active = this.lastProductiveTick != Long.MIN_VALUE && now - this.lastProductiveTick <= ACTIVE_LINGER_TICKS;
        BlockState state = getBlockState();
        if (state.hasProperty(BioReactorMultiblock.ACTIVE) && state.getValue(BioReactorMultiblock.ACTIVE) != active) {
            applyActive(active);
        }
    }

    public boolean isActive() {
        BlockState state = getBlockState();
        return state.hasProperty(BioReactorMultiblock.ACTIVE) && state.getValue(BioReactorMultiblock.ACTIVE);
    }

    public boolean isFormed() {
        BlockState state = getBlockState();
        return state.hasProperty(Multiblock.FORMED) && state.getValue(Multiblock.FORMED);
    }

    private void applyActive(boolean active) {
        BlockState own = getBlockState();
        level.setBlock(worldPosition, own.setValue(BioReactorMultiblock.ACTIVE, active), Block.UPDATE_CLIENTS);
        if (!isFormed()) {
            return;
        }
        Block part = partBlock();
        forEachFormedPosition(pos -> {
            if (pos.equals(worldPosition)) {
                return;
            }
            BlockState state = level.getBlockState(pos);
            if (state.is(part) && state.hasProperty(BioReactorMultiblock.ACTIVE) && state.getValue(BioReactorMultiblock.ACTIVE) != active) {
                level.setBlock(pos, state.setValue(BioReactorMultiblock.ACTIVE, active), Block.UPDATE_CLIENTS);
            }
        });
    }

    public void forEachFormedPosition(Consumer<BlockPos> consumer) {
        MultiblockData data = this.multiblockData;
        if (data == null || !data.valid() || data.direction() == null || data.layers() == null) {
            return;
        }
        Multiblock multiblock = multiblock();
        Vec3i relative = MultiblockHelper.getRelativeControllerPos(multiblock);
        BlockPos first = MultiblockHelper.getFirstBlockPos(data.direction(), worldPosition, relative);
        List<IntIntPair> widths = multiblock.getWidths();
        for (int y = 0; y < data.layers().length && y < widths.size(); y++) {
            int width = Math.max(1, widths.get(y).leftInt());
            int[] layer = data.layers()[y].layer();
            for (int index = 0; index < layer.length; index++) {
                consumer.accept(MultiblockHelper.getCurPos(first, new Vec3i(index % width, y, index / width), data.direction()));
            }
        }
    }

    @Override
    protected boolean acceptsNow(int slot, ItemResource resource) {
        if (slot != dishInSlot()) {
            return true;
        }
        return DishPort.accepts(this, resource, dishEmptyOutSlot(), dishOutSlot(), colonySlots(), this::weakestColony, colony -> true, this::agedColony);
    }

    @Override
    public SlotRoles itemRoles() {
        return slotRoles;
    }

    @Override
    public Direction front() {
        HorizontalDirection fixed = multiblock().getFixedDirection();
        return fixed != null ? fixed.toRegularDirection() : super.front();
    }

    public SideMode itemMode(Direction direction) {
        return getSideConfig().get(SideKind.ITEMS, RelativeFace.of(front(), direction));
    }

    @Override
    public void sideConfigChanged() {
        super.sideConfigChanged();
        if (level != null && !level.isClientSide()) {
            forEachFormedPosition(level::invalidateCapabilities);
        }
    }

    @Override
    public <T> Map<Direction, Pair<IOActions, int[]>> getSidedInteractions(BlockCapability<T, @Nullable Direction> capability) {
        return Map.of();
    }

    @Override
    protected void onItemsChanged(int slot) {
        super.onItemsChanged(slot);
        if (isNutrientSlot(slot)) {
            Arrays.fill(this.nextFeedCheck, 0);
        }
    }

    @Override
    public @NotNull Component getDisplayName() {
        return getBlockState().getBlock().getName();
    }

    @Override
    public MultiblockData getMultiblockData() {
        return this.multiblockData;
    }

    @Override
    public void setMultiblockData(MultiblockData data) {
        this.multiblockData = data;
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        if (MultiblockEntity.UNFORMING.compareAndSet(false, true)) {
            try {
                MultiblockHelper.unform(multiblock(), pos, level);
            } finally {
                MultiblockEntity.UNFORMING.set(false);
            }
        }
        super.preRemoveSideEffects(pos, state);
    }

    @Override
    protected void saveData(ValueOutput out) {
        super.saveData(out);
        out.store("multiblockData", CompoundTag.CODEC, saveMBData());
        for (int i = 0; i < colonies; i++) {
            out.putFloat("progress" + i, this.progress[i]);
            out.putFloat("vitality" + i, this.vitality[i]);
            out.putFloat("vitalityCapacity" + i, this.vitalityCapacity[i]);
            out.putInt("workTicks" + i, this.workTicks[i]);
        }
    }

    @Override
    protected void loadData(ValueInput in) {
        super.loadData(in);
        getItemStackHandler().ensureSize(totalItemSlots());
        this.multiblockData = loadMBData(in.read("multiblockData", CompoundTag.CODEC).orElseGet(CompoundTag::new));
        int[] legacyProgress = in.getIntArray("progress").orElse(new int[0]);
        for (int i = 0; i < colonies; i++) {
            float legacy = i < legacyProgress.length ? legacyProgress[i] : 0;
            this.progress[i] = in.getFloatOr("progress" + i, legacy);
            this.vitality[i] = in.getFloatOr("vitality" + i, 0);
            this.vitalityCapacity[i] = in.getFloatOr("vitalityCapacity" + i, this.vitality[i]);
            this.workTicks[i] = in.getIntOr("workTicks" + i, 0);
        }
        Arrays.fill(this.productCache, null);
    }

    public float getProgress(int colony) {
        return this.progress[colony];
    }

    public float getVitality(int colony) {
        return this.vitality[colony];
    }

    public float getVitalityCapacity(int colony) {
        return Math.max(this.vitalityCapacity[colony], this.vitality[colony]);
    }

    public void setVitality(int colony, float amount) {
        this.vitality[colony] = amount;
        this.vitalityCapacity[colony] = amount;
        setChanged();
    }

    private record ProductCache(ResourceKey<Bacteria> bacteria, ItemStack product, float multiplier) {
    }
}
