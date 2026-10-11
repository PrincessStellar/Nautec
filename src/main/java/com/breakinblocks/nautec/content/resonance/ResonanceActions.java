package com.breakinblocks.nautec.content.resonance;

import java.util.Optional;
import org.jetbrains.annotations.Nullable;
import com.breakinblocks.nautec.utils.valueio.ValueOutput;
import com.breakinblocks.nautec.utils.valueio.ValueInput;
import net.minecraft.core.UUIDUtil;
import com.breakinblocks.nautec.api.menu.NTAbstractContainerMenu;
import com.breakinblocks.nautec.network.ResonanceActionPayload;
import net.minecraft.core.BlockPos;
import com.breakinblocks.nautec.registries.NTCriteriaTriggers;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;

public final class ResonanceActions {
    private static final double REACH = 8.0;

    private ResonanceActions() {
    }

    public static void apply(ServerPlayer player, ResonanceActionPayload payload) {
        BlockPos pos = payload.pos();
        if (player.distanceToSqr(pos.getCenter()) > REACH * REACH || !player.level().isLoaded(pos)) {
            return;
        }
        if (!(player.containerMenu instanceof NTAbstractContainerMenu<?> menu)
                || !menu.getBlockEntity().getBlockPos().equals(pos)
                || !menu.stillValid(player)) {
            return;
        }
        if (!(player.level().getBlockEntity(pos) instanceof ResonanceTunable pylon)) {
            return;
        }
        ResonanceNetworks networks = ResonanceNetworks.get(player.level().getServer());
        ResonanceNetwork current = pylon.getNetwork();
        if (current != null && !ResonanceNetworks.canUse(player, current)) {
            fail(player, "nautec.resonance.error.no_access");
            return;
        }

        String error = switch (payload.action()) {
            case ResonanceActionPayload.SELECT -> select(player, pylon, networks, payload.network().orElse(null));
            case ResonanceActionPayload.CREATE -> {
                ResonanceNetworks.Result result = networks.create(player, payload.text());
                if (result.success()) {
                    pylon.setNetwork(result.network());
                    NTCriteriaTriggers.RESONANCE_NETWORK.get().trigger(player);
                }
                yield result.error();
            }
            case ResonanceActionPayload.PRIORITY -> {
                if (pylon instanceof ResonanceNodeBlockEntity node) {
                    node.setPriority(parse(payload.text(), node.getPriority()));
                }
                yield null;
            }
            case ResonanceActionPayload.LIMIT -> {
                if (pylon instanceof ResonanceNodeBlockEntity node) {
                    node.setLimit(parse(payload.text(), node.getLimit()));
                }
                yield null;
            }
            case ResonanceActionPayload.MODE -> {
                if (pylon instanceof ResonanceNodeBlockEntity node) {
                    node.setOutput(!node.isOutput());
                }
                yield null;
            }
            case ResonanceActionPayload.CHUNK -> {
                if (pylon instanceof ChunkLoadable loadable) {
                    loadable.setChunkLoading(!loadable.isChunkLoading());
                }
                yield null;
            }
            case ResonanceActionPayload.TRUST -> current == null ? "nautec.resonance.error.no_network" : trust(player, networks, current, payload.text());
            case ResonanceActionPayload.UNTRUST -> current == null ? "nautec.resonance.error.no_network"
                    : untrust(player, networks, current, payload.text());
            case ResonanceActionPayload.TEAM -> current == null ? "nautec.resonance.error.no_network"
                    : networks.setTeamAccess(player, current.id(), !current.teamAccess()).error();
            case ResonanceActionPayload.RENAME -> current == null ? "nautec.resonance.error.no_network"
                    : networks.rename(player, current.id(), payload.text()).error();
            case ResonanceActionPayload.DELETE -> {
                if (current == null) {
                    yield "nautec.resonance.error.no_network";
                }
                ResonanceNetworks.Result result = networks.delete(player, current.id());
                if (result.success()) {
                    pylon.setNetwork(null);
                }
                yield result.error();
            }
            default -> null;
        };
        if (error != null) {
            fail(player, error);
        }
        ResonanceSync.send(player, pylon);
    }

    private static int parse(String text, int fallback) {
        try {
            return Integer.parseInt(text.trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    public static void copyNetwork(@Nullable UUID network, ValueOutput out) {
        if (network != null) {
            out.store("network", UUIDUtil.CODEC, network);
        }
    }

    public static boolean pasteNetwork(ServerPlayer player, ResonanceTunable target, ValueInput in) {
        Optional<UUID> id = in.read("network", UUIDUtil.CODEC);
        if (id.isEmpty()) {
            return false;
        }
        ResonanceNetwork network = ResonanceNetworks.get(player.level().getServer()).get(id.get());
        if (network == null || !ResonanceNetworks.canUse(player, network)) {
            player.sendSystemMessage(Component.translatable("nautec.resonance.error.no_access").withStyle(ChatFormatting.RED));
            return false;
        }
        target.setNetwork(network);
        return true;
    }

    private static String select(ServerPlayer player, ResonanceTunable pylon, ResonanceNetworks networks, UUID id) {
        if (id == null) {
            pylon.setNetwork(null);
            return null;
        }
        ResonanceNetwork network = networks.get(id);
        if (network == null || !ResonanceNetworks.canUse(player, network)) {
            return "nautec.resonance.error.no_access";
        }
        pylon.setNetwork(network);
        return null;
    }

    private static String trust(ServerPlayer player, ResonanceNetworks networks, ResonanceNetwork network, String name) {
        ServerPlayer target = player.level().getServer().getPlayerList().getPlayerByName(name.strip());
        if (target == null) {
            return "nautec.resonance.error.player_offline";
        }
        return networks.trust(player, network.id(), target.getUUID(), target.getGameProfile().getName()).error();
    }

    private static String untrust(ServerPlayer player, ResonanceNetworks networks, ResonanceNetwork network, String id) {
        try {
            return networks.untrust(player, network.id(), UUID.fromString(id)).error();
        } catch (IllegalArgumentException e) {
            return "nautec.resonance.error.player_offline";
        }
    }

    private static void fail(ServerPlayer player, String key) {
        player.displayClientMessage(Component.translatable(key).withStyle(ChatFormatting.RED), true);
    }
}
