package com.breakinblocks.nautec.content.blockentities.generators;

import com.breakinblocks.nautec.registries.NTBlocks;
import com.breakinblocks.nautec.api.sides.SlotRoles;
import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.capabilities.IOActions;
import com.breakinblocks.nautec.capabilities.fluid.FluidTank;
import com.breakinblocks.nautec.capabilities.fluid.SidedFluidHandler;
import com.breakinblocks.nautec.capabilities.item.SidedItemHandler;
import com.breakinblocks.nautec.content.blockentities.fusion.FusionControllerBlockEntity;
import com.breakinblocks.nautec.content.blocks.generators.ThermalVentTapBlock;
import com.breakinblocks.nautec.registries.NTBlockEntityTypes;
import com.breakinblocks.nautec.registries.NTItems;
import com.breakinblocks.nautec.tags.NTTags;
import com.breakinblocks.nautec.worldgen.NTBiomeKeys;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class ThermalVentTapBlockEntity extends FeGeneratorBlockEntity {
    public static final int BUFFER = 100_000;
    public static final int FUEL_CAPACITY = 8_000;
    public static final int HEAT_SPOTS = 9;
    public static final int VENT_BIOME_HEAT = 3;
    public static final int VENT_BLOCK_HEAT = 3;
    private static final int SCAN_INTERVAL = 40;
    private static final int PUSH_INTERVAL = 10;

    private final FluidTank fuel = new FluidTank(FUEL_CAPACITY) {
        @Override
        public boolean isValid(int index, @NotNull FluidResource resource) {
            return FusionControllerBlockEntity.isFuel(resource.toStack(1));
        }

        @Override
        protected void onContentsChanged(int index, FluidStack stack) {
            super.onContentsChanged(index, stack);
            setChanged();
        }
    };
    private final ResourceHandler<FluidResource> fuelInput = new SidedFluidHandler(fuel, Pair.of(IOActions.INSERT, new int[]{0}));

    private final List<BlockCapabilityCache<ResourceHandler<ItemResource>, @Nullable Direction>> itemOutputs = new ArrayList<>();

    private Status status = Status.NO_HEAT;
    private int heat;
    private int burned;

    private static final SlotRoles ITEM_ROLES = SlotRoles.of(new int[0], new int[]{0});

    public ThermalVentTapBlockEntity(BlockPos pos, BlockState state) {
        super(NTBlockEntityTypes.THERMAL_VENT_TAP.get(), pos, state, BUFFER);
        addItemHandler(1, (slot, stack) -> false);
    }

    public Status getStatus() {
        return status;
    }

    public int getHeat() {
        return heat;
    }

    public FluidTank getFuelTank() {
        return fuel;
    }

    public ResourceHandler<FluidResource> getFuelInput() {
        return fuelInput;
    }

    @Override
    public SlotRoles itemRoles() {
        return ITEM_ROLES;
    }

    public int rate() {
        if (heat <= 0) {
            return 0;
        }
        int min = NTConfig.ventTapMinOutput;
        int max = Math.max(min, NTConfig.ventTapMaxOutput);
        double fraction = Math.min(1.0, (heat - 1) / (double) (HEAT_SPOTS - 1));
        return (int) Math.round(min + (max - min) * fraction);
    }

    @Override
    protected void serverTick(ServerLevel level) {
        tick(level);
        pushSalt(level);
        boolean lit = status == Status.RUNNING;
        if (getBlockState().getValue(ThermalVentTapBlock.LIT) != lit) {
            level.setBlock(worldPosition, getBlockState().setValue(ThermalVentTapBlock.LIT, lit), Block.UPDATE_CLIENTS);
        }
    }

    private void tick(ServerLevel level) {
        if ((level.getGameTime() + worldPosition.hashCode()) % SCAN_INTERVAL == 0 || status == Status.NO_HEAT && level.getGameTime() % 20 == 0) {
            scan(level);
        }
        if (heat <= 0) {
            status = Status.NO_HEAT;
            return;
        }
        int burn = Math.max(1, NTConfig.ventTapSaltWaterPerTick);
        if (fuel.getFluidAmount() < burn) {
            status = Status.NO_FUEL;
            return;
        }
        if (space() <= 0) {
            status = Status.BUFFER_FULL;
            return;
        }
        fuel.drain(burn);
        generate(rate());
        status = Status.RUNNING;
        burned += burn;
        int perSalt = Math.max(1, NTConfig.ventTapSaltWaterPerSalt);
        if (burned >= perSalt) {
            burned -= perSalt;
            ItemStack salt = saltByproduct();
            if (!salt.isEmpty()) {
                forceInsertItem(0, salt, false);
            }
        }
    }

    public static ItemStack saltByproduct() {
        String id = NTConfig.ventTapSaltItem.trim();
        if (id.isEmpty()) {
            return ItemStack.EMPTY;
        }
        Identifier key = Identifier.tryParse(id);
        if (key != null) {
            Optional<Item> item = BuiltInRegistries.ITEM.getOptional(key);
            if (item.isPresent()) {
                return item.get() == Items.AIR ? ItemStack.EMPTY : new ItemStack(item.get());
            }
        }
        return new ItemStack(NTItems.SALT.get());
    }

    private void pushSalt(ServerLevel level) {
        if (level.getGameTime() % PUSH_INTERVAL != 0 || getItemHandler().getResource(0).isEmpty()) {
            return;
        }
        if (itemOutputs.isEmpty()) {
            for (Direction direction : Direction.values()) {
                itemOutputs.add(BlockCapabilityCache.create(Capabilities.Item.BLOCK, level, worldPosition.relative(direction), direction.getOpposite()));
            }
        }
        for (BlockCapabilityCache<ResourceHandler<ItemResource>, @Nullable Direction> cache : itemOutputs) {
            ResourceHandler<ItemResource> target = cache.getCapability();
            if (target == null) {
                continue;
            }
            try (Transaction tx = Transaction.openRoot()) {
                ResourceHandlerUtil.moveStacking(getItemHandler(), target, resource -> true, Integer.MAX_VALUE, tx);
                tx.commit();
            }
            if (getItemHandler().getResource(0).isEmpty()) {
                return;
            }
        }
    }

    public void scan(ServerLevel level) {
        int found = 0;
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                cursor.setWithOffset(worldPosition, x, -1, z);
                BlockState state = level.getBlockState(cursor);
                if (state.is(NTBlocks.HYDROTHERMAL_VENT.get())) {
                    found += VENT_BLOCK_HEAT;
                } else if (state.is(NTTags.Blocks.VENT_HEAT_SOURCES) || state.getFluidState().is(FluidTags.LAVA)) {
                    found++;
                }
            }
        }
        if (found > 0 && level.getBiome(worldPosition).is(NTBiomeKeys.HYDROTHERMAL_VENTS)) {
            found = found + VENT_BIOME_HEAT;
        }
        found = Math.min(HEAT_SPOTS, found);
        this.heat = found;
    }

    @Override
    protected void saveData(ValueOutput out) {
        super.saveData(out);
        fuel.serialize(out.child("fuel"));
        out.putInt("burned", burned);
    }

    @Override
    protected void loadData(ValueInput in) {
        super.loadData(in);
        fuel.deserialize(in.childOrEmpty("fuel"));
        this.burned = in.getIntOr("burned", 0);
    }

    public enum Status {
        RUNNING,
        NO_HEAT,
        NO_FUEL,
        BUFFER_FULL;

        public String translationKey() {
            return "nautec.vent_tap.status." + name().toLowerCase(Locale.ROOT);
        }

        public static Status byId(int id) {
            Status[] values = values();
            return id >= 0 && id < values.length ? values[id] : NO_HEAT;
        }
    }
}
