package com.breakinblocks.nautec.content.resonance;

import com.breakinblocks.nautec.transfer.TransferCapabilities;
import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.api.blockentities.ContainerBlockEntity;
import com.breakinblocks.nautec.capabilities.IOActions;
import com.breakinblocks.nautec.registries.NTBlockEntityTypes;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.Pair;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.UUIDUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import com.breakinblocks.nautec.utils.valueio.ValueInput;
import com.breakinblocks.nautec.utils.valueio.ValueOutput;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;

import com.breakinblocks.nautec.transfer.energy.EnergyHandler;
import com.breakinblocks.nautec.transfer.energy.EnergyHandlerUtil;
import com.breakinblocks.nautec.transfer.energy.SimpleEnergyHandler;
import com.breakinblocks.nautec.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class PrismaticEmitterBlockEntity extends ContainerBlockEntity {
    private static final int VISUAL_HOLD = 20;
    private static final int PRUNE_INTERVAL = 10;

    public record Link(BlockPos pos, Direction face) {
        public static final Codec<Link> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                BlockPos.CODEC.fieldOf("pos").forGetter(Link::pos),
                Direction.CODEC.fieldOf("face").forGetter(Link::face)
        ).apply(instance, Link::new));
    }

    public enum LinkResult {
        LINKED,
        UNLINKED,
        TOO_FAR,
        FULL,
        NO_ENERGY,
        LOOP,
        INVALID
    }

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
    private final List<Link> links = new ArrayList<>();
    private final Map<Link, BlockCapabilityCache<EnergyHandler, @Nullable Direction>> caches = new HashMap<>();

    private @Nullable UUID owner;
    private String ownerName = "";
    private int flow;
    private int flowAccumulator;
    private int activeTicks;
    private boolean visualActive;
    private int cursor;

    public PrismaticEmitterBlockEntity(BlockPos pos, BlockState state) {
        super(NTBlockEntityTypes.PRISMATIC_EMITTER.get(), pos, state);
        this.energy = new SimpleEnergyHandler(NTConfig.emitterBuffer, Integer.MAX_VALUE, Integer.MAX_VALUE) {
            @Override
            protected void onEnergyChanged(int previousAmount) {
                setChanged();
            }
        };
    }

    public EnergyHandler getPort() {
        return port;
    }

    public SimpleEnergyHandler getEnergyStorage() {
        return energy;
    }

    public List<Link> getLinks() {
        return links;
    }

    public int getFlow() {
        return flow;
    }

    public boolean isVisualActive() {
        return visualActive;
    }

    public String getOwnerName() {
        return ownerName.isEmpty() ? "?" : ownerName;
    }

    public void setOwner(UUID owner, String name) {
        this.owner = owner;
        this.ownerName = name;
        setChanged();
    }

    public boolean canTune(ServerPlayer player) {
        return owner == null || owner.equals(player.getUUID())
                || player.hasPermissions(Commands.LEVEL_GAMEMASTERS);
    }

    public static boolean accepts(ServerLevel level, BlockPos pos, Direction face) {
        if (level.getBlockEntity(pos) instanceof ResonanceNodeBlockEntity) {
            return false;
        }
        return level.getCapability(TransferCapabilities.Energy.BLOCK, pos, face) != null;
    }

    public LinkResult toggle(BlockPos pos, Direction face) {
        if (!(level instanceof ServerLevel serverLevel) || pos.equals(worldPosition)) {
            return LinkResult.INVALID;
        }
        for (Link link : links) {
            if (link.pos().equals(pos)) {
                links.remove(link);
                caches.remove(link);
                changed();
                return LinkResult.UNLINKED;
            }
        }
        if (pos.getCenter().distanceTo(worldPosition.getCenter()) > NTConfig.emitterRange) {
            return LinkResult.TOO_FAR;
        }
        if (links.size() >= NTConfig.emitterMaxLinks) {
            return LinkResult.FULL;
        }
        if (!accepts(serverLevel, pos, face)) {
            return LinkResult.NO_ENERGY;
        }
        if (feedsBack(serverLevel, pos)) {
            return LinkResult.LOOP;
        }
        links.add(new Link(pos.immutable(), face));
        changed();
        return LinkResult.LINKED;
    }

    private boolean feedsBack(ServerLevel serverLevel, BlockPos start) {
        Set<BlockPos> seen = new HashSet<>();
        Deque<BlockPos> queue = new ArrayDeque<>();
        queue.add(start);
        while (!queue.isEmpty()) {
            BlockPos pos = queue.poll();
            if (pos.equals(worldPosition)) {
                return true;
            }
            if (!seen.add(pos) || !serverLevel.isLoaded(pos)) {
                continue;
            }
            if (serverLevel.getBlockEntity(pos) instanceof PrismaticEmitterBlockEntity other) {
                for (Link link : other.links) {
                    queue.add(link.pos());
                }
            }
        }
        return false;
    }

    private void pruneLinks(ServerLevel serverLevel) {
        boolean removed = links.removeIf(link -> {
            if (!serverLevel.isLoaded(link.pos()) || serverLevel.getCapability(TransferCapabilities.Energy.BLOCK, link.pos(), link.face()) != null) {
                return false;
            }
            caches.remove(link);
            return true;
        });
        if (removed) {
            cursor = 0;
            changed();
        }
    }

    public void clearLinks() {
        links.clear();
        caches.clear();
        changed();
    }

    private void changed() {
        setChanged();
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    public void commonTick() {
        super.commonTick();
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        if (!links.isEmpty() && (serverLevel.getGameTime() + worldPosition.hashCode()) % PRUNE_INTERVAL == 0) {
            pruneLinks(serverLevel);
        }
        if (!links.isEmpty() && energy.getAmountAsInt() > 0) {
            int budget = Math.min(NTConfig.emitterThroughput, energy.getAmountAsInt());
            int count = links.size();
            int sent = 0;
            for (int i = 0; i < count && budget - sent > 0; i++) {
                Link link = links.get((cursor + i) % count);
                if (!serverLevel.isLoaded(link.pos())) {
                    continue;
                }
                EnergyHandler target = caches.computeIfAbsent(link,
                        key -> BlockCapabilityCache.create(TransferCapabilities.Energy.BLOCK, serverLevel, key.pos(), key.face())).getCapability();
                if (target == null) {
                    continue;
                }
                int share = Math.max(1, (budget - sent) / (count - i));
                sent += EnergyHandlerUtil.move(energy, target, share, null);
            }
            cursor = count == 0 ? 0 : (cursor + 1) % count;
            if (sent > 0) {
                flowAccumulator += sent;
                activeTicks = VISUAL_HOLD;
            }
        }
        if (level.getGameTime() % 20 == 0) {
            flow = flowAccumulator / 20;
            flowAccumulator = 0;
        }
        if (activeTicks > 0) {
            activeTicks--;
        }
        boolean active = activeTicks > 0;
        if (active != visualActive) {
            visualActive = active;
            changed();
        }
    }

    @Override
    public <T> Map<Direction, Pair<IOActions, int[]>> getSidedInteractions(BlockCapability<T, @Nullable Direction> capability) {
        return Map.of();
    }

    @Override
    protected void saveData(ValueOutput out) {
        super.saveData(out);
        energy.serialize(out.child("energy"));
        out.store("links", Link.CODEC.listOf(), List.copyOf(links));
        if (owner != null) {
            out.store("owner", UUIDUtil.CODEC, owner);
        }
        out.putString("owner_name", ownerName);
        out.putBoolean("active", visualActive);
    }

    @Override
    protected void loadData(ValueInput in) {
        super.loadData(in);
        energy.deserialize(in.childOrEmpty("energy"));
        links.clear();
        caches.clear();
        links.addAll(in.read("links", Link.CODEC.listOf()).orElse(List.of()));
        this.owner = in.read("owner", UUIDUtil.CODEC).orElse(null);
        this.ownerName = in.getStringOr("owner_name", "");
        this.visualActive = in.getBooleanOr("active", false);
    }
}
