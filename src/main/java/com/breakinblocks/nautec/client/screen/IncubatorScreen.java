package com.breakinblocks.nautec.client.screen;

import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.api.client.screen.NTMachineScreen;
import com.breakinblocks.nautec.api.menu.NTMachineMenu;
import com.breakinblocks.nautec.content.blockentities.IncubatorBlockEntity;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

import java.util.List;

public class IncubatorScreen extends NTMachineScreen<IncubatorBlockEntity> {
    public static final Identifier PROGRESS_ARROW = Nautec.rl("container/incubator/progress_arrow");
    public static final Identifier PROGRESS_ARROW_OFF = Nautec.rl("container/incubator/progress_arrow_off");

    public IncubatorScreen(NTMachineMenu<IncubatorBlockEntity> menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(guiGraphics, mouseX, mouseY, partialTick);
        int i = this.leftPos;
        int j = this.topPos;

        int progress = menu.blockEntity.getProgress();

        int j1 = (int) Math.ceil(((float) progress / Math.max(1, NTConfig.incubatorCraftingSpeed)) * 29f);

        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, PROGRESS_ARROW_OFF, i + 65, j + 18, 46, 29);
        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, PROGRESS_ARROW, 46, 29, 0, 29 - j1, i + 65, j + 47 - j1, 46, j1);
    }

    @Override
    public List<RecipeArea> recipeAreas() {
        return List.of(new RecipeArea(65, 18, 46, 29, false));
    }
}
