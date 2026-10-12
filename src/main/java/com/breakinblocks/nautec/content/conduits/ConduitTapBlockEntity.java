package com.breakinblocks.nautec.content.conduits;

import com.breakinblocks.nautec.api.blockentities.ContainerBlockEntity;
import com.breakinblocks.nautec.capabilities.IOActions;
import com.breakinblocks.nautec.content.menus.ConduitTapMenu;
import com.breakinblocks.nautec.network.ConduitTapSyncPayload;
import com.breakinblocks.nautec.registries.NTBlockEntityTypes;
import it.unimi.dsi.fastutil.HashCommon;
import it.unimi.dsi.fastutil.Pair;
import java.util.ArrayDeque;
import java.util.Map;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.WorldlyContainerHolder;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.energy.EnergyHandlerUtil;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.VanillaContainerWrapper;
import net.neoforged.neoforge.transfer.item.WorldlyContainerWrapper;
import net.neoforged.neoforge.transfer.resource.Resource;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ConduitTapBlockEntity extends ContainerBlockEntity implements MenuProvider {
    public static final int UPGRADE_SLOT = 0;
    public static final int FILTER_SLOT = 1;
    public static final int SLOTS = 2;

    private static final Direction[] DIRECTIONS = ConduitPartBlock.DIRECTIONS;
    private static final int FACES = DIRECTIONS.length;
    private static final int ALL_FACES = (1 << FACES) - 1;
    private static final int MAX_IDLE = 20;
    private static final int MAX_ENERGY_IDLE = 4;

    private static final Channel<ItemResource> ITEMS = new Channel<>() {
        @Override
        public ConduitChannel kind() {
            return ConduitChannel.ITEMS;
        }

        @Override
        public boolean accepts(ConduitTapBlockEntity tap, Direction face, TapSide side, ItemResource resource) {
            return tap.faces[face.ordinal()].filter(side).passes(resource, tap.itemFilterSlots());
        }

        @Override
        public int insert(ConduitTapBlockEntity tap, Direction face, ItemResource resource, int amount, TransactionContext transaction) {
            return ResourceHandlerUtil.insertStacking(tap.items(face), resource, amount, transaction);
        }
    };
    private static final Channel<FluidResource> FLUIDS = new Channel<>() {
        @Override
        public ConduitChannel kind() {
            return ConduitChannel.FLUIDS;
        }

        @Override
        public boolean accepts(ConduitTapBlockEntity tap, Direction face, TapSide side, FluidResource resource) {
            return tap.faces[face.ordinal()].filter(side).passes(resource, tap.unlocked);
        }

        @Override
        public int insert(ConduitTapBlockEntity tap, Direction face, FluidResource resource, int amount, TransactionContext transaction) {
            return ResourceHandlerUtil.insertStacking(tap.target(Capabilities.Fluid.BLOCK, tap.fluidCaches, face), resource, amount, transaction);
        }
    };
    private static final Channel<Boolean> ENERGY = new Channel<>() {
        @Override
        public ConduitChannel kind() {
            return ConduitChannel.ENERGY;
        }

        @Override
        public boolean accepts(ConduitTapBlockEntity tap, Direction face, TapSide side, Boolean resource) {
            return true;
        }

        @Override
        public int insert(ConduitTapBlockEntity tap, Direction face, Boolean resource, int amount, TransactionContext transaction) {
            EnergyHandler target = tap.target(Capabilities.Energy.BLOCK, tap.energyCaches, face);
            return target == null ? 0 : target.insert(amount, transaction);
        }
    };

    private static int routing;

    private interface Channel<R> {
        ConduitChannel kind();

        boolean accepts(ConduitTapBlockEntity tap, Direction face, TapSide side, R resource);

        int insert(ConduitTapBlockEntity tap, Direction face, R resource, int amount, TransactionContext transaction);
    }

    private static final class Budget extends SnapshotJournal<long[]> {
        private static final int POOL_LIMIT = 256;
        private static final ArrayDeque<long[]> POOL = new ArrayDeque<>();

        private long windowStart = -(1L << 40);
        private int used;
        private int cursor;

        int available(long gameTime, int window, int limit) {
            if (gameTime - windowStart >= window || gameTime < windowStart) {
                windowStart = gameTime;
                used = 0;
            }
            return limit == Integer.MAX_VALUE ? Integer.MAX_VALUE : Math.max(0, limit - used);
        }

        void consume(int amount, TransactionContext transaction) {
            updateSnapshots(transaction);
            used = (int) Math.min(Integer.MAX_VALUE, (long) used + amount);
            cursor++;
        }

        @Override
        protected long[] createSnapshot() {
            long[] snapshot = POOL.poll();
            if (snapshot == null) {
                snapshot = new long[2];
            }
            snapshot[0] = used;
            snapshot[1] = cursor;
            return snapshot;
        }

        @Override
        protected void revertToSnapshot(long[] snapshot) {
            used = (int) snapshot[0];
            cursor = (int) snapshot[1];
        }

        @Override
        protected void releaseSnapshot(long[] snapshot) {
            if (POOL.size() < POOL_LIMIT) {
                POOL.push(snapshot);
            }
        }
    }

    private final TapFace[] faces = new TapFace[FACES];
    private final Budget[] budgets = new Budget[ConduitChannel.ALL.length * FACES];
    private final int[] idle = new int[ConduitChannel.ALL.length * FACES];
    private final long[] nextAttempt = new long[ConduitChannel.ALL.length * FACES];
    private final int[][] nearestOrder = new int[ConduitChannel.ALL.length * FACES][];
    private final int[] nearestVersion = new int[ConduitChannel.ALL.length * FACES];
    @SuppressWarnings("unchecked")
    private final BlockCapabilityCache<ResourceHandler<ItemResource>, @Nullable Direction>[] itemCaches = new BlockCapabilityCache[FACES];
    @SuppressWarnings("unchecked")
    private final BlockCapabilityCache<ResourceHandler<FluidResource>, @Nullable Direction>[] fluidCaches = new BlockCapabilityCache[FACES];
    @SuppressWarnings("unchecked")
    private final BlockCapabilityCache<EnergyHandler, @Nullable Direction>[] energyCaches = new BlockCapabilityCache[FACES];
    @SuppressWarnings("unchecked")
    private final ResourceHandler<ItemResource>[] itemSinks = new ResourceHandler[FACES];
    @SuppressWarnings("unchecked")
    private final ResourceHandler<FluidResource>[] fluidSinks = new ResourceHandler[FACES];
    private final EnergyHandler[] energySinks = new EnergyHandler[FACES];
    @SuppressWarnings("unchecked")
    private final Predicate<ItemResource>[] itemFilters = new Predicate[FACES];
    @SuppressWarnings("unchecked")
    private final Predicate<FluidResource>[] fluidFilters = new Predicate[FACES];
    private final int tickOffset;
    private @Nullable ConduitNetwork network;
    private int machineMask;
    private int flow;
    private int syncMask;
    private boolean configDirty;
    private boolean armsDirty;
    private boolean powered;
    private int tier;
    private boolean unlocked;
    private boolean intricate;

    public ConduitTapBlockEntity(BlockPos pos, BlockState state) {
        super(NTBlockEntityTypes.CONDUIT_TAP.get(), pos, state);
        addItemHandler(SLOTS, 1, (slot, stack) -> slot == UPGRADE_SLOT ? stack.getItem() instanceof ConduitUpgradeItem : stack.getItem() instanceof FilterItem);
        for (Direction direction : DIRECTIONS) {
            int d = direction.ordinal();
            TapFace face = new TapFace();
            faces[d] = face;
            itemSinks[d] = new Sink<>(direction, ITEMS, ItemResource.EMPTY);
            fluidSinks[d] = new Sink<>(direction, FLUIDS, FluidResource.EMPTY);
            energySinks[d] = new EnergySink(direction);
            itemFilters[d] = resource -> face.filter(TapSide.INPUT).passes(resource, itemFilterSlots());
            fluidFilters[d] = resource -> face.filter(TapSide.INPUT).passes(resource, unlocked);
        }
        for (int i = 0; i < budgets.length; i++) {
            budgets[i] = new Budget();
        }
        tickOffset = (int) (HashCommon.mix(pos.asLong()) & Integer.MAX_VALUE);
        machineMask = machineMask(state);
    }

    private static int index(ConduitChannel channel, Direction direction) {
        return channel.ordinal() * FACES + direction.ordinal();
    }

    private static int machineMask(BlockState state) {
        if (!(state.getBlock() instanceof ConduitTapBlock)) {
            return 0;
        }
        int mask = 0;
        for (Direction direction : DIRECTIONS) {
            if (state.getValue(ConduitTapBlock.ARMS[direction.ordinal()]) == TapArm.MACHINE) {
                mask |= 1 << direction.ordinal();
            }
        }
        return mask;
    }

    @Override
    public void setBlockState(@NotNull BlockState state) {
        super.setBlockState(state);
        machineMask = machineMask(state);
    }

    public TapFace face(Direction direction) {
        return faces[direction.ordinal()];
    }

    public boolean powered() {
        return powered;
    }

    public boolean hasFilterUpgrade() {
        return unlocked;
    }

    public boolean hasIntricateFilter() {
        return intricate;
    }

    public int itemFilterSlots() {
        return !unlocked ? 0 : intricate ? TapFilter.ITEM_SLOTS : TapFilter.BASIC_ITEM_SLOTS;
    }

    public int tier() {
        return tier;
    }

    public TapRates rates() {
        return TapRates.of(tier);
    }

    public boolean isMachineFace(Direction direction) {
        return (machineMask & (1 << direction.ordinal())) != 0;
    }

    private boolean connected(Direction direction) {
        return isMachineFace(direction) && !faces[direction.ordinal()].disabled();
    }

    private boolean active(Direction direction, TapSide side) {
        return faces[direction.ordinal()].redstone(side).active(powered);
    }

    public boolean receives(ConduitChannel channel, Direction direction) {
        return faces[direction.ordinal()].mode(channel).inserts() && connected(direction) && active(direction, TapSide.OUTPUT);
    }

    public boolean sends(ConduitChannel channel, Direction direction) {
        return faces[direction.ordinal()].mode(channel).extracts() && connected(direction) && active(direction, TapSide.INPUT);
    }

    private void readSlots() {
        ItemStack upgrade = getItemStackHandler().getStackInSlot(UPGRADE_SLOT);
        ItemStack filterStack = getItemStackHandler().getStackInSlot(FILTER_SLOT);
        tier = upgrade.getItem() instanceof ConduitUpgradeItem item ? item.tier() : 0;
        unlocked = filterStack.getItem() instanceof FilterItem;
        intricate = filterStack.getItem() instanceof FilterItem item && item.intricate();
    }

    @Override
    protected void onItemsChanged(int slot) {
        readSlots();
        wake();
    }

    public void configChanged() {
        configDirty = true;
        syncMask = ALL_FACES;
    }

    public void configChanged(Direction face) {
        configDirty = true;
        syncMask |= 1 << face.ordinal();
    }

    public void setPowered(boolean powered) {
        if (this.powered != powered) {
            this.powered = powered;
            configDirty = true;
        }
    }

    private void applyConfig(ServerLevel serverLevel) {
        configDirty = false;
        setChanged();
        serverLevel.invalidateCapabilities(worldPosition);
        ConduitNetwork current = network;
        if (current != null && !current.dirty()) {
            current.markRoutesDirty();
        } else {
            ConduitNetworks.routesChanged(serverLevel, worldPosition);
        }
        armsDirty = true;
        int mask = syncMask;
        syncMask = 0;
        if (mask != 0) {
            pushFaces(serverLevel, mask);
        }
    }

    private void pushFaces(ServerLevel serverLevel, int mask) {
        for (ServerPlayer player : serverLevel.players()) {
            if (player.containerMenu instanceof ConduitTapMenu menu && menu.blockEntity == this) {
                for (int bits = mask; bits != 0; bits &= bits - 1) {
                    int d = Integer.numberOfTrailingZeros(bits);
                    resync(player, menu.containerId, DIRECTIONS[d]);
                }
            }
        }
    }

    public void resync(ServerPlayer player, int containerId, Direction direction) {
        PacketDistributor.sendToPlayer(player, new ConduitTapSyncPayload(containerId, direction.ordinal(), faces[direction.ordinal()].copy()));
    }

    public void refreshArms() {
        if (!(level instanceof ServerLevel)) {
            return;
        }
        armsDirty = false;
        BlockState state = getBlockState();
        if (!(state.getBlock() instanceof ConduitTapBlock)) {
            return;
        }
        BlockState next = state;
        int nextFlow = 0;
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (Direction direction : DIRECTIONS) {
            cursor.setWithOffset(worldPosition, direction);
            TapArm arm = computeArm(direction, cursor);
            next = next.setValue(ConduitTapBlock.ARMS[direction.ordinal()], arm);
            if (arm == TapArm.MACHINE) {
                nextFlow |= computeFlow(direction).ordinal() << (direction.ordinal() * 2);
            }
        }
        boolean flowChanged = nextFlow != flow;
        flow = nextFlow;
        if (next != state) {
            level.setBlock(worldPosition, next, Block.UPDATE_ALL);
        } else if (flowChanged) {
            level.sendBlockUpdated(worldPosition, state, state, Block.UPDATE_CLIENTS);
        }
        if (flowChanged) {
            setChanged();
        }
    }

    private TapFlow computeFlow(Direction direction) {
        TapFace face = faces[direction.ordinal()];
        boolean input = false;
        boolean output = false;
        if (items(direction) != null) {
            FlowMode mode = face.mode(ConduitChannel.ITEMS);
            input = mode.extracts();
            output = mode.inserts();
        }
        if (target(Capabilities.Fluid.BLOCK, fluidCaches, direction) != null) {
            FlowMode mode = face.mode(ConduitChannel.FLUIDS);
            input |= mode.extracts();
            output |= mode.inserts();
        }
        if (target(Capabilities.Energy.BLOCK, energyCaches, direction) != null) {
            FlowMode mode = face.mode(ConduitChannel.ENERGY);
            input |= mode.extracts();
            output |= mode.inserts();
        }
        return TapFlow.of(input, output);
    }

    public TapFlow flow(Direction direction) {
        return TapFlow.ALL[(flow >>> (direction.ordinal() * 2)) & 3];
    }

    private TapArm computeArm(Direction direction, BlockPos neighbourPos) {
        if (faces[direction.ordinal()].disabled()) {
            return TapArm.NONE;
        }
        BlockState neighbour = level.getBlockState(neighbourPos);
        if (neighbour.isAir()) {
            return TapArm.NONE;
        }
        if (ConduitTapBlock.isConduitPart(neighbour)) {
            return CurrentConduitBlock.joins(level, neighbourPos, direction.getOpposite(), neighbour) ? TapArm.CONDUIT : TapArm.NONE;
        }
        boolean machine = items(direction) != null
                || target(Capabilities.Fluid.BLOCK, fluidCaches, direction) != null
                || target(Capabilities.Energy.BLOCK, energyCaches, direction) != null;
        return machine ? TapArm.MACHINE : TapArm.NONE;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        readSlots();
        if (level != null && !level.isClientSide()) {
            powered = level.hasNeighborSignal(worldPosition);
            armsDirty = true;
        }
    }

    public void serverTick(ServerLevel serverLevel) {
        if (configDirty) {
            applyConfig(serverLevel);
        }
        if (armsDirty) {
            refreshArms();
        }
        if (machineMask == 0) {
            return;
        }
        long time = serverLevel.getGameTime();
        TapRates rates = rates();
        for (Direction direction : DIRECTIONS) {
            if (!connected(direction) || !active(direction, TapSide.INPUT)) {
                continue;
            }
            int d = direction.ordinal();
            TapFace face = faces[d];
            int energy = index(ConduitChannel.ENERGY, direction);
            if (face.mode(ConduitChannel.ENERGY).extracts() && ready(energy, time) && routesFor(ConduitChannel.ENERGY) != null) {
                EnergyHandler source = target(Capabilities.Energy.BLOCK, energyCaches, direction);
                if (source != null) {
                    backOff(energy, EnergyHandlerUtil.move(source, energySinks[d], rates.energyRate(), null) > 0, MAX_ENERGY_IDLE, time);
                }
            }
            int fluid = index(ConduitChannel.FLUIDS, direction);
            if (face.mode(ConduitChannel.FLUIDS).extracts() && ready(fluid, time) && routesFor(ConduitChannel.FLUIDS) != null) {
                ResourceHandler<FluidResource> source = target(Capabilities.Fluid.BLOCK, fluidCaches, direction);
                if (source != null) {
                    backOff(fluid, ResourceHandlerUtil.move(source, fluidSinks[d], fluidFilters[d], rates.fluidRate(), null) > 0, MAX_IDLE, time);
                }
            }
            int item = index(ConduitChannel.ITEMS, direction);
            if (face.mode(ConduitChannel.ITEMS).extracts() && (time + tickOffset) % rates.itemInterval() == 0 && ready(item, time)
                    && routesFor(ConduitChannel.ITEMS) != null) {
                ResourceHandler<ItemResource> source = items(direction);
                if (source != null) {
                    backOff(item, ResourceHandlerUtil.move(source, itemSinks[d], itemFilters[d], rates.itemsPerOp(), null) > 0, MAX_IDLE, time);
                }
            }
        }
    }

    private boolean ready(int index, long time) {
        return time >= nextAttempt[index];
    }

    private void backOff(int index, boolean moved, int maxIdle, long time) {
        if (moved) {
            idle[index] = 0;
            nextAttempt[index] = 0;
            return;
        }
        idle[index] = Math.min(maxIdle, Math.max(1, idle[index] * 2));
        nextAttempt[index] = time + idle[index];
    }

    public void wake() {
        for (int i = 0; i < idle.length; i++) {
            idle[i] = 0;
            nextAttempt[i] = 0;
        }
        armsDirty = true;
    }

    private <T> @Nullable T target(BlockCapability<T, @Nullable Direction> capability, BlockCapabilityCache<T, @Nullable Direction>[] caches, Direction direction) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return null;
        }
        int d = direction.ordinal();
        BlockCapabilityCache<T, @Nullable Direction> cache = caches[d];
        if (cache == null) {
            cache = BlockCapabilityCache.create(capability, serverLevel, worldPosition.relative(direction), direction.getOpposite(),
                    () -> !isRemoved(), this::wake);
            caches[d] = cache;
        }
        return cache.getCapability();
    }

    private @Nullable ResourceHandler<ItemResource> items(Direction direction) {
        ResourceHandler<ItemResource> handler = target(Capabilities.Item.BLOCK, itemCaches, direction);
        return handler != null || level == null ? handler : containerAt(level, worldPosition.relative(direction), direction.getOpposite());
    }

    public static @Nullable ResourceHandler<ItemResource> containerAt(Level level, BlockPos pos, Direction side) {
        BlockState state = level.getBlockState(pos);
        Container container = null;
        if (state.getBlock() instanceof WorldlyContainerHolder holder) {
            container = holder.getContainer(state, level, pos);
        } else if (state.hasBlockEntity() && level.getBlockEntity(pos) instanceof Container blockContainer) {
            container = blockContainer;
        }
        if (container == null) {
            return null;
        }
        return container instanceof WorldlyContainer worldly ? new WorldlyContainerWrapper(worldly, side) : VanillaContainerWrapper.of(container);
    }

    public @Nullable ResourceHandler<ItemResource> itemSink(@Nullable Direction direction) {
        return direction != null && sends(ConduitChannel.ITEMS, direction) ? itemSinks[direction.ordinal()] : null;
    }

    public @Nullable ResourceHandler<FluidResource> fluidSink(@Nullable Direction direction) {
        return direction != null && sends(ConduitChannel.FLUIDS, direction) ? fluidSinks[direction.ordinal()] : null;
    }

    public @Nullable EnergyHandler energySink(@Nullable Direction direction) {
        return direction != null && sends(ConduitChannel.ENERGY, direction) ? energySinks[direction.ordinal()] : null;
    }

    @Override
    public ResourceHandler<ItemResource> getItemHandlerOnSide(Direction direction) {
        return itemSink(direction);
    }

    private int @Nullable [] order(ConduitNetwork.Routes routes, ConduitChannel channel, Direction source) {
        if (faces[source.ordinal()].distribution() != DistributionMode.NEAREST) {
            return null;
        }
        int key = index(channel, source);
        int[] order = nearestOrder[key];
        if (order != null && nearestVersion[key] == routes.version) {
            return order;
        }
        order = new int[routes.size];
        long[] distances = new long[routes.size];
        for (int i = 0; i < routes.size; i++) {
            long distance = routes.taps[i].getBlockPos().distManhattan(worldPosition);
            int at = i;
            while (at > 0 && routes.priorities[order[at - 1]] == routes.priorities[i] && distances[at - 1] > distance) {
                order[at] = order[at - 1];
                distances[at] = distances[at - 1];
                at--;
            }
            order[at] = i;
            distances[at] = distance;
        }
        nearestOrder[key] = order;
        nearestVersion[key] = routes.version;
        return order;
    }

    private static int pick(ConduitNetwork.Routes routes, int @Nullable [] order, int position, int rotation) {
        if (order != null) {
            return order[position];
        }
        int start = routes.groupStart[position];
        int span = routes.groupEnd[position] - start;
        return start + Math.floorMod(position - start + rotation, span);
    }

    private boolean sameTarget(ConduitTapBlockEntity tap, Direction face, Direction source) {
        if (tap == this) {
            return face == source;
        }
        BlockPos other = tap.worldPosition;
        return other.getX() + face.getStepX() == worldPosition.getX() + source.getStepX()
                && other.getY() + face.getStepY() == worldPosition.getY() + source.getStepY()
                && other.getZ() + face.getStepZ() == worldPosition.getZ() + source.getStepZ();
    }

    private @Nullable ConduitNetwork.Routes routesFor(ConduitChannel channel) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return null;
        }
        ConduitNetwork current = network;
        if (current == null || current.dirty()) {
            current = ConduitNetworks.get(serverLevel, worldPosition);
            network = current;
        }
        ConduitNetwork.Routes routes = current.routes(serverLevel, channel);
        return routes.size == 0 ? null : routes;
    }

    private int rotation(Direction source, Budget budget) {
        return faces[source.ordinal()].distribution() == DistributionMode.RANDOM && level != null ? level.getRandom().nextInt(1 << 16) : budget.cursor;
    }

    private <R> int route(Channel<R> channel, Direction source, R resource, int amount, TransactionContext transaction) {
        ConduitChannel kind = channel.kind();
        if (amount <= 0 || routing > 0 || level == null || !sends(kind, source) || !channel.accepts(this, source, TapSide.INPUT, resource)) {
            return 0;
        }
        TapRates rates = rates();
        Budget budget = budgets[index(kind, source)];
        amount = Math.min(amount, budget.available(level.getGameTime(), rates.window(kind), rates.limit(kind)));
        if (amount <= 0) {
            return 0;
        }
        ConduitNetwork.Routes routes = routesFor(kind);
        if (routes == null) {
            return 0;
        }
        int[] order = order(routes, kind, source);
        int rotation = rotation(source, budget);
        routing++;
        try {
            int moved = 0;
            for (int position = 0; position < routes.size && moved < amount; position++) {
                int index = pick(routes, order, position, rotation);
                ConduitTapBlockEntity tap = routes.taps[index];
                Direction face = routes.faces[index];
                if (tap.isRemoved() || sameTarget(tap, face, source) || !channel.accepts(tap, face, TapSide.OUTPUT, resource)) {
                    continue;
                }
                moved += channel.insert(tap, face, resource, amount - moved, transaction);
            }
            if (moved > 0) {
                budget.consume(moved, transaction);
            }
            return moved;
        } finally {
            routing--;
        }
    }

    private final class Sink<R extends Resource> implements ResourceHandler<R> {
        private final Direction source;
        private final Channel<R> channel;
        private final R empty;

        Sink(Direction source, Channel<R> channel, R empty) {
            this.source = source;
            this.channel = channel;
            this.empty = empty;
        }

        @Override
        public int size() {
            return 1;
        }

        @Override
        public R getResource(int index) {
            return empty;
        }

        @Override
        public long getAmountAsLong(int index) {
            return 0;
        }

        @Override
        public long getCapacityAsLong(int index, R resource) {
            return Integer.MAX_VALUE;
        }

        @Override
        public boolean isValid(int index, R resource) {
            return channel.accepts(ConduitTapBlockEntity.this, source, TapSide.INPUT, resource);
        }

        @Override
        public int insert(int index, R resource, int amount, TransactionContext transaction) {
            return resource.isEmpty() ? 0 : route(channel, source, resource, amount, transaction);
        }

        @Override
        public int extract(int index, R resource, int amount, TransactionContext transaction) {
            return 0;
        }
    }

    private final class EnergySink implements EnergyHandler {
        private final Direction source;

        EnergySink(Direction source) {
            this.source = source;
        }

        @Override
        public long getAmountAsLong() {
            return 0;
        }

        @Override
        public long getCapacityAsLong() {
            return Integer.MAX_VALUE;
        }

        @Override
        public int insert(int amount, TransactionContext transaction) {
            return route(ENERGY, source, Boolean.TRUE, amount, transaction);
        }

        @Override
        public int extract(int amount, TransactionContext transaction) {
            return 0;
        }
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        super.preRemoveSideEffects(pos, state);
        if (level != null && !level.isClientSide()) {
            drop();
        }
    }

    @Override
    public <T> Map<Direction, Pair<IOActions, int[]>> getSidedInteractions(BlockCapability<T, @Nullable Direction> capability) {
        return Map.of();
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.nautec.conduit_tap");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new ConduitTapMenu(containerId, inventory, this);
    }

    public void writeFaces(RegistryFriendlyByteBuf buffer) {
        for (TapFace face : faces) {
            TapFace.STREAM_CODEC.encode(buffer, face);
        }
    }

    public void readFaces(RegistryFriendlyByteBuf buffer) {
        for (TapFace face : faces) {
            face.copyFrom(TapFace.STREAM_CODEC.decode(buffer));
        }
    }

    private void writeFaces(ValueOutput out, String key) {
        ValueOutput.ValueOutputList list = out.childrenList(key);
        for (TapFace face : faces) {
            face.save(list.addChild());
        }
    }

    private int readFaces(ValueInput in, String key) {
        int count = 0;
        for (ValueInput child : in.childrenListOrEmpty(key)) {
            if (count < faces.length) {
                faces[count++].load(child);
            }
        }
        return count;
    }

    @Override
    public int[] cardSlots() {
        return new int[]{UPGRADE_SLOT, FILTER_SLOT};
    }

    @Override
    public void saveSettings(ValueOutput out) {
        writeFaces(out, "tap_faces");
    }

    @Override
    public boolean loadSettings(ValueInput in, ServerPlayer player) {
        if (readFaces(in, "tap_faces") == 0) {
            return false;
        }
        for (TapFace face : faces) {
            face.sanitize(player.registryAccess());
        }
        configChanged();
        return true;
    }

    @Override
    protected void saveData(ValueOutput out) {
        super.saveData(out);
        writeFaces(out, "faces");
        out.putInt("flow", flow);
    }

    @Override
    protected void loadData(ValueInput in) {
        super.loadData(in);
        readFaces(in, "faces");
        readSlots();
        flow = in.getIntOr("flow", 0);
    }

    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public @NotNull CompoundTag getUpdateTag(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("flow", flow);
        return tag;
    }

    @Override
    public void handleUpdateTag(ValueInput input) {
        readFlow(input);
    }

    @Override
    public void onDataPacket(Connection connection, ValueInput input) {
        readFlow(input);
    }

    private void readFlow(ValueInput input) {
        int next = input.getIntOr("flow", 0);
        if (next == flow) {
            return;
        }
        flow = next;
        if (level != null && level.isClientSide()) {
            BlockState state = getBlockState();
            level.sendBlockUpdated(worldPosition, state, state, Block.UPDATE_CLIENTS);
        }
    }
}
