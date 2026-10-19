package cimmerdev.studbook.gui;

import cimmerdev.studbook.data.NodeWidgetData;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import static cimmerdev.studbook.gui.LineageScreen.*;

public class LineageNodeWidget extends AbstractWidget {

    public final NodeWidgetData data;
    public boolean isSelected = false;
    LivingEntity entityToRender;

    private static final int PADDING = 5;

    MultiLineTextWidget textWidget = new MultiLineTextWidget(getMessage(), net.minecraft.client.Minecraft.getInstance().font);
    MultiLineTextWidget iconsWidget = new MultiLineTextWidget(getMessage(), net.minecraft.client.Minecraft.getInstance().font);

    public LineageNodeWidget(int x, int y, int width, int height, NodeWidgetData data) {
        super(x, y, width, height, Component.literal(data.name));
        this.data = data;
        Component message = Component.empty()
                .append(data.gender.getSymbol())
                .append(data.status.getStatusIcon());
        this.iconsWidget.setMessage(message);

        // Mob's name render
        int textX = getX() + (this.width / 3);
        int textY = getY() + PADDING + (FONT_LINE_HEIGHT / 2);
        int maxWidth = (this.width / 3 * 2) - PADDING;
        textWidget.setMaxWidth(maxWidth); // TODO: test if this can be moved to initialization - can I stay there?
        textWidget.setPosition(textX, textY);
        iconsWidget.setMaxWidth(maxWidth);
        iconsWidget.setPosition((getX() + width - PADDING - iconsWidget.getWidth()*2)/2, (getY() + height - PADDING*2 - iconsWidget.getHeight())/2);

        this.entityToRender = this.data.getOrCreateClientEntity();
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {

        int textureU = 0;
        int textureV = 154;
        int cornerSize = 5;
        int textureSize = 26;
        float colorIntensity = this.isSelected ? 0.75f : 1.f;
        graphics.setColor(colorIntensity,colorIntensity, colorIntensity, 1.f);
         //node texture
        graphics.blitNineSliced(
                ADVANCEMENT_WIDGETS,
                this.getX(), this.getY(),
                this.width, this.height,
                cornerSize,
                textureSize, textureSize,
                textureU, textureV
        );
        // square for preview mesh
        graphics.blitNineSliced(
                ADVANCEMENT_WIDGETS,
                this.getX() + PADDING, this.getY() + PADDING,
                this.width/3 - PADDING*2, this.width/3 - PADDING*2,
                cornerSize,
                textureSize, textureSize,
                textureSize, textureV
        );
        graphics.setColor(1.f, 1.f, 1.f, 1.f);

        textWidget.render(graphics, mouseX, mouseY, partialTick);
        InventoryScreen.renderEntityInInventory(graphics, this.getX() + this.width/3/2, this.getY() + this.height - PADDING*2 - PADDING/2, 20, DEFAULT_ENTITY_POSE, DEFAULT_CAMERA_POSE, entityToRender);

        // Hover outline
        int borderColor = this.isHoveredOrFocused() ? 0xFFFFFFFF : 0x00A0A0A0;
        graphics.renderOutline(getX(), getY(), this.width, this.height, borderColor);

        graphics.pose().pushPose();
        graphics.pose().scale(2.0f, 2.0f, 1.0f);
        iconsWidget.render(graphics, mouseX, mouseY, partialTick);
        graphics.pose().popPose();
    }

    public void setSelected(boolean selected) {
        this.isSelected = selected;
    }

    @Override
    protected void updateWidgetNarration(@NotNull NarrationElementOutput narrationOutput) {
        this.defaultButtonNarrationText(narrationOutput);
    }
}
