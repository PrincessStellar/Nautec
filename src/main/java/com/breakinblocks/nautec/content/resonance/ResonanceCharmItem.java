package com.breakinblocks.nautec.content.resonance;

import net.neoforged.neoforge.network.PacketDistributor;
import com.breakinblocks.nautec.network.OpenCharmScreenPayload;
import top.theillusivec4.curios.api.SlotResult;
import java.util.function.IntUnaryOperator;
import com.breakinblocks.nautec.capabilities.power.IPowerStorage;
import com.breakinblocks.nautec.api.items.ICurioItem;
import com.breakinblocks.nautec.content.augments.ResonanceAugment;
import com.breakinblocks.nautec.capabilities.NTCapabilities;
import com.breakinblocks.nautec.data.NTDataComponents;
import com.breakinblocks.nautec.registries.NTParticles;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import org.jetbrains.annotations.Nullable;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class ResonanceCharmItem extends Item implements ICurioItem {
    public static final int INTERVAL = 10;

    public ResonanceCharmItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return stack.has(NTDataComponents.RESONANCE_BINDING.get());
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (!(level.getBlockEntity(context.getClickedPos()) instanceof ResonanceTunable pylon)) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (!(context.getPlayer() instanceof ServerPlayer player)) {
            return InteractionResult.PASS;
        }
        ResonanceNetwork network = pylon.getNetwork();
        if (network == null) {
            player.displayClientMessage(Component.translatable("nautec.resonance_charm.no_network").withStyle(ChatFormatting.RED), true);
            return InteractionResult.FAIL;
        }
        if (!ResonanceNetworks.canUse(player, network)) {
            player.displayClientMessage(Component.translatable("nautec.resonance.error.no_access").withStyle(ChatFormatting.RED), true);
            return InteractionResult.FAIL;
        }
        context.getItemInHand().set(NTDataComponents.RESONANCE_BINDING.get(), new ResonanceBinding(network.id(), network.name()));
        player.displayClientMessage(Component.translatable("nautec.resonance_charm.bound", network.name()).withStyle(ChatFormatting.AQUA), true);
        level.playSound(null, context.getClickedPos(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.0F, 1.4F);
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack charm = player.getItemInHand(hand);
        if (player.isSecondaryUseActive() && charm.has(NTDataComponents.RESONANCE_BINDING.get())) {
            if (!level.isClientSide()) {
                charm.remove(NTDataComponents.RESONANCE_BINDING.get());
                player.displayClientMessage(Component.translatable("nautec.resonance_charm.unbound"), true);
            }
            return InteractionResultHolder.success(charm);
        }
        if (player instanceof ServerPlayer serverPlayer) {
            PacketDistributor.sendToPlayer(serverPlayer, new OpenCharmScreenPayload(hand.ordinal(), info(serverPlayer, charm)));
        }
        return InteractionResultHolder.success(charm);
    }

    @Override
    public void curioTick(ItemStack stack, SlotContext slotContext) {
        ResonanceBinding binding = stack.get(NTDataComponents.RESONANCE_BINDING.get());
        if (binding != null && slotContext.entity() instanceof ServerPlayer player) {
            SatelliteGrid.charm(player, binding.network());
        }
    }

    public record Tuning(ItemStack charm, UUID network, int priority) {
    }

    public static OpenCharmScreenPayload.Info info(ServerPlayer player, ItemStack charm) {
        return info(player, charm.get(NTDataComponents.RESONANCE_BINDING.get()), priority(charm));
    }

    public static OpenCharmScreenPayload.Info info(ServerPlayer player, @Nullable ResonanceBinding binding, int priority) {
        if (binding == null) {
            return new OpenCharmScreenPayload.Info(false, "", "", false, 0, 0, 0, priority);
        }
        ResonanceNetwork network = ResonanceNetworks.get(player.level().getServer()).get(binding.network());
        if (network == null) {
            return new OpenCharmScreenPayload.Info(true, binding.name(), "?", false, 0, 0, 0, priority);
        }
        if (!ResonanceNetworks.canUse(player, network)) {
            return new OpenCharmScreenPayload.Info(true, network.name(), network.ownerName(), false, 0, 0, 0, priority);
        }
        int uplinks = 0;
        long stored = 0;
        for (SatelliteArrayBlockEntity array : SatelliteGrid.members(network.id())) {
            if (!array.isRemoved() && array.transmitting()) {
                uplinks++;
                stored += array.getEnergy().getAmountAsInt();
            }
        }
        int nodes = 0;
        for (ResonanceNodeBlockEntity node : SatelliteGrid.nodes(network.id())) {
            if (!node.isRemoved() && node.isOutput()) {
                nodes++;
            }
        }
        return new OpenCharmScreenPayload.Info(true, network.name(), network.ownerName(), true, uplinks, nodes,
                (int) Math.min(Integer.MAX_VALUE, stored), priority);
    }

    public static int priority(ItemStack charm) {
        return charm.getOrDefault(NTDataComponents.RESONANCE_PRIORITY.get(), 0);
    }

    public static ItemStack equipped(ServerPlayer player) {
        return CuriosApi.getCuriosInventory(player)
                .flatMap(handler -> handler.findFirstCurio(stack -> stack.getItem() instanceof ResonanceCharmItem
                        && stack.has(NTDataComponents.RESONANCE_BINDING.get())))
                .map(SlotResult::stack)
                .orElse(ItemStack.EMPTY);
    }

    public static @Nullable Tuning tuning(ItemStack charm) {
        ResonanceBinding binding = charm.get(NTDataComponents.RESONANCE_BINDING.get());
        return binding == null ? null : new Tuning(charm, binding.network(), priority(charm));
    }

    public static @Nullable Tuning tuning(ServerPlayer player) {
        Tuning worn = tuning(equipped(player));
        if (worn != null) {
            return worn;
        }
        ResonanceAugment augment = ResonanceAugment.installed(player);
        if (augment != null && augment.getBinding() != null) {
            return new Tuning(ItemStack.EMPTY, augment.getBinding().network(), augment.getPriority());
        }
        return null;
    }

    public static @Nullable ResonanceNetwork network(ServerPlayer player, Tuning tuning) {
        ResonanceNetwork network = ResonanceNetworks.get(player.level().getServer()).get(tuning.network());
        return network != null && ResonanceNetworks.canUse(player, network) ? network : null;
    }

    public static int demand(ServerPlayer player, Tuning tuning, int cap) {
        int total = 0;
        for (ItemStack target : targets(player, tuning.charm())) {
            if (total >= cap) {
                break;
            }
            total += fill(target, cap - total, amount -> amount, true);
        }
        return total;
    }

    public static int deliver(ServerPlayer player, Tuning tuning, int amount) {
        return charge(player, tuning.charm(), amount, available -> available);
    }

    public static void charged(ServerPlayer player) {
        player.getInventory().setChanged();
        player.serverLevel().sendParticles(NTParticles.CRYSTAL_MOTE.get(), player.getX(), player.getY() + 1.0, player.getZ(), 3, 0.35, 0.5, 0.35, 0.0);
    }

    private static int charge(ServerPlayer player, ItemStack charm, int budget, IntUnaryOperator source) {
        int delivered = 0;
        for (ItemStack target : targets(player, charm)) {
            if (budget - delivered <= 0) {
                break;
            }
            delivered += fill(target, budget - delivered, source, false);
        }
        return delivered;
    }

    private static List<ItemStack> targets(ServerPlayer player, ItemStack charm) {
        List<ItemStack> targets = new ArrayList<>();
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (!stack.isEmpty() && stack != charm) {
                targets.add(stack);
            }
        }
        Optional<ICuriosItemHandler> curios = CuriosApi.getCuriosInventory(player);
        curios.ifPresent(handler -> {
            for (int slot = 0; slot < handler.getEquippedCurios().getSlots(); slot++) {
                ItemStack stack = handler.getEquippedCurios().getStackInSlot(slot);
                if (!stack.isEmpty() && stack != charm) {
                    targets.add(stack);
                }
            }
        });
        return targets;
    }

    private static int fill(ItemStack target, int limit, IntUnaryOperator source, boolean simulate) {
        IEnergyStorage energy = target.getCount() == 1 ? target.getCapability(Capabilities.EnergyStorage.ITEM) : null;
        if (energy != null) {
            int demand = energy.receiveEnergy(limit, true);
            if (simulate || demand <= 0) {
                return demand;
            }
            int drawn = source.applyAsInt(demand);
            if (drawn > 0) {
                energy.receiveEnergy(drawn, false);
            }
            return drawn;
        }
        IPowerStorage power = power(target);
        if (power != null) {
            int demand = power.tryFillPower(limit, true);
            if (simulate || demand <= 0) {
                return demand;
            }
            int drawn = source.applyAsInt(demand);
            if (drawn > 0) {
                power.tryFillPower(drawn, false);
            }
            return drawn;
        }
        return 0;
    }

    private static @Nullable IPowerStorage power(ItemStack stack) {
        return stack.getCount() == 1 ? stack.getCapability(NTCapabilities.PowerStorage.ITEM) : null;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        ResonanceBinding binding = stack.get(NTDataComponents.RESONANCE_BINDING.get());
        if (binding != null) {
            tooltip.add(Component.translatable("nautec.resonance_charm.tooltip.bound", binding.name()).withStyle(ChatFormatting.AQUA));
        } else {
            tooltip.add(Component.translatable("nautec.resonance_charm.tooltip.unbound").withStyle(ChatFormatting.GRAY));
        }
        tooltip.add(Component.translatable("nautec.resonance_charm.tooltip.priority", priority(stack)).withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("nautec.resonance_charm.tooltip.monocle").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("nautec.resonance_charm.tooltip.usage").withStyle(ChatFormatting.DARK_GRAY));
    }
}
