package com.breakinblocks.nautec.api.blockentities;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.Codec;
import com.breakinblocks.nautec.api.sides.RelativeFace;
import com.breakinblocks.nautec.api.sides.SideConfig;
import com.breakinblocks.nautec.api.sides.SideKind;
import com.breakinblocks.nautec.api.sides.SideMode;
import com.breakinblocks.nautec.api.sides.SlotRoles;
import com.breakinblocks.nautec.capabilities.IOActions;
import com.breakinblocks.nautec.capabilities.RoleResourceHandler;
import com.breakinblocks.nautec.capabilities.bacteria.BacteriaStorage;
import com.breakinblocks.nautec.capabilities.bacteria.DishPort;
import com.breakinblocks.nautec.capabilities.bacteria.IBacteriaStorage;
import com.breakinblocks.nautec.capabilities.fluid.FluidTank;
import com.breakinblocks.nautec.capabilities.fluid.SidedFluidHandler;
import com.breakinblocks.nautec.capabilities.fluid.TankList;
import com.breakinblocks.nautec.capabilities.item.ItemStackHandler;
import com.breakinblocks.nautec.capabilities.item.SidedItemHandler;
import com.breakinblocks.nautec.capabilities.power.IPowerStorage;
import com.breakinblocks.nautec.capabilities.power.PowerStorage;
import com.breakinblocks.nautec.utils.ItemTemplates;
import com.breakinblocks.nautec.utils.TemplateSanitizer;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.ints.IntSets;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.resource.Resource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

public abstract class ContainerBlockEntity extends BlockEntity {
    public static final int SYNC_INTERVAL = 10;
    private @Nullable ItemStackHandler itemHandler;
    private BiPredicate<Integer, ItemStack> itemValidation = (slot, stack) -> true;
    private final SideConfig sideConfig = new SideConfig();
    private final Int2ObjectMap<ItemStack> ghostInputs = new Int2ObjectOpenHashMap<>();

    private record GhostEntry(int slot, ItemStack stack) {
        private static final Codec<GhostEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.INT.fieldOf("slot").forGetter(GhostEntry::slot),
                ItemStack.CODEC.fieldOf("stack").forGetter(GhostEntry::stack)
        ).apply(instance, GhostEntry::new));
    }
    private @Nullable FluidTank fluidTank;
    private @Nullable FluidTank secondaryFluidTank;
    private @Nullable PowerStorage powerStorage;
    private @Nullable BacteriaStorage bacteriaStorage;
    private long lastSync = -SYNC_INTERVAL;
    private boolean syncScheduled;

    public ContainerBlockEntity(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
        super(blockEntityType, blockPos, blockState);
    }

    public void commonTick() {
    }

    public ResourceHandler<ItemResource> getItemHandler() {
        return itemHandler;
    }

    public ResourceHandler<FluidResource> getFluidHandler() {
        return fluidTank;
    }

    public ResourceHandler<FluidResource> getSecondaryFluidHandler() {
        return secondaryFluidTank;
    }

    public IPowerStorage getPowerStorage() {
        return powerStorage;
    }

    public IBacteriaStorage getBacteriaStorage() {
        return bacteriaStorage;
    }

    public ItemStackHandler getItemStackHandler() {
        return itemHandler;
    }

    public FluidTank getFluidTank() {
        return fluidTank;
    }

    public FluidTank getSecondaryFluidTank() {
        return secondaryFluidTank;
    }

    protected PowerStorage getPowerStorageImpl() {
        return powerStorage;
    }

    protected BacteriaStorage getBacteriaStorageImpl() {
        return bacteriaStorage;
    }

    @Override
    protected final void loadAdditional(@NotNull ValueInput in) {
        super.loadAdditional(in);
        if (this.getFluidTank() != null)
            in.child("fluid_tank").ifPresent(this.getFluidTank()::deserialize);
        if (this.getSecondaryFluidTank() != null)
            in.child("secondary_fluid").ifPresent(this.getSecondaryFluidTank()::deserialize);
        if (this.getItemStackHandler() != null)
            in.child("itemhandler").ifPresent(this.getItemStackHandler()::deserialize);
        if (this.getPowerStorageImpl() != null)
            in.child("power_storage").ifPresent(this.getPowerStorageImpl()::deserialize);
        if (this.getBacteriaStorageImpl() != null)
            in.child("bacteria_storage").ifPresent(this.getBacteriaStorageImpl()::deserialize);
        if (hasSideConfig()) {
            in.child("side_config").ifPresent(config -> {
                sideConfig.reset();
                sideConfig.load(config);
            });
        }
        in.read("ghost_inputs", GhostEntry.CODEC.listOf()).ifPresent(list -> {
            ghostInputs.clear();
            list.forEach(entry -> ghostInputs.put(entry.slot(), entry.stack()));
        });
        loadData(in);
    }

    @Override
    protected final void saveAdditional(@NotNull ValueOutput out) {
        super.saveAdditional(out);
        if (getFluidTank() != null)
            getFluidTank().serialize(out.child("fluid_tank"));
        if (getSecondaryFluidTank() != null)
            getSecondaryFluidTank().serialize(out.child("secondary_fluid"));
        if (getItemStackHandler() != null)
            getItemStackHandler().serialize(out.child("itemhandler"));
        if (getPowerStorageImpl() != null)
            getPowerStorageImpl().serialize(out.child("power_storage"));
        if (getBacteriaStorageImpl() != null)
            getBacteriaStorageImpl().serialize(out.child("bacteria_storage"));
        if (hasSideConfig())
            sideConfig.save(out.child("side_config"));
        if (itemHandler != null && ghostSlots().length > 0) {
            List<GhostEntry> ghosts = new ArrayList<>();
            ghostInputs.int2ObjectEntrySet().forEach(entry -> ghosts.add(new GhostEntry(entry.getIntKey(), entry.getValue())));
            out.store("ghost_inputs", GhostEntry.CODEC.listOf(), ghosts);
        }
        saveData(out);
    }

    protected void loadData(ValueInput in) {
    }

    protected void saveData(ValueOutput out) {
    }

    protected final void addItemHandler(int slots) {
        addItemHandler(slots, (slot, itemStack) -> true);
    }

    protected final void addItemHandler(int slots, int slotLimit) {
        addItemHandler(slots, slotLimit, (slot, itemStack) -> true);
    }

    protected final void addItemHandler(int slots, BiPredicate<Integer, ItemStack> validation) {
        addItemHandler(slots, 64, validation);
    }

    protected final void addItemHandler(int slots, UnaryOperator<Integer> slotLimit, BiPredicate<Integer, ItemStack> validation) {
        addItemHandlerInternal(slots, slotLimit, validation);
    }

    protected final void addItemHandler(int slots, int slotLimit, BiPredicate<Integer, ItemStack> validation) {
        addItemHandlerInternal(slots, slot -> slotLimit, validation);
    }

    private void addItemHandlerInternal(int slots, UnaryOperator<Integer> slotLimit, BiPredicate<Integer, ItemStack> validation) {
        this.itemValidation = validation;
        this.itemHandler = new ItemStackHandler(slots) {
            @Override
            protected void onContentsChanged(int slot, ItemStack stack) {
                super.onContentsChanged(slot, stack);
                update();
                onItemsChanged(slot);
            }

            @Override
            public boolean isValid(int slot, @NotNull ItemResource resource) {
                ItemStack ghost = ghostInputs.get(slot);
                return (ghost == null || ghostMatches(slot, ghost, resource)) && validation.test(slot, resource.toStack())
                        && acceptsNow(slot, resource);
            }

            @Override
            protected int getCapacity(int slot, ItemResource resource) {
                int limit = slotLimit.apply(slot);
                return resource.isEmpty() ? limit : Math.min(limit, resource.getMaxStackSize());
            }
        };
    }

    protected boolean acceptsNow(int slot, ItemResource resource) {
        return true;
    }

    public ItemStack forceExtractItem(int slot, int amount, boolean simulate) {
        if (amount == 0 || itemHandler == null)
            return ItemStack.EMPTY;

        ItemStack existing = itemHandler.getStackInSlot(slot);

        if (existing.isEmpty())
            return ItemStack.EMPTY;

        int toExtract = Math.min(amount, existing.getMaxStackSize());

        if (existing.getCount() <= toExtract) {
            if (!simulate) {
                itemHandler.setStackInSlot(slot, ItemStack.EMPTY);
                return existing;
            } else {
                return existing.copy();
            }
        } else {
            if (!simulate) {
                itemHandler.setStackInSlot(slot, existing.copyWithCount(existing.getCount() - toExtract));
            }

            return existing.copyWithCount(toExtract);
        }
    }

    public ItemStack forceInsertItem(int slot, ItemStack stack, boolean simulate) {
        if (stack.isEmpty() || itemHandler == null)
            return ItemStack.EMPTY;

        ItemStack existing = itemHandler.getStackInSlot(slot);

        int limit = Math.min(itemHandler.getSlotLimit(slot), stack.getMaxStackSize());

        if (!existing.isEmpty()) {
            if (!ItemStack.isSameItemSameComponents(stack, existing))
                return stack;

            limit -= existing.getCount();
        }

        if (limit <= 0)
            return stack;

        boolean reachedLimit = stack.getCount() > limit;

        if (!simulate) {
            if (existing.isEmpty()) {
                itemHandler.setStackInSlot(slot, reachedLimit ? stack.copyWithCount(limit) : stack.copy());
            } else {
                itemHandler.setStackInSlot(slot, existing.copyWithCount(existing.getCount() + (reachedLimit ? limit : stack.getCount())));
            }
        }

        return reachedLimit ? stack.copyWithCount(stack.getCount() - limit) : ItemStack.EMPTY;
    }


    protected final void addFluidTank(int capacityInMb) {
        addFluidTank(capacityInMb, ignored -> true);
    }

    protected final void addSecondaryFluidTank(int capacityInMb) {
        addSecondaryFluidTank(capacityInMb, ignored -> true);
    }

    protected final void addSecondaryFluidTank(int capacityInMb, Predicate<FluidStack> validation) {
        addFluidTank(capacityInMb, validation, true);
    }

    protected final void addFluidTank(int capacityInMn, Predicate<FluidStack> validation) {
        addFluidTank(capacityInMn, validation, false);
    }

    protected final void addFluidTank(int capacityInMb, Predicate<FluidStack> validation, boolean secondary) {
        FluidTank tank = new FluidTank(capacityInMb) {
            @Override
            protected void onContentsChanged(int index, FluidStack stack) {
                super.onContentsChanged(index, stack);
                update();
                onFluidChanged();
            }

            @Override
            public boolean isValid(int index, @NotNull FluidResource resource) {
                return validation.test(resource.toStack(1));
            }
        };
        if (!secondary) {
            this.fluidTank = tank;
        } else {
            this.secondaryFluidTank = tank;
        }
    }

    protected final void addPowerStorage(int powerCapacity) {
        this.powerStorage = new PowerStorage() {
            @Override
            public void onEnergyChanged() {
                update();
                ContainerBlockEntity.this.onPowerChanged();
            }
        };
        this.powerStorage.setPowerCapacity(powerCapacity);
    }

    protected final void addBacteriaStorage(int slots) {
        this.bacteriaStorage = new BacteriaStorage(slots) {
            @Override
            public void onBacteriaChanged(int slot) {
                update();
                ContainerBlockEntity.this.onBacteriaChanged(slot);
            }
        };
    }

    public void update() {
        setChanged();
        requestSync();
    }

    public final void requestSync() {
        if (!(level instanceof ServerLevel serverLevel) || syncScheduled) {
            return;
        }
        if (readyToSync(serverLevel.getGameTime())) {
            flushSync();
        } else {
            syncScheduled = true;
            BlockEntitySyncQueue.schedule(serverLevel, this);
        }
    }

    final boolean readyToSync(long gameTime) {
        return gameTime - lastSync >= SYNC_INTERVAL;
    }

    final void flushSync() {
        syncScheduled = false;
        if (level == null || isRemoved()) {
            return;
        }
        lastSync = level.getGameTime();
        level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
    }

    final void cancelScheduledSync() {
        syncScheduled = false;
    }

    protected void onItemsChanged(int slot) {
    }

    protected void onFluidChanged() {
    }

    public void onPowerChanged() {
    }

    public void onBacteriaChanged(int slot) {
    }

    public void drop() {
        ItemStack[] stacks = getItemHandlerStacks();
        if (stacks != null) {
            SimpleContainer inventory = new SimpleContainer(stacks);
            Containers.dropContents(this.level, this.worldPosition, inventory);
        }
    }

    public @Nullable ItemStack[] getItemHandlerStacks() {
        ItemStackHandler itemStackHandler = getItemStackHandler();

        if (itemStackHandler == null) return null;

        ItemStack[] itemStacks = new ItemStack[itemStackHandler.getSlots()];
        for (int i = 0; i < itemStackHandler.getSlots(); i++) {
            itemStacks[i] = itemStackHandler.getStackInSlot(i);
        }
        return itemStacks;
    }

    public List<ItemStack> getItemHandlerStacksList() {
        ItemStackHandler itemStackHandler = getItemStackHandler();

        if (itemStackHandler == null) return null;

        int slots = itemStackHandler.getSlots();
        ObjectList<ItemStack> itemStacks = new ObjectArrayList<>(slots);
        for (int i = 0; i < slots; i++) {
            ItemStack stack = itemStackHandler.getStackInSlot(i);
            if (!stack.isEmpty()) {
                itemStacks.add(stack);
            }
        }
        return itemStacks;
    }

    public <T> T getHandlerOnSide(BlockCapability<T, @Nullable Direction> capability, SidedHandlerSupplier<T> handlerSupplier, Direction direction, T baseHandler) {
        if (direction == null) {
            return baseHandler;
        }

        Map<Direction, Pair<IOActions, int[]>> ioPorts = getSidedInteractions(capability);
        if (ioPorts.containsKey(direction)) {

            if (direction == Direction.UP || direction == Direction.DOWN) {
                return handlerSupplier.get(baseHandler, ioPorts.get(direction));
            }

            if (this.getBlockState().hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
                Direction localDir = this.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);

                return getCapOnSide(handlerSupplier, direction, baseHandler, ioPorts, localDir);
            }

            if (getBlockState().hasProperty(BlockStateProperties.FACING)) {
                Direction localDir = this.getBlockState().getValue(BlockStateProperties.FACING);

                return getCapOnSide(handlerSupplier, direction, baseHandler, ioPorts, localDir);
            }

            return handlerSupplier.get(baseHandler, ioPorts.get(direction));
        }

        return null;
    }

    @Nullable
    private <T> T getCapOnSide(SidedHandlerSupplier<T> handlerSupplier, Direction direction, T baseHandler, Map<Direction, Pair<IOActions, int[]>> ioPorts, Direction localDir) {
        return switch (localDir) {
            case NORTH -> handlerSupplier.get(baseHandler, ioPorts.get(direction.getOpposite()));
            case EAST -> handlerSupplier.get(baseHandler, ioPorts.get(direction.getClockWise()));
            case SOUTH -> handlerSupplier.get(baseHandler, ioPorts.get(direction));
            case WEST -> handlerSupplier.get(baseHandler, ioPorts.get(direction.getCounterClockWise()));
            default -> null;
        };
    }

    public @Nullable SlotRoles itemRoles() {
        return null;
    }

    public @Nullable SlotRoles fluidRoles() {
        return null;
    }

    public boolean hasSideConfig() {
        return itemRoles() != null || fluidRoles() != null;
    }

    public boolean hasSideConfig(SideKind kind) {
        return (kind == SideKind.ITEMS ? itemRoles() : fluidRoles()) != null;
    }

    public int[] ghostSlots() {
        SlotRoles roles = itemRoles();
        return roles == null ? new int[0] : roles.inputs().toIntArray();
    }

    public boolean isGhostSlot(int slot) {
        for (int candidate : ghostSlots()) {
            if (candidate == slot) {
                return true;
            }
        }
        return false;
    }

    public ItemStack getGhost(int slot) {
        ItemStack ghost = ghostInputs.get(slot);
        return ghost == null ? ItemStack.EMPTY : ghost;
    }

    public Int2ObjectMap<ItemStack> ghosts() {
        return ghostInputs;
    }

    public boolean ghostMatches(int slot, ItemStack ghost, ItemResource resource) {
        return ItemTemplates.matches(ghost, resource);
    }

    protected boolean acceptsGhost(int slot, ItemStack stack) {
        return itemValidation.test(slot, stack);
    }

    public boolean setGhost(int slot, ItemStack stack) {
        if (itemHandler == null || !isGhostSlot(slot) || slot < 0 || slot >= itemHandler.size()) {
            return false;
        }
        ItemStack previous = ghostInputs.remove(slot);
        if (!stack.isEmpty()) {
            if (!acceptsGhost(slot, stack)) {
                if (previous != null) {
                    ghostInputs.put(slot, previous);
                }
                return false;
            }
            ItemStack template = (DishPort.isDish(stack) ? TemplateSanitizer.item(stack) : stack).copyWithCount(1);
            if (level != null && !TemplateSanitizer.fits(template, level.registryAccess())) {
                if (previous != null) {
                    ghostInputs.put(slot, previous);
                }
                return false;
            }
            ghostInputs.put(slot, template);
        }
        update();
        return true;
    }

    public SideConfig getSideConfig() {
        return sideConfig;
    }

    public Direction front() {
        BlockState state = getBlockState();
        if (state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
            return state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        }
        if (state.hasProperty(BlockStateProperties.FACING)) {
            return state.getValue(BlockStateProperties.FACING);
        }
        return Direction.NORTH;
    }

    public void setSideMode(SideKind kind, RelativeFace face, SideMode mode) {
        sideConfig.set(kind, face, mode);
        sideConfigChanged();
    }

    public void sideConfigChanged() {
        update();
        if (level != null && !level.isClientSide()) {
            level.invalidateCapabilities(worldPosition);
        }
    }

    public void saveSettings(ValueOutput out) {
    }

    public boolean loadSettings(ValueInput in, ServerPlayer player) {
        return false;
    }

    public int[] cardSlots() {
        return new int[0];
    }

    public List<FluidTank> fluidTanks() {
        List<FluidTank> tanks = new ArrayList<>();
        if (fluidTank != null) {
            tanks.add(fluidTank);
        }
        if (secondaryFluidTank != null) {
            tanks.add(secondaryFluidTank);
        }
        return tanks;
    }

    private <R extends Resource> @Nullable ResourceHandler<R> roleHandler(ResourceHandler<R> inner, SlotRoles roles, SideMode mode, R empty) {
        if (mode == SideMode.NONE) {
            return null;
        }
        return new RoleResourceHandler<>(inner, mode.inserts() ? roles.inputs() : IntSets.EMPTY_SET,
                mode.extracts() ? roles.outputs() : IntSets.EMPTY_SET, empty);
    }

    public ResourceHandler<ItemResource> getItemHandlerOnSide(Direction direction) {
        SlotRoles roles = itemRoles();
        if (roles != null && direction != null) {
            return roleHandler(getItemHandler(), roles, sideConfig.get(SideKind.ITEMS, RelativeFace.of(front(), direction)), ItemResource.EMPTY);
        }
        return getHandlerOnSide(
                Capabilities.Item.BLOCK,
                SidedItemHandler::new,
                direction,
                getItemHandler()
        );
    }

    public ResourceHandler<FluidResource> getFluidHandlerOnSide(Direction direction) {
        SlotRoles roles = fluidRoles();
        if (roles != null) {
            ResourceHandler<FluidResource> tanks = new TankList(fluidTanks());
            if (direction == null) {
                return tanks;
            }
            return roleHandler(tanks, roles, sideConfig.get(SideKind.FLUIDS, RelativeFace.of(front(), direction)), FluidResource.EMPTY);
        }
        return getHandlerOnSide(
                Capabilities.Fluid.BLOCK,
                SidedFluidHandler::new,
                direction,
                getFluidHandler()
        );
    }

    /**
     * Get the input/output config for the blockenitity.
     * If directions are not defined in the map, they are assumed to be {@link IOActions#NONE} and do not affect any slot.
     *
     * @return Map of directions that each map to a pair that defines the IOAction as well as the tanks that are affected. Return an empty map if you do not have an itemhandler
     */
    public abstract <T> Map<Direction, Pair<IOActions, int[]>> getSidedInteractions(BlockCapability<T, @Nullable Direction> capability);

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public @NotNull CompoundTag getUpdateTag(HolderLookup.Provider provider) {
        return saveWithoutMetadata(provider);
    }

    @FunctionalInterface
    public interface SidedHandlerSupplier<T> {
        T get(T handler, Pair<IOActions, int[]> supportedActions);
    }
}
