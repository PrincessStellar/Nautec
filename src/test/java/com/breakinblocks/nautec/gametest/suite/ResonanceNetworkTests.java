package com.breakinblocks.nautec.gametest.suite;

import com.breakinblocks.nautec.content.resonance.ResonanceNetwork;
import com.breakinblocks.nautec.content.resonance.ResonanceNetworks;
import com.mojang.authlib.GameProfile;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerPlayer;

import java.util.Map;
import java.util.UUID;

public final class ResonanceNetworkTests {
    private ResonanceNetworkTests() {
    }

    private static ServerPlayer player(GameTestHelper helper, String name) {
        return new ServerPlayer(helper.getLevel().getServer(), helper.getLevel(), new GameProfile(UUID.randomUUID(), name),
                ClientInformation.createDefault());
    }

    public static void register(NTTestRegistrar r) {
        r.add("resonance/access_follows_owner_and_trust", 20, helper -> {
            UUID owner = UUID.randomUUID();
            UUID friend = UUID.randomUUID();
            UUID stranger = UUID.randomUUID();
            ResonanceNetwork network = new ResonanceNetwork(UUID.randomUUID(), "Base", owner, "Owner", Map.of(friend, "Friend"), true);
            helper.assertTrue(network.canAccess(owner), "the owner has access");
            helper.assertTrue(network.canAccess(friend), "a trusted player has access");
            helper.assertFalse(network.canAccess(stranger), "a stranger has no access, even with team access on and no shared team");
            helper.succeed();
        });

        r.add("resonance/networks_are_written_to_disk", 20, helper -> {
            ServerPlayer owner = player(helper, "Saver");
            ResonanceNetworks networks = ResonanceNetworks.get(helper.getLevel().getServer());
            ResonanceNetwork network = networks.create(owner, "Saved grid").network();
            networks.trust(owner, network.id(), UUID.randomUUID(), "Friend");
            helper.assertTrue(networks.isDirty(), "creating a network marks the data for saving");
            helper.getLevel().getServer().overworld().getDataStorage().saveAndJoin();
            helper.assertFalse(networks.isDirty(), "saving writes it out");

            Tag encoded = ResonanceNetworks.CODEC.encodeStart(NbtOps.INSTANCE, networks).getOrThrow();
            ResonanceNetworks decoded = ResonanceNetworks.CODEC.parse(NbtOps.INSTANCE, encoded).getOrThrow();
            ResonanceNetwork loaded = decoded.get(network.id());
            helper.assertTrue(loaded != null, "the network comes back after loading");
            helper.assertValueEqual(loaded.name(), "Saved grid", "name");
            helper.assertValueEqual(loaded.owner(), owner.getUUID(), "owner");
            helper.assertValueEqual(loaded.trusted().size(), 1, "trusted players");
            networks.delete(owner, network.id());
            helper.succeed();
        });

        r.add("resonance/strangers_cannot_use_a_network", 20, helper -> {
            ServerPlayer owner = player(helper, "NetOwner");
            ServerPlayer stranger = player(helper, "Stranger");
            ResonanceNetworks networks = ResonanceNetworks.get(helper.getLevel().getServer());
            ResonanceNetwork network = networks.create(owner, "Private").network();
            helper.assertTrue(ResonanceNetworks.canUse(owner, network), "the owner can use it");
            helper.assertFalse(ResonanceNetworks.canUse(stranger, network), "a stranger cannot");
            helper.assertFalse(networks.trust(stranger, network.id(), stranger.getUUID(), "Sneak").success(), "a stranger cannot trust themselves");
            helper.assertTrue(networks.trust(owner, network.id(), stranger.getUUID(), "Friend").success(), "the owner can trust them");
            helper.assertTrue(ResonanceNetworks.canUse(stranger, network), "now they can use it");
            helper.assertFalse(networks.delete(stranger, network.id()).success(), "a trusted player still cannot delete it");
            networks.delete(owner, network.id());
            helper.succeed();
        });
    }
}
