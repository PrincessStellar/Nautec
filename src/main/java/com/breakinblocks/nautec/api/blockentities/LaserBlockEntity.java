package com.breakinblocks.nautec.api.blockentities;

import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.utils.BeamOverclock;
import com.breakinblocks.nautec.content.recipes.ItemTransformationRecipe;
import com.breakinblocks.nautec.content.recipes.inputs.ItemTransformationRecipeInput;
import com.breakinblocks.nautec.utils.RecipeRevision;
import it.unimi.dsi.fastutil.floats.FloatCollection;
import it.unimi.dsi.fastutil.floats.FloatIterator;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.objects.Object2FloatArrayMap;
import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public abstract class LaserBlockEntity extends ContainerBlockEntity {
    protected final Object2IntMap<Direction> laserDistances;
    private static final int PROCESS_INTERVAL = 5;

    private final Object2ObjectMap<Direction, Map<ItemEntity, Transformation>> activeTransformations;
    private final RecipeRevision recipeRevision = new RecipeRevision();

    private int powerToTransfer;
    protected int power;

    private final Object2IntMap<Direction> powerPerSide;
    private final Object2FloatMap<Direction> purityPerSide;

    private float newPurity;
    protected float purity;

    private float clientLaserTime;

    private LongOpenHashSet beamTrail = new LongOpenHashSet();
    private LongOpenHashSet incomingTrail = new LongOpenHashSet();

    public LaserBlockEntity(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
        super(blockEntityType, blockPos, blockState);
        this.laserDistances = new Object2IntOpenHashMap<>();
        this.activeTransformations = new Object2ObjectArrayMap<>();
        this.powerPerSide = new Object2IntArrayMap<>();
        this.purityPerSide = new Object2FloatArrayMap<>();
        this.purity = 0;
    }

    public abstract Set<Direction> getLaserInputs();

    public abstract Set<Direction> getLaserOutputs();

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        super.preRemoveSideEffects(pos, state);
        drop();
    }

    public boolean shouldRender(Direction direction) {
        BlockPos pos = worldPosition.relative(direction, this.laserDistances.getInt(direction));
        return getLaserOutputs().contains(direction)
                && !pos.equals(worldPosition)
                && !beamTrail.contains(pos.asLong())
                && level.getBlockEntity(pos) instanceof LaserBlockEntity be
                && be.getLaserInputs().contains(direction.getOpposite())
                && (power > 0 || powerToTransfer > 0);
    }

    public Object2IntMap<Direction> getLaserDistances() {
        return laserDistances;
    }

    public int getMaxLaserDistance() {
        return NTConfig.laserDistance;
    }

    protected int checkConnectionsInterval() {
        return 10;
    }

    public void setPowerPerSide(Direction direction, int power) {
        this.powerPerSide.put(direction, power);
    }

    public int getPower() {
        return power;
    }

    public int getRequiredPower() {
        return 0;
    }

    public float beamSpeed() {
        return getRequiredPower() > 0 ? BeamOverclock.speed(getPower(), getRequiredPower()) : 0F;
    }

    public int getPowerToTransfer() {
        return powerToTransfer;
    }

    public int apSent() {
        int sent = 0;
        for (Direction direction : getLaserOutputs()) {
            if (this.laserDistances.getInt(direction) > 0 && !loopsBack(direction)) {
                sent += Math.max(0, outgoingPower(direction));
            }
        }
        return sent;
    }

    public float apSentPurity() {
        for (Direction direction : getLaserOutputs()) {
            if (this.laserDistances.getInt(direction) > 0 && !loopsBack(direction) && outgoingPower(direction) > 0) {
                return outgoingPurity(direction);
            }
        }
        return 0F;
    }

    public boolean receivesAp() {
        return !getLaserInputs().isEmpty();
    }

    public boolean sendsAp() {
        return !getPotentialLaserOutputs().isEmpty();
    }

    public int apStored() {
        return 0;
    }

    public int apCapacity() {
        return 0;
    }

    public void transmitPower(int amount) {
        this.powerToTransfer = amount;
    }

    protected int outgoingPower(Direction direction) {
        return this.powerToTransfer;
    }

    protected float outgoingPurity(Direction direction) {
        return this.purity;
    }

    protected int connectedOutputs() {
        int connected = 0;
        for (Direction direction : getLaserOutputs()) {
            if (this.laserDistances.getInt(direction) > 0 && !loopsBack(direction)) {
                connected++;
            }
        }
        return connected;
    }

    public boolean loopsBack(Direction direction) {
        int distance = this.laserDistances.getInt(direction);
        if (distance <= 0) {
            return false;
        }
        long target = BlockPos.offset(worldPosition.asLong(),
                direction.getStepX() * distance, direction.getStepY() * distance, direction.getStepZ() * distance);
        return beamTrail.contains(target);
    }

    protected void extendTrail(LaserBlockEntity upstream) {
        beamTrail.addAll(upstream.beamTrail);
    }

    public void receivePower(int amount, Direction direction, BlockPos originPos) {
        int prevAmount = this.powerPerSide.getInt(direction);
        setPowerPerSide(direction, amount);
        if (prevAmount != amount) {
            onPowerChanged();
        }
    }

    public void setPurityPerSide(Direction direction, float purity) {
        this.purityPerSide.put(direction, purity);
    }

    public float getPurity() {
        return purity;
    }

    public void setPurity(float amount) {
        this.newPurity = amount;
    }

    public void receiveNewPurity(float amount, Direction direction, BlockPos originPos) {
        setPurityPerSide(direction, amount);
    }

    @Override
    public void commonTick() {
        super.commonTick();

        if (level.isClientSide()) {
            if (clientLaserTime < getLaserAnimTimeDuration()) {
                this.clientLaserTime += 0.5f;
            } else {
                this.clientLaserTime = 0;
            }
        }

        if (level.getGameTime() % checkConnectionsInterval() == 0) {
            checkConnections();
        }

        boolean processing = !level.isClientSide()
                && (level.getGameTime() + Math.floorMod(worldPosition.hashCode(), PROCESS_INTERVAL)) % PROCESS_INTERVAL == 0;
        if (processing && level instanceof ServerLevel serverLevel && recipeRevision.changed(serverLevel)) {
            activeTransformations.clear();
        }
        for (Direction direction : getLaserOutputs()) {
            int distance = this.laserDistances.getInt(direction);
            if (distance > 0) {
                BlockPos targetPos = worldPosition.relative(direction, distance);
                if (beamTrail.contains(targetPos.asLong())) {
                    activeTransformations.remove(direction);
                    continue;
                }

                if (!level.isClientSide() && outgoingPower(direction) <= 0) {
                    activeTransformations.remove(direction);
                } else if (processing) {
                    processBeam(createLaserBeamAABB(direction, distance), direction);
                }

                if (level.isLoaded(targetPos) && level.getBlockEntity(targetPos) instanceof LaserBlockEntity laserBE) {
                    int amount = outgoingPower(direction);
                    laserBE.receivePower(amount, direction, worldPosition);
                    laserBE.receiveNewPurity(outgoingPurity(direction), direction, worldPosition);
                    if (amount > 0 && !laserBE.getLaserOutputs().isEmpty()) {
                        laserBE.incomingTrail.addAll(beamTrail);
                    }
                }
            }
        }

        int power = 0;
        IntIterator powers = this.powerPerSide.values().iterator();
        while (powers.hasNext()) {
            power += powers.nextInt();
        }
        this.power = power;

        this.purity = newPurity + mergedPurity(this.purityPerSide.values());

        this.powerPerSide.clear();
        this.purityPerSide.clear();

        LongOpenHashSet previousTrail = beamTrail;
        beamTrail = incomingTrail;
        beamTrail.add(worldPosition.asLong());
        incomingTrail = previousTrail;
        incomingTrail.clear();
    }

    public static float mergedPurity(FloatCollection purities) {
        if (purities.isEmpty()) {
            return 0;
        }
        float sum = 0;
        float highest = 0;
        FloatIterator iterator = purities.iterator();
        while (iterator.hasNext()) {
            float purity = iterator.nextFloat();
            sum += purity;
            highest = Math.max(highest, purity);
        }
        float average = sum / purities.size();
        return highest - (highest - average) * (float) NTConfig.beamMergePurityDrop;
    }

    public static float mergedPurity(long amountA, float purityA, long amountB, float purityB) {
        if (amountA <= 0) {
            return amountB <= 0 ? 0F : purityB;
        }
        if (amountB <= 0) {
            return purityA;
        }
        float highest = Math.max(purityA, purityB);
        float average = (float) ((amountA * (double) purityA + amountB * (double) purityB) / (amountA + amountB));
        return highest - (highest - average) * (float) NTConfig.beamMergePurityDrop;
    }

    private Optional<ItemTransformationRecipe> getCurrentRecipe(ItemStack itemStack, float beamPurity, int beamPower) {
        if (!(this.level instanceof ServerLevel serverLevel)) {
            return Optional.empty();
        }
        return ItemTransformationRecipe.findBest(serverLevel, new ItemTransformationRecipeInput(itemStack, beamPurity, beamPower));
    }

    private void spawnTransformationResult(ItemEntity cookingItem, ItemTransformationRecipe recipe) {
        ItemStack input = cookingItem.getItem();
        int perCraft = Math.max(1, recipe.ingredient().count());
        int crafts = input.getCount() / perCraft;
        ItemStack result = recipe.result();
        int remaining = result.getCount() * crafts;
        int maxStack = Math.max(1, result.getMaxStackSize());
        while (remaining > 0) {
            int count = Math.min(remaining, maxStack);
            ItemEntity resultEntity = new ItemEntity(level, cookingItem.getX(), cookingItem.getY(), cookingItem.getZ(), result.copyWithCount(count));
            level.addFreshEntity(resultEntity);
            remaining -= count;
        }

        int leftover = input.getCount() - crafts * perCraft;
        if (leftover > 0) {
            cookingItem.setItem(input.copyWithCount(leftover));
        } else {
            cookingItem.discard();
        }
    }

    private void processBeam(AABB box, Direction direction) {
        Map<ItemEntity, Transformation> tracked = activeTransformations.get(direction);
        float beamPurity = outgoingPurity(direction);
        int beamPower = outgoingPower(direction);
        for (Entity entity : level.getEntities((Entity) null, box, candidate -> candidate instanceof LivingEntity || candidate instanceof ItemEntity)) {
            if (entity instanceof LivingEntity livingEntity) {
                livingEntity.hurt(level.damageSources().inFire(), 3);
            } else if (entity instanceof ItemEntity itemEntity) {
                if (tracked == null) {
                    tracked = new IdentityHashMap<>();
                    activeTransformations.put(direction, tracked);
                }
                tracked.computeIfAbsent(itemEntity, ignored -> new Transformation());
            }
        }
        if (tracked == null) {
            return;
        }

        Iterator<Map.Entry<ItemEntity, Transformation>> iterator = tracked.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<ItemEntity, Transformation> entry = iterator.next();
            ItemEntity cookingItem = entry.getKey();
            Transformation transformation = entry.getValue();

            if (!cookingItem.isAlive() || !box.intersects(cookingItem.getBoundingBox())) {
                iterator.remove();
                continue;
            }

            ItemTransformationRecipe recipe = transformation.recipe(cookingItem.getItem(), beamPurity, beamPower);
            if (recipe == null) {
                continue;
            }
            if (!transformation.started) {
                transformation.started = true;
                continue;
            }
            if (transformation.progress >= recipe.duration()) {
                spawnTransformationResult(cookingItem, recipe);
                iterator.remove();
            } else {
                transformation.progress += PROCESS_INTERVAL;
                if (level instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(ParticleTypes.END_ROD, cookingItem.getX(), cookingItem.getY() + 0.25, cookingItem.getZ(), 6, 0.25, 0.25, 0.25, 0.01);
                }
            }
        }
        if (tracked.isEmpty()) {
            activeTransformations.remove(direction);
        }
    }

    private final class Transformation {
        private ItemStack key = ItemStack.EMPTY;
        private float purity = Float.NaN;
        private int power = -1;
        private @Nullable ItemTransformationRecipe recipe;
        private boolean started;
        private int progress;

        private @Nullable ItemTransformationRecipe recipe(ItemStack stack, float beamPurity, int beamPower) {
            if (purity != beamPurity || power != beamPower || !ItemStack.matches(key, stack)) {
                key = stack.copy();
                purity = beamPurity;
                power = beamPower;
                recipe = getCurrentRecipe(stack, beamPurity, beamPower).orElse(null);
            }
            return recipe;
        }
    }

    private @NotNull AABB createLaserBeamAABB(Direction direction, int distance) {
        BlockPos pos = worldPosition.relative(direction, distance);

        Vec3 start = worldPosition.relative(direction).getCenter();
        double v = 0.3;
        if (direction == Direction.UP || direction == Direction.DOWN) {
            start = start.subtract(v, 0, v);
        } else if (direction == Direction.NORTH || direction == Direction.SOUTH) {
            start = start.subtract(v, v, 0);
        } else if (direction == Direction.EAST || direction == Direction.WEST) {
            start = start.subtract(0, v, v);
        }

        Vec3 end = pos.getCenter().add(0.1, 0, 0.1);
        if (direction == Direction.UP || direction == Direction.DOWN) {
            Vec3 endPos = pos.below().getCenter();
            end = endPos.add(v, 0, v);
        } else if (direction == Direction.NORTH || direction == Direction.SOUTH) {
            end = end.add(v, v, 0);
        } else if (direction == Direction.EAST || direction == Direction.WEST) {
            end = end.add(0, v, v);
        }

        return new AABB(start, end);
    }

    public Set<Direction> getPotentialLaserOutputs() {
        return getLaserOutputs();
    }

    public BeamScan scanBeam(Direction direction) {
        int maxLaserDistance = loadedReach(direction, getMaxLaserDistance());
        if (maxLaserDistance <= 0) {
            return new BeamScan(direction, BeamScan.Status.NO_TARGET, 0);
        }
        Vec3 from = worldPosition.relative(direction).getCenter();
        Vec3 to = worldPosition.relative(direction, maxLaserDistance).getCenter();
        BlockHitResult blockHitResult = level.clip(new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, CollisionContext.empty()));
        Vec3i diffVec3 = blockHitResult.getBlockPos().subtract(worldPosition);
        int hitDistance = Math.min(maxLaserDistance, Math.abs(diffVec3.getX() + diffVec3.getY() + diffVec3.getZ()));
        int wrongSideDistance = 0;
        for (int i = 1; i <= hitDistance; i++) {
            if (level.getBlockEntity(worldPosition.relative(direction, i)) instanceof LaserBlockEntity laserBlockEntity) {
                if (laserBlockEntity.getLaserInputs().contains(direction.getOpposite())) {
                    return new BeamScan(direction, BeamScan.Status.CONNECTED, i);
                }
                if (wrongSideDistance == 0) {
                    wrongSideDistance = i;
                }
            }
        }
        if (wrongSideDistance > 0) {
            return new BeamScan(direction, BeamScan.Status.WRONG_SIDE, wrongSideDistance);
        }
        if (blockHitResult.getType() == HitResult.Type.BLOCK) {
            return new BeamScan(direction, BeamScan.Status.BLOCKED, hitDistance);
        }
        return new BeamScan(direction, BeamScan.Status.NO_TARGET, 0);
    }

    protected int loadedReach(Direction direction, int maxDistance) {
        for (int i = 1; i <= maxDistance; i++) {
            if (!level.isLoaded(worldPosition.relative(direction, i))) {
                return i - 1;
            }
        }
        return maxDistance;
    }

    protected void checkConnections() {
        for (Direction direction : getLaserOutputs()) {
            int newDistance = scanBeam(direction).connectedDistance();
            int prevDistance = this.laserDistances.getInt(direction);
            if (prevDistance != newDistance) {
                this.laserDistances.put(direction, newDistance);
                onLaserDistancesChanged(direction, prevDistance);
            }
        }
    }

    protected void onLaserDistancesChanged(Direction direction, int prevDistance) {

    }

    public int getLaserAnimTimeDuration() {
        return 80;
    }

    public float getClientLaserTime() {
        return clientLaserTime;
    }

    public float getLaserScale(float partialTick) {
        return (this.clientLaserTime + partialTick) / (float) this.getLaserAnimTimeDuration();
    }
}
