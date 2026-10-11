package com.breakinblocks.nautec.gametest.suite;

import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.api.augments.AugmentSlot;
import com.breakinblocks.nautec.capabilities.NTCapabilities;
import com.breakinblocks.nautec.capabilities.power.IPowerStorage;
import com.breakinblocks.nautec.content.augments.ResonanceAugment;
import com.breakinblocks.nautec.content.resonance.ResonanceBinding;
import com.breakinblocks.nautec.content.resonance.ResonanceCharmItem;
import com.breakinblocks.nautec.content.resonance.ResonanceNetwork;
import com.breakinblocks.nautec.content.resonance.ResonanceNetworks;
import com.breakinblocks.nautec.data.NTDataComponents;
import com.breakinblocks.nautec.registries.NTAugmentSlots;
import com.breakinblocks.nautec.registries.NTAugments;
import com.breakinblocks.nautec.registries.NTItems;
import com.breakinblocks.nautec.utils.AugmentHelper;
import com.mojang.authlib.GameProfile;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.UUID;

public final class ResonanceCharmTests {
    private static final int BUDGET = NTConfig.charmTransferRate * ResonanceCharmItem.INTERVAL;

    private ResonanceCharmTests() {
    }

    private static ServerPlayer player(NTGameTestHelper helper, String name) {
        return new ServerPlayer(helper.getLevel().getServer(), helper.getLevel(), new GameProfile(UUID.randomUUID(), name),
                ClientInformation.createDefault());
    }

    private static ItemStack charm(ResonanceNetwork network) {
        ItemStack charm = new ItemStack(NTItems.RESONANCE_CHARM.get());
        charm.set(NTDataComponents.RESONANCE_BINDING.get(), new ResonanceBinding(network.id(), network.name()));
        return charm;
    }

    public static void register(NTTestRegistrar r) {
        r.add("charm/charges_ap_items_from_the_network", 40, helper -> {
            ServerPlayer owner = player(helper, "CharmOwner");
            ResonanceNetworks networks = ResonanceNetworks.get(helper.getLevel().getServer());
            ResonanceNetwork network = networks.create(owner, "Charm grid").network();
            ItemStack battery = new ItemStack(NTItems.PRISMATIC_BATTERY.get());
            owner.getInventory().setItem(0, battery);
            ResonanceCharmItem.Tuning tuning = ResonanceCharmItem.tuning(charm(network));

            helper.runAfterDelay(2, () -> {
                helper.assertTrue(ResonanceCharmItem.network(owner, tuning) != null, "the owner's charm reaches its network");
                int demand = ResonanceCharmItem.demand(owner, tuning, BUDGET);
                int delivered = ResonanceCharmItem.deliver(owner, tuning, demand);
                IPowerStorage power = owner.getInventory().getItem(0).getCapability(NTCapabilities.PowerStorage.ITEM);
                helper.assertTrue(delivered > 0, "the charm delivered power");
                helper.assertValueEqual(power.getPowerStored(), delivered, "the battery holds what was delivered");
                networks.delete(owner, network.id());
                helper.succeed();
            });
        });

        r.add("charm/augment_takes_the_charm_binding_and_charges", 40, helper -> {
            ServerPlayer owner = player(helper, "Implant");
            ResonanceNetworks networks = ResonanceNetworks.get(helper.getLevel().getServer());
            ResonanceNetwork network = networks.create(owner, "Implant grid").network();
            owner.getInventory().setItem(0, new ItemStack(NTItems.PRISMATIC_BATTERY.get()));
            ItemStack charm = charm(network);
            charm.set(NTDataComponents.RESONANCE_PRIORITY.get(), 3);
            AugmentSlot slot = NTAugmentSlots.HEAD.get();
            ResonanceAugment augment = (ResonanceAugment) NTAugments.RESONANCE.get().create(slot);
            augment.setPlayer(owner);
            augment.readParts(List.of(charm));
            AugmentHelper.setAugment(owner, slot, augment);

            helper.runAfterDelay(2, () -> {
                helper.assertValueEqual(augment.getBinding().network(), network.id(), "the augment took the charm's network");
                helper.assertValueEqual(augment.getPriority(), 3, "the augment took the charm's priority");
                ResonanceAugment restored = (ResonanceAugment) NTAugments.RESONANCE.get().create(slot);
                restored.deserializeNBT(helper.getLevel().registryAccess(), augment.serializeNBT(helper.getLevel().registryAccess()));
                helper.assertValueEqual(restored.getBinding(), augment.getBinding(), "the binding survives a save");
                helper.assertValueEqual(restored.getPriority(), 3, "the priority survives a save");
                ResonanceCharmItem.Tuning tuning = ResonanceCharmItem.tuning(owner);
                helper.assertTrue(tuning != null && tuning.network().equals(network.id()), "the player is tuned through the augment");
                helper.assertTrue(tuning.charm().isEmpty(), "no charm item is involved");
                int delivered = ResonanceCharmItem.deliver(owner, tuning, ResonanceCharmItem.demand(owner, tuning, BUDGET));
                IPowerStorage power = owner.getInventory().getItem(0).getCapability(NTCapabilities.PowerStorage.ITEM);
                helper.assertTrue(delivered > 0, "the augment delivered power");
                helper.assertValueEqual(power.getPowerStored(), delivered, "the battery holds what was delivered");
                networks.delete(owner, network.id());
                helper.succeed();
            });
        });

        r.add("charm/does_nothing_without_access", 40, helper -> {
            ServerPlayer owner = player(helper, "Owner");
            ServerPlayer thief = player(helper, "Thief");
            ResonanceNetworks networks = ResonanceNetworks.get(helper.getLevel().getServer());
            ResonanceNetwork network = networks.create(owner, "Locked grid").network();
            thief.getInventory().setItem(0, new ItemStack(NTItems.PRISMATIC_BATTERY.get()));

            helper.runAfterDelay(2, () -> {
                helper.assertTrue(ResonanceCharmItem.network(thief, ResonanceCharmItem.tuning(charm(network))) == null,
                        "a charm bound by someone without access reaches no network");
                helper.assertTrue(ResonanceCharmItem.tuning(new ItemStack(NTItems.RESONANCE_CHARM.get())) == null, "an unbound charm tunes to nothing");
                networks.delete(owner, network.id());
                helper.succeed();
            });
        });
    }
}
