package com.breakinblocks.nautec.client.renderer.blockentities;

import com.breakinblocks.nautec.api.client.renderer.blockentities.LaserBlockEntityRenderer;
import com.breakinblocks.nautec.api.client.renderer.blockentities.LaserRenderState;
import com.breakinblocks.nautec.client.render.LaserBeamRenderer;
import com.breakinblocks.nautec.client.render.PrismarineCrystalRenderer;
import com.breakinblocks.nautec.content.resonance.SatelliteArrayBlockEntity;
import com.breakinblocks.nautec.registries.NTItems;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import com.breakinblocks.nautec.utils.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

public class SatelliteArrayBERenderer extends LaserBlockEntityRenderer<SatelliteArrayBlockEntity, SatelliteArrayBERenderer.ArrayRenderState> {
    public static final float FOCUS = 1.95F;
    public static final float ORBIT_HEIGHT = 40F;
    public static final float ORBIT_RADIUS = 4F;
    private static final float ORBIT_SPEED = 0.004F;
    private static final float CRYSTAL_SCALE = 0.06F;
    private static final float SATELLITE_SCALE = 3F;
    private static final int UPLINK_COLOR = 0x86E7F3;
    private static final int FULL_BRIGHT = 15728880;
    private @Nullable ItemStack satelliteStack;

    public SatelliteArrayBERenderer(BlockEntityRendererProvider.Context ctx) {
        super(ctx);
    }

    @Override
    public ArrayRenderState createRenderState() {
        return new ArrayRenderState();
    }

    public static Vector3f orbit(float ticks, long seed) {
        float angle = ticks * ORBIT_SPEED + (seed & 0xFFFF) / 65535F * Mth.TWO_PI;
        return new Vector3f(0.5F + ORBIT_RADIUS * Mth.cos(angle), FOCUS + ORBIT_HEIGHT, 0.5F + ORBIT_RADIUS * Mth.sin(angle));
    }

    @Override
    public void extractRenderState(SatelliteArrayBlockEntity array, ArrayRenderState state, float partialTick, Vec3 cameraPos) {
        super.extractRenderState(array, state, partialTick, cameraPos);
        state.online = array.getStatus() == SatelliteArrayBlockEntity.STATUS_ONLINE;
        state.active = state.online && array.getRelay() > 0;
        state.satellite = array.hasSatellite();
        long gameTime = array.getLevel() == null ? 0L : array.getLevel().getGameTime();
        state.ticks = (float) (gameTime % PrismarineCrystalRenderer.TICK_WRAP) + partialTick;
        state.seed = PrismarineCrystalRenderer.seed(array.getBlockPos());
        state.launch = array.getLaunchedAt() == Long.MIN_VALUE ? 1F
                : Mth.clamp((gameTime - array.getLaunchedAt() + partialTick) / SatelliteArrayBlockEntity.LAUNCH_TICKS, 0F, 1F);
        if (state.satellite) {
            state.item.update(satelliteStack(), ItemDisplayContext.FIXED,
                    array.getLevel(), 0);
        } else {
            state.item.clear();
        }
    }

    @Override
    public void submit(ArrayRenderState state, PoseStack poseStack, MultiBufferSource buffers, Vec3 cameraPos) {
        super.submit(state, poseStack, buffers, cameraPos);
        float pulse = 0.5F + 0.5F * Mth.sin(state.ticks * 0.2F);
        float hover = FOCUS + Mth.sin(state.ticks * 0.06F) * 0.03F;
        poseStack.pushPose();
        poseStack.translate(0.5F, hover, 0.5F);
        poseStack.scale(CRYSTAL_SCALE, CRYSTAL_SCALE, CRYSTAL_SCALE);
        PrismarineCrystalRenderer.submit(poseStack, buffers, state.ticks * (state.active ? 3F : state.online ? 1.2F : 0.5F), state.seed,
                state.active ? 0.45F : state.online ? 0.1F : 0F, true);
        poseStack.popPose();

        Vector3f focus = new Vector3f(0.5F, hover, 0.5F);
        if (!state.satellite) {
            return;
        }
        Vector3f satellite = satellitePosition(state, focus);
        submitSatellite(state, poseStack, buffers, satellite);
        if (state.active && state.launch >= 1F) {
            int beam = ARGB.color(Math.round(150F + 90F * pulse), UPLINK_COLOR);
            LaserBeamRenderer.submitBeam(poseStack, buffers, focus, satellite, 0.1F, beam, true);
            LaserBeamRenderer.submitFlare(poseStack, buffers, satellite, 0.5F + 0.2F * pulse, beam, true);
            LaserBeamRenderer.submitFlare(poseStack, buffers, focus, 0.2F, beam, true);
        }
    }

    private ItemStack satelliteStack() {
        if (satelliteStack == null) {
            satelliteStack = new ItemStack(NTItems.PRISM_SATELLITE.get());
        }
        return satelliteStack;
    }

    private static Vector3f satellitePosition(ArrayRenderState state, Vector3f focus) {
        if (state.launch >= 1F) {
            return orbit(state.ticks, state.seed);
        }
        float eased = state.launch * state.launch * (3F - 2F * state.launch);
        Vector3f target = orbit(state.ticks, state.seed);
        Vector3f rise = new Vector3f(focus.x, focus.y + 0.6F + (target.y - focus.y) * state.launch * state.launch, focus.z);
        return rise.lerp(target, eased * eased);
    }

    private static void submitSatellite(ArrayRenderState state, PoseStack poseStack, MultiBufferSource buffers, Vector3f at) {
        if (state.item.isEmpty()) {
            return;
        }
        float scale = Mth.lerp(Math.min(1F, state.launch * 3F), 0.6F, SATELLITE_SCALE);
        poseStack.pushPose();
        poseStack.translate(at.x, at.y, at.z);
        poseStack.mulPose(Axis.YP.rotation(-(state.ticks * ORBIT_SPEED)));
        poseStack.mulPose(Axis.ZP.rotationDegrees(Mth.sin(state.ticks * 0.02F) * 6F));
        poseStack.scale(scale, scale, scale);
        state.item.submit(poseStack, buffers, FULL_BRIGHT, OverlayTexture.NO_OVERLAY, 0);
        poseStack.popPose();
    }

    @Override
    public @NotNull AABB getRenderBoundingBox(SatelliteArrayBlockEntity array) {
        return super.getRenderBoundingBox(array)
                .minmax(new AABB(array.getBlockPos()).expandTowards(0, FOCUS + ORBIT_HEIGHT + 3, 0).inflate(ORBIT_RADIUS + 3, 0, ORBIT_RADIUS + 3));
    }

    @Override
    public boolean shouldRenderOffScreen(SatelliteArrayBlockEntity blockEntity) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 160;
    }

    public static class ArrayRenderState extends LaserRenderState {
        public final ItemStackRenderState item = new ItemStackRenderState();
        public boolean online;
        public boolean active;
        public boolean satellite;
        public float launch;
        public float ticks;
        public long seed;
    }
}
