package com.breakinblocks.nautec.client.renderer.blockentities;

import com.breakinblocks.nautec.content.biometank.BiomeTankBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Holder;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class BiomeTankBERenderer implements BlockEntityRenderer<BiomeTankBlockEntity, BiomeTankBERenderer.TankRenderState> {
    private static final float BLOCK_SCALE = 1.4F;
    private static final float ITEM_SCALE = 1.2F;

    private final ItemModelResolver itemModelResolver;

    public BiomeTankBERenderer(BlockEntityRendererProvider.Context ctx) {
        this.itemModelResolver = ctx.itemModelResolver();
    }

    @Override
    public TankRenderState createRenderState() {
        return new TankRenderState();
    }

    @Override
    public void extractRenderState(BiomeTankBlockEntity blockEntity, TankRenderState state, float partialTick, Vec3 cameraPos,
                                   ModelFeatureRenderer.@Nullable CrumblingOverlay crumbling) {
        BlockEntityRenderState.extractBase(blockEntity, state, crumbling);
        Holder<Item> plant = blockEntity.getPlant();
        if (plant == null) {
            state.item.clear();
            return;
        }
        ItemStack stack = new ItemStack(plant);
        state.scale = stack.getItem() instanceof BlockItem ? BLOCK_SCALE : ITEM_SCALE;
        state.spin = blockEntity.getLevel() == null ? 0F : (blockEntity.getLevel().getGameTime() % 360L + partialTick) * 0.5F;
        this.itemModelResolver.updateForTopItem(state.item, stack, ItemDisplayContext.GROUND, blockEntity.getLevel(), null, 0);
    }

    @Override
    public void submit(TankRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        if (state.item.isEmpty()) {
            return;
        }
        poseStack.pushPose();
        poseStack.translate(0.5F, 0.3F, 0.5F);
        poseStack.mulPose(Axis.YP.rotationDegrees(state.spin));
        poseStack.scale(state.scale, state.scale, state.scale);
        state.item.submit(poseStack, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
        poseStack.popPose();
    }

    public static class TankRenderState extends BlockEntityRenderState {
        public final ItemStackRenderState item = new ItemStackRenderState();
        public float scale = ITEM_SCALE;
        public float spin;
    }
}
