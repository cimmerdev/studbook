package cimmerdev.studbook.gui;

import cimmerdev.studbook.ClientConfig;
import cimmerdev.studbook.data.EntityTypeInfoListener;
import cimmerdev.studbook.data.NodeWidgetData;
import cimmerdev.studbook.data.Gender;
import cimmerdev.studbook.util.LineageLayoutEngine;
import com.mojang.blaze3d.systems.RenderSystem;
import cimmerdev.studbook.api.EntityInfoTag;
import cimmerdev.studbook.util.VanillaEntityTags;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class LineageScreen extends Screen {

    private final UUID firstTarget;
    LivingEntity entityToRender;

    //Constants for GUI layouts and design
    private static final int HEADER_HEIGHT = 25;
    private static final int PADDING = 5;
    private static final int WINDOW_PADDING = 10;
    private static final int SIDEPANEL_WIDTH = 180;
    public static final int NODE_WIDTH = 180; // Node height is calculated automatically by dividing this value by 3
    public static final int NODE_HEIGHT = NODE_WIDTH / 3;
    public static final int NODE_PADDING = 10; //min distance between nodes

    public static final Quaternionf DEFAULT_ENTITY_POSE = new Quaternionf( 0.916,  -0.050,  0.379, -0.121);
    public static final Quaternionf DEFAULT_CAMERA_POSE = new Quaternionf(0, 0, 0, 1);

    int viewportX = WINDOW_PADDING*2 + SIDEPANEL_WIDTH;
    int viewportY = WINDOW_PADDING*2 + HEADER_HEIGHT + HEADER_HEIGHT;
    int viewportWidth;
    int viewportHeight;
    float viewportScale;
    LineageTreeViewportWidget viewport;

    int textMaxWidth = SIDEPANEL_WIDTH + PADDING;
    public static final Font FONT = Minecraft.getInstance().font;
    public static final int FONT_LINE_HEIGHT = Minecraft.getInstance().font.lineHeight;

    public static final ResourceLocation ADVANCEMENT_WIDGETS = ResourceLocation.fromNamespaceAndPath("minecraft", "textures/gui/advancements/widgets.png");

    public static final int WINDOW_WIDTH = 252;
    public static final int WINDOW_HEIGHT = 140;
    private static final int WINDOW_INSIDE_X = 9;
    private static final int WINDOW_INSIDE_Y = 18;
    public static final int WINDOW_INSIDE_WIDTH = 234;
    public static final int WINDOW_INSIDE_HEIGHT = 113;
    private static final int WINDOW_TITLE_X = 8;
    private static final int WINDOW_TITLE_Y = 6;
    public static final ResourceLocation ADVANCEMENT_WINDOW = ResourceLocation.fromNamespaceAndPath("minecraft", "textures/gui/advancements/window.png");

    EntityInfoScrollPanel scrollBox;

    MultiLineTextWidget targetNodeText = new MultiLineTextWidget(getNarrationMessage(), Minecraft.getInstance().font);
    MultiLineTextWidget statusNodeText = new MultiLineTextWidget(getNarrationMessage(), Minecraft.getInstance().font) {
        @Override
        public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            super.render(guiGraphics, mouseX, mouseY, partialTick);

            if (isMouseOver(mouseX, mouseY)) {
                int relativeX = mouseX - this.getX();
                Style hoveredStyle = this.getFont().getSplitter().componentStyleAtWidth(this.getMessage(), relativeX);
                if(hoveredStyle != null && hoveredStyle.getHoverEvent() != null) {
                    guiGraphics.renderComponentHoverEffect(this.getFont(), hoveredStyle, mouseX, mouseY);
                }
            }
        }

        @Override
        public boolean isMouseOver(double mouseX, double mouseY) {
            return mouseX >= (double)this.getX() && mouseX < (double)(this.getX() + getWidth())
                    && mouseY >= (double)this.getY() && mouseY < (double)(this.getY() + getHeight());
        }
    };

    public LineageScreen(List<NodeWidgetData> data, UUID targetEntity) {
        super(Component.literal("Lineage Screen"));
        this.firstTarget = targetEntity;
        viewport = new LineageTreeViewportWidget(viewportX, viewportY, (int) (WINDOW_INSIDE_WIDTH * viewportScale), (int) (WINDOW_INSIDE_HEIGHT * viewportScale), this, LineageLayoutEngine.calculateLayout(data));
    }

    @Override
    protected void init() {
        super.init();

        if (WINDOW_WIDTH > (this.width/2 - PADDING * 2)) {
            viewportScale = (float) (this.width/2 - PADDING * 2) / WINDOW_WIDTH;
        } else { viewportScale = 1.f; } //scales down viewport if it doesn't fit half of the screen

        viewportWidth = (int) (WINDOW_WIDTH * viewportScale);
        viewportHeight = (int) (WINDOW_HEIGHT * viewportScale);
        viewportX = Math.round(((((this.width * 3) / 4) - viewportWidth/2)) / viewportScale);
        viewportY = Math.round((((this.height - viewportHeight)/2)) / viewportScale);
        int viewportXInner = (int) ((viewportX * viewportScale) + WINDOW_INSIDE_X * viewportScale);
        int viewportYInner = (int) ((viewportY * viewportScale) + WINDOW_INSIDE_Y * viewportScale);
        viewport.setPosition(viewportXInner, viewportYInner);
        viewport.setWidth((int) (WINDOW_INSIDE_WIDTH * viewportScale));
        viewport.setHeight((int) (WINDOW_INSIDE_HEIGHT * viewportScale));
        addRenderableWidget(viewport);

        targetNodeText.setMaxWidth(textMaxWidth);
        statusNodeText.setMaxWidth(textMaxWidth);
        statusNodeText.setCentered(true);
        statusNodeText.active = true;
        targetNodeText.setCentered(true);

        int widthSidePanel = this.width > (SIDEPANEL_WIDTH * 2) ? SIDEPANEL_WIDTH : viewportWidth;
        scrollBox = new EntityInfoScrollPanel(this.minecraft, widthSidePanel - PADDING*2, viewportHeight/2 - PADDING * 2,  ((this.height/2) + font.lineHeight*4) + PADDING, (int) ((this.width/4 - widthSidePanel/2) / viewportScale) + PADDING);
        addRenderableWidget(scrollBox);

        viewport.selectNode(firstTarget);
        viewport.centerOnNode(firstTarget);

    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {

        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        // lineage tree viewport
        viewport.render(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.pose().pushPose();
        guiGraphics.pose().scale(viewportScale, viewportScale, 1.f);
        RenderSystem.enableBlend();
        guiGraphics.blit(ADVANCEMENT_WINDOW, viewportX, viewportY, 0, 0, WINDOW_WIDTH, WINDOW_HEIGHT);
        guiGraphics.drawString(font, Component.translatable("studbook.gui.viewport.title") , (viewportX + WINDOW_TITLE_X), (viewportY + WINDOW_TITLE_Y), 4210752, false);
        guiGraphics.pose().popPose();

        // side panel texture 252x140 px
        guiGraphics.setColor(0.75f, 0.75f, 0.75f, 1.f);
        int widthSidePanel = this.width > 360 ? 180 : viewportWidth;
        guiGraphics.blitNineSliced(
                ADVANCEMENT_WIDGETS,
                (int) ((this.width/4 - widthSidePanel/2) / viewportScale), (int) ((this.height/2) + FONT_LINE_HEIGHT*4),
                widthSidePanel, viewportHeight/2,
                5,
                26, 26,
                0, 154
        );
        guiGraphics.setColor(1.f, 1.f, 1.f, 1.f);

        scrollBox.render(guiGraphics, mouseX, mouseY, partialTick);

        InventoryScreen.renderEntityInInventory(guiGraphics, this.width/4, (this.height/2) - PADDING - FONT_LINE_HEIGHT*2, 30, DEFAULT_ENTITY_POSE, DEFAULT_CAMERA_POSE, entityToRender);

        targetNodeText.render(guiGraphics, mouseX, mouseY, partialTick);
        statusNodeText.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_EQUAL || keyCode == GLFW.GLFW_KEY_KP_ADD) {
            double mouseX = this.minecraft.mouseHandler.xpos() * this.minecraft.getWindow().getGuiScaledWidth() / this.minecraft.getWindow().getWidth();
            double mouseY = this.minecraft.mouseHandler.ypos() * this.minecraft.getWindow().getGuiScaledHeight() / this.minecraft.getWindow().getHeight();
            viewport.mouseScrolled(mouseX, mouseY, 1.0);
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_MINUS || keyCode == GLFW.GLFW_KEY_KP_SUBTRACT) {
            double mouseX = this.minecraft.mouseHandler.xpos() * this.minecraft.getWindow().getGuiScaledWidth() / this.minecraft.getWindow().getWidth();
            double mouseY = this.minecraft.mouseHandler.ypos() * this.minecraft.getWindow().getGuiScaledHeight() / this.minecraft.getWindow().getHeight();
            viewport.mouseScrolled(mouseX, mouseY, -1.0);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    public void OnNodeSelected(NodeWidgetData nodeData)
    {
        assert minecraft != null;
        assert minecraft.level != null;
        long currentTime = minecraft.level.getGameTime();

        targetNodeText.setMessage(Component.literal(nodeData.name));
        targetNodeText.setPosition(this.width/4 - targetNodeText.getWidth()/2, this.height/2 - (targetNodeText.getHeight()/2));

        Component genderText;
        if(nodeData.gender != Gender.NONE) {
            genderText = Component.empty()
                    .append(Component.literal(" | ")).withStyle(ChatFormatting.GRAY)
                    .append(Component.translatable("studbook.gui.sidepanel.gender"))
                    .append(Gender.getTranslatableGender(EntityTypeInfoListener.getGenderTagInfo(nodeData.entityType), nodeData.getEntityTag()));
        }
        else genderText = Component.empty();

        Component timeText;
        if(ClientConfig.shouldDisplayBirthTime() && nodeData.getBirthTimeWorld() >= 0)
        {
            timeText = Component.empty()
                    .append(Component.literal(", ")).withStyle(ChatFormatting.GRAY)
                    .append(ClientConfig.getBirthTimeFormat(nodeData.getBirthTimeWorld(), currentTime));
        }
        else timeText = Component.empty();

        Component finalStatusText = Component.empty()
                .append(Component.translatable("studbook.gui.sidepanel.status").withStyle(ChatFormatting.GRAY))
                .append(nodeData.status.getStatusText(nodeData.deathCause, nodeData.getDeathTimeWorld(), currentTime))
                .append(genderText)
                .append(Component.literal("\n"))
                .append(Component.translatable("studbook.gui.sidepanel.breeder", nodeData.breeder).withStyle(ChatFormatting.GRAY))
                .append(timeText);
        statusNodeText.setMessage(finalStatusText);
        statusNodeText.setPosition(this.width/4 - statusNodeText.getWidth()/2, this.height/2 + FONT_LINE_HEIGHT); // as name of max length can only take 2 rows

        List<EntityInfoEntry> newList = new ArrayList<>(VanillaEntityTags.getEntries(nodeData));
        List<EntityInfoTag> taglist = EntityTypeInfoListener.getTagsInfo(nodeData.entityType);
        if(taglist != null) {
            for(EntityInfoTag tagInfo : taglist)
            {
                Object rawResult = (tagInfo.variable_type().readFromNbt(nodeData.getEntityTag(),tagInfo.tag_name()));
                if (rawResult != null) {
                    String result = rawResult.toString();
                    Component translated_value = tagInfo.mapped_values().get(result);
                    if (translated_value != null) {
                        newList.add(new EntityInfoEntry(tagInfo.translated_name(), translated_value));
                    }
                    else {
                        newList.add(new EntityInfoEntry(tagInfo.translated_name(), Component.literal(result)));
                    }
                }
            }
        }
        scrollBox.updateEntryList(newList);
        entityToRender = nodeData.getOrCreateClientEntity();
    }
}