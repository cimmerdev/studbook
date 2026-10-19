package cimmerdev.studbook.util;

public class Color {
    public int a;
    public int r;
    public int g;
    public int b;

    public Color(int a, int r, int g, int b) {
        this.a = a;
        this.r = r;
        this.g = g;
        this.b = b;
    }

    public static Color fromHex(int color) {
        int a = (color >> 24) & 0xFF;
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;
        return new Color(a, r, g, b);
    }

    public static int toHex(Color color)
    {
        int result = 0;
        result |= (color.a & 255) << 24;
        result |= (color.r & 255) << 16;
        result |= (color.g & 255) << 8;
        result |= (color.b & 255);
        return  result;
    }

    /// Get a color that is evaluated from A and B colors, mapped to 0 and 1 accordingly
    public static Color getMiddleColorByAlpha(float alpha, Color colorA, Color colorB)
    {
        int a = Math.round(((colorB.a - colorA.a) * alpha) + colorA.a);
        int r = Math.round(((colorB.r - colorA.r) * alpha) + colorA.r);
        int g = Math.round(((colorB.g - colorA.g) * alpha) + colorA.g);
        int b = Math.round(((colorB.b - colorA.b) * alpha) + colorA.b);
        return new Color(a, r, g, b);
    }

    public static Color getColorByAlphaTriGradient(float alpha, Color colorMin, Color colorMid, Color colorMax) {
        if (alpha >0.5f) return getMiddleColorByAlpha((alpha - 0.5f) * 2, colorMid, colorMax);
        else return getMiddleColorByAlpha(alpha * 2, colorMin, colorMid);
    }
}
