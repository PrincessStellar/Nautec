package com.breakinblocks.nautec.content.bubble;

import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.api.blockentities.LaserBlockEntity;
import com.breakinblocks.nautec.api.sides.SlotRoles;
import com.breakinblocks.nautec.capabilities.IOActions;
import com.breakinblocks.nautec.content.menus.BubbleAnchorMenu;
import com.breakinblocks.nautec.registries.NTBlockEntityTypes;
import com.breakinblocks.nautec.registries.NTBlocks;
import com.breakinblocks.nautec.utils.DataSlots;
import it.unimi.dsi.fastutil.Pair;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BubbleColumnBlock;
import net.minecraft.world.level.block.KelpBlock;
import net.minecraft.world.level.block.KelpPlantBlock;
import net.minecraft.world.level.block.SeagrassBlock;
import net.minecraft.world.level.block.TallSeagrassBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import com.breakinblocks.nautec.utils.valueio.ValueInput;
import com.breakinblocks.nautec.utils.valueio.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

public class BubbleAnchorBlockEntity extends LaserBlockEntity implements MenuProvider {
    public static final int STATUS_OFF = 0;
    public static final int STATUS_NO_FUEL = 1;
    public static final int STATUS_FUEL = 2;
    public static final int STATUS_LASER = 3;

    public static final int DATA_ENABLED = 0;
    public static final int DATA_FILL = 1;
    public static final int DATA_ABOVE = 2;
    public static final int DATA_BURN = 3;
    public static final int DATA_TOTAL = 5;
    public static final int DATA_STATUS = 7;
    public static final int DATA_RADIUS = 8;
    public static final int DATA_COUNT = 9;

    private static final int WARNING_TICKS = 200;
    private static final int LASER_GRACE = 40;
    private static final SlotRoles ITEM_ROLES = SlotRoles.of(new int[]{0}, new int[0]);
    private static final Map<Integer, int[][]> OFFSETS = new HashMap<>();
    private static final Set<BubbleAnchorBlockEntity> ACTIVE = Collections.newSetFromMap(new WeakHashMap<>());
    private static final List<PendingRelease> PENDING_RELEASES = new ArrayList<>();
    private static final int MAINTENANCE_BUDGET = 128;

    private boolean enabled = true;
    private boolean fillWater = true;
    private boolean above;
    private int burn;
    private int burnTotal;
    private int status = STATUS_OFF;
    private @Nullable BlockPos heldCenter;
    private int heldRadius;
    private int cursor;
    private boolean releasing;
    private int swept;
    private int laserHold;
    private int laserRadius;

    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case DATA_ENABLED -> enabled ? 1 : 0;
                case DATA_FILL -> fillWater ? 1 : 0;
                case DATA_ABOVE -> above ? 1 : 0;
                case DATA_BURN -> DataSlots.low(burn);
                case DATA_BURN + 1 -> DataSlots.high(burn);
                case DATA_TOTAL -> DataSlots.low(burnTotal);
                case DATA_TOTAL + 1 -> DataSlots.high(burnTotal);
                case DATA_STATUS -> status;
                case DATA_RADIUS -> currentRadius();
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

    public BubbleAnchorBlockEntity(BlockPos pos, BlockState state) {
        super(NTBlockEntityTypes.BUBBLE_ANCHOR.get(), pos, state);
        addItemHandler(1, (slot, stack) -> fuelTicks(stack) > 0);
    }

    public static int fuelTicks(ItemStack stack) {
        Item item = stack.getItem();
        int base;
        if (item == Items.DRIED_KELP_BLOCK) {
            base = 2400;
        } else if (item == Items.SEA_PICKLE) {
            base = 600;
        } else if (item == Items.DRIED_KELP) {
            base = 240;
        } else if (item == Items.KELP) {
            base = 120;
        } else {
            return 0;
        }
        return (int) Math.max(1, Math.round(base * NTConfig.bubbleAnchorFuelMultiplier));
    }

    public ContainerData getData() {
        return data;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean fillsWater() {
        return fillWater;
    }

    public boolean isAbove() {
        return above;
    }

    public int getBurn() {
        return burn;
    }

    public int getStatus() {
        return status;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        setChanged();
    }

    public void setFillWater(boolean fillWater) {
        this.fillWater = fillWater;
        setChanged();
    }

    public void setAbove(boolean above) {
        this.above = above;
        setChanged();
    }

    public boolean laserPowered() {
        return getPower() >= NTConfig.bubbleAnchorLaserPower || laserHold > 0;
    }

    public int currentRadius() {
        if (!laserPowered()) {
            return NTConfig.bubbleAnchorRadius;
        }
        return getPower() >= NTConfig.bubbleAnchorLaserPower ? purityRadius() : Math.max(NTConfig.bubbleAnchorRadius, laserRadius);
    }

    private int purityRadius() {
        float purity = getPurity();
        int radius = NTConfig.bubbleAnchorRadius;
        if (purity >= 1.5F) {
            radius += 1;
        }
        if (purity >= 2.0F) {
            radius += 2;
        }
        if (purity >= 2.5F) {
            radius += 2;
        }
        return Math.min(radius, NTConfig.bubbleAnchorMaxRadius);
    }

    public BlockPos center(int radius) {
        return above ? worldPosition.above(radius + 1) : worldPosition;
    }

    public boolean covers(BlockPos pos) {
        if (heldCenter == null || releasing) {
            return false;
        }
        return Math.abs(pos.getX() - heldCenter.getX()) <= heldRadius && Math.abs(pos.getY() - heldCenter.getY()) <= heldRadius
                && Math.abs(pos.getZ() - heldCenter.getZ()) <= heldRadius;
    }

    private static int[][] offsets(int radius) {
        return OFFSETS.computeIfAbsent(radius, r -> {
            List<int[]> list = new ArrayList<>();
            for (int dx = -r; dx <= r; dx++) {
                for (int dy = -r; dy <= r; dy++) {
                    for (int dz = -r; dz <= r; dz++) {
                        list.add(new int[]{dx, dy, dz});
                    }
                }
            }
            list.sort(Comparator.comparingInt(o -> Math.max(Math.abs(o[0]), Math.max(Math.abs(o[1]), Math.abs(o[2])))));
            return list.toArray(new int[0][]);
        });
    }

    @Override
    public Set<Direction> getLaserInputs() {
        return EnumSet.allOf(Direction.class);
    }

    @Override
    public Set<Direction> getLaserOutputs() {
        return Set.of();
    }

    @Override
    public SlotRoles itemRoles() {
        return ITEM_ROLES;
    }

    @Override
    public void commonTick() {
        super.commonTick();
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        if (getPower() >= NTConfig.bubbleAnchorLaserPower) {
            laserHold = LASER_GRACE;
            laserRadius = purityRadius();
        } else if (laserHold > 0) {
            laserHold--;
        }
        boolean laser = laserPowered();
        boolean active = false;
        if (enabled) {
            if (laser) {
                active = true;
            } else {
                if (burn <= 0) {
                    ItemStack fuel = getItemStackHandler().getStackInSlot(0);
                    int ticks = fuelTicks(fuel);
                    if (ticks > 0) {
                        getItemStackHandler().extractItem(0, 1, false);
                        burn = ticks;
                        burnTotal = ticks;
                    }
                }
                if (burn > 0) {
                    burn--;
                    active = true;
                    if (burn == WARNING_TICKS && fuelTicks(getItemStackHandler().getStackInSlot(0)) <= 0) {
                        warn(serverLevel);
                    }
                }
            }
        }
        int newStatus = !enabled ? STATUS_OFF : laser ? STATUS_LASER : active ? STATUS_FUEL : STATUS_NO_FUEL;
        if (newStatus != status) {
            status = newStatus;
            setChanged();
        }
        boolean lit = active || heldCenter != null;
        if (getBlockState().getValue(BubbleAnchorBlock.ACTIVE) != lit) {
            level.setBlock(worldPosition, getBlockState().setValue(BubbleAnchorBlock.ACTIVE, lit), Block.UPDATE_CLIENTS);
        }

        if (active) {
            ACTIVE.add(this);
        } else {
            ACTIVE.remove(this);
        }
        int radius = currentRadius();
        BlockPos desiredCenter = active ? center(radius) : null;
        if (heldCenter != null && (desiredCenter == null || !desiredCenter.equals(heldCenter) || radius != heldRadius)) {
            if (!releasing) {
                releasing = true;
                cursor = 0;
            }
            if (releaseStep(serverLevel, desiredCenter, radius, NTConfig.bubbleAnchorBlocksPerTick)) {
                releasing = false;
                heldCenter = desiredCenter;
                heldRadius = desiredCenter == null ? 0 : radius;
                cursor = 0;
                swept = 0;
                setChanged();
            }
            return;
        }
        if (desiredCenter != null) {
            if (heldCenter == null) {
                heldCenter = desiredCenter;
                heldRadius = radius;
                cursor = 0;
                swept = 0;
                setChanged();
            }
            clearStep(serverLevel, NTConfig.bubbleAnchorBlocksPerTick);
        }
    }

    private void warn(ServerLevel serverLevel) {
        AABB area = new AABB(worldPosition).inflate(heldRadius + 4);
        for (Player player : serverLevel.getEntitiesOfClass(Player.class, area)) {
            player.displayClientMessage(Component.translatable("nautec.bubble_anchor.warning").withStyle(ChatFormatting.GOLD), true);
        }
        serverLevel.playSound(null, worldPosition, SoundEvents.NOTE_BLOCK_BELL.value(), SoundSource.BLOCKS, 1.0F, 0.6F);
    }

    private void clearStep(ServerLevel serverLevel, int budget) {
        int[][] offsets = offsets(heldRadius);
        cursor = Math.floorMod(cursor, offsets.length);
        if (swept >= offsets.length) {
            budget = Math.min(budget, MAINTENANCE_BUDGET);
        } else {
            swept += budget;
        }
        for (int i = 0; i < budget; i++) {
            int[] offset = offsets[cursor];
            cursor = (cursor + 1) % offsets.length;
            clear(serverLevel, heldCenter.offset(offset[0], offset[1], offset[2]));
        }
    }

    private void clear(ServerLevel serverLevel, BlockPos pos) {
        if (!pos.equals(worldPosition)) {
            hold(serverLevel, pos);
        }
    }

    static void hold(ServerLevel serverLevel, BlockPos pos) {
        if (!serverLevel.isLoaded(pos)) {
            return;
        }
        BlockState state = serverLevel.getBlockState(pos);
        Block block = state.getBlock();
        if (block == Blocks.WATER || block instanceof KelpBlock || block instanceof KelpPlantBlock || block instanceof SeagrassBlock
                || block instanceof TallSeagrassBlock || block instanceof BubbleColumnBlock) {
            serverLevel.setBlock(pos, NTBlocks.HELD_WATER.get().defaultBlockState(), Block.UPDATE_CLIENTS);
        } else if (state.hasProperty(BlockStateProperties.WATERLOGGED) && state.getValue(BlockStateProperties.WATERLOGGED)) {
            serverLevel.setBlock(pos, state.setValue(BlockStateProperties.WATERLOGGED, false), Block.UPDATE_ALL);
        }
    }

    private boolean releaseStep(ServerLevel serverLevel, @Nullable BlockPos keepCenter, int keepRadius, int budget) {
        int[][] offsets = offsets(heldRadius);
        int done = 0;
        while (cursor < offsets.length && done < budget) {
            int[] offset = offsets[offsets.length - 1 - cursor];
            cursor++;
            BlockPos pos = heldCenter.offset(offset[0], offset[1], offset[2]);
            if (keepCenter != null && Math.abs(pos.getX() - keepCenter.getX()) <= keepRadius && Math.abs(pos.getY() - keepCenter.getY()) <= keepRadius
                    && Math.abs(pos.getZ() - keepCenter.getZ()) <= keepRadius) {
                continue;
            }
            release(serverLevel, pos, fillWater, this);
            done++;
        }
        return cursor >= offsets.length;
    }

    static void release(Level world, BlockPos pos, boolean fillWater, @Nullable Object self) {
        if (!world.isLoaded(pos) || !world.getBlockState(pos).is(NTBlocks.HELD_WATER.get())) {
            return;
        }
        for (BubbleAnchorBlockEntity other : ACTIVE) {
            if (other != self && !other.isRemoved() && other.level == world && other.covers(pos)) {
                return;
            }
        }
        if (AirPocketBlockEntity.covered(world, pos, self)) {
            return;
        }
        world.setBlock(pos, fillWater ? Blocks.WATER.defaultBlockState() : Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
    }

    public void releaseAll() {
        ACTIVE.remove(this);
        if (heldCenter == null || !(level instanceof ServerLevel serverLevel)) {
            return;
        }
        PendingRelease pending = new PendingRelease(serverLevel, heldCenter, offsets(heldRadius), fillWater);
        if (!pending.step(Math.max(NTConfig.bubbleAnchorBlocksPerTick, MAINTENANCE_BUDGET))) {
            PENDING_RELEASES.add(pending);
        }
        heldCenter = null;
        heldRadius = 0;
    }

    private static final class PendingRelease {
        private final ServerLevel level;
        private final BlockPos center;
        private final int[][] offsets;
        private final boolean fillWater;
        private int cursor;

        private PendingRelease(ServerLevel level, BlockPos center, int[][] offsets, boolean fillWater) {
            this.level = level;
            this.center = center;
            this.offsets = offsets;
            this.fillWater = fillWater;
        }

        private boolean step(int budget) {
            int end = Math.min(offsets.length, cursor + budget);
            for (; cursor < end; cursor++) {
                int[] offset = offsets[offsets.length - 1 - cursor];
                release(level, center.offset(offset[0], offset[1], offset[2]), fillWater, null);
            }
            return cursor >= offsets.length;
        }
    }

    @EventBusSubscriber(modid = Nautec.MODID)
    public static final class Releases {
        private Releases() {
        }

        @SubscribeEvent
        public static void onLevelTick(LevelTickEvent.Post event) {
            if (PENDING_RELEASES.isEmpty() || !(event.getLevel() instanceof ServerLevel serverLevel)) {
                return;
            }
            int budget = Math.max(NTConfig.bubbleAnchorBlocksPerTick, MAINTENANCE_BUDGET);
            PENDING_RELEASES.removeIf(pending -> pending.level == serverLevel && pending.step(budget));
        }

        @SubscribeEvent
        public static void onServerStopping(ServerStoppingEvent event) {
            for (PendingRelease pending : PENDING_RELEASES) {
                pending.step(Integer.MAX_VALUE);
            }
            PENDING_RELEASES.clear();
        }
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        releaseAll();
        super.preRemoveSideEffects(pos, state);
    }

    @Override
    public void setRemoved() {
        ACTIVE.remove(this);
        super.setRemoved();
    }

    @Override
    public <T> Map<Direction, Pair<IOActions, int[]>> getSidedInteractions(BlockCapability<T, @Nullable Direction> capability) {
        return Map.of();
    }

    @Override
    public Component getDisplayName() {
        return getBlockState().getBlock().getName();
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new BubbleAnchorMenu(containerId, inventory, this);
    }

    @Override
    protected void saveData(ValueOutput out) {
        super.saveData(out);
        out.putBoolean("enabled", enabled);
        out.putBoolean("fill_water", fillWater);
        out.putBoolean("above", above);
        out.putInt("burn", burn);
        out.putInt("burn_total", burnTotal);
        out.putInt("status", status);
        if (heldCenter != null) {
            out.store("held_center", BlockPos.CODEC, heldCenter);
            out.putInt("held_radius", heldRadius);
        }
        out.putInt("cursor", cursor);
        out.putBoolean("releasing", releasing);
        out.putInt("laser_hold", laserHold);
        out.putInt("laser_radius", laserRadius);
    }

    @Override
    protected void loadData(ValueInput in) {
        super.loadData(in);
        this.enabled = in.getBooleanOr("enabled", true);
        this.fillWater = in.getBooleanOr("fill_water", true);
        this.above = in.getBooleanOr("above", false);
        this.burn = in.getIntOr("burn", 0);
        this.burnTotal = in.getIntOr("burn_total", 0);
        this.status = in.getIntOr("status", STATUS_OFF);
        this.heldCenter = in.read("held_center", BlockPos.CODEC).orElse(null);
        this.heldRadius = in.getIntOr("held_radius", 0);
        this.cursor = in.getIntOr("cursor", 0);
        this.releasing = in.getBooleanOr("releasing", false);
        this.laserHold = in.getIntOr("laser_hold", 0);
        this.laserRadius = in.getIntOr("laser_radius", 0);
    }
}
