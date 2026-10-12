package com.breakinblocks.nautec.api.client.screen;

import com.breakinblocks.nautec.client.screen.PanelStyle;
import com.breakinblocks.nautec.client.screen.GhostSlots;
import org.jetbrains.annotations.Nullable;
import net.minecraft.client.renderer.Rect2i;
import com.breakinblocks.nautec.client.screen.SideConfigPanel;
import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.api.bacteria.Bacteria;
import com.breakinblocks.nautec.api.bacteria.BacteriaInstance;
import com.breakinblocks.nautec.api.blockentities.ContainerBlockEntity;
import com.breakinblocks.nautec.api.menu.NTMachineMenu;
import com.breakinblocks.nautec.api.menu.slots.SlotBacteriaStorage;
import com.breakinblocks.nautec.api.menu.slots.SlotFluidHandler;
import com.breakinblocks.nautec.network.BacteriaSlotClickedPayload;
import com.breakinblocks.nautec.registries.NTItems;
import com.breakinblocks.nautec.utils.BacteriaHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import java.text.NumberFormat;
import java.util.List;

public abstract class NTMachineScreen<T extends ContainerBlockEntity> extends AbstractContainerScreen<NTMachineMenu<T>> implements SideConfigHost, RecipeAreaHost {
    private static final Identifier BACTERIA_OVERLAY_TEXTURE = Nautec.rl("textures/item/petri_dish_overlay.png");
    private static final Identifier DISH_TEXTURE = Nautec.rl("textures/item/petri_dish.png");
    private static final int DISH_GHOST = 0x66FFFFFF;

    private SlotFluidHandler hoveredFluidHandlerSlot;
    private @Nullable SideConfigPanel sidePanel;
    private SlotBacteriaStorage hoveredBacteriaStorageSlot;

    private final NumberFormat nf = NumberFormat.getIntegerInstance();

    public NTMachineScreen(NTMachineMenu<T> menu, Inventory playerInventory, Component title) {
        this(menu, playerInventory, title, 176, 174);
    }

    public NTMachineScreen(NTMachineMenu<T> menu, Inventory playerInventory, Component title, int imageWidth, int imageHeight) {
        super(menu, playerInventory, title, imageWidth, imageHeight);

        this.titleLabelY = 6;
    }

    @Override
    public List<RecipeArea> recipeAreas() {
        return List.of();
    }

    @Override
    public ItemStack recipeCatalyst() {
        return new ItemStack(this.menu.blockEntity.getBlockState().getBlock());
    }

    @Override
    protected void init() {
        super.init();
        if (sidePanel == null) {
            sidePanel = SideConfigPanel.create(menu.blockEntity, menu.containerId);
        }
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(guiGraphics, mouseX, mouseY, partialTick);
        Identifier texture = getBackgroundTexture();
        if (texture != null) {
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, texture, leftPos, topPos, 0F, 0F, imageWidth, imageHeight, 256, 256);
        } else {
            extractPanel(guiGraphics);
        }
        extractDishPort(guiGraphics);
    }

    protected void extractPanel(GuiGraphicsExtractor guiGraphics) {
        PanelStyle.panel(guiGraphics, leftPos, topPos, imageWidth, imageHeight);
        for (Slot slot : this.menu.slots) {
            if (slot.isActive()) {
                PanelStyle.slot(guiGraphics, leftPos + slot.x, topPos + slot.y);
            }
        }
        for (SlotBacteriaStorage slot : this.menu.getBacteriaStorageSlots()) {
            PanelStyle.bacteriaSlot(guiGraphics, leftPos + slot.getX(), topPos + slot.getY());
        }
        for (SlotFluidHandler slot : this.menu.getFluidTankSlots()) {
            PanelStyle.tank(guiGraphics, leftPos + slot.getX(), topPos + slot.getY(), slot.getWidth(), slot.getHeight());
        }
    }

    protected void extractSlotHint(GuiGraphicsExtractor guiGraphics, @Nullable Slot slot, Identifier icon) {
        if (slot != null && !slot.hasItem()) {
            PanelStyle.icon(guiGraphics, icon, leftPos + slot.x, topPos + slot.y, 16, 16);
        }
    }

    protected void extractDishPort(GuiGraphicsExtractor guiGraphics) {
        Slot in = this.menu.getDishIn();
        Slot out = this.menu.getDishOut();
        Slot emptyOut = this.menu.getDishEmptyOut();
        if (in == null || out == null) {
            return;
        }
        if (getBackgroundTexture() != null) {
            extractSlotFrame(guiGraphics, in.x, in.y);
            extractSlotFrame(guiGraphics, out.x, out.y);
            if (emptyOut != null) {
                extractSlotFrame(guiGraphics, emptyOut.x, emptyOut.y);
            }
        }
        if (!in.hasItem()) {
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, DISH_TEXTURE, leftPos + in.x, topPos + in.y, 0F, 0F, 16, 16, 16, 16, DISH_GHOST);
        }
    }

    protected void extractSlotFrame(GuiGraphicsExtractor guiGraphics, int slotX, int slotY) {
        PanelStyle.slot(guiGraphics, leftPos + slotX, topPos + slotY);
    }

    private void dishPortTooltip(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
        Slot in = this.menu.getDishIn();
        Slot out = this.menu.getDishOut();
        Slot emptyOut = this.menu.getDishEmptyOut();
        if (in == null || out == null) {
            return;
        }
        if (!in.hasItem() && isHovering(in.x, in.y, 16, 16, mouseX, mouseY)) {
            guiGraphics.setComponentTooltipForNextFrame(this.font, List.of(
                    Component.translatable("nautec.dish_port.title"),
                    Component.translatable("nautec.dish_port.load").withStyle(ChatFormatting.GRAY),
                    Component.translatable("nautec.dish_port.unload").withStyle(ChatFormatting.GRAY),
                    Component.translatable("nautec.dish_port.ready").withStyle(ChatFormatting.GRAY),
                    Component.translatable("nautec.dish_port.automation").withStyle(ChatFormatting.DARK_GRAY)
            ), mouseX, mouseY);
        } else if (!out.hasItem() && isHovering(out.x, out.y, 16, 16, mouseX, mouseY)) {
            guiGraphics.setComponentTooltipForNextFrame(this.font, List.of(
                    Component.translatable("nautec.dish_port.title"),
                    Component.translatable("nautec.dish_port.colony_out").withStyle(ChatFormatting.GRAY)
            ), mouseX, mouseY);
        } else if (emptyOut != null && !emptyOut.hasItem() && isHovering(emptyOut.x, emptyOut.y, 16, 16, mouseX, mouseY)) {
            guiGraphics.setComponentTooltipForNextFrame(this.font, List.of(
                    Component.translatable("nautec.dish_port.title"),
                    Component.translatable("nautec.dish_port.empty_out").withStyle(ChatFormatting.GRAY)
            ), mouseX, mouseY);
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(guiGraphics, mouseX, mouseY, partialTick);

        hoverFluidSlot(mouseX, mouseY);
        hoverBacteriaSlot(mouseX, mouseY);
        dishPortTooltip(guiGraphics, mouseX, mouseY);

        Font font = minecraft.font;
        SlotBacteriaStorage slot = this.hoveredBacteriaStorageSlot;
        if (slot != null) {
            BacteriaInstance bacteria = slot.getBacteriaStorage().getBacteria(slot.getSlot());
            if (!bacteria.isEmpty()) {
                List<Component> tooltip = bacteria.getTooltip();
                guiGraphics.setComponentTooltipForNextFrame(font, tooltip, mouseX, mouseY);
            }
            int color = ARGB.color(20, 30, 30, 30);
            guiGraphics.fillGradient(
                    this.leftPos + slot.getX() + 2,
                    this.topPos + slot.getY() + 2,
                    this.leftPos + slot.getX() + slot.getWidth() - 2,
                    this.topPos + slot.getY() + slot.getHeight() - 2, color,
                    color
            );
        }

        if (this.hoveredFluidHandlerSlot != null) {
            FluidStack fluid = this.hoveredFluidHandlerSlot.getFluidStack();
            guiGraphics.setComponentTooltipForNextFrame(font, List.of(
                    fluid.getHoverName(),
                    Component.translatable("nautec.tooltip.liquid.amount_with_capacity",
                            nf.format(fluid.getAmount()),
                            nf.format(this.hoveredFluidHandlerSlot.getFluidCapacity())
                    ).withStyle(ChatFormatting.GRAY)
            ), mouseX, mouseY);
        }

        for (SlotBacteriaStorage bSlot : this.menu.getBacteriaStorageSlots()) {
            renderBacteria(guiGraphics, bSlot.getBacteriaInstance(), this.leftPos + bSlot.getX(), this.topPos + bSlot.getY());
        }

        for (SlotFluidHandler fSlot : this.menu.getFluidTankSlots()) {
            fSlot.getRenderer().render(guiGraphics, this.leftPos + fSlot.getX(), this.topPos + fSlot.getY(), fSlot.getFluidStack());
            PanelStyle.tankTicks(guiGraphics, this.leftPos + fSlot.getX(), this.topPos + fSlot.getY(), fSlot.getWidth(), fSlot.getHeight());
        }

        GhostSlots.extract(guiGraphics, this.menu, this.menu.blockEntity, this.leftPos, this.topPos);
        GhostSlots.tooltip(guiGraphics, font, this.menu.blockEntity, this.hoveredSlot, mouseX, mouseY);

        if (sidePanel != null) {
            sidePanel.extract(guiGraphics, font, sideAnchorX(), sideAnchorY(), mouseX, mouseY);
        }
    }

    private int sideAnchorX() {
        return this.leftPos + this.imageWidth + 2;
    }

    private int sideAnchorY() {
        return this.topPos + 4;
    }

    @Override
    public @Nullable Rect2i sideConfigArea() {
        return sidePanel == null ? null : sidePanel.area(sideAnchorX(), sideAnchorY());
    }

    @Override
    protected boolean hasClickedOutside(double mouseX, double mouseY, int left, int top) {
        if (sidePanel != null && sidePanel.contains(mouseX, mouseY, sideAnchorX(), sideAnchorY())) {
            return false;
        }
        return super.hasClickedOutside(mouseX, mouseY, left, top);
    }

    private void renderBacteria(GuiGraphicsExtractor guiGraphics, BacteriaInstance instance, int x, int y) {
        if (!instance.isEmpty()) {
            Bacteria bacteria = BacteriaHelper.getBacteria(Minecraft.getInstance().level.registryAccess(), instance.getBacteria());
            int color = bacteria.stats().color();
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, BACTERIA_OVERLAY_TEXTURE, x + 1, y, 0F, 0F, 16, 16, 16, 16, color);
        }
    }

    private void hoverFluidSlot(int pMouseX, int pMouseY) {
        for (SlotFluidHandler fSlot : menu.getFluidTankSlots()) {
            if (isHovering(fSlot.getX(), fSlot.getY(), fSlot.getWidth(), fSlot.getHeight(), pMouseX, pMouseY)) {
                this.hoveredFluidHandlerSlot = fSlot;
                return;
            }
        }
        this.hoveredFluidHandlerSlot = null;
    }

    private void hoverBacteriaSlot(int pMouseX, int pMouseY) {
        for (SlotBacteriaStorage bSlot : menu.getBacteriaStorageSlots()) {
            if (isHovering(bSlot.getX(), bSlot.getY(), bSlot.getWidth(), bSlot.getHeight(), pMouseX, pMouseY)) {
                this.hoveredBacteriaStorageSlot = bSlot;
                return;
            }
        }
        this.hoveredBacteriaStorageSlot = null;
    }

    public @Nullable Identifier getBackgroundTexture() {
        return null;
    }

    public SlotFluidHandler getHoveredFluidHandlerSlot() {
        return hoveredFluidHandlerSlot;
    }

    public SlotBacteriaStorage getHoveredBacteriaStorageSlot() {
        return hoveredBacteriaStorageSlot;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (sidePanel != null && sidePanel.mouseClicked(event, sideAnchorX(), sideAnchorY())) {
            return true;
        }
        if (GhostSlots.click(event, this.menu, this.menu.blockEntity, this.hoveredSlot)) {
            return true;
        }
        ItemStack carried = menu.getCarried();
        SlotBacteriaStorage slot = getHoveredBacteriaStorageSlot();
        if (carried.is(NTItems.PETRI_DISH) && slot != null) {
            ClientPacketDistributor.sendToServer(new BacteriaSlotClickedPayload(menu.blockEntity.getBlockPos(), menu.containerId, slot.getSlot()));
        }
        return super.mouseClicked(event, doubleClick);
    }
}
