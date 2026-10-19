package cimmerdev.studbook.gui;

import com.mojang.blaze3d.vertex.Tesselator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraftforge.client.gui.widget.ScrollPanel;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class EntityInfoScrollPanel extends ScrollPanel {

    public static final int ENTRIES_PADDING = 2;

    private List<EntityInfoEntry> entries = new ArrayList<EntityInfoEntry>();

    public EntityInfoScrollPanel(Minecraft client, int width, int height, int top, int left)
    {
        super(client, width, height, top, left);
    }

    @Override
    protected int getContentHeight() {
        int total = 0;
        for (EntityInfoEntry entry : entries)
        {
            total += entry.GetHeight();
        }
        return total + ENTRIES_PADDING;
    }

    @Override
    protected void drawPanel(GuiGraphics guiGraphics, int entryRight, int relativeY, Tesselator tess, int mouseX, int mouseY) {
        if(getContentHeight() < this.height) scrollDistance = 0;
        int currentY = (int) (this.top + ENTRIES_PADDING - scrollDistance); //a bit of padding
        for (EntityInfoEntry entry : entries)
        {
            if (currentY + entry.GetHeight() >= this.top && currentY <= this.bottom)
            {
                entry.render(guiGraphics, 1, currentY, left, width, height, mouseX, mouseY, false);
            }
            currentY +=entry.GetHeight();
        }
    }

    @Override
    protected void drawBackground(GuiGraphics graphics, Tesselator tess, float partialTick) {
        return;
    }

    @Override
    public @NotNull NarrationPriority narrationPriority() {
        return NarrationPriority.FOCUSED;
    }

    @Override
    public void updateNarration(@NotNull NarrationElementOutput output) {

    }

    public void updateEntryList(List<EntityInfoEntry> newList)
    {
        entries = newList;
    }
}
