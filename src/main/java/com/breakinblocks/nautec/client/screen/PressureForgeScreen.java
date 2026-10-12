package com.breakinblocks.nautec.client.screen;

import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.api.client.screen.NTMachineScreen;
import com.breakinblocks.nautec.api.client.screen.RecipeAreaHost;
import com.breakinblocks.nautec.api.menu.NTMachineMenu;
import com.breakinblocks.nautec.content.blockentities.PressureForgeBlockEntity;
import com.breakinblocks.nautec.content.menus.PressureForgeMenu;
import com.breakinblocks.nautec.registries.NTItems;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PressureForgeScreen extends NTMachineScreen<PressureForgeBlockEntity> {
    private static final Identifier EMPTY_ARROW = Nautec.rl("container/furnace/empty_arrow");
    private static final Identifier BURN_PROGRESS = Identifier.withDefaultNamespace("container/furnace/burn_progress");
    private static final int ARROW_X = 70;
    private static final int ARROW_Y = PressureForgeMenu.ROW_Y + 1;
    private static final int READOUT_X = 8;
    private static final int READOUT_Y = 56;
    private static final int READOUT_WIDTH = 160;
    private static final int READOUT_HEIGHT = 50;
    private static final int ROW_HEIGHT = 9;
    private static final int ROWS = 5;
    private static final int GHOST = 0xAA45504A;

    public PressureForgeScreen(NTMachineMenu<PressureForgeBlockEntity> menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, 176, PressureForgeMenu.INVENTORY_Y + 82);
        this.inventoryLabelY = PressureForgeMenu.INVENTORY_Y - 11;
    }

    private PressureForgeMenu forge() {
        return (PressureForgeMenu) this.menu;
    }

    private record Requirement(Component line, boolean met, boolean neutral, Component description) {
    }

    private List<Requirement> requirements() {
        PressureForgeMenu forge = forge();
        PressureForgeBlockEntity.Synthesizer synthesizer = forge.getSynthesizer();
        List<Requirement> rows = new ArrayList<>(ROWS);

        int limit = forge.getDepthLimit();
        if (synthesizer == PressureForgeBlockEntity.Synthesizer.ATLANTEAN || limit == PressureForgeBlockEntity.NO_VALUE) {
            rows.add(new Requirement(Component.translatable("nautec.pressure_forge.depth.any"), true, false,
                    Component.translatable("nautec.pressure_forge.depth.any.desc")));
        } else {
            rows.add(new Requirement(Component.translatable("nautec.pressure_forge.depth", forge.getY(), limit), forge.getY() <= limit, false,
                    Component.translatable("nautec.pressure_forge.depth.desc", NTConfig.pressureForgeDepth)));
        }

        if (synthesizer != PressureForgeBlockEntity.Synthesizer.NONE) {
            rows.add(new Requirement(Component.translatable("nautec.pressure_forge.water.synthesizer"), true, false,
                    Component.translatable("nautec.pressure_forge.water.synthesizer.desc")));
        } else {
            int required = NTConfig.pressureForgeWaterColumn;
            rows.add(new Requirement(Component.translatable("nautec.pressure_forge.water", forge.getWater(), required), forge.getWater() >= required, false,
                    Component.translatable("nautec.pressure_forge.water.desc", required)));
        }

        int power = forge.getPower();
        int needed = NTConfig.pressureForgePowerUsage;
        rows.add(new Requirement(Component.translatable("nautec.pressure_forge.beam", power, needed), power >= needed, false,
                Component.translatable("nautec.pressure_forge.beam.desc", needed)));

        String purity = String.format(Locale.ROOT, "%.2f", forge.getPurity());
        if (forge.hasRecipe()) {
            String recipePurity = String.format(Locale.ROOT, "%.2f", forge.getRecipePurity());
            rows.add(new Requirement(Component.translatable("nautec.pressure_forge.purity", purity, recipePurity),
                    forge.getPurity() >= forge.getRecipePurity(), false, Component.translatable("nautec.pressure_forge.purity.desc")));
        } else {
            rows.add(new Requirement(Component.translatable("nautec.pressure_forge.purity.unknown", purity), false, true,
                    Component.translatable("nautec.pressure_forge.purity.desc")));
        }

        int acid = forge.getAcid();
        int usage = NTConfig.pressureForgeAcidUsage;
        rows.add(new Requirement(Component.translatable("nautec.pressure_forge.acid", acid, usage), acid >= usage, false,
                Component.translatable("nautec.pressure_forge.acid.desc", usage)));
        return rows;
    }

    private Component status() {
        PressureForgeMenu forge = forge();
        return switch (forge.getStatus()) {
            case PressureForgeBlockEntity.STATUS_RUNNING -> {
                int duration = Math.max(1, forge.getDuration());
                yield Component.translatable("nautec.pressure_forge.status.running", Math.min(100, forge.getProgress() * 100 / duration));
            }
            case PressureForgeBlockEntity.STATUS_NO_RECIPE -> Component.translatable("nautec.pressure_forge.status.no_recipe");
            case PressureForgeBlockEntity.STATUS_OUTPUT_FULL -> Component.translatable("nautec.pressure_forge.status.output_full");
            case PressureForgeBlockEntity.STATUS_WAITING -> Component.translatable("nautec.pressure_forge.status.waiting");
            default -> Component.translatable("nautec.pressure_forge.status.empty");
        };
    }

    private int statusColor() {
        return switch (forge().getStatus()) {
            case PressureForgeBlockEntity.STATUS_RUNNING -> PanelStyle.SEND_COLOR;
            case PressureForgeBlockEntity.STATUS_WAITING -> 0xFF8C6A1E;
            case PressureForgeBlockEntity.STATUS_EMPTY -> PanelStyle.LABEL;
            default -> PanelStyle.DANGER;
        };
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        PressureForgeMenu forge = forge();

        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, EMPTY_ARROW, leftPos + ARROW_X, topPos + ARROW_Y, 24, 16);
        int duration = forge.getDuration();
        if (duration > 0 && forge.getProgress() > 0) {
            int width = Math.max(1, Math.min(24, Math.round(24F * forge.getProgress() / duration)));
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, BURN_PROGRESS, 24, 16, 0, 0, leftPos + ARROW_X, topPos + ARROW_Y, width, 16);
        }

        if (forge.getSynthesizer() == PressureForgeBlockEntity.Synthesizer.NONE) {
            int x = leftPos + PressureForgeMenu.SYNTHESIZER_X;
            int y = topPos + PressureForgeMenu.ROW_Y;
            graphics.fakeItem(new ItemStack(NTItems.PRESSURE_SYNTHESIZER.get()), x, y);
            graphics.fill(x, y, x + 16, y + 16, GHOST);
        }

        PanelStyle.screen(graphics, leftPos + READOUT_X, topPos + READOUT_Y, READOUT_WIDTH, READOUT_HEIGHT);
        List<Requirement> rows = requirements();
        for (int i = 0; i < rows.size(); i++) {
            Requirement row = rows.get(i);
            int rowY = topPos + READOUT_Y + 5 + i * ROW_HEIGHT;
            int color = row.neutral() ? PanelStyle.READOUT_DIM : row.met() ? PanelStyle.ONLINE : PanelStyle.OFFLINE;
            graphics.fill(leftPos + READOUT_X + 5, rowY + 2, leftPos + READOUT_X + 9, rowY + 6, color);
            graphics.text(this.font, row.line(), leftPos + READOUT_X + 13, rowY, row.neutral() || row.met() ? PanelStyle.READOUT : PanelStyle.OFFLINE, false);
        }
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        super.extractLabels(graphics, mouseX, mouseY);
        Component status = status();
        graphics.text(this.font, status, this.imageWidth - 8 - this.font.width(status), this.titleLabelY, statusColor(), false);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        PressureForgeMenu forge = forge();
        if (isHovering(ARROW_X, ARROW_Y, 24, 16, mouseX, mouseY)) {
            List<Component> lines = new ArrayList<>();
            lines.add(status());
            if (forge.getDuration() > 0) {
                lines.add(Component.translatable("nautec.pressure_forge.progress", forge.getProgress(), forge.getDuration()).withStyle(ChatFormatting.GRAY));
            }
            graphics.setComponentTooltipForNextFrame(this.font, RecipeAreaHost.withRecipeHint(lines), mouseX, mouseY);
            return;
        }
        if (forge.getSynthesizer() == PressureForgeBlockEntity.Synthesizer.NONE && this.menu.getCarried().isEmpty()
                && isHovering(PressureForgeMenu.SYNTHESIZER_X, PressureForgeMenu.ROW_Y, 16, 16, mouseX, mouseY)) {
            graphics.setComponentTooltipForNextFrame(this.font, List.of(
                    Component.translatable("nautec.pressure_forge.synthesizer_slot").withStyle(ChatFormatting.AQUA),
                    Component.translatable("nautec.pressure_forge.synthesizer_slot.desc").withStyle(ChatFormatting.GRAY)), mouseX, mouseY);
            return;
        }
        List<Requirement> rows = requirements();
        for (int i = 0; i < rows.size(); i++) {
            if (isHovering(READOUT_X, READOUT_Y + 4 + i * ROW_HEIGHT, READOUT_WIDTH, ROW_HEIGHT, mouseX, mouseY)) {
                Requirement row = rows.get(i);
                graphics.setComponentTooltipForNextFrame(this.font, List.of(row.line(), row.description().copy().withStyle(ChatFormatting.GRAY)), mouseX, mouseY);
                return;
            }
        }
    }

    @Override
    public List<RecipeArea> recipeAreas() {
        return List.of(new RecipeArea(ARROW_X, ARROW_Y, 24, 16, true));
    }
}
