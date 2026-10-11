package com.breakinblocks.nautec.content.blockentities;

import com.breakinblocks.nautec.utils.BeamOverclock;
import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.NTRegistries;
import com.breakinblocks.nautec.api.bacteria.Bacteria;
import com.breakinblocks.nautec.api.bacteria.BacteriaInstance;
import com.breakinblocks.nautec.api.blockentities.LaserBlockEntity;
import com.breakinblocks.nautec.api.sides.SlotRoles;
import com.breakinblocks.nautec.capabilities.IOActions;
import com.breakinblocks.nautec.capabilities.bacteria.DishPort;
import com.breakinblocks.nautec.capabilities.bacteria.IBacteriaStorage;
import com.breakinblocks.nautec.content.bacteria.ProductNutrients;
import com.breakinblocks.nautec.content.bacteria.SimpleCollapsedStats;
import com.breakinblocks.nautec.content.blocks.ColonyReplicatorBlock;
import com.breakinblocks.nautec.content.menus.ColonyReplicatorMenu;
import com.breakinblocks.nautec.utils.DataSlots;
import com.breakinblocks.nautec.registries.NTBlockEntityTypes;
import com.breakinblocks.nautec.utils.BacteriaHelper;
import it.unimi.dsi.fastutil.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import com.breakinblocks.nautec.utils.valueio.ValueInput;
import com.breakinblocks.nautec.utils.valueio.ValueOutput;
import net.neoforged.neoforge.capabilities.BlockCapability;
import com.breakinblocks.nautec.transfer.item.ItemResource;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

public class ColonyReplicatorBlockEntity extends LaserBlockEntity implements MenuProvider {
    private final BeamOverclock overclock = new BeamOverclock();

    @Override
    public int getRequiredPower() {
        return NTConfig.replicatorPowerUsage;
    }

    public static final int TEMPLATE = 0;
    public static final int PARTNER = 1;
    public static final int FODDER = 2;
    public static final int RESULT = 3;
    public static final int DISH_IN = 0;
    public static final int DISH_OUT = 1;
    public static final int DISH_EMPTY_OUT = 2;
    public static final int FODDER_ITEM = 3;
    private static final int[] LOAD_SLOTS = {FODDER};

    public static final int STATUS_RUNNING = 0;
    public static final int STATUS_NO_TEMPLATE = 1;
    public static final int STATUS_NOT_ANALYZED = 2;
    public static final int STATUS_NO_PARTNER = 3;
    public static final int STATUS_WRONG_STRAIN = 4;
    public static final int STATUS_NO_BIOMASS = 5;
    public static final int STATUS_OUTPUT_FULL = 6;
    public static final int STATUS_LOW_POWER = 7;
    public static final int STATUS_LOW_PURITY = 8;
    public static final int STATUS_UNSUPPORTED = 9;

    public static final int DATA_PROGRESS = 0;
    public static final int DATA_DURATION = 2;
    public static final int DATA_STATUS = 4;
    public static final int DATA_SPLICE = 5;
    public static final int DATA_BIOMASS = 6;
    public static final int DATA_COUNT = 8;

    private static final SlotRoles ITEM_ROLES = SlotRoles.of(new int[]{DISH_IN, FODDER_ITEM}, new int[]{DISH_OUT, DISH_EMPTY_OUT});

    private boolean splice;
    private int progress;
    private int status = STATUS_NO_TEMPLATE;
    private long biomass;
    private @Nullable ResourceKey<Bacteria> biomassStrain;

    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            int stored = (int) Math.min(Integer.MAX_VALUE, biomass);
            return switch (index) {
                case DATA_PROGRESS -> DataSlots.low(progress);
                case DATA_PROGRESS + 1 -> DataSlots.high(progress);
                case DATA_DURATION -> DataSlots.low(NTConfig.replicatorDuration);
                case DATA_DURATION + 1 -> DataSlots.high(NTConfig.replicatorDuration);
                case DATA_STATUS -> status;
                case DATA_SPLICE -> splice ? 1 : 0;
                case DATA_BIOMASS -> DataSlots.low(stored);
                case DATA_BIOMASS + 1 -> DataSlots.high(stored);
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

    public ColonyReplicatorBlockEntity(BlockPos pos, BlockState state) {
        super(NTBlockEntityTypes.COLONY_REPLICATOR.get(), pos, state);
        addBacteriaStorage(4);
        addItemHandler(4, slot -> slot == FODDER_ITEM ? 64 : 1,
                (slot, stack) -> (slot == DISH_IN && DishPort.isDish(stack)) || (slot == FODDER_ITEM && fodderBiomass(stack) > 0));
    }

    public ContainerData getData() {
        return data;
    }

    public boolean isSplice() {
        return splice;
    }

    public void setSplice(boolean splice) {
        this.splice = splice;
        this.progress = 0;
        setChanged();
    }

    public long getBiomass() {
        return biomass;
    }

    public int getStatus() {
        return status;
    }

    public int getProgress() {
        return progress;
    }

    @Override
    public SlotRoles itemRoles() {
        return ITEM_ROLES;
    }

    @Override
    public Set<Direction> getLaserInputs() {
        Set<Direction> inputs = EnumSet.allOf(Direction.class);
        inputs.remove(getBlockState().getValue(ColonyReplicatorBlock.FACING));
        return inputs;
    }

    @Override
    public Set<Direction> getLaserOutputs() {
        return Set.of();
    }

    @Override
    public void commonTick() {
        super.commonTick();
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        if (serverLevel.getGameTime() % DishPort.INTERVAL == 0) {
            DishPort.tick(this, DISH_IN, DISH_EMPTY_OUT, DISH_OUT, LOAD_SLOTS, this::copyReady, slot -> {
            });
        }
        absorbFodder();

        int newStatus = checkStatus();
        if (newStatus == STATUS_RUNNING) {
            progress += overclock.advance(beamSpeed());
            if (progress >= NTConfig.replicatorDuration) {
                produce(serverLevel.getRandom());
                progress = 0;
            }
        } else if (newStatus != STATUS_LOW_POWER && newStatus != STATUS_LOW_PURITY) {
            progress = 0;
        }
        if (newStatus != status) {
            status = newStatus;
            update();
        }
        boolean active = status == STATUS_RUNNING;
        if (getBlockState().getValue(ColonyReplicatorBlock.ACTIVE) != active) {
            level.setBlock(worldPosition, getBlockState().setValue(ColonyReplicatorBlock.ACTIVE, active), Block.UPDATE_CLIENTS);
        }
    }

    public long fodderBiomass(ItemStack stack) {
        BacteriaInstance template = getBacteriaStorage().getBacteria(TEMPLATE);
        if (level == null || template.isEmpty() || stack.isEmpty()) {
            return 0;
        }
        Bacteria strain = BacteriaHelper.getBacteria(level.registryAccess(), template.getBacteria());
        return strain == null ? 0 : ProductNutrients.biomass(strain, stack);
    }

    @Override
    protected boolean acceptsNow(int slot, ItemResource resource) {
        if (slot == FODDER_ITEM) {
            return fodderBiomass(resource.toStack()) > 0;
        }
        if (slot != DISH_IN) {
            return true;
        }
        return DishPort.accepts(this, resource, DISH_EMPTY_OUT, DISH_OUT, LOAD_SLOTS, this::copyReady, this::isFodder);
    }

    private int copyReady() {
        return getBacteriaStorage().getBacteria(RESULT).isEmpty() ? -1 : RESULT;
    }

    private boolean isFodder(BacteriaInstance colony) {
        BacteriaInstance template = getBacteriaStorage().getBacteria(TEMPLATE);
        return !template.isEmpty() && colony.is(template.getBacteria());
    }

    private void absorbFodder() {
        IBacteriaStorage storage = getBacteriaStorage();
        BacteriaInstance template = storage.getBacteria(TEMPLATE);
        BacteriaInstance fodder = storage.getBacteria(FODDER);
        if (template.isEmpty()) {
            return;
        }
        if (biomassStrain != null && !template.is(biomassStrain)) {
            biomass = 0;
            biomassStrain = null;
            setChanged();
        }
        absorbFodderItems(template);
        if (fodder.isEmpty() || !fodder.is(template.getBacteria()) || biomass >= NTConfig.replicatorBiomassCap) {
            return;
        }
        biomass = Math.min(NTConfig.replicatorBiomassCap, biomass + fodder.getSize());
        biomassStrain = template.getBacteria();
        storage.setBacteria(FODDER, BacteriaInstance.EMPTY);
        storage.onBacteriaChanged(FODDER);
        setChanged();
    }

    private void absorbFodderItems(BacteriaInstance template) {
        ItemStack stack = getItemStackHandler().getStackInSlot(FODDER_ITEM);
        long each = fodderBiomass(stack);
        if (each <= 0 || biomass >= NTConfig.replicatorBiomassCap) {
            return;
        }
        long room = NTConfig.replicatorBiomassCap - biomass;
        int count = (int) Math.min(stack.getCount(), (room + each - 1) / each);
        getItemStackHandler().extractItem(FODDER_ITEM, count, false);
        biomass = Math.min(NTConfig.replicatorBiomassCap, biomass + each * count);
        biomassStrain = template.getBacteria();
        setChanged();
    }

    private int checkStatus() {
        IBacteriaStorage storage = getBacteriaStorage();
        BacteriaInstance template = storage.getBacteria(TEMPLATE);
        if (template.isEmpty()) {
            return STATUS_NO_TEMPLATE;
        }
        if (!template.isAnalyzed()) {
            return STATUS_NOT_ANALYZED;
        }
        if (!(template.getStats() instanceof SimpleCollapsedStats)) {
            return STATUS_UNSUPPORTED;
        }
        BacteriaInstance fodder = storage.getBacteria(FODDER);
        if (!fodder.isEmpty() && !fodder.is(template.getBacteria())) {
            return STATUS_WRONG_STRAIN;
        }
        if (splice) {
            BacteriaInstance partner = storage.getBacteria(PARTNER);
            if (partner.isEmpty() || !partner.isAnalyzed()) {
                return STATUS_NO_PARTNER;
            }
            if (!partner.is(template.getBacteria()) || !(partner.getStats() instanceof SimpleCollapsedStats)) {
                return STATUS_WRONG_STRAIN;
            }
        }
        if (biomass < NTConfig.replicatorBiomassCost) {
            return STATUS_NO_BIOMASS;
        }
        if (!storage.getBacteria(RESULT).isEmpty()) {
            return STATUS_OUTPUT_FULL;
        }
        if (getPower() < NTConfig.replicatorPowerUsage) {
            return STATUS_LOW_POWER;
        }
        if (getPurity() < NTConfig.replicatorPurity) {
            return STATUS_LOW_PURITY;
        }
        return STATUS_RUNNING;
    }

    private void produce(RandomSource random) {
        IBacteriaStorage storage = getBacteriaStorage();
        BacteriaInstance template = storage.getBacteria(TEMPLATE);
        if (!(template.getStats() instanceof SimpleCollapsedStats first)) {
            return;
        }
        SimpleCollapsedStats stats = first;
        if (splice && storage.getBacteria(PARTNER).getStats() instanceof SimpleCollapsedStats second) {
            stats = splice(first, second, random);
        }
        stats = copy(stats, random);
        Bacteria strain = BacteriaHelper.getBacteria(level.registryAccess(), template.getBacteria());
        long size = Math.max(strain.rollSize(), Math.min(NTConfig.bacteriaColonySizeCap,
                Math.round(NTConfig.replicatorBiomassCost * NTConfig.replicatorCopySize)));
        BacteriaInstance child = new BacteriaInstance(template.getBacteria(), size, stats, true, 0);
        biomass -= NTConfig.replicatorBiomassCost;
        storage.setBacteria(RESULT, child);
        storage.onBacteriaChanged(RESULT);
        setChanged();
    }

    public static SimpleCollapsedStats splice(SimpleCollapsedStats a, SimpleCollapsedStats b, RandomSource random) {
        double chance = NTConfig.replicatorSpliceChance;
        float growth = pick(a.growthRate(), b.growthRate(), chance, random);
        float resistance = pick(a.mutationResistance(), b.mutationResistance(), chance, random);
        float production = pick(a.productionRate(), b.productionRate(), chance, random);
        int lifespan = (int) pick(a.lifespan(), b.lifespan(), chance, random);
        return new SimpleCollapsedStats(a.baseStats(), growth, resistance, production, lifespan, a.color());
    }

    private static float pick(float a, float b, double chance, RandomSource random) {
        float best = Math.max(a, b);
        float other = Math.min(a, b);
        return random.nextDouble() < chance ? best : other;
    }

    public static double fidelity(float resistance) {
        if (NTConfig.bacteriaMutationResistanceCap <= 0) {
            return 1.0;
        }
        return Math.max(0.0, Math.min(1.0, resistance / NTConfig.bacteriaMutationResistanceCap));
    }

    public static SimpleCollapsedStats copy(SimpleCollapsedStats stats, RandomSource random) {
        double fidelity = fidelity(stats.mutationResistance());
        double chance = NTConfig.replicatorErrorChance * (1.0 - fidelity);
        double loss = 0.1 * (1.0 - fidelity);
        float growth = stats.growthRate();
        float resistance = stats.mutationResistance();
        float production = stats.productionRate();
        int lifespan = stats.lifespan();
        if (random.nextDouble() < chance) {
            growth = (float) (growth * (1.0 - random.nextDouble() * loss));
        }
        if (random.nextDouble() < chance) {
            production = (float) (production * (1.0 - random.nextDouble() * loss));
        }
        if (random.nextDouble() < chance) {
            lifespan = (int) Math.round(lifespan * (1.0 - random.nextDouble() * loss));
        }
        if (random.nextDouble() < chance) {
            resistance = (float) (resistance * (1.0 - random.nextDouble() * loss));
        }
        return new SimpleCollapsedStats(stats.baseStats(), growth, resistance, production, lifespan, stats.color());
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
        return new ColonyReplicatorMenu(containerId, inventory, this);
    }

    @Override
    protected void saveData(ValueOutput out) {
        super.saveData(out);
        out.putBoolean("splice", splice);
        out.putInt("progress", progress);
        out.putInt("status", status);
        out.putLong("biomass", biomass);
        if (biomassStrain != null) {
            out.store("biomass_strain", ResourceLocation.CODEC, biomassStrain.location());
        }
    }

    @Override
    protected void loadData(ValueInput in) {
        super.loadData(in);
        getItemStackHandler().ensureSize(4);
        this.splice = in.getBooleanOr("splice", false);
        this.progress = in.getIntOr("progress", 0);
        this.status = in.getIntOr("status", STATUS_NO_TEMPLATE);
        this.biomass = in.getLongOr("biomass", 0L);
        this.biomassStrain = in.read("biomass_strain", ResourceLocation.CODEC)
                .map(id -> ResourceKey.create(NTRegistries.BACTERIA_KEY, id)).orElse(null);
    }
}
