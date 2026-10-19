package cimmerdev.studbook.util;

import cimmerdev.studbook.api.TagVarType;
import cimmerdev.studbook.gui.EntityInfoEntry;
import cimmerdev.studbook.data.NodeWidgetData;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class VanillaEntityTags {

    /// Generates entity info entries from vanilla variables and attributes and displays them in player-friendly way by applying colors and converting units
    public static List<EntityInfoEntry> getEntries(NodeWidgetData nodeData) {

        List<EntityInfoEntry> list = new ArrayList<>();
        Object rawResult;

        rawResult = TagVarType.ATTRIBUTE.readFromNbt(nodeData.getEntityTag(),"minecraft:generic.max_health");
        if (rawResult != null) {
            double result = ((double) rawResult);
            String value = String.format("%.1f", result / 2);
            Component translated_value = Component.empty()
                    .append(Component.literal(value).withStyle(style -> style.withColor(getAttributeColorByRarity(result, 15.f, 30.f))))
                    .append(Component.literal(" heart(s)"));
            Component translated_name = Component.translatable("attribute.name.generic.max_health");//.withStyle(ChatFormatting.YELLOW);
                list.add(new EntityInfoEntry(translated_name, translated_value));
        }

        rawResult = TagVarType.ATTRIBUTE.readFromNbt(nodeData.getEntityTag(),"minecraft:generic.movement_speed");
        if (rawResult != null) {
            double result = ((double) rawResult);
            String value = String.format("%.2f", result * 42.16);
            Component translated_value = Component.empty()
                    .append(Component.literal(value).withStyle(style -> style.withColor(getAttributeColorByRarity(result, 0.1125f, 0.3375f))))
                    .append(Component.literal(" m/s"));
            Component translated_name = Component.translatable("attribute.name.generic.movement_speed"); //.withStyle(ChatFormatting.YELLOW);
            list.add(new EntityInfoEntry(translated_name, translated_value));
        }

        rawResult = TagVarType.ATTRIBUTE.readFromNbt(nodeData.getEntityTag(),"minecraft:horse.jump_strength");
        if (rawResult != null) {
            double result = (double) rawResult;
            String value = String.format("%.2f", getJumpHeightBlocksPerSecond(result));
            Component translated_value = Component.empty()
                    .append(Component.literal(value).withStyle(style -> style.withColor(getAttributeColorByRarity(result, 0.4f, 1.f))))
                    .append(Component.literal(" m"));
            Component translated_name = Component.translatable("attribute.name.horse.jump_strength");
            list.add(new EntityInfoEntry(translated_name, translated_value));
        }

        return list;
    }

    private static double getJumpHeightBlocksPerSecond(double jumpAttribute) {
        return -0.1817584952 * (jumpAttribute * jumpAttribute * jumpAttribute) + 3.689713992 * (jumpAttribute * jumpAttribute) + 2.128599134 * (jumpAttribute) - 0.343930367;
    }

    /// Calculates the color for the style using gradient that is mapped to min and max values. It helps to represent the rarity and the relative "greatness" of this stat value for horse entities
    public static int getAttributeColorByRarity(double value, double min, double max) {
        float alpha = (float) ((value - min) / (max - min));

        Color lowColor = new Color(255, 255, 85, 85);
        Color middleColor = new Color(255, 255, 255, 85);
        Color highColor = new Color(255, 85, 255, 85);

        Color resultColor = Color.getColorByAlphaTriGradient(alpha, lowColor, middleColor, highColor);
        return Color.toHex(resultColor);
    }
}
