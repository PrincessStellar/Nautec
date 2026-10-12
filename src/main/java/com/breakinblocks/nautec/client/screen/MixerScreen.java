package com.breakinblocks.nautec.client.screen;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import com.breakinblocks.nautec.api.client.screen.NTMachineScreen;
import com.breakinblocks.nautec.api.menu.NTMachineMenu;
import com.breakinblocks.nautec.content.blockentities.MixerBlockEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import com.breakinblocks.nautec.api.client.screen.RecipeAreaHost;

import java.util.List;

public class MixerScreen extends NTMachineScreen<MixerBlockEntity> {
    private static final int BAR_X = 78;
    private static final int BAR_Y = 38;
    private static final int BAR_WIDTH = 4;
    private static final int BAR_HEIGHT = 23;

    public MixerScreen(NTMachineMenu<MixerBlockEntity> menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    private float progress() {
        MixerBlockEntity mixer = menu.blockEntity;
        int max = mixer.getMaxDuration();
        return max > 0 ? Math.min(1F, mixer.getDuration() / (float) max) : 0F;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(guiGraphics, mouseX, mouseY, partialTick);
        PanelStyle.icon(guiGraphics, PanelStyle.ICON_WHISK, leftPos + 66, topPos + 37, 8, 25);
        PanelStyle.bar(guiGraphics, leftPos + BAR_X, topPos + BAR_Y, BAR_WIDTH, BAR_HEIGHT, progress(), PanelStyle.ONLINE, PanelStyle.READOUT);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(guiGraphics, mouseX, mouseY, partialTick);
        if (isHovering(BAR_X - 1, BAR_Y - 1, BAR_WIDTH + 2, BAR_HEIGHT + 2, mouseX, mouseY)) {
            Component line = menu.blockEntity.getMaxDuration() > 0
                    ? Component.translatable("nautec.mixer.progress", Math.round(progress() * 100))
                    : Component.translatable("nautec.mixer.idle");
            guiGraphics.setComponentTooltipForNextFrame(font, RecipeAreaHost.withRecipeHint(List.of(line)), mouseX, mouseY);
        }
    }

    @Override
    public List<RecipeArea> recipeAreas() {
        return List.of(new RecipeArea(BAR_X - 1, BAR_Y - 1, BAR_WIDTH + 2, BAR_HEIGHT + 2, true));
    }
}
