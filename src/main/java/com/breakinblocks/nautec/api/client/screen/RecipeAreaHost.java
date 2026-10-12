package com.breakinblocks.nautec.api.client.screen;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;

import java.util.ArrayList;
import java.util.List;

public interface RecipeAreaHost {
    List<RecipeArea> recipeAreas();

    ItemStack recipeCatalyst();

    record RecipeArea(int x, int y, int width, int height, boolean ownTooltip) {
    }

    static List<Component> withRecipeHint(List<Component> lines) {
        if (!ModList.get().isLoaded("jei")) {
            return lines;
        }
        List<Component> hinted = new ArrayList<>(lines);
        hinted.add(Component.translatable("nautec.jei.show_recipes").withStyle(ChatFormatting.DARK_GRAY));
        return hinted;
    }
}
