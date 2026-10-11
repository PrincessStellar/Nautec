package com.breakinblocks.nautec.content.resonance;

import com.breakinblocks.nautec.utils.DataSlots;

import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.api.blockentities.LaserBlockEntity;
import com.breakinblocks.nautec.capabilities.IOActions;
import com.breakinblocks.nautec.content.menus.ResonanceNodeMenu;
import com.breakinblocks.nautec.registries.NTBlockEntityTypes;
import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import com.breakinblocks.nautec.registries.NTCriteriaTriggers;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.energy.EnergyHandlerUtil;
import net.neoforged.neoforge.transfer.energy.SimpleEnergyHandler;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class ResonanceNodeBlockEntity extends LaserBlockEntity implements MenuProvider, ResonanceTunable, ChunkLoadable {
    public static final int MIN_PRIORITY = -100;
    public static final int MAX_PRIORITY = 100;

    public static final int STATUS_ONLINE = 0;
    public static final int STATUS_NO_NETWORK = 1;
    public static final int STATUS_NO_CORE = 2;

    public static final int DATA_FLOW = 0;
    public static final int DATA_PURITY = 2;
    public static final int DATA_STATUS = 3;
    public static final int DATA_CORES = 4;
    public static final int DATA_OUTPUT = 5;
    public static final int DATA_AP = 6;
    public static final int DATA_FE = 8;
    public static final int DATA_PRIORITY = 10;
    public static final int DATA_LIMIT = 11;
    public static final int DATA_CHUNK = 13;
    public static final int DATA_COUNT = 14;

    private static final int SYNC_INTERVAL = 20;
    private static final Set<Direction> ALL = EnumSet.allOf(Direction.class);

    private final ResonanceChunkLoading.Ticket ticket = new ResonanceChunkLoading.Ticket();
    private final Map<Direction, BlockCapabilityCache<EnergyHandler, @Nullable Direction>> neighbours = new EnumMap<>(Direction.class);

    private @Nullable UUID networkId;
    private String networkName = "";
    private @Nullable UUID joined;
    private boolean output;
    private int priority;
    private int limit = Integer.MAX_VALUE;
    private boolean chunkLoading;
    private int status = STATUS_NO_NETWORK;
    private int cores;
    private int apStored;
    private float apPurity;
    private int beam;
    private int flow;
    private boolean relayed;
    private long lastSync;

    private final SimpleEnergyHandler energy;
    private final EnergyHandler port = new EnergyHandler() {
        @Override
        public long getAmountAsLong() {
            return energy.getAmountAsLong();
        }

        @Override
        public long getCapacityAsLong() {
            return energy.getCapacityAsLong();
        }

        @Override
        public int insert(int amount, TransactionContext transaction) {
            return output ? 0 : energy.insert(amount, transaction);
        }

        @Override
        public int extract(int amount, TransactionContext transaction) {
            return output ? energy.extract(amount, transaction) : 0;
        }
    };

    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case DATA_FLOW -> DataSlots.low(flow);
                case DATA_FLOW + 1 -> DataSlots.high(flow);
                case DATA_PURITY -> Math.round(apPurity * 1000F);
                case DATA_STATUS -> status;
                case DATA_CORES -> cores;
                case DATA_OUTPUT -> output ? 1 : 0;
                case DATA_AP -> DataSlots.low(apStored);
                case DATA_AP + 1 -> DataSlots.high(apStored);
                case DATA_FE -> DataSlots.low(energy.getAmountAsInt());
                case DATA_FE + 1 -> DataSlots.high(energy.getAmountAsInt());
                case DATA_PRIORITY -> priority;
                case DATA_LIMIT -> DataSlots.low(effectiveLimit());
                case DATA_LIMIT + 1 -> DataSlots.high(effectiveLimit());
                case DATA_CHUNK -> ResonanceChunkLoading.state(chunkLoading);
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

    public ResonanceNodeBlockEntity(BlockPos pos, BlockState state) {
        super(NTBlockEntityTypes.RESONANCE_NODE.get(), pos, state);
        this.energy = new SimpleEnergyHandler(NTConfig.resonanceNodeFeBuffer, Integer.MAX_VALUE, Integer.MAX_VALUE) {
            @Override
            protected void onEnergyChanged(int previousAmount) {
                setChanged();
            }
        };
    }

    public ContainerData getData() {
        return data;
    }

    public EnergyHandler getPort() {
        return port;
    }

    public SimpleEnergyHandler getEnergy() {
        return energy;
    }

    public Direction facing() {
        return getBlockState().getValue(ResonanceNodeBlock.FACING);
    }

    public ResourceKey<Level> dimension() {
        return level.dimension();
    }

    public boolean isOutput() {
        return output;
    }

    public void setOutput(boolean output) {
        if (this.output == output) {
            return;
        }
        this.output = output;
        this.beam = 0;
        setChanged();
        sync();
        if (level != null) {
            level.invalidateCapabilities(worldPosition);
        }
    }

    public boolean isOnline() {
        return status == STATUS_ONLINE;
    }

    public int getStatus() {
        return status;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = Mth.clamp(priority, ResonanceNodeBlockEntity.MIN_PRIORITY, ResonanceNodeBlockEntity.MAX_PRIORITY);
        setChanged();
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = Math.max(0, limit);
        setChanged();
    }

    public int effectiveLimit() {
        return Math.max(0, Math.min(limit, NTConfig.satelliteTransferLimit));
    }

    public int getApStored() {
        return apStored;
    }

    public float getApPurity() {
        return apPurity;
    }

    public static String statusKey(int status) {
        return switch (status) {
            case STATUS_ONLINE -> "nautec.resonance_node.status.online";
            case STATUS_NO_CORE -> "nautec.resonance_node.status.no_core";
            default -> "nautec.resonance_node.status.no_network";
        };
    }

    public int apOffer() {
        return output || status != STATUS_ONLINE ? 0 : Math.min(apStored, effectiveLimit());
    }

    public int feOffer() {
        return output || status != STATUS_ONLINE ? 0 : Math.min(energy.getAmountAsInt(), effectiveLimit());
    }

    public int apDemand() {
        return output && status == STATUS_ONLINE ? Math.max(0, Math.min(effectiveLimit(), NTConfig.resonanceNodeApBuffer - apStored)) : 0;
    }

    public int feDemand() {
        return output && status == STATUS_ONLINE ? Math.max(0, Math.min(effectiveLimit(), energy.getCapacityAsInt() - energy.getAmountAsInt())) : 0;
    }

    public int takeAp(int amount) {
        int taken = Math.max(0, Math.min(amount, apStored));
        apStored -= taken;
        if (apStored == 0) {
            apPurity = 0F;
        }
        setChanged();
        return taken;
    }

    public void giveAp(int amount, float purity) {
        addAp(amount, purity);
        if (amount > 0 && !relayed && level instanceof ServerLevel serverLevel) {
            relayed = true;
            NTCriteriaTriggers.triggerNear(NTCriteriaTriggers.SATELLITE_RELAY.get(), serverLevel, worldPosition, 32.0);
        }
    }

    private void addAp(int amount, float purity) {
        int added = Math.min(amount, Math.max(0, NTConfig.resonanceNodeApBuffer - apStored));
        if (added <= 0) {
            return;
        }
        apPurity = mergedPurity(apStored, apPurity, added, purity);
        apStored += added;
        setChanged();
    }

    public int takeFe(int amount) {
        int taken = Math.max(0, Math.min(amount, energy.getAmountAsInt()));
        energy.set(energy.getAmountAsInt() - taken);
        return taken;
    }

    public void giveFe(int amount) {
        if (amount > 0) {
            energy.set(Math.min(energy.getCapacityAsInt(), energy.getAmountAsInt() + amount));
        }
    }

    @Override
    public boolean isChunkLoading() {
        return chunkLoading;
    }

    @Override
    public void setChunkLoading(boolean chunkLoading) {
        this.chunkLoading = chunkLoading;
        setChanged();
        if (level instanceof ServerLevel serverLevel) {
            ticket.update(serverLevel, worldPosition, chunkLoading);
        }
    }

    @Override
    public Set<Direction> getLaserInputs() {
        return output ? Set.of() : ALL;
    }

    @Override
    public Set<Direction> getLaserOutputs() {
        return output ? Set.of(facing()) : Set.of();
    }

    @Override
    public Set<Direction> getPotentialLaserOutputs() {
        return Set.of(facing());
    }

    @Override
    protected int outgoingPower(Direction direction) {
        return direction == facing() ? beam : 0;
    }

    @Override
    public @Nullable UUID getNetworkId() {
        return networkId;
    }

    public String getNetworkName() {
        return networkName;
    }

    @Override
    public @Nullable ResonanceNetwork getNetwork() {
        if (networkId == null || !(level instanceof ServerLevel serverLevel)) {
            return null;
        }
        return ResonanceNetworks.get(serverLevel.getServer()).get(networkId);
    }

    @Override
    public void setNetwork(@Nullable ResonanceNetwork network) {
        UUID id = network == null ? null : network.id();
        if (joined != null && !joined.equals(id)) {
            SatelliteGrid.leaveNode(joined, this);
            joined = null;
        }
        this.networkId = id;
        this.networkName = network == null ? "" : network.name();
        setChanged();
        sync();
    }

    @Override
    public int members(ResonanceNetwork network) {
        List<ResonanceNodeBlockEntity> members = SatelliteGrid.nodes(network.id());
        boolean pending = network.id().equals(networkId) && !members.contains(this);
        return SatelliteGrid.members(network.id()).size() + members.size() + (pending ? 1 : 0);
    }

    @Override
    public void commonTick() {
        if (level instanceof ServerLevel serverLevel) {
            serverTick(serverLevel);
            if (output) {
                int newBeam = connectedOutputs() > 0 ? Math.min(effectiveLimit(), apStored) : 0;
                if (newBeam > 0) {
                    apStored -= newBeam;
                    setChanged();
                }
                if (newBeam != beam) {
                    beam = newBeam;
                    setChanged();
                    requestSync();
                }
                flow = beam;
                push(serverLevel);
            }
        }
        if (output) {
            transmitPower(beam);
            setPurity(apStored > 0 || beam > 0 ? apPurity : 0F);
        }
        super.commonTick();
        if (output) {
            this.power = beam;
        } else if (level instanceof ServerLevel) {
            flow = getPower();
            addAp(getPower(), getPurity());
        }
    }

    private void push(ServerLevel serverLevel) {
        int budget = Math.min(energy.getAmountAsInt(), effectiveLimit());
        for (Direction direction : Direction.values()) {
            if (budget <= 0) {
                return;
            }
            BlockPos target = worldPosition.relative(direction);
            if (serverLevel.getBlockEntity(target) instanceof ResonanceNodeBlockEntity) {
                continue;
            }
            EnergyHandler handler = neighbours.computeIfAbsent(direction, side ->
                    BlockCapabilityCache.create(Capabilities.Energy.BLOCK, serverLevel, target, side.getOpposite())).getCapability();
            if (handler != null) {
                budget -= EnergyHandlerUtil.move(energy, handler, budget, null);
            }
        }
    }

    private void serverTick(ServerLevel serverLevel) {
        long tick = serverLevel.getGameTime();
        if (tick % 20 == 0) {
            ticket.update(serverLevel, worldPosition, chunkLoading);
        }
        ResonanceNetwork network = networkId == null ? null : ResonanceNetworks.get(serverLevel.getServer()).get(networkId);
        if (network == null) {
            if (joined != null) {
                SatelliteGrid.leaveNode(joined, this);
                joined = null;
            }
            if (networkId != null && tick % 20 == 0) {
                setNetwork(null);
            }
        } else {
            if (!network.name().equals(networkName)) {
                this.networkName = network.name();
                sync();
            }
            if (joined == null) {
                SatelliteGrid.joinNode(networkId, this);
                joined = networkId;
            }
        }
        SatelliteGrid.Link link = network == null ? SatelliteGrid.EMPTY : SatelliteGrid.link(network.id(), tick);
        int newStatus = network == null ? STATUS_NO_NETWORK : link.uplinks() == 0 ? STATUS_NO_CORE : STATUS_ONLINE;
        boolean flipped = newStatus != status;
        if (newStatus != STATUS_ONLINE) {
            relayed = false;
        }
        this.status = newStatus;
        this.cores = link.uplinks();
        if (flipped || tick - lastSync >= SYNC_INTERVAL * 4) {
            lastSync = tick;
            setChanged();
            sync();
        }
    }

    private void sync() {
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        if (level instanceof ServerLevel serverLevel) {
            ticket.release(serverLevel, pos);
        }
        super.preRemoveSideEffects(pos, state);
    }

    @Override
    public void setRemoved() {
        if (joined != null) {
            SatelliteGrid.leaveNode(joined, this);
            joined = null;
        }
        super.setRemoved();
    }

    @Override
    public <T> Map<Direction, Pair<IOActions, int[]>> getSidedInteractions(BlockCapability<T, @Nullable Direction> capability) {
        return Map.of();
    }

    @Override
    public void saveSettings(ValueOutput out) {
        ResonanceActions.copyNetwork(networkId, out);
        out.putBoolean("output", output);
        out.putInt("priority", priority);
        out.putInt("limit", limit);
        out.putBoolean("chunk_loading", chunkLoading);
    }

    @Override
    public boolean loadSettings(ValueInput in, ServerPlayer player) {
        boolean applied = ResonanceActions.pasteNetwork(player, this, in);
        if (in.read("output", Codec.BOOL).isPresent()) {
            setOutput(in.getBooleanOr("output", output));
            applied = true;
        }
        if (in.getInt("priority").isPresent()) {
            setPriority(in.getIntOr("priority", priority));
            applied = true;
        }
        if (in.getInt("limit").isPresent()) {
            setLimit(in.getIntOr("limit", limit));
            applied = true;
        }
        if (in.read("chunk_loading", Codec.BOOL).isPresent()) {
            setChunkLoading(in.getBooleanOr("chunk_loading", chunkLoading));
            applied = true;
        }
        return applied;
    }

    @Override
    protected void saveData(ValueOutput out) {
        super.saveData(out);
        if (networkId != null) {
            out.store("network", UUIDUtil.CODEC, networkId);
        }
        out.putString("network_name", networkName);
        out.putBoolean("output", output);
        out.putInt("priority", priority);
        out.putInt("limit", limit);
        out.putBoolean("chunk_loading", chunkLoading);
        out.putInt("status", status);
        out.putInt("cores", cores);
        out.putInt("ap_stored", apStored);
        out.putFloat("ap_purity", apPurity);
        out.putInt("beam", beam);
        energy.serialize(out.child("energy"));
    }

    @Override
    protected void loadData(ValueInput in) {
        super.loadData(in);
        this.networkId = in.read("network", UUIDUtil.CODEC).orElse(null);
        this.networkName = in.getStringOr("network_name", "");
        this.output = in.getBooleanOr("output", false);
        this.priority = in.getIntOr("priority", 0);
        this.limit = in.getIntOr("limit", Integer.MAX_VALUE);
        this.chunkLoading = in.getBooleanOr("chunk_loading", false);
        this.status = in.getIntOr("status", STATUS_NO_NETWORK);
        this.cores = in.getIntOr("cores", 0);
        this.apStored = in.getIntOr("ap_stored", 0);
        this.apPurity = in.getFloatOr("ap_purity", 0F);
        this.beam = in.getIntOr("beam", 0);
        energy.deserialize(in.childOrEmpty("energy"));
    }

    @Override
    public Component getDisplayName() {
        return getBlockState().getBlock().getName();
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new ResonanceNodeMenu(containerId, inventory, this);
    }
}
