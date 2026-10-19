package cimmerdev.studbook.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

import static cimmerdev.studbook.gui.EntityInfoScrollPanel.ENTRIES_PADDING;
import static cimmerdev.studbook.gui.LineageScreen.FONT;
import static cimmerdev.studbook.gui.LineageScreen.FONT_LINE_HEIGHT;

public class EntityInfoEntry {

    private final MutableComponent displayText;

    public EntityInfoEntry(Component key, Component value) {
        Style keyStyle = key.getStyle();
        this.displayText = Component.empty()
                .append(key)
                .append(Component.literal(": ").withStyle(keyStyle))
                .append(value);
    }

    public void render(GuiGraphics guiGraphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean isMouseOver) {
        guiGraphics.drawString(FONT, displayText, left + ENTRIES_PADDING, top, 0xFFFFFF, false);
    }

    public final int GetHeight()
    {
        return FONT_LINE_HEIGHT;
    }
}
