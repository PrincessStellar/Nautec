package com.breakinblocks.nautec.client.screen;

import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.api.client.screen.NTMachineScreen;
import com.breakinblocks.nautec.api.menu.NTMachineMenu;
import com.breakinblocks.nautec.content.blockentities.LaserCraftingMatrixBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

import com.breakinblocks.nautec.api.client.screen.RecipeAreaHost;

import java.util.List;
import java.util.Locale;

public class LaserCraftingMatrixScreen extends NTMachineScreen<LaserCraftingMatrixBlockEntity> {
    private static final Identifier EMPTY_ARROW = Nautec.rl("container/furnace/empty_arrow");
    private static final Identifier BURN_PROGRESS = Identifier.withDefaultNamespace("container/furnace/burn_progress");
    private static final int ARROW_X = 76;
    private static final int ARROW_Y = 35;
    private static final int CENTRE_X = 88;
    private static final int MET = 0xFF2E7D4F;
    private static final int UNMET = 0xFF8C2F2F;

    public LaserCraftingMatrixScreen(NTMachineMenu<LaserCraftingMatrixBlockEntity> menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(guiGraphics, mouseX, mouseY, partialTick);
        LaserCraftingMatrixBlockEntity matrix = menu.blockEntity;
        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, EMPTY_ARROW, leftPos + ARROW_X, topPos + ARROW_Y, 24, 16);
        int max = matrix.getMaxProgress();
        if (max > 0 && matrix.getProgress() > 0) {
            int width = Math.max(1, Math.round(24F * matrix.getProgress() / max));
            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, BURN_PROGRESS, 24, 16, 0, 0, leftPos + ARROW_X, topPos + ARROW_Y, width, 16);
        }
        if (matrix.hasRecipe()) {
            String power = matrix.getRequiredPower() + " AP";
            String purity = String.format(Locale.ROOT, "%.1f P", matrix.getRecipePurity());
            int powerColor = matrix.getPower() >= matrix.getRequiredPower() && matrix.getPower() > 0 ? MET : UNMET;
            int purityColor = matrix.getPurity() >= matrix.getRecipePurity() ? MET : UNMET;
            guiGraphics.text(font, power, leftPos + CENTRE_X - font.width(power) / 2, topPos + 22, powerColor, false);
            guiGraphics.text(font, purity, leftPos + CENTRE_X - font.width(purity) / 2, topPos + 56, purityColor, false);
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(guiGraphics, mouseX, mouseY, partialTick);
        if (isHovering(ARROW_X - 10, 20, 44, 46, mouseX, mouseY)) {
            guiGraphics.setComponentTooltipForNextFrame(font, RecipeAreaHost.withRecipeHint(statusLines(menu.blockEntity)), mouseX, mouseY);
        }
    }

    private static List<Component> statusLines(LaserCraftingMatrixBlockEntity matrix) {
        if (!matrix.hasRecipe()) {
            return List.of(Component.translatable("nautec.laser_crafting_matrix.no_recipe").withStyle(ChatFormatting.GRAY));
        }
        return List.of(
                Component.translatable("nautec.laser_crafting_matrix.progress", matrix.getProgress(), matrix.getMaxProgress()),
                Component.translatable("nautec.laser_crafting_matrix.power", matrix.getPower(), matrix.getRequiredPower())
                        .withStyle(matrix.getPower() >= matrix.getRequiredPower() && matrix.getPower() > 0 ? ChatFormatting.GREEN : ChatFormatting.RED),
                Component.translatable("nautec.laser_crafting_matrix.purity",
                                String.format(Locale.ROOT, "%.2f", matrix.getPurity()), String.format(Locale.ROOT, "%.2f", matrix.getRecipePurity()))
                        .withStyle(matrix.getPurity() >= matrix.getRecipePurity() ? ChatFormatting.GREEN : ChatFormatting.RED),
                Component.translatable("nautec.laser_crafting_matrix.top_only").withStyle(ChatFormatting.DARK_GRAY));
    }

    @Override
    public List<RecipeArea> recipeAreas() {
        return List.of(new RecipeArea(ARROW_X, ARROW_Y, 24, 16, true));
    }
}
