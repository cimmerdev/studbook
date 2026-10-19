package cimmerdev.studbook.gui;

import cimmerdev.studbook.data.NodeWidgetData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import cimmerdev.studbook.util.Color;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.joml.Math;
import org.joml.Matrix4f;

import java.util.*;

import static cimmerdev.studbook.gui.LineageScreen.NODE_HEIGHT;
import static cimmerdev.studbook.gui.LineageScreen.NODE_WIDTH;

public class LineageTreeViewportWidget extends AbstractWidget {

    // Nodes list
    private final Map<UUID, LineageNodeWidget> nodes = new HashMap<>();
    Set<UUID> visibleNodes = new HashSet<>();
    UUID selectedNode;

    // "camera" controls
    private float offsetX = 0;
    private float offsetY = 0;
    private float zoomScale = 0.5f;
    private static final float MIN_ZOOM = 0.05f;
    private static final float MAX_ZOOM = 1.0f;

    int LINE_COLOR_DEFAULT = 0xFF8b8b8b;
    int LINE_COLOR_SELECTED = 0xFFFFFFFF;

    private static final ResourceLocation BACKGROUND_TEXTURE = ResourceLocation.withDefaultNamespace("textures/gui/advancements/backgrounds/husbandry.png");

    private final LineageScreen screen;

    public LineageTreeViewportWidget(int x, int y, int width, int height, LineageScreen sc, List<NodeWidgetData> nodesInput) {
        super(x, y, width, height, Component.literal("Lineage Viewport"));
        screen = sc;
        for (NodeWidgetData node : nodesInput) {
            nodes.put(node.uuid, new LineageNodeWidget(node.getX(), node.getY(), NODE_WIDTH, NODE_HEIGHT, node));
        }
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {

        graphics.setColor(0.5f, 0.5f, 0.5f, 1.0f);
        graphics.blit(
                BACKGROUND_TEXTURE,
                this.getX(), this.getY(),
                this.width, this.height,
                (offsetX) * -1, (offsetY) * -1,
                this.width, this.height,
                16, 16
        );
        graphics.setColor(1.0f, 1.0f, 1.0f, 1.0f);

        graphics.enableScissor(getX(), getY(), getX() + getWidth(), getY() + getHeight());
        graphics.pose().pushPose();
        graphics.pose().translate(getX() + offsetX, getY() + offsetY, 0);
        graphics.pose().scale(zoomScale, zoomScale, 1.0f);
        int transformedMouseX = (int) ((mouseX - getX() - offsetX) / zoomScale);
        int transformedMouseY = (int) ((mouseY - getY() - offsetY) / zoomScale);
        boolean mouseInViewport = this.isHoveredOrFocused();

        visibleNodes.clear();
        for(Map.Entry<UUID, LineageNodeWidget> entry : nodes.entrySet()) {
            if (isNodeVisible(entry.getValue(), this.getX(), this.getY(), this.getWidth(), this.getHeight())) {
                visibleNodes.add(entry.getKey());

                //draw lines between parents and children
                UUID parentAId = entry.getValue().data.parentA;
                UUID parentBId = entry.getValue().data.parentB;
                if (entry.getKey() != selectedNode)
                {
                    drawLinesBetweenParentsAndChild(graphics, parentAId, parentBId, entry.getValue(), LINE_COLOR_DEFAULT);
                }
                //draw children lines as well even if they're not visible
                for (UUID uuid : entry.getValue().data.children) {
                    drawLinesBetweenParentsAndChild(graphics, nodes.get(uuid).data.parentA, nodes.get(uuid).data.parentB, nodes.get(uuid), LINE_COLOR_DEFAULT);
                }
            }
        }
        //draw selected node's connection separately to make sure it's always on top
        drawLinesBetweenParentsAndChild(graphics, nodes.get(selectedNode).data.parentA, nodes.get(selectedNode).data.parentB, nodes.get(selectedNode), LINE_COLOR_SELECTED);

        for(UUID id : visibleNodes) {
            nodes.get(id).render(graphics,
                    mouseInViewport ? transformedMouseX : -9999,
                    mouseInViewport ? transformedMouseY : -9999,
                    partialTick
            );
        }
        graphics.pose().popPose();
        graphics.disableScissor();
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (this.isMouseOver(mouseX, mouseY)) {
            this.offsetX = (float) (this.offsetX + dragX);
            this.offsetY = (float) (this.offsetY + dragY);
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (this.isMouseOver(mouseX, mouseY)) {
            double oldZoom = this.zoomScale;

            this.zoomScale = Math.max(MIN_ZOOM, Math.min(MAX_ZOOM, this.zoomScale + ((float)delta * 0.1f)));

            double zoomFactor = this.zoomScale / oldZoom;
            double localMouseX = mouseX - getX() - offsetX;
            double localMouseY = mouseY - getY() - offsetY;
            this.offsetX = (float) (this.offsetX - (localMouseX * zoomFactor - localMouseX));
            this.offsetY = (float) (this.offsetY - (localMouseY * zoomFactor - localMouseY));

            return true;
        }
        return false;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.isMouseOver(mouseX, mouseY)) {
            double transformedMouseX = (mouseX - getX() - offsetX) / zoomScale;
            double transformedMouseY = (mouseY - getY() - offsetY) / zoomScale;

            for (Map.Entry<UUID, LineageNodeWidget> entry : nodes.entrySet()) {
                LineageNodeWidget node = entry.getValue();
                if (transformedMouseX >= node.getX() && transformedMouseX < node.getX() + node.getWidth() &&
                        transformedMouseY >= node.getY() && transformedMouseY < node.getY() + node.getHeight()) {
                    selectNode(node.data.uuid);
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    public void selectNode(UUID uuid) {
        for(Map.Entry<UUID, LineageNodeWidget> entry : nodes.entrySet()) { entry.getValue().setSelected(false); }
        LineageNodeWidget node = nodes.get(uuid);
        node.setSelected(true);
        selectedNode = node.data.uuid;
        screen.OnNodeSelected(node.data);
    }

    private boolean isNodeVisible(LineageNodeWidget node, int viewX, int viewY, int viewWidth, int viewHeight) {
        double nodeTrueX = node.getX() * zoomScale + getX() + offsetX;
        double nodeTrueY = node.getY() * zoomScale + getY() + offsetY;

        double nodeTrueWidth = node.getWidth() * zoomScale;
        double nodeTrueHeight = node.getHeight() * zoomScale;

        if (nodeTrueX + nodeTrueWidth <= viewX) return false;
        if (nodeTrueX >= viewX + viewWidth) return false;
        if (nodeTrueY + nodeTrueHeight <= viewY) return false;
        if (nodeTrueY >= viewY + viewHeight) return false;
        return true;
    }

    public void centerOnNode(UUID uuid) {
        LineageNodeWidget targetNode = nodes.get(uuid);
        float viewportCenterX = this.getX() + (this.width / 2f);
        float viewportCenterY = this.getY() + (this.height / 2f);
        float nodeCenterX = targetNode.getX() + (targetNode.getWidth() / 2f);
        float nodeCenterY = targetNode.getY() + (targetNode.getHeight() / 2f);
        this.offsetX = viewportCenterX - this.getX() - (nodeCenterX * zoomScale);
        this.offsetY = viewportCenterY - this.getY() - (nodeCenterY * zoomScale);
    }

    public void drawLinesBetweenParentsAndChild(GuiGraphics graphics, UUID parentAId, UUID parentBId, LineageNodeWidget child, int lineColor)
    {
        if(parentAId != null) {
            LineageNodeWidget parentA = nodes.get(parentAId);
            if (parentA != null) { drawLineBetweenNodes(graphics, parentA, child, 2.f, Color.fromHex(0xFF000000)); }
        }
        if(parentBId != null) {
            LineageNodeWidget parentB = nodes.get(parentBId);
            if (parentB != null) { drawLineBetweenNodes(graphics, parentB, child, 2.f, Color.fromHex(0xFF000000)); }
        }
        // We separate outer and inner lines render to prevent outline borders between two parents of the same node
        if(parentAId != null) {
            LineageNodeWidget parentA = nodes.get(parentAId);
            if (parentA != null) { drawLineBetweenNodes(graphics, parentA, child, 1.f, Color.fromHex(lineColor)); }
        }
        if(parentBId != null) {
            LineageNodeWidget parentB = nodes.get(parentBId);
            if (parentB != null) { drawLineBetweenNodes(graphics, parentB, child, 1.f, Color.fromHex(lineColor)); }
        }
    }

    public void drawLineBetweenNodes(GuiGraphics graphics, LineageNodeWidget nodeParent, LineageNodeWidget nodeChild, float lineWidth, Color color) {

        // nodeA implied to be above nodeB
        int nodeAX = nodeParent.getX() + nodeParent.getWidth() / 2;
        int nodeAY = nodeParent.getY() + nodeParent.getHeight() / 2;
        int nodeBX = nodeChild.getX() + nodeChild.getWidth() / 2;
        int nodeBY = nodeChild.getY() + nodeChild.getHeight() / 2;
        int nodeMidY = nodeBY - (LineageScreen.NODE_HEIGHT + LineageScreen.NODE_PADDING);

        PoseStack pose = graphics.pose();
        Matrix4f matrix = pose.last().pose();

        drawLineFromAtoB(graphics, matrix, nodeAX, nodeAY, nodeBX, nodeBY, nodeMidY, lineWidth, color);

        graphics.flush();
    }

    private void drawLineFromAtoB(GuiGraphics graphics, Matrix4f matrix, int nodeAX, int nodeAY, int nodeBX, int nodeBY, float halfLineWidth, Color color) {
        VertexConsumer buffer = graphics.bufferSource().getBuffer(RenderType.gui());
        int nodeMidY = nodeAY + (nodeBY - nodeAY) / 2;
        drawInternalQuad(buffer, matrix, nodeAX - halfLineWidth, nodeAY, nodeAX + halfLineWidth, nodeMidY, color.r, color.g, color.b, color.a);
        float minX = Math.min(nodeAX, nodeBX) - halfLineWidth;
        float maxX = Math.max(nodeAX, nodeBX) + halfLineWidth;
        // horizontal connector box
        drawInternalQuad(buffer, matrix, minX, nodeMidY - halfLineWidth, maxX, nodeMidY + halfLineWidth, color.r, color.g, color.b, color.a);
        // vertical box between child and horizontal line
        drawInternalQuad(buffer, matrix, nodeBX - halfLineWidth, nodeMidY, nodeBX + halfLineWidth, nodeBY, color.r, color.g, color.b, color.a);
    }

    private void drawLineFromAtoB(GuiGraphics graphics, Matrix4f matrix, int nodeAX, int nodeAY, int nodeBX, int nodeBY, int nodeMidY, float halfLineWidth, Color color) {
        VertexConsumer buffer = graphics.bufferSource().getBuffer(RenderType.gui());
        drawInternalQuad(buffer, matrix, nodeAX - halfLineWidth, nodeAY, nodeAX + halfLineWidth, nodeMidY, color.r, color.g, color.b, color.a);
        float minX = Math.min(nodeAX, nodeBX) - halfLineWidth;
        float maxX = Math.max(nodeAX, nodeBX) + halfLineWidth;
        // horizontal connector box
        drawInternalQuad(buffer, matrix, minX, nodeMidY - halfLineWidth, maxX, nodeMidY + halfLineWidth, color.r, color.g, color.b, color.a);
        // vertical box between child and horizontal line
        drawInternalQuad(buffer, matrix, nodeBX - halfLineWidth, nodeMidY, nodeBX + halfLineWidth, nodeBY, color.r, color.g, color.b, color.a);
    }


    private void drawInternalQuad(VertexConsumer buffer, Matrix4f matrix, float minX, float minY, float maxX, float maxY, int r, int g, int b, int a) {
        buffer.vertex(matrix, minX, maxY, 0.0F).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, maxX, maxY, 0.0F).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, maxX, minY, 0.0F).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, minX, minY, 0.0F).color(r, g, b, a).endVertex();
    }

    @Override
    protected void updateWidgetNarration(@NotNull NarrationElementOutput narrationOutput) {
        this.defaultButtonNarrationText(narrationOutput);
    }
}
