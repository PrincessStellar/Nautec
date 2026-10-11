package com.breakinblocks.nautec.client.renderer.blockentities;

import com.breakinblocks.nautec.api.client.renderer.blockentities.BERenderState;
import com.breakinblocks.nautec.api.client.renderer.blockentities.NTBERenderer;
import com.breakinblocks.nautec.content.biometank.BiomeTankBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Holder;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public class BiomeTankBERenderer extends NTBERenderer<BiomeTankBlockEntity, BiomeTankBERenderer.TankRenderState> {
    private static final float BLOCK_SCALE = 1.4F;
    private static final float ITEM_SCALE = 1.2F;

    public BiomeTankBERenderer(BlockEntityRendererProvider.Context ctx) {
        super(ctx);
    }

    @Override
    public TankRenderState createRenderState() {
        return new TankRenderState();
    }

    @Override
    public void extractRenderState(BiomeTankBlockEntity blockEntity, TankRenderState state, float partialTick, Vec3 cameraPos) {
        BERenderState.extractBase(blockEntity, state);
        Holder<Item> plant = blockEntity.getPlant();
        if (plant == null) {
            state.item.clear();
            return;
        }
        ItemStack stack = new ItemStack(plant);
        state.scale = stack.getItem() instanceof BlockItem ? BLOCK_SCALE : ITEM_SCALE;
        state.spin = blockEntity.getLevel() == null ? 0F : (blockEntity.getLevel().getGameTime() % 360L + partialTick) * 0.5F;
        state.item.update(stack, ItemDisplayContext.GROUND, blockEntity.getLevel(), 0);
    }

    @Override
    public void submit(TankRenderState state, PoseStack poseStack, MultiBufferSource buffers, Vec3 cameraPos) {
        if (state.item.isEmpty()) {
            return;
        }
        poseStack.pushPose();
        poseStack.translate(0.5F, 0.3F, 0.5F);
        poseStack.mulPose(Axis.YP.rotationDegrees(state.spin));
        poseStack.scale(state.scale, state.scale, state.scale);
        state.item.submit(poseStack, buffers, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
        poseStack.popPose();
    }

    public static class TankRenderState extends BERenderState {
        public final ItemStackRenderState item = new ItemStackRenderState();
        public float scale = ITEM_SCALE;
        public float spin;
    }
}
