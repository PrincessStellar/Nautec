package com.breakinblocks.nautec.client.screen;

import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.content.resonance.ResonanceNodeBlockEntity;
import com.breakinblocks.nautec.network.OpenCharmScreenPayload;
import com.breakinblocks.nautec.network.SetCharmPriorityPayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.neoforged.neoforge.network.PacketDistributor;

import java.text.NumberFormat;

public class ResonanceCharmScreen extends Screen {
    private static final int WIDTH = 196;
    private static final int HEIGHT = 128;
    private static final int OUTLINE = PanelStyle.OUTLINE;
    private static final int PANEL = PanelStyle.PANEL;
    private static final int PANEL_LIGHT = PanelStyle.PANEL_LIGHT;
    private static final int SCREEN_FILL = PanelStyle.SCREEN_FILL;
    private static final int SCREEN_EDGE = PanelStyle.SCREEN_EDGE;
    private static final int LABEL = PanelStyle.LABEL;
    private static final int READOUT = PanelStyle.READOUT;
    private static final int READOUT_DIM = PanelStyle.READOUT_DIM;
    private static final int WARNING = 0xFFE36A5C;

    private final int hand;
    private final OpenCharmScreenPayload.Info info;
    private final NumberFormat format = NumberFormat.getIntegerInstance();
    private int priority;

    public ResonanceCharmScreen(OpenCharmScreenPayload payload) {
        super(Component.translatable(payload.hand() == OpenCharmScreenPayload.AUGMENT
                ? "nautec.resonance_augment.screen.title" : "nautec.resonance_charm.screen.title"));
        this.hand = payload.hand();
        this.info = payload.info();
        this.priority = info.priority();
    }

    private int left() {
        return (this.width - WIDTH) / 2;
    }

    private int top() {
        return (this.height - HEIGHT) / 2;
    }

    @Override
    protected void init() {
        int x = left();
        int y = top();
        addRenderableWidget(Button.builder(Component.literal("-"), button -> setPriority(priority - 1)).bounds(x + 96, y + 100, 20, 18).build());
        addRenderableWidget(Button.builder(Component.literal("+"), button -> setPriority(priority + 1)).bounds(x + 168, y + 100, 20, 18).build());
    }

    private void setPriority(int value) {
        priority = Mth.clamp(value, ResonanceNodeBlockEntity.MIN_PRIORITY, ResonanceNodeBlockEntity.MAX_PRIORITY);
        PacketDistributor.sendToServer(new SetCharmPriorityPayload(hand, priority));
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(graphics, mouseX, mouseY, partialTick);
        int x = left();
        int y = top();
        graphics.fill(x, y, x + WIDTH, y + HEIGHT, OUTLINE);
        graphics.fill(x + 1, y + 1, x + WIDTH - 1, y + HEIGHT - 1, PANEL_LIGHT);
        graphics.fill(x + 3, y + 3, x + WIDTH - 1, y + HEIGHT - 1, PANEL);
        graphics.fill(x + 7, y + 19, x + WIDTH - 7, y + 93, OUTLINE);
        graphics.fill(x + 8, y + 20, x + WIDTH - 8, y + 92, SCREEN_EDGE);
        graphics.fill(x + 9, y + 21, x + WIDTH - 9, y + 91, SCREEN_FILL);
        graphics.fill(x + 117, y + 100, x + 167, y + 118, OUTLINE);
        graphics.fill(x + 118, y + 101, x + 166, y + 117, SCREEN_FILL);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        int x = left();
        int y = top();
        graphics.drawString(this.font, this.title, x + 8, y + 7, LABEL, false);
        int tx = x + 13;
        int ty = y + 25;
        if (!info.bound()) {
            graphics.drawString(this.font, Component.translatable("nautec.resonance_charm.screen.unbound"), tx, ty, WARNING, false);
            graphics.drawString(this.font, Component.translatable(hand == OpenCharmScreenPayload.AUGMENT
                    ? "nautec.resonance_augment.screen.unbound.hint" : "nautec.resonance_charm.screen.unbound.hint"), tx, ty + 12, READOUT_DIM, false);
        } else {
            graphics.drawString(this.font, Component.translatable("nautec.resonance_charm.screen.network", info.network()), tx, ty, READOUT, false);
            graphics.drawString(this.font, Component.translatable("nautec.resonance_charm.screen.owner", info.owner()), tx, ty + 11, READOUT_DIM, false);
            if (!info.access()) {
                graphics.drawString(this.font, Component.translatable("nautec.resonance_charm.screen.no_access"), tx, ty + 22, WARNING, false);
            } else {
                graphics.drawString(this.font, Component.translatable("nautec.resonance_charm.screen.links", info.uplinks(), info.nodes()),
                        tx, ty + 22, READOUT_DIM, false);
                graphics.drawString(this.font, Component.translatable("nautec.resonance_charm.screen.stored", format.format(info.stored())),
                        tx, ty + 33, READOUT_DIM, false);
                graphics.drawString(this.font, Component.translatable("nautec.resonance_charm.screen.rate", format.format(NTConfig.charmTransferRate)),
                        tx, ty + 44, READOUT_DIM, false);
            }
        }
        graphics.drawString(this.font, Component.translatable("nautec.resonance_charm.screen.priority"), x + 8, y + 105, LABEL, false);
        String value = Integer.toString(priority);
        graphics.drawString(this.font, value, x + 142 - this.font.width(value) / 2, y + 105, READOUT, false);
        if (mouseX >= x + 8 && mouseX < x + 166 && mouseY >= y + 100 && mouseY < y + 118) {
            graphics.renderTooltip(this.font, Component.translatable("nautec.resonance_charm.screen.priority.desc"), mouseX, mouseY);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
