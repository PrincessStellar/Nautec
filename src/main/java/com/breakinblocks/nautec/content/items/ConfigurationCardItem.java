package com.breakinblocks.nautec.content.items;

import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.api.blockentities.ContainerBlockEntity;
import com.breakinblocks.nautec.api.sides.SideConfig;
import com.breakinblocks.nautec.capabilities.item.ItemStackHandler;
import com.breakinblocks.nautec.data.NTDataComponents;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class ConfigurationCardItem extends Item {
    public static final String SIDE_CONFIG = "side_config";
    public static final String SETTINGS = "settings";
    public static final String UPGRADES = "upgrades";

    public record CardSlot(int slot, ItemStack stack) {
        public static final Codec<CardSlot> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.INT.fieldOf("slot").forGetter(CardSlot::slot),
                ItemStack.CODEC.fieldOf("stack").forGetter(CardSlot::stack)
        ).apply(instance, CardSlot::new));
    }

    public ConfigurationCardItem(Properties properties) {
        super(properties);
    }

    public static @Nullable MachineSettings copy(ContainerBlockEntity machine, ServerLevel level) {
        try (ProblemReporter.ScopedCollector reporter = new ProblemReporter.ScopedCollector(Nautec.LOGGER)) {
            TagValueOutput out = TagValueOutput.createWithContext(reporter, level.registryAccess());
            if (machine.hasSideConfig()) {
                machine.getSideConfig().save(out.child(SIDE_CONFIG));
            }
            machine.saveSettings(out.child(SETTINGS));
            List<CardSlot> upgrades = upgradesOf(machine);
            if (!upgrades.isEmpty()) {
                out.store(UPGRADES, CardSlot.CODEC.listOf(), upgrades);
            }
            CompoundTag data = out.buildResult();
            if (data.getCompoundOrEmpty(SETTINGS).isEmpty()) {
                data.remove(SETTINGS);
            }
            if (data.isEmpty()) {
                return null;
            }
            return new MachineSettings(BuiltInRegistries.BLOCK.getKey(machine.getBlockState().getBlock()),
                    BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(machine.getType()), data);
        }
    }

    private static List<CardSlot> upgradesOf(ContainerBlockEntity machine) {
        List<CardSlot> upgrades = new ArrayList<>();
        ItemStackHandler handler = machine.getItemStackHandler();
        if (handler == null) {
            return upgrades;
        }
        for (int slot : machine.cardSlots()) {
            ItemStack stack = handler.getStackInSlot(slot);
            if (!stack.isEmpty()) {
                upgrades.add(new CardSlot(slot, stack.copy()));
            }
        }
        return upgrades;
    }

    public static List<CardSlot> upgrades(MachineSettings settings, HolderLookup.Provider registries) {
        if (!settings.data().contains(UPGRADES)) {
            return List.of();
        }
        return TagValueInput.create(ProblemReporter.DISCARDING, registries, settings.data()).read(UPGRADES, CardSlot.CODEC.listOf()).orElse(List.of());
    }

    private static boolean sameType(MachineSettings settings, ContainerBlockEntity machine) {
        return settings.blockEntityType().equals(BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(machine.getType()));
    }

    private static List<CardSlot> applicableUpgrades(MachineSettings settings, ContainerBlockEntity machine, Player player) {
        ItemStackHandler handler = machine.getItemStackHandler();
        if (handler == null || !sameType(settings, machine)) {
            return List.of();
        }
        int[] allowed = machine.cardSlots();
        List<CardSlot> applicable = new ArrayList<>();
        for (CardSlot entry : upgrades(settings, player.registryAccess())) {
            for (int slot : allowed) {
                if (slot == entry.slot() && slot < handler.getSlots()) {
                    applicable.add(entry);
                    break;
                }
            }
        }
        return applicable;
    }

    private static int needed(CardSlot entry, ItemStack current) {
        if (ItemStack.isSameItemSameComponents(current, entry.stack())) {
            return Math.max(0, entry.stack().getCount() - current.getCount());
        }
        return entry.stack().getCount();
    }

    public static List<ItemStack> missingUpgrades(MachineSettings settings, ContainerBlockEntity machine, Player player) {
        List<ItemStack> missing = new ArrayList<>();
        if (player.getAbilities().instabuild) {
            return missing;
        }
        List<ItemStack> wanted = new ArrayList<>();
        ItemStackHandler handler = machine.getItemStackHandler();
        for (CardSlot entry : applicableUpgrades(settings, machine, player)) {
            int need = needed(entry, handler.getStackInSlot(entry.slot()));
            if (need <= 0) {
                continue;
            }
            boolean merged = false;
            for (ItemStack stack : wanted) {
                if (ItemStack.isSameItemSameComponents(stack, entry.stack())) {
                    stack.grow(need);
                    merged = true;
                    break;
                }
            }
            if (!merged) {
                wanted.add(entry.stack().copyWithCount(need));
            }
        }
        for (ItemStack stack : wanted) {
            int have = ContainerHelper.clearOrCountMatchingItems(player.getInventory(),
                    candidate -> ItemStack.isSameItemSameComponents(candidate, stack), 0, true);
            if (have < stack.getCount()) {
                missing.add(stack.copyWithCount(stack.getCount() - have));
            }
        }
        return missing;
    }

    private static boolean migrateUpgrades(MachineSettings settings, ContainerBlockEntity machine, Player player) {
        List<CardSlot> applicable = applicableUpgrades(settings, machine, player);
        if (applicable.isEmpty()) {
            return false;
        }
        ItemStackHandler handler = machine.getItemStackHandler();
        Inventory inventory = player.getInventory();
        boolean free = player.getAbilities().instabuild;
        for (CardSlot entry : applicable) {
            ItemStack current = handler.getStackInSlot(entry.slot()).copy();
            ItemStack wanted = entry.stack();
            int need = needed(entry, current);
            if (ItemStack.isSameItemSameComponents(current, wanted)) {
                if (current.getCount() > wanted.getCount()) {
                    give(player, current.copyWithCount(current.getCount() - wanted.getCount()));
                }
            } else if (!current.isEmpty()) {
                give(player, current);
            }
            if (need > 0 && !free) {
                ContainerHelper.clearOrCountMatchingItems(inventory, candidate -> ItemStack.isSameItemSameComponents(candidate, wanted), need, false);
            }
            handler.setStackInSlot(entry.slot(), wanted.copy());
        }
        inventory.setChanged();
        return true;
    }

    private static void give(Player player, ItemStack stack) {
        if (!player.getInventory().add(stack) && !stack.isEmpty()) {
            player.drop(stack, false);
        }
    }

    public static boolean paste(MachineSettings settings, ContainerBlockEntity machine, ServerPlayer player) {
        if (!missingUpgrades(settings, machine, player).isEmpty()) {
            return false;
        }
        boolean applied = migrateUpgrades(settings, machine, player);
        try (ProblemReporter.ScopedCollector reporter = new ProblemReporter.ScopedCollector(Nautec.LOGGER)) {
            ValueInput in = TagValueInput.create(reporter, player.level().registryAccess(), settings.data());
            Optional<ValueInput> sides = in.child(SIDE_CONFIG);
            if (sides.isPresent() && machine.hasSideConfig()) {
                SideConfig config = new SideConfig();
                config.load(sides.get());
                machine.getSideConfig().copyFrom(config);
                machine.sideConfigChanged();
                applied = true;
            }
            Optional<ValueInput> own = in.child(SETTINGS);
            if (own.isPresent() && sameType(settings, machine) && machine.loadSettings(own.get(), player)) {
                applied = true;
            }
        }
        return applied;
    }

    private static Component list(List<ItemStack> stacks) {
        MutableComponent joined = Component.empty();
        for (int i = 0; i < stacks.size(); i++) {
            if (i > 0) {
                joined.append(", ");
            }
            joined.append(Component.translatable("nautec.configuration_card.stack", stacks.get(i).getCount(), stacks.get(i).getHoverName()));
        }
        return joined;
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack card, UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        if (player == null || !(level.getBlockEntity(pos) instanceof ContainerBlockEntity machine)) {
            return InteractionResult.PASS;
        }
        if (!(level instanceof ServerLevel serverLevel) || !(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.SUCCESS;
        }
        Component name = machine.getBlockState().getBlock().getName();
        if (player.isSecondaryUseActive()) {
            MachineSettings settings = copy(machine, serverLevel);
            if (settings == null) {
                player.sendOverlayMessage(Component.translatable("nautec.configuration_card.nothing_to_copy", name).withStyle(ChatFormatting.RED));
                return InteractionResult.FAIL;
            }
            card.set(NTDataComponents.MACHINE_SETTINGS.get(), settings);
            player.sendOverlayMessage(Component.translatable("nautec.configuration_card.copied", name).withStyle(ChatFormatting.AQUA));
            level.playSound(null, pos, SoundEvents.BOOK_PAGE_TURN, SoundSource.PLAYERS, 0.8F, 1.2F);
            return InteractionResult.SUCCESS;
        }
        MachineSettings settings = card.get(NTDataComponents.MACHINE_SETTINGS.get());
        if (settings == null) {
            player.sendOverlayMessage(Component.translatable("nautec.configuration_card.empty").withStyle(ChatFormatting.RED));
            return InteractionResult.FAIL;
        }
        if (!level.mayInteract(player, pos)) {
            return InteractionResult.FAIL;
        }
        List<ItemStack> missing = missingUpgrades(settings, machine, player);
        if (!missing.isEmpty()) {
            player.sendOverlayMessage(Component.translatable("nautec.configuration_card.missing", list(missing)).withStyle(ChatFormatting.RED));
            return InteractionResult.FAIL;
        }
        if (!paste(settings, machine, serverPlayer)) {
            player.sendOverlayMessage(Component.translatable("nautec.configuration_card.nothing_fits", name).withStyle(ChatFormatting.RED));
            return InteractionResult.FAIL;
        }
        player.sendOverlayMessage(Component.translatable("nautec.configuration_card.pasted", name).withStyle(ChatFormatting.AQUA));
        level.playSound(null, pos, SoundEvents.UI_LOOM_TAKE_RESULT, SoundSource.PLAYERS, 0.8F, 1.4F);
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack card = player.getItemInHand(hand);
        if (!player.isSecondaryUseActive() || !card.has(NTDataComponents.MACHINE_SETTINGS.get())) {
            return InteractionResult.PASS;
        }
        card.remove(NTDataComponents.MACHINE_SETTINGS.get());
        if (!level.isClientSide()) {
            player.sendOverlayMessage(Component.translatable("nautec.configuration_card.cleared").withStyle(ChatFormatting.GRAY));
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return stack.has(NTDataComponents.MACHINE_SETTINGS.get());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        MachineSettings settings = stack.get(NTDataComponents.MACHINE_SETTINGS.get());
        if (settings == null) {
            tooltip.accept(Component.translatable("nautec.configuration_card.tooltip.empty").withStyle(ChatFormatting.GRAY));
        } else {
            Component source = BuiltInRegistries.BLOCK.getValue(settings.block()).getName();
            tooltip.accept(Component.translatable("nautec.configuration_card.tooltip.source", source).withStyle(ChatFormatting.AQUA));
            if (settings.data().contains(SIDE_CONFIG)) {
                tooltip.accept(Component.translatable("nautec.configuration_card.tooltip.sides").withStyle(ChatFormatting.GRAY));
            }
            if (settings.data().contains(SETTINGS)) {
                tooltip.accept(Component.translatable("nautec.configuration_card.tooltip.settings", source).withStyle(ChatFormatting.GRAY));
            }
            HolderLookup.Provider registries = context.registries();
            if (registries != null && settings.data().contains(UPGRADES)) {
                List<ItemStack> upgrades = new ArrayList<>();
                for (CardSlot entry : upgrades(settings, registries)) {
                    upgrades.add(entry.stack());
                }
                if (!upgrades.isEmpty()) {
                    tooltip.accept(Component.translatable("nautec.configuration_card.tooltip.upgrades", list(upgrades)).withStyle(ChatFormatting.GRAY));
                }
            }
            tooltip.accept(Component.translatable("nautec.configuration_card.tooltip.clear").withStyle(ChatFormatting.DARK_GRAY));
        }
        tooltip.accept(Component.translatable("nautec.configuration_card.tooltip.use").withStyle(ChatFormatting.DARK_GRAY));
    }
}
