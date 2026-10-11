package com.breakinblocks.nautec.content.blockentities;

import net.minecraft.world.level.block.AmethystClusterBlock;
import com.breakinblocks.nautec.registries.NTBlocks;
import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.api.sides.SlotRoles;
import com.breakinblocks.nautec.api.blockentities.BeamScan;
import com.breakinblocks.nautec.api.blockentities.LaserBlockEntity;
import com.breakinblocks.nautec.capabilities.IOActions;
import com.breakinblocks.nautec.content.blocks.AquaticCatalystBlock;
import com.breakinblocks.nautec.content.recipes.AquaticCatalystChannelingRecipe;
import com.breakinblocks.nautec.registries.NTBlockEntityTypes;
import com.breakinblocks.nautec.utils.RecipeRevision;
import com.breakinblocks.nautec.utils.SidedCapUtils;
import it.unimi.dsi.fastutil.Pair;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.capabilities.BlockCapability;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AquaticCatalystBlockEntity extends LaserBlockEntity {
    private RecipeHolder<AquaticCatalystChannelingRecipe> currentRecipe;
    private RecipeHolder<AquaticCatalystChannelingRecipe> nextRecipe;
    private int duration;
    private Identifier currentRecipeId;
    private Identifier nextRecipeId;
    private int syncedTransfer;
    private boolean burning;
    private boolean paused;
    private BeamScan beamScan;
    private int boosters;
    private final RecipeRevision validatorRevision = new RecipeRevision();
    private @Nullable Item validatedItem;
    private boolean validatedResult;

    private static final SlotRoles ITEM_ROLES = SlotRoles.of(new int[]{0}, new int[0]);

    public AquaticCatalystBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(NTBlockEntityTypes.AQUATIC_CATALYST.get(), blockPos, blockState);
        addItemHandler(1, (slot, stack) -> acceptsFuel(stack));
    }

    private boolean acceptsFuel(ItemStack stack) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return false;
        }
        if (validatorRevision.changed(serverLevel)) {
            validatedItem = null;
        }
        if (stack.getItem() != validatedItem) {
            validatedItem = stack.getItem();
            validatedResult = getRecipeForCache(stack) != null;
        }
        return validatedResult;
    }

    @Override
    public void commonTick() {
        super.commonTick();

        if (level.isClientSide()) {
            transmitPower(isActive() ? syncedTransfer : 0);
            return;
        }

        if (currentRecipe == null && !paused) {
            startNextRecipe();
        }

        if (beamScan == null || level.getGameTime() % checkConnectionsInterval() == 0) {
            beamScan = scanBeam(getEmitterDirection());
            int counted = countBoosters();
            if (counted != boosters) {
                boosters = counted;
                update();
            }
        }
        if (boosters > 0 && level.getGameTime() % 20 == 0) {
            consumeClusters();
        }

        boolean burningNow = false;
        if (paused) {
            transmitPower(0);
        } else if (currentRecipe != null) {
            int distance = getLaserDistances().getInt(getEmitterDirection());
            if (distance > 0 && beamScan.connected()) {
                transmitPower(transferAmount());
                setPurity(boostedPurity());
                duration++;
                burningNow = true;
            } else {
                transmitPower(0);
            }
            if (duration >= currentRecipe.value().duration()) {
                duration = 0;
                currentRecipe = null;
                startNextRecipe();
                if (currentRecipe == null) {
                    setPurity(0);
                }
            }
        } else {
            transmitPower(0);
        }
        this.burning = burningNow;

        BlockState state = getBlockState();
        BlockState newState = state
                .setValue(AquaticCatalystBlock.ACTIVE, burningNow)
                .setValue(AquaticCatalystBlock.LINKED, beamScan.connected());
        if (newState != state) {
            level.setBlockAndUpdate(worldPosition, newState);
        }
    }

    public int transferAmount() {
        if (currentRecipe == null) {
            return 0;
        }
        int base = currentRecipe.value().powerAmount() / currentRecipe.value().duration();
        return (int) Math.round(base * (1 + boosters * NTConfig.catalystBuddingOutputBonus));
    }

    public float boostedPurity() {
        if (currentRecipe == null) {
            return 0;
        }
        float base = currentRecipe.value().purity();
        if (boosters == 0 || base >= NTConfig.catalystBuddingMaxPurity) {
            return base;
        }
        return (float) Math.min(NTConfig.catalystBuddingMaxPurity, base + boosters * NTConfig.catalystBuddingPurityBonus);
    }

    public int getBoosters() {
        return boosters;
    }

    private int countBoosters() {
        int count = 0;
        for (BlockPos pos : boosterPositions()) {
            if (level.getBlockState(pos).is(NTBlocks.BUDDING_PRISMARINE.get())) {
                count++;
            }
        }
        return count;
    }

    private List<BlockPos> boosterPositions() {
        List<BlockPos> positions = new ArrayList<>();
        Direction emitter = getEmitterDirection();
        for (Direction direction : Direction.values()) {
            if (direction != emitter) {
                positions.add(worldPosition.relative(direction));
            }
        }
        return positions;
    }

    private void consumeClusters() {
        for (BlockPos budding : boosterPositions()) {
            if (!level.getBlockState(budding).is(NTBlocks.BUDDING_PRISMARINE.get())) {
                continue;
            }
            for (Direction direction : Direction.values()) {
                BlockPos clusterPos = budding.relative(direction);
                BlockState cluster = level.getBlockState(clusterPos);
                if (cluster.is(NTBlocks.PRISMARINE_CLUSTER.get())
                        && cluster.getValue(AmethystClusterBlock.FACING) == direction) {
                    level.destroyBlock(clusterPos, false);
                }
            }
        }
    }

    private void startNextRecipe() {
        if (nextRecipe == null && !getItemStackHandler().getStackInSlot(0).isEmpty()) {
            nextRecipe = getRecipeForCache(getItemStackHandler().getStackInSlot(0));
        }
        if (nextRecipe != null) {
            currentRecipe = nextRecipe;
            getItemStackHandler().extractItem(0, 1, false);
        }
    }

    public boolean isActive() {
        if (level != null && level.isClientSide()) {
            return getBlockState().getValue(AquaticCatalystBlock.ACTIVE);
        }
        return burning;
    }

    public boolean isWaiting() {
        return currentRecipe != null && !paused && !isActive();
    }

    public boolean isPaused() {
        return paused;
    }

    public void setPaused(boolean paused) {
        if (this.paused != paused) {
            this.paused = paused;
            setChanged();
        }
    }

    public Direction getEmitterDirection() {
        return getBlockState().getValue(BlockStateProperties.FACING).getOpposite();
    }

    public BeamScan getBeamScan() {
        if (beamScan == null) {
            beamScan = scanBeam(getEmitterDirection());
        }
        return beamScan;
    }

    public List<Component> diagnosticLines() {
        List<Component> lines = new ArrayList<>();
        lines.add(Component.translatable("nautec.catalyst.diagnostics.header").withStyle(ChatFormatting.AQUA));

        ItemStack fuel = getProcessingItem();
        if (!fuel.isEmpty()) {
            lines.add(Component.translatable("nautec.catalyst.diagnostics.fuel", fuel.getCount(), fuel.getHoverName())
                    .withStyle(ChatFormatting.WHITE));
        } else if (currentRecipe == null) {
            lines.add(Component.translatable("nautec.catalyst.diagnostics.fuel.empty").withStyle(ChatFormatting.RED));
        }

        Direction emitter = getEmitterDirection();
        lines.add(Component.translatable("nautec.catalyst.diagnostics.emitter", directionName(emitter))
                .withStyle(ChatFormatting.WHITE));

        BeamScan scan = scanBeam(emitter);
        this.beamScan = scan;
        Component target = level.getBlockState(scan.targetPos(worldPosition)).getBlock().getName();
        switch (scan.status()) {
            case CONNECTED -> lines.add(Component.translatable("nautec.catalyst.diagnostics.beam.connected",
                    target, distanceText(scan.distance(), emitter)).withStyle(ChatFormatting.GREEN));
            case WRONG_SIDE -> {
                lines.add(Component.translatable("nautec.catalyst.diagnostics.beam.wrong_side",
                        target, distanceText(scan.distance(), emitter)).withStyle(ChatFormatting.RED));
                lines.add(Component.translatable("nautec.catalyst.diagnostics.hint.wrong_side").withStyle(ChatFormatting.GRAY));
            }
            case BLOCKED -> {
                lines.add(Component.translatable("nautec.catalyst.diagnostics.beam.blocked",
                        target, distanceText(scan.distance(), emitter)).withStyle(ChatFormatting.RED));
                lines.add(Component.translatable("nautec.catalyst.diagnostics.hint.blocked").withStyle(ChatFormatting.GRAY));
            }
            case NO_TARGET -> {
                lines.add(Component.translatable("nautec.catalyst.diagnostics.beam.no_target",
                        getMaxLaserDistance(), directionName(emitter)).withStyle(ChatFormatting.RED));
                lines.add(Component.translatable("nautec.catalyst.diagnostics.hint.no_target").withStyle(ChatFormatting.GRAY));
            }
        }

        int found = countBoosters();
        if (found > 0) {
            lines.add(Component.translatable("nautec.catalyst.diagnostics.boosters", found,
                    Math.round(found * NTConfig.catalystBuddingOutputBonus * 100),
                    String.format("%.2f", found * NTConfig.catalystBuddingPurityBonus)).withStyle(ChatFormatting.LIGHT_PURPLE));
        }

        if (paused) {
            lines.add(Component.translatable("nautec.catalyst.diagnostics.status.paused").withStyle(ChatFormatting.YELLOW));
        } else if (isActive()) {
            lines.add(Component.translatable("nautec.catalyst.diagnostics.status.burning", getPowerToTransfer())
                    .withStyle(ChatFormatting.GREEN));
        } else if (currentRecipe != null) {
            lines.add(Component.translatable("nautec.catalyst.diagnostics.status.waiting").withStyle(ChatFormatting.YELLOW));
        } else {
            lines.add(Component.translatable("nautec.catalyst.diagnostics.status.idle").withStyle(ChatFormatting.YELLOW));
        }
        return lines;
    }

    public static MutableComponent directionName(Direction direction) {
        return Component.translatable("nautec.direction." + direction.getSerializedName());
    }

    public static MutableComponent distanceText(int distance, Direction direction) {
        return distance == 1
                ? Component.translatable("nautec.catalyst.distance.one", directionName(direction))
                : Component.translatable("nautec.catalyst.distance", distance, directionName(direction));
    }

    public RecipeHolder<AquaticCatalystChannelingRecipe> getCurrentRecipe() {
        return currentRecipe;
    }

    public int getRemainingDuration() {
        return currentRecipe != null ? currentRecipe.value().duration() - duration : 0;
    }

    public ItemStack getProcessingItem() {
        return getItemStackHandler().getStackInSlot(0);
    }

    public int getDuration() {
        return duration;
    }

    @Override
    protected void onItemsChanged(int slot) {
        super.onItemsChanged(slot);

        this.nextRecipe = getRecipeForCache(getItemStackHandler().getStackInSlot(0));
        setStage();
    }

    @Override
    protected void onLaserDistancesChanged(Direction direction, int prevDistance) {
        super.onLaserDistancesChanged(direction, prevDistance);

        this.nextRecipe = getRecipeForCache(getItemStackHandler().getStackInSlot(0));
    }

    public void setStage() {
        float i = (float) getItemStackHandler().getStackInSlot(0).getCount() / getItemStackHandler().getSlotLimit(0);
        int stage = Mth.ceil(i * 8);
        level.setBlockAndUpdate(worldPosition, getBlockState()
                .setValue(AquaticCatalystBlock.STAGE, stage));
    }

    public RecipeHolder<AquaticCatalystChannelingRecipe> getRecipeForCache(ItemStack stack) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return null;
        }
        return serverLevel.recipeAccess()
                .getRecipeFor(AquaticCatalystChannelingRecipe.Type.INSTANCE, new SingleRecipeInput(stack), level)
                .orElse(null);
    }

    @Override
    public Set<Direction> getLaserInputs() {
        return Set.of();
    }

    @Override
    public Set<Direction> getLaserOutputs() {
        boolean emitting = level != null && level.isClientSide()
                ? getBlockState().getValue(AquaticCatalystBlock.ACTIVE)
                : currentRecipe != null && !paused;
        if (emitting) {
            return Set.of(getEmitterDirection());
        }
        return Collections.emptySet();
    }

    @Override
    public Set<Direction> getPotentialLaserOutputs() {
        return Set.of(getEmitterDirection());
    }

    @Override
    public <T> Map<Direction, Pair<IOActions, int[]>> getSidedInteractions(BlockCapability<T, @Nullable Direction> capability) {
        return Map.of();
    }

    @Override
    public SlotRoles itemRoles() {
        return ITEM_ROLES;
    }

    @Override
    protected void loadData(ValueInput in) {
        super.loadData(in);

        this.duration = in.getIntOr("duration", 0);
        this.currentRecipeId = in.getString("current_recipe").map(Identifier::parse).orElse(null);
        this.nextRecipeId = in.getString("next_recipe").map(Identifier::parse).orElse(null);
        this.syncedTransfer = in.getIntOr("transfer", 0);
    }

    @Override
    public void onLoad() {
        super.onLoad();

        this.currentRecipe = loadRecipe(currentRecipeId);
        this.nextRecipe = loadRecipe(nextRecipeId);
        if (level != null && !level.isClientSide()) {
            this.paused = level.hasNeighborSignal(worldPosition);
        }
    }

    private RecipeHolder<AquaticCatalystChannelingRecipe> loadRecipe(Identifier location) {
        if (location == null || !(level instanceof ServerLevel serverLevel)) {
            return null;
        }
        return (RecipeHolder<AquaticCatalystChannelingRecipe>) serverLevel.recipeAccess()
                .byKey(ResourceKey.create(Registries.RECIPE, location))
                .orElse(null);
    }

    @Override
    protected void saveData(ValueOutput out) {
        super.saveData(out);

        out.putInt("duration", duration);
        if (currentRecipe != null) {
            out.putString("current_recipe", currentRecipe.id().identifier().toString());
            out.putInt("transfer", transferAmount());
        }
        if (nextRecipe != null) out.putString("next_recipe", nextRecipe.id().identifier().toString());
    }
}
