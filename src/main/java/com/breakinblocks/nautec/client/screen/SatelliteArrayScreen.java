package com.breakinblocks.nautec.client.screen;

import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.content.menus.SatelliteArrayMenu;
import com.breakinblocks.nautec.content.resonance.SatelliteArrayBlockEntity;
import com.breakinblocks.nautec.network.ResonanceSyncPayload;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;

public class SatelliteArrayScreen extends ResonanceNetworkScreen<SatelliteArrayMenu> {
    private static final int ONLINE = 0xFF5FE8B0;
    private static final int OFFLINE = 0xFFE36A5C;

    public SatelliteArrayScreen(SatelliteArrayMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    protected int chunkState() {
        return this.menu.getChunkState();
    }

    @Override
    protected void addHeaderWidgets(int x, int y) {
    }

    private String statusKey() {
        return SatelliteArrayBlockEntity.statusKey(this.menu.getStatus());
    }

    @Override
    protected void extractReadoutBackground(GuiGraphicsExtractor graphics, int rx, int ry, int rw) {
        int color = this.menu.getStatus() == SatelliteArrayBlockEntity.STATUS_ONLINE ? ONLINE : OFFLINE;
        graphics.fill(rx + 5, ry + 5, rx + 9, ry + 9, color);
    }

    @Override
    protected void extractReadout(GuiGraphicsExtractor graphics, int tx, int ty, ResonanceSyncPayload.@Nullable NetworkView view) {
        boolean online = this.menu.getStatus() == SatelliteArrayBlockEntity.STATUS_ONLINE;
        graphics.text(this.font, Component.translatable(statusKey()), tx + 9, ty - 10, online ? READOUT : READOUT_DIM, false);
        Component kind = Component.translatable("nautec.satellite.kind.uplink");
        graphics.text(this.font, kind, IMAGE_WIDTH - 13 - this.font.width(kind), ty - 10, READOUT_DIM, false);
        String purity = String.format(Locale.ROOT, "%.2f", this.menu.getPurity());
        graphics.text(this.font, Component.translatable("nautec.satellite.sending",
                number(this.menu.getPower()), purity), tx, ty + 2, READOUT, false);
        graphics.text(this.font, Component.translatable("nautec.satellite.stored", compact(this.menu.getAp()), compact(NTConfig.satelliteApBuffer),
                compact(this.menu.getFe()), compact(NTConfig.satelliteFeBuffer)), tx, ty + 13, READOUT_DIM, false);
    }

    @Override
    protected boolean readoutTooltip(List<Component> lines, int mouseX, int mouseY, int rx, int ry) {
        if (!inside(mouseX, mouseY, rx, ry, IMAGE_WIDTH - 16, READOUT_HEIGHT)) {
            return false;
        }
        lines.add(Component.translatable(statusKey()));
        lines.add(Component.translatable(statusKey() + ".desc").withStyle(ChatFormatting.GRAY));
        lines.add(Component.translatable("nautec.satellite.links", this.menu.getUplinks(), this.menu.getNodes()).withStyle(ChatFormatting.GRAY));
        lines.add(Component.translatable("nautec.satellite.loss.desc", Math.round(NTConfig.satelliteLoss * 100)).withStyle(ChatFormatting.GRAY));
        lines.add(Component.translatable("nautec.satellite.buffer.uplink").withStyle(ChatFormatting.GRAY));
        return true;
    }
}
