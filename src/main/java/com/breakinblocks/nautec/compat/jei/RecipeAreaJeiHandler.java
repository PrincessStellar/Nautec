package com.breakinblocks.nautec.compat.jei;

import com.breakinblocks.nautec.api.client.screen.RecipeAreaHost;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.handlers.IGuiClickableArea;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import mezz.jei.api.recipe.IFocusFactory;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.runtime.IRecipesGui;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class RecipeAreaJeiHandler implements IGuiContainerHandler<AbstractContainerScreen<?>> {
    @Override
    public Collection<IGuiClickableArea> getGuiClickableAreas(AbstractContainerScreen<?> screen, double guiMouseX, double guiMouseY) {
        if (!(screen instanceof RecipeAreaHost host)) {
            return List.of();
        }
        List<RecipeAreaHost.RecipeArea> areas = host.recipeAreas();
        if (areas.isEmpty()) {
            return List.of();
        }
        ItemStack catalyst = host.recipeCatalyst();
        if (catalyst.isEmpty()) {
            return List.of();
        }
        List<IGuiClickableArea> clickable = new ArrayList<>(areas.size());
        for (RecipeAreaHost.RecipeArea area : areas) {
            clickable.add(new IGuiClickableArea() {
                @Override
                public Rect2i getArea() {
                    return new Rect2i(area.x(), area.y(), area.width(), area.height());
                }

                @Override
                public boolean isTooltipEnabled() {
                    return !area.ownTooltip();
                }

                @Override
                public void onClick(IFocusFactory focusFactory, IRecipesGui recipesGui) {
                    recipesGui.show(focusFactory.createFocus(RecipeIngredientRole.CRAFTING_STATION, VanillaTypes.ITEM_STACK, catalyst));
                }
            });
        }
        return clickable;
    }
}
