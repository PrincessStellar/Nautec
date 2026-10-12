package com.breakinblocks.nautec.client.screen;

import com.breakinblocks.nautec.api.client.screen.NTGui;
import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.api.client.screen.NTMachineScreen;
import com.breakinblocks.nautec.api.menu.NTMachineMenu;
import com.breakinblocks.nautec.content.blockentities.MutatorBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;

import java.util.List;
import java.util.Locale;

public class MutatorScreen extends NTMachineScreen<MutatorBlockEntity> {
    public static final ResourceLocation PROGRESS_ARROW = Nautec.rl("container/mutator/progress_arrow");
    public static final ResourceLocation PROGRESS_ARROW_OFF = Nautec.rl("container/mutator/progress_arrow_off");
    private static final int BOOSTER_X = 99;
    private static final int BOOSTER_Y = 61;

    public MutatorScreen(NTMachineMenu<MutatorBlockEntity> menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        int i = this.leftPos;
        int j = this.topPos;

        int progress = menu.blockEntity.getProgress();

        int j1 = Mth.ceil(((float) progress / NTConfig.mutatorCraftingSpeed) * 62f);

        NTGui.blitSprite(guiGraphics, PROGRESS_ARROW_OFF, i + 56, j + 36, 62, 14);
        NTGui.blitSprite(guiGraphics, PROGRESS_ARROW, 62, 14, 0, 0, i + 56, j + 36, j1, 14);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        if (mouseX >= leftPos + BOOSTER_X && mouseX < leftPos + BOOSTER_X + 16 && mouseY >= topPos + BOOSTER_Y && mouseY < topPos + BOOSTER_Y + 16
                && menu.blockEntity.getItemStackHandler().getStackInSlot(MutatorBlockEntity.BOOSTER).isEmpty()) {
            guiGraphics.renderComponentTooltip(this.font, List.of(
                    Component.translatable("nautec.mutator.slot.booster"),
                    Component.translatable("nautec.mutator.slot.booster.desc", String.format(Locale.ROOT, "%.0f", NTConfig.mutatorBoosterMultiplier))
                            .withStyle(ChatFormatting.GRAY)), mouseX, mouseY);
        }
    }

    @Override
    public List<RecipeArea> recipeAreas() {
        return List.of(new RecipeArea(56, 36, 62, 14, false));
    }
}
