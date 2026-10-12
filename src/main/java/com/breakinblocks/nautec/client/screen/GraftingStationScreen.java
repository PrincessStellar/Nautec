package com.breakinblocks.nautec.client.screen;

import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.api.client.screen.NTMachineScreen;
import com.breakinblocks.nautec.api.menu.NTMachineMenu;
import com.breakinblocks.nautec.content.blockentities.GraftingStationBlockEntity;
import com.breakinblocks.nautec.content.blocks.GraftingStationBlock;
import com.breakinblocks.nautec.content.menus.GraftingStationMenu;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;

import com.breakinblocks.nautec.api.client.screen.RecipeAreaHost;

import java.util.List;

public class GraftingStationScreen extends NTMachineScreen<GraftingStationBlockEntity> {
    public static final Identifier PROGRESS_ARROW = Nautec.rl("container/bacterial_analyzer/progress_arrow");
    public static final Identifier PROGRESS_ARROW_OFF = Nautec.rl("container/bacterial_analyzer/progress_arrow_off");

    public GraftingStationScreen(NTMachineMenu<GraftingStationBlockEntity> menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    private GraftingStationMenu station() {
        return (GraftingStationMenu) this.menu;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(guiGraphics, mouseX, mouseY, partialTick);
        int width = Mth.ceil((float) station().getProgress() / station().getDuration() * 24.0F);
        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, PROGRESS_ARROW_OFF, leftPos + 76, topPos + 25, 24, 24);
        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, PROGRESS_ARROW, 24, 24, 0, 0, leftPos + 76, topPos + 25, width, 24);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(guiGraphics, mouseX, mouseY, partialTick);
        if (isHovering(76, 36, 24, 12, mouseX, mouseY)) {
            int status = station().getStatus();
            boolean running = status == GraftingStationBlockEntity.STATUS_RUNNING;
            guiGraphics.setComponentTooltipForNextFrame(this.font, RecipeAreaHost.withRecipeHint(List.of(
                    Component.translatable(GraftingStationBlock.statusKey(status)).withStyle(running ? ChatFormatting.AQUA : ChatFormatting.RED),
                    Component.translatable("nautec.grafting_station.requirements", station().getRequiredPower(),
                            String.format("%.1f", NTConfig.graftingStationPurity), NTConfig.graftingStationSaltWaterUsage).withStyle(ChatFormatting.GRAY)
            )), mouseX, mouseY);
        }
    }

    @Override
    public List<RecipeArea> recipeAreas() {
        return List.of(new RecipeArea(76, 36, 24, 12, true));
    }
}
