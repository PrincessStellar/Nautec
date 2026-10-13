package com.breakinblocks.nautec.client.screen;

import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.api.client.screen.NTMachineScreen;
import com.breakinblocks.nautec.api.client.screen.RecipeAreaHost;
import com.breakinblocks.nautec.api.menu.NTMachineMenu;
import com.breakinblocks.nautec.content.blockentities.LaserPurificationSmelterBlockEntity;
import com.breakinblocks.nautec.content.blocks.LaserPurificationSmelterBlock;
import com.breakinblocks.nautec.content.menus.LaserPurificationSmelterMenu;
import com.breakinblocks.nautec.network.SmelterAutoDistributePayload;
import com.breakinblocks.nautec.registries.NTItems;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.transfer.item.ResourceHandlerSlot;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class LaserPurificationSmelterScreen extends NTMachineScreen<LaserPurificationSmelterBlockEntity> {
    private static final Identifier EMPTY_ARROW = Nautec.rl("container/furnace/empty_arrow");
    private static final Identifier BURN_PROGRESS = Identifier.withDefaultNamespace("container/furnace/burn_progress");
    private static final int ARROW_X = 65;
    private static final int ARROW_Y = 37;
    private static final int AUTO_X = 63;
    private static final int AUTO_Y = 57;
    private static final int AUTO_WIDTH = 28;
    private static final int AUTO_HEIGHT = 14;
    private static final int READOUT_X = 8;
    private static final int READOUT_Y = 79;
    private static final int READOUT_WIDTH = 160;
    private static final int READOUT_HEIGHT = 41;
    private static final int ROW_HEIGHT = 9;
    private static final int ROWS = 4;
    private static final int LOCKED_FILL = 0xFF2A302D;
    private static final int LOCK = 0xFF8C8F80;
    private static final int LOCK_SHADE = 0xFF5B5E52;

    public LaserPurificationSmelterScreen(NTMachineMenu<LaserPurificationSmelterBlockEntity> menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, 176, LaserPurificationSmelterMenu.INVENTORY_Y + 82);
        this.inventoryLabelY = LaserPurificationSmelterMenu.INVENTORY_Y - 11;
    }

    private LaserPurificationSmelterMenu smelter() {
        return (LaserPurificationSmelterMenu) this.menu;
    }

    @Override
    protected void init() {
        super.init();
        addRenderableWidget(new NTPanelButton(this.font, leftPos + AUTO_X, topPos + AUTO_Y, AUTO_WIDTH, AUTO_HEIGHT,
                () -> Component.translatable("nautec.laser_purification_smelter.auto"),
                () -> smelter().isAutoDistribute() ? PanelStyle.SEND_COLOR : PanelStyle.NEUTRAL,
                () -> smelter().isAutoDistribute() ? PanelStyle.SEND_HOVER : PanelStyle.NEUTRAL_HOVER,
                () -> Component.translatable(smelter().isAutoDistribute()
                        ? "nautec.laser_purification_smelter.auto.on" : "nautec.laser_purification_smelter.auto.off"),
                () -> ClientPacketDistributor.sendToServer(new SmelterAutoDistributePayload(this.menu.containerId))));
    }

    private record Row(Component line, boolean met, boolean neutral, Component description) {
    }

    private static String format(float value) {
        return String.format(Locale.ROOT, "%.2f", value);
    }

    private List<Row> rows() {
        LaserPurificationSmelterMenu smelter = smelter();
        List<Row> rows = new ArrayList<>(ROWS);
        int power = smelter.getPower();
        int cost = smelter.getCost();
        int multiplier = LaserPurificationSmelterBlockEntity.multiplierForTier(smelter.getTier());
        rows.add(new Row(Component.translatable("nautec.laser_purification_smelter.beam", power, cost), power >= cost, smelter.getBusy() == 0,
                Component.translatable("nautec.laser_purification_smelter.beam.desc", multiplier)));

        float purity = smelter.getPurity();
        if (purity > 1F) {
            rows.add(new Row(Component.translatable("nautec.laser_purification_smelter.purity", format(purity), format(1F / purity)), true, false,
                    Component.translatable("nautec.laser_purification_smelter.purity.desc")));
        } else {
            rows.add(new Row(Component.translatable("nautec.laser_purification_smelter.purity.none", format(purity)), true, true,
                    Component.translatable("nautec.laser_purification_smelter.purity.desc")));
        }

        int interval = smelter.getInterval();
        Component speed = interval == 1
                ? Component.translatable("nautec.laser_purification_smelter.speed.tick", smelter.getItemsPerInterval())
                : Component.translatable("nautec.laser_purification_smelter.speed", smelter.getItemsPerInterval(), interval);
        rows.add(new Row(speed, true, true, Component.translatable("nautec.laser_purification_smelter.speed.desc")));

        rows.add(new Row(Component.translatable("nautec.laser_purification_smelter.slots", smelter.getUnlocked(), LaserPurificationSmelterBlockEntity.LANES),
                true, true, Component.translatable("nautec.laser_purification_smelter.slots.desc",
                LaserPurificationSmelterBlockEntity.slotsForTier(0), LaserPurificationSmelterBlockEntity.slotsForTier(1),
                LaserPurificationSmelterBlockEntity.slotsForTier(2), LaserPurificationSmelterBlockEntity.slotsForTier(3),
                LaserPurificationSmelterBlockEntity.slotsForTier(4))));
        return rows;
    }

    private Component status() {
        return Component.translatable(LaserPurificationSmelterBlock.statusKey(smelter().getStatus()));
    }

    private int statusColor() {
        return switch (smelter().getStatus()) {
            case LaserPurificationSmelterBlockEntity.STATUS_SMELTING -> PanelStyle.SEND_COLOR;
            case LaserPurificationSmelterBlockEntity.STATUS_WAITING -> 0xFF8C6A1E;
            case LaserPurificationSmelterBlockEntity.STATUS_IDLE -> PanelStyle.LABEL;
            default -> PanelStyle.DANGER;
        };
    }

    private static ItemStack upgradeFor(int tier) {
        return switch (tier) {
            case 1 -> new ItemStack(NTItems.EDDY_UPGRADE.get());
            case 2 -> new ItemStack(NTItems.SURGE_UPGRADE.get());
            case 3 -> new ItemStack(NTItems.RIPTIDE_UPGRADE.get());
            case 4 -> new ItemStack(NTItems.MAELSTROM_UPGRADE.get());
            default -> ItemStack.EMPTY;
        };
    }

    private @Nullable Slot machineSlot(int index) {
        for (Slot slot : this.menu.slots) {
            if (slot instanceof ResourceHandlerSlot handlerSlot && handlerSlot.getResourceHandler() == this.menu.blockEntity.getItemStackHandler()
                    && slot.getContainerSlot() == index) {
                return slot;
            }
        }
        return null;
    }

    private void extractLock(GuiGraphicsExtractor graphics, int x, int y) {
        graphics.fill(x, y, x + 16, y + 16, LOCKED_FILL);
        graphics.fill(x + 5, y + 3, x + 11, y + 4, LOCK);
        graphics.fill(x + 5, y + 4, x + 6, y + 8, LOCK);
        graphics.fill(x + 10, y + 4, x + 11, y + 8, LOCK);
        graphics.fill(x + 4, y + 8, x + 12, y + 14, LOCK);
        graphics.fill(x + 7, y + 10, x + 9, y + 12, LOCK_SHADE);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        LaserPurificationSmelterMenu smelter = smelter();

        int unlocked = smelter.getUnlocked();
        for (int lane = unlocked; lane < LaserPurificationSmelterBlockEntity.LANES; lane++) {
            extractLock(graphics, leftPos + LaserPurificationSmelterMenu.inputX(lane), topPos + LaserPurificationSmelterMenu.slotY(lane));
            extractLock(graphics, leftPos + LaserPurificationSmelterMenu.outputX(lane), topPos + LaserPurificationSmelterMenu.slotY(lane));
        }

        extractSlotHint(graphics, machineSlot(LaserPurificationSmelterBlockEntity.UPGRADE_SLOT), PanelStyle.ICON_UPGRADE);

        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, EMPTY_ARROW, leftPos + ARROW_X, topPos + ARROW_Y, 24, 16);
        int interval = smelter.getInterval();
        if (smelter.getProgress() > 0) {
            int width = Math.max(1, Math.min(24, Math.round(24F * smelter.getProgress() / interval)));
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, BURN_PROGRESS, 24, 16, 0, 0, leftPos + ARROW_X, topPos + ARROW_Y, width, 16);
        }

        PanelStyle.screen(graphics, leftPos + READOUT_X, topPos + READOUT_Y, READOUT_WIDTH, READOUT_HEIGHT);
        List<Row> rows = rows();
        for (int i = 0; i < rows.size(); i++) {
            Row row = rows.get(i);
            int rowY = topPos + READOUT_Y + 3 + i * ROW_HEIGHT;
            int color = row.neutral() ? PanelStyle.READOUT_DIM : row.met() ? PanelStyle.ONLINE : PanelStyle.OFFLINE;
            graphics.fill(leftPos + READOUT_X + 5, rowY + 2, leftPos + READOUT_X + 9, rowY + 6, color);
            graphics.text(this.font, row.line(), leftPos + READOUT_X + 13, rowY, row.neutral() || row.met() ? PanelStyle.READOUT : PanelStyle.OFFLINE, false);
        }
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        super.extractLabels(graphics, mouseX, mouseY);
        Component status = status();
        graphics.text(this.font, status, this.imageWidth - 8 - this.font.width(status), this.inventoryLabelY, statusColor(), false);
    }

    private @Nullable List<Component> lockTooltip(int lane) {
        if (lane < smelter().getUnlocked()) {
            return null;
        }
        List<Component> lines = new ArrayList<>();
        lines.add(Component.translatable("nautec.laser_purification_smelter.locked").withStyle(ChatFormatting.RED));
        ItemStack upgrade = upgradeFor(LaserPurificationSmelterBlockEntity.tierUnlocking(lane));
        lines.add((upgrade.isEmpty()
                ? Component.translatable("nautec.laser_purification_smelter.locked.never")
                : Component.translatable("nautec.laser_purification_smelter.locked.unlock", upgrade.getHoverName())).withStyle(ChatFormatting.GRAY));
        lines.add(Component.translatable("nautec.laser_purification_smelter.locked.items").withStyle(ChatFormatting.DARK_GRAY));
        return lines;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        LaserPurificationSmelterMenu smelter = smelter();
        if (isHovering(ARROW_X, ARROW_Y, 24, 16, mouseX, mouseY)) {
            List<Component> lines = new ArrayList<>();
            lines.add(status());
            if (smelter.getStatus() == LaserPurificationSmelterBlockEntity.STATUS_SMELTING || smelter.getProgress() > 0) {
                lines.add(Component.translatable("nautec.laser_purification_smelter.progress", smelter.getProgress(), smelter.getInterval())
                        .withStyle(ChatFormatting.GRAY));
            }
            graphics.setComponentTooltipForNextFrame(this.font, RecipeAreaHost.withRecipeHint(lines), mouseX, mouseY);
            return;
        }
        if (!this.menu.getCarried().isEmpty()) {
            return;
        }
        for (int lane = 0; lane < LaserPurificationSmelterBlockEntity.LANES; lane++) {
            int y = LaserPurificationSmelterMenu.slotY(lane);
            boolean overInput = isHovering(LaserPurificationSmelterMenu.inputX(lane), y, 16, 16, mouseX, mouseY);
            boolean overOutput = isHovering(LaserPurificationSmelterMenu.outputX(lane), y, 16, 16, mouseX, mouseY);
            if (!overInput && !overOutput) {
                continue;
            }
            int index = overInput ? LaserPurificationSmelterBlockEntity.INPUT_START + lane : LaserPurificationSmelterBlockEntity.OUTPUT_START + lane;
            if (!this.menu.blockEntity.getItemStackHandler().getStackInSlot(index).isEmpty()) {
                return;
            }
            List<Component> lines = lockTooltip(lane);
            if (lines != null) {
                graphics.setComponentTooltipForNextFrame(this.font, lines, mouseX, mouseY);
            }
            return;
        }
        if (this.menu.blockEntity.getItemStackHandler().getStackInSlot(LaserPurificationSmelterBlockEntity.UPGRADE_SLOT).isEmpty()
                && isHovering(LaserPurificationSmelterMenu.UPGRADE_X, LaserPurificationSmelterMenu.UPGRADE_Y, 16, 16, mouseX, mouseY)) {
            graphics.setComponentTooltipForNextFrame(this.font, List.of(
                    Component.translatable("nautec.laser_purification_smelter.upgrade_slot").withStyle(ChatFormatting.AQUA),
                    Component.translatable("nautec.laser_purification_smelter.upgrade_slot.desc").withStyle(ChatFormatting.GRAY)), mouseX, mouseY);
            return;
        }
        List<Row> rows = rows();
        for (int i = 0; i < rows.size(); i++) {
            if (isHovering(READOUT_X, READOUT_Y + 2 + i * ROW_HEIGHT, READOUT_WIDTH, ROW_HEIGHT, mouseX, mouseY)) {
                Row row = rows.get(i);
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
