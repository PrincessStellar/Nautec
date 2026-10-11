package com.breakinblocks.nautec.content.resonance;

import com.breakinblocks.nautec.utils.DataSlots;

import com.mojang.serialization.Codec;
import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.api.blockentities.LaserBlockEntity;
import com.breakinblocks.nautec.capabilities.IOActions;
import com.breakinblocks.nautec.content.menus.SatelliteArrayMenu;
import com.breakinblocks.nautec.registries.NTBlockEntityTypes;
import com.breakinblocks.nautec.registries.NTItems;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.energy.SimpleEnergyHandler;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class SatelliteArrayBlockEntity extends LaserBlockEntity implements MenuProvider, ResonanceTunable, ChunkLoadable {
    public static final int STATUS_ONLINE = 0;
    public static final int STATUS_NO_NETWORK = 1;
    public static final int STATUS_NO_SATELLITE = 2;
    public static final int STATUS_SKY_BLOCKED = 3;
    public static final int STATUS_NO_OUTPUT = 4;

    public static final int DATA_POWER = 0;
    public static final int DATA_PURITY = 2;
    public static final int DATA_STATUS = 3;
    public static final int DATA_UPLINKS = 4;
    public static final int DATA_AP = 5;
    public static final int DATA_FE = 7;
    public static final int DATA_CHUNK = 9;
    public static final int DATA_NODES = 10;
    public static final int DATA_COUNT = 11;

    public static final int LAUNCH_TICKS = 80;
    private static final int SKY_INTERVAL = 20;
    private static final int SYNC_INTERVAL = 10;
    private static final Set<Direction> PORTS = ObjectSet.of(Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST, Direction.DOWN);

    private @Nullable UUID networkId;
    private String networkName = "";
    private @Nullable UUID joined;
    private boolean satellite;
    private long launchedAt = Long.MIN_VALUE;
    private boolean sky = true;
    private int relay;
    private float relayPurity;
    private int status = STATUS_NO_NETWORK;
    private int uplinks;
    private long lastSync;

    private int apStored;
    private float apPurity;
    private int nodes;
    private boolean chunkLoading;
    private final ResonanceChunkLoading.Ticket ticket = new ResonanceChunkLoading.Ticket();

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
            return energy.insert(amount, transaction);
        }

        @Override
        public int extract(int amount, TransactionContext transaction) {
            return 0;
        }
    };

    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case DATA_POWER -> DataSlots.low(relay);
                case DATA_POWER + 1 -> DataSlots.high(relay);
                case DATA_PURITY -> Math.round(relayPurity * 1000F);
                case DATA_STATUS -> status;
                case DATA_UPLINKS -> uplinks;
                case DATA_AP -> DataSlots.low(apStored);
                case DATA_AP + 1 -> DataSlots.high(apStored);
                case DATA_FE -> DataSlots.low(energy.getAmountAsInt());
                case DATA_FE + 1 -> DataSlots.high(energy.getAmountAsInt());
                case DATA_CHUNK -> ResonanceChunkLoading.state(chunkLoading);
                case DATA_NODES -> nodes;
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

    public SatelliteArrayBlockEntity(BlockPos pos, BlockState state) {
        super(NTBlockEntityTypes.SATELLITE_ARRAY.get(), pos, state);
        this.energy = new SimpleEnergyHandler(NTConfig.satelliteFeBuffer, Integer.MAX_VALUE, Integer.MAX_VALUE) {
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

    public ResourceKey<Level> dimension() {
        return level.dimension();
    }

    public boolean hasSatellite() {
        return satellite;
    }

    public long getLaunchedAt() {
        return launchedAt;
    }

    public boolean hasSky() {
        return sky;
    }

    public int getStatus() {
        return status;
    }

    public static String statusKey(int status) {
        return switch (status) {
            case STATUS_ONLINE -> "nautec.satellite.status.online";
            case STATUS_NO_SATELLITE -> "nautec.satellite.status.no_satellite";
            case STATUS_SKY_BLOCKED -> "nautec.satellite.status.sky";
            case STATUS_NO_OUTPUT -> "nautec.satellite.status.no_output";
            default -> "nautec.satellite.status.no_network";
        };
    }

    public int getRelay() {
        return relay;
    }

    public float getRelayPurity() {
        return relayPurity;
    }

    public int getApStored() {
        return apStored;
    }

    public float getApPurity() {
        return apPurity;
    }

    public boolean transmitting() {
        return satellite && sky && networkId != null;
    }

    public int takeAp(int amount) {
        int taken = Math.max(0, Math.min(amount, apStored));
        apStored -= taken;
        if (apStored == 0) {
            apPurity = 0F;
        }
        relay += taken;
        setChanged();
        return taken;
    }

    private void addAp(int amount, float purity) {
        int room = Math.max(0, NTConfig.satelliteApBuffer - apStored);
        int added = Math.min(amount, room);
        if (added <= 0) {
            return;
        }
        apPurity = mergedPurity(apStored, apPurity, added, purity);
        apStored += added;
        setChanged();
    }

    public int apRoom() {
        return transmitting() ? Math.max(0, NTConfig.satelliteApBuffer - apStored) : 0;
    }

    public int feRoom() {
        return transmitting() ? Math.max(0, energy.getCapacityAsInt() - energy.getAmountAsInt()) : 0;
    }

    public void storeAp(int amount, float purity) {
        addAp(amount, purity);
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
    public Set<Direction> getLaserInputs() {
        return PORTS;
    }

    @Override
    public Set<Direction> getLaserOutputs() {
        return Set.of();
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
            SatelliteGrid.leave(joined, this);
            joined = null;
        }
        this.networkId = id;
        this.networkName = network == null ? "" : network.name();
        setChanged();
        sync();
    }

    @Override
    public int members(ResonanceNetwork network) {
        List<SatelliteArrayBlockEntity> members = SatelliteGrid.members(network.id());
        boolean pending = network.id().equals(networkId) && !members.contains(this);
        return members.size() + (pending ? 1 : 0);
    }

    public boolean launch() {
        if (satellite || level == null) {
            return false;
        }
        this.satellite = true;
        this.launchedAt = level.getGameTime();
        setChanged();
        sync();
        return true;
    }

    @Override
    public void commonTick() {
        if (level instanceof ServerLevel serverLevel) {
            serverTick(serverLevel);
        }
        super.commonTick();
        if (level instanceof ServerLevel) {
            addAp(getPower(), getPurity());
        }
    }

    public void beginTransfer() {
        relay = 0;
    }

    public void finishTransfer() {
        relayPurity = relay > 0 ? apPurity : 0F;
    }

    private void serverTick(ServerLevel serverLevel) {
        long tick = serverLevel.getGameTime();
        if (tick % 20 == 0) {
            ticket.update(serverLevel, worldPosition, chunkLoading);
        }
        if (tick % SKY_INTERVAL == 0) {
            sky = SatelliteGrid.clearSky(serverLevel, worldPosition.above());
        }

        ResonanceNetwork network = networkId == null ? null : ResonanceNetworks.get(serverLevel.getServer()).get(networkId);
        if (network == null) {
            relay = 0;
            if (joined != null) {
                SatelliteGrid.leave(joined, this);
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
                SatelliteGrid.join(networkId, this);
                joined = networkId;
            }
        }

        SatelliteGrid.Link link = network == null ? SatelliteGrid.EMPTY : SatelliteGrid.link(network.id(), tick);
        int newStatus = network == null ? STATUS_NO_NETWORK : !satellite ? STATUS_NO_SATELLITE : !sky ? STATUS_SKY_BLOCKED
                : link.nodes() == 0 ? STATUS_NO_OUTPUT : STATUS_ONLINE;

        boolean changed = newStatus != status || link.uplinks() != uplinks || link.nodes() != nodes;
        boolean flipped = newStatus != status;
        this.status = newStatus;
        this.uplinks = link.uplinks();
        this.nodes = link.nodes();
        if ((changed && (flipped || tick - lastSync >= SYNC_INTERVAL)) || tick - lastSync >= SYNC_INTERVAL * 4) {
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
        if (satellite && level != null) {
            satellite = false;
            Containers.dropItemStack(level, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, new ItemStack(NTItems.PRISM_SATELLITE.get()));
        }
    }

    @Override
    public void setRemoved() {
        if (joined != null) {
            SatelliteGrid.leave(joined, this);
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
        out.putBoolean("chunk_loading", chunkLoading);
    }

    @Override
    public boolean loadSettings(ValueInput in, ServerPlayer player) {
        boolean applied = ResonanceActions.pasteNetwork(player, this, in);
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
        out.putBoolean("satellite", satellite);
        out.putLong("launched_at", launchedAt);
        out.putBoolean("sky", sky);
        out.putInt("relay", relay);
        out.putFloat("relay_purity", relayPurity);
        out.putInt("status", status);
        out.putInt("uplinks", uplinks);
        out.putInt("ap_stored", apStored);
        out.putFloat("ap_purity", apPurity);
        out.putInt("nodes", nodes);
        out.putBoolean("chunk_loading", chunkLoading);
        energy.serialize(out.child("energy"));
    }

    @Override
    protected void loadData(ValueInput in) {
        super.loadData(in);
        this.networkId = in.read("network", UUIDUtil.CODEC).orElse(null);
        this.networkName = in.getStringOr("network_name", "");
        this.satellite = in.getBooleanOr("satellite", false);
        this.launchedAt = in.getLongOr("launched_at", Long.MIN_VALUE);
        this.sky = in.getBooleanOr("sky", true);
        this.relay = in.getIntOr("relay", 0);
        this.relayPurity = in.getFloatOr("relay_purity", 0F);
        this.status = in.getIntOr("status", STATUS_NO_NETWORK);
        this.uplinks = in.getIntOr("uplinks", 0);
        this.apStored = in.getIntOr("ap_stored", 0);
        this.apPurity = in.getFloatOr("ap_purity", 0F);
        this.nodes = in.getIntOr("nodes", 0);
        this.chunkLoading = in.getBooleanOr("chunk_loading", false);
        energy.deserialize(in.childOrEmpty("energy"));
    }

    @Override
    public Component getDisplayName() {
        return getBlockState().getBlock().getName();
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new SatelliteArrayMenu(containerId, inventory, this);
    }
}
