package com.breakinblocks.nautec.content.resonance;

import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.api.blockentities.LaserBlockEntity;
import com.breakinblocks.nautec.registries.NTCriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.IntUnaryOperator;

@EventBusSubscriber(modid = Nautec.MODID)
public final class SatelliteGrid {
    public static final Link EMPTY = new Link(0, 0F, 0, 0);

    private static final Map<UUID, Set<SatelliteArrayBlockEntity>> ARRAYS = new HashMap<>();
    private static final Map<UUID, Set<ResonanceNodeBlockEntity>> NODES = new HashMap<>();
    private static final Map<UUID, Cached> CACHE = new HashMap<>();
    private static final Map<UUID, CharmEntry> CHARMS = new HashMap<>();

    private SatelliteGrid() {
    }

    public record Link(int total, float purity, int uplinks, int nodes) {
    }

    private record Cached(long tick, Link link) {
    }

    private record CharmEntry(UUID network, long seen) {
    }

    private interface Receiver {
        int priority();

        int demand();

        int deliver(int amount);
    }

    public static void join(UUID network, SatelliteArrayBlockEntity array) {
        ARRAYS.computeIfAbsent(network, key -> new LinkedHashSet<>()).add(array);
    }

    public static void leave(UUID network, SatelliteArrayBlockEntity array) {
        Set<SatelliteArrayBlockEntity> members = ARRAYS.get(network);
        if (members != null) {
            members.remove(array);
            if (members.isEmpty()) {
                ARRAYS.remove(network);
            }
        }
    }

    public static void joinNode(UUID network, ResonanceNodeBlockEntity node) {
        NODES.computeIfAbsent(network, key -> new LinkedHashSet<>()).add(node);
    }

    public static void leaveNode(UUID network, ResonanceNodeBlockEntity node) {
        Set<ResonanceNodeBlockEntity> members = NODES.get(network);
        if (members != null) {
            members.remove(node);
            if (members.isEmpty()) {
                NODES.remove(network);
            }
        }
    }

    public static List<SatelliteArrayBlockEntity> members(UUID network) {
        Set<SatelliteArrayBlockEntity> members = ARRAYS.get(network);
        return members == null ? List.of() : List.copyOf(members);
    }

    public static List<ResonanceNodeBlockEntity> nodes(UUID network) {
        Set<ResonanceNodeBlockEntity> members = NODES.get(network);
        return members == null ? List.of() : List.copyOf(members);
    }

    public static void charm(ServerPlayer player, UUID network) {
        CHARMS.put(player.getUUID(), new CharmEntry(network, player.level().getGameTime()));
    }

    public static boolean clearSky(Level level, BlockPos top) {
        int surface = level.getHeight(Heightmap.Types.WORLD_SURFACE, top.getX(), top.getZ());
        BlockPos.MutableBlockPos cursor = top.mutable();
        for (int y = top.getY() + 1; y < surface; y++) {
            BlockState state = level.getBlockState(cursor.setY(y));
            if (state.canOcclude() && !state.getOcclusionShape(level, cursor).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    public static Link link(UUID network, long tick) {
        Cached cached = CACHE.get(network);
        if (cached != null && cached.tick() == tick) {
            return cached.link();
        }
        Link link = compute(members(network), nodes(network));
        CACHE.put(network, new Cached(tick, link));
        return link;
    }

    public static Link compute(List<SatelliteArrayBlockEntity> members, List<ResonanceNodeBlockEntity> nodes) {
        long total = 0;
        double weighted = 0;
        int uplinks = 0;
        for (SatelliteArrayBlockEntity array : members) {
            if (!array.isRemoved() && array.transmitting()) {
                uplinks++;
                int power = Math.max(0, array.getPower());
                total += power;
                weighted += (double) power * array.getPurity();
            }
        }
        int outputs = 0;
        for (ResonanceNodeBlockEntity node : nodes) {
            if (!node.isRemoved() && node.isOutput()) {
                outputs++;
            }
        }
        int capped = (int) Math.min(Integer.MAX_VALUE, total);
        float purity = total > 0 ? (float) (weighted / total) : 0F;
        return new Link(capped, purity, uplinks, outputs);
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        MinecraftServer server = event.getServer();
        long tick = server.overworld().getGameTime();
        boolean charmTick = tick % ResonanceCharmItem.INTERVAL == 0;
        Map<UUID, Integer> charmDelivered = new HashMap<>();
        Map<UUID, ServerPlayer> charmPlayers = new HashMap<>();
        if (charmTick) {
            Iterator<Map.Entry<UUID, CharmEntry>> charms = CHARMS.entrySet().iterator();
            while (charms.hasNext()) {
                Map.Entry<UUID, CharmEntry> entry = charms.next();
                ServerPlayer player = server.getPlayerList().getPlayer(entry.getKey());
                if (player == null || tick - entry.getValue().seen() > ResonanceCharmItem.INTERVAL * 2L) {
                    charms.remove();
                    continue;
                }
                charmPlayers.put(entry.getKey(), player);
            }
        }

        Set<UUID> networks = new HashSet<>(ARRAYS.keySet());
        networks.addAll(NODES.keySet());
        for (UUID network : networks) {
            List<SatelliteArrayBlockEntity> arrays = new ArrayList<>();
            for (SatelliteArrayBlockEntity array : ARRAYS.getOrDefault(network, Set.of())) {
                if (!array.isRemoved() && array.getLevel() != null) {
                    arrays.add(array);
                }
            }
            List<ResonanceNodeBlockEntity> nodes = new ArrayList<>();
            for (ResonanceNodeBlockEntity node : NODES.getOrDefault(network, Set.of())) {
                if (!node.isRemoved() && node.getLevel() != null) {
                    nodes.add(node);
                }
            }
            List<ServerPlayer> players = new ArrayList<>();
            if (charmTick) {
                for (Map.Entry<UUID, ServerPlayer> charm : charmPlayers.entrySet()) {
                    CharmEntry charmEntry = CHARMS.get(charm.getKey());
                    if (charmEntry != null && charmEntry.network().equals(network)) {
                        players.add(charm.getValue());
                    }
                }
            }
            transfer(arrays, nodes, players, tick, charmDelivered);
        }

        if (charmTick) {
            for (Map.Entry<UUID, ServerPlayer> charm : charmPlayers.entrySet()) {
                ServerPlayer player = charm.getValue();
                ResonanceCharmItem.Tuning tuning = ResonanceCharmItem.tuning(player);
                if (tuning == null) {
                    continue;
                }
                if (charmDelivered.getOrDefault(charm.getKey(), 0) > 0) {
                    ResonanceCharmItem.charged(player);
                    NTCriteriaTriggers.CHARM_CHARGED.get().trigger(player);
                }
            }
        }
    }

    private static void transfer(List<SatelliteArrayBlockEntity> arrays, List<ResonanceNodeBlockEntity> nodes, List<ServerPlayer> players, long tick,
                                 Map<UUID, Integer> charmDelivered) {
        List<SatelliteArrayBlockEntity> cores = new ArrayList<>();
        for (SatelliteArrayBlockEntity array : arrays) {
            array.beginTransfer();
            if (array.transmitting()) {
                cores.add(array);
            }
        }
        List<ResonanceNodeBlockEntity> inputs = new ArrayList<>();
        List<ResonanceNodeBlockEntity> outputs = new ArrayList<>();
        for (ResonanceNodeBlockEntity node : nodes) {
            (node.isOutput() ? outputs : inputs).add(node);
        }
        if (!cores.isEmpty()) {
            List<SatelliteArrayBlockEntity> rotated = rotate(cores, tick);
            feedCores(inputs, rotated);
            moveAp(rotated, outputs);
            moveFe(rotated, outputs, players, charmDelivered);
        }
        for (SatelliteArrayBlockEntity array : arrays) {
            array.finishTransfer();
        }
    }

    private static <T> List<T> rotate(List<T> list, long tick) {
        int offset = (int) Math.floorMod(tick, (long) list.size());
        List<T> rotated = new ArrayList<>(list.size());
        for (int i = 0; i < list.size(); i++) {
            rotated.add(list.get((i + offset) % list.size()));
        }
        return rotated;
    }

    private static double factor() {
        return 1.0 - NTConfig.satelliteLoss;
    }

    public static float crossDimension(float purity) {
        return (float) (purity * (1.0 - NTConfig.satelliteCrossDimensionPurityLoss));
    }

    private static void feedCores(List<ResonanceNodeBlockEntity> inputs, List<SatelliteArrayBlockEntity> cores) {
        for (ResonanceNodeBlockEntity node : inputs) {
            ResourceKey<Level> dimension = node.dimension();
            for (int pass = 0; pass < 2; pass++) {
                boolean sameDimension = pass == 0;
                for (SatelliteArrayBlockEntity core : cores) {
                    if (core.dimension().equals(dimension) != sameDimension) {
                        continue;
                    }
                    int ap = Math.min(node.apOffer(), core.apRoom());
                    if (ap > 0) {
                        float purity = sameDimension ? node.getApPurity() : crossDimension(node.getApPurity());
                        core.storeAp(node.takeAp(ap), purity);
                    }
                    int fe = Math.min(node.feOffer(), core.feRoom());
                    if (fe > 0) {
                        core.giveFe(node.takeFe(fe));
                    }
                }
            }
        }
    }

    private static void moveAp(List<SatelliteArrayBlockEntity> cores, List<ResonanceNodeBlockEntity> outputs) {
        long supply = 0;
        float purity = 0F;
        Set<ResourceKey<Level>> supplied = new HashSet<>();
        for (SatelliteArrayBlockEntity core : cores) {
            int stored = core.getApStored();
            if (stored <= 0) {
                continue;
            }
            purity = LaserBlockEntity.mergedPurity(supply, purity, stored, core.getApPurity());
            supply += stored;
            supplied.add(core.dimension());
        }
        if (supply <= 0 || outputs.isEmpty()) {
            return;
        }
        float local = purity;
        float remote = crossDimension(purity);
        List<Receiver> receivers = new ArrayList<>();
        for (ResonanceNodeBlockEntity node : outputs) {
            float given = supplied.contains(node.dimension()) ? local : remote;
            receivers.add(receiver(node.getPriority(), node.apDemand(), amount -> {
                node.giveAp(amount, given);
                return amount;
            }));
        }
        long delivered = allocate(receivers, (long) Math.floor(supply * factor()));
        long cost = Math.min(supply, (long) Math.ceil(delivered / factor()));
        for (SatelliteArrayBlockEntity core : cores) {
            if (cost <= 0) {
                break;
            }
            cost -= core.takeAp((int) Math.min(Integer.MAX_VALUE, cost));
        }
    }

    private static void moveFe(List<SatelliteArrayBlockEntity> cores, List<ResonanceNodeBlockEntity> outputs, List<ServerPlayer> players,
                               Map<UUID, Integer> charmDelivered) {
        long supply = 0;
        for (SatelliteArrayBlockEntity core : cores) {
            supply += core.getEnergy().getAmountAsInt();
        }
        if (supply <= 0 || (outputs.isEmpty() && players.isEmpty())) {
            return;
        }
        List<Receiver> receivers = new ArrayList<>();
        for (ResonanceNodeBlockEntity node : outputs) {
            receivers.add(receiver(node.getPriority(), node.feDemand(), amount -> {
                node.giveFe(amount);
                return amount;
            }));
        }
        for (ServerPlayer player : players) {
            ResonanceCharmItem.Tuning tuning = ResonanceCharmItem.tuning(player);
            if (tuning == null || ResonanceCharmItem.network(player, tuning) == null) {
                continue;
            }
            int demand = ResonanceCharmItem.demand(player, tuning, NTConfig.charmTransferRate * ResonanceCharmItem.INTERVAL);
            receivers.add(receiver(tuning.priority(), demand, amount -> {
                int used = ResonanceCharmItem.deliver(player, tuning, amount);
                charmDelivered.merge(player.getUUID(), used, Integer::sum);
                return used;
            }));
        }
        long delivered = allocate(receivers, (long) Math.floor(supply * factor()));
        long cost = Math.min(supply, (long) Math.ceil(delivered / factor()));
        for (SatelliteArrayBlockEntity core : cores) {
            if (cost <= 0) {
                break;
            }
            cost -= core.takeFe((int) Math.min(Integer.MAX_VALUE, cost));
        }
    }

    private static Receiver receiver(int priority, int demand, IntUnaryOperator deliver) {
        return new Receiver() {
            @Override
            public int priority() {
                return priority;
            }

            @Override
            public int demand() {
                return demand;
            }

            @Override
            public int deliver(int amount) {
                return deliver.applyAsInt(amount);
            }
        };
    }

    private static long allocate(List<Receiver> receivers, long available) {
        List<Receiver> sorted = new ArrayList<>(receivers);
        sorted.sort(Comparator.comparingInt(Receiver::priority).reversed());
        long delivered = 0;
        int index = 0;
        while (index < sorted.size() && available > 0) {
            int priority = sorted.get(index).priority();
            List<Receiver> tier = new ArrayList<>();
            List<Integer> needs = new ArrayList<>();
            while (index < sorted.size() && sorted.get(index).priority() == priority) {
                Receiver receiver = sorted.get(index++);
                int need = receiver.demand();
                if (need > 0) {
                    tier.add(receiver);
                    needs.add(need);
                }
            }
            int[] shares = new int[tier.size()];
            long left = available;
            List<Integer> open = new ArrayList<>();
            for (int i = 0; i < tier.size(); i++) {
                open.add(i);
            }
            while (left > 0 && !open.isEmpty()) {
                long each = Math.max(1, left / open.size());
                Iterator<Integer> it = open.iterator();
                while (it.hasNext() && left > 0) {
                    int i = it.next();
                    int room = needs.get(i) - shares[i];
                    int give = (int) Math.min(Math.min(each, room), left);
                    shares[i] += give;
                    left -= give;
                    if (shares[i] >= needs.get(i)) {
                        it.remove();
                    }
                }
            }
            for (int i = 0; i < tier.size(); i++) {
                if (shares[i] > 0) {
                    int used = tier.get(i).deliver(shares[i]);
                    delivered += used;
                    available -= used;
                }
            }
        }
        return delivered;
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        ARRAYS.clear();
        NODES.clear();
        CACHE.clear();
        CHARMS.clear();
    }
}
