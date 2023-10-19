package org.mslivo.core.engine.ui_engine.misc;

import com.badlogic.gdx.math.MathUtils;
import org.mslivo.core.engine.tools.Tools;

import java.io.Serializable;

public class FColor implements Serializable {

    public static final FColor WHITE = new FColor(1, 1, 1, 1);
    public static final FColor BLACK = new FColor(0, 0, 0, 1);
    public static final FColor CLEAR = new FColor(0, 0, 0, 0);
    public static final FColor GREEN_BRIGHT = new FColor(0.18039216f, 0.8f, 0.44313726f, 1f);
    public static final FColor GREEN_DARK = new FColor(0.15294118f, 0.68235296f, 0.3764706f, 1f);
    public static final FColor BLUE_BRIGHT = new FColor(0.20392157f, 0.59607846f, 0.85882354f, 1f);
    public static final FColor BLUE_DARK = new FColor(0.16078432f, 0.5019608f, 0.7254902f, 1f);
    public static final FColor ORANGE_BRIGHT = new FColor(0.9019608f, 0.49411765f, 0.13333334f, 1f);
    public static final FColor ORANGE_DARK = new FColor(0.827451f, 0.32941177f, 0.0f, 1f);
    public static final FColor RED_BRIGHT = new FColor(0.90588236f, 0.29803923f, 0.23529412f, 1f);
    public static final FColor RED_DARK = new FColor(0.7529412f, 0.22352941f, 0.16862746f, 1f);
    public static final FColor GRAY_BRIGHT = new FColor(0.58431375f, 0.64705884f, 0.6509804f, 1f);
    public static final FColor GRAY_DARK = new FColor(0.49803922f, 0.54901963f, 0.5529412f, 1f);
    public static final FColor SILVER_BRIGHT = new FColor(0.9254902f, 0.9411765f, 0.94509804f, 1f);
    public static final FColor SILVER_DARK = new FColor(0.7411765f, 0.7647059f, 0.78039217f, 1f);
    public static final FColor YELLOW_BRIGHT = new FColor(0.94509804f, 0.76862746f, 0.05882353f, 1f);
    public static final FColor YELLOW_DARK = new FColor(0.9529412f, 0.6117647f, 0.07058824f, 1f);
    public static final FColor TURQUOISE_BRIGHT = new FColor(0.101960786f, 0.7372549f, 0.6117647f, 1f);
    public static final FColor TURQUOISE_DARK = new FColor(0.08627451f, 0.627451f, 0.52156866f, 1f);
    public static final FColor PURPLE_BRIGHT = new FColor(0.60784316f, 0.34901962f, 0.7137255f, 1f);
    public static final FColor PURPLE_DARK = new FColor(0.5568628f, 0.26666668f, 0.6784314f, 1f);
    public static final FColor NAVY_BLUE_BRIGHT = new FColor(0.20392157f, 0.28627452f, 0.36862746f, 1);
    public static final FColor NAVY_BLUE_DARK = new FColor(0.17254902f, 0.24313726f, 0.3137255f, 1f);
    public static final FColor BROWN_BRIGHT = new FColor(0.6509804f, 0.48235294f, 0.31764707f, 1f);
    public static final FColor BROWN_DARK = new FColor(0.38039216f, 0.23137255f, 0.08627451f, 1f);
    public static final FColor[] ALL_COLORS = new FColor[]{
            WHITE, BLACK, GREEN_BRIGHT,
            GREEN_DARK, BLUE_BRIGHT, BLUE_DARK, ORANGE_BRIGHT,
            ORANGE_DARK, RED_BRIGHT, RED_DARK, GRAY_BRIGHT,
            GRAY_DARK, SILVER_BRIGHT, SILVER_DARK, YELLOW_BRIGHT,
            YELLOW_DARK, TURQUOISE_BRIGHT, TURQUOISE_DARK, PURPLE_BRIGHT,
            PURPLE_DARK, NAVY_BLUE_BRIGHT, NAVY_BLUE_DARK, BROWN_BRIGHT, BROWN_DARK
    };

    public final float r, g, b, a;

    public FColor(float r, float g, float b, float a) {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
    }


    public static FColor create(float r, float g, float b, float a) {
        return new FColor(
                Tools.Calc.inBounds(r, 0f, 1f),
                Tools.Calc.inBounds(g, 0f, 1f),
                Tools.Calc.inBounds(b, 0f, 1f),
                Tools.Calc.inBounds(a, 0f, 1f));
    }

    public static FColor create(FColor fColor) {
        return create(fColor.r, fColor.g, fColor.b, fColor.a);
    }

    public static FColor create(float r, float g, float b) {
        return create(r, g, b, 1f);
    }

    public static FColor createDarker(FColor color, float amount) {
        float r = Tools.Calc.inBounds(color.r - (color.r * amount), 0f, 1f);
        float g = Tools.Calc.inBounds(color.g - (color.g * amount), 0f, 1f);
        float b = Tools.Calc.inBounds(color.b - (color.b * amount), 0f, 1f);
        return create(r, g, b, color.a);
    }

    public static FColor createBrighter(FColor color, float amount) {
        float r = Tools.Calc.inBounds(color.r + (color.r * amount), 0f, 1f);
        float g = Tools.Calc.inBounds(color.g + (color.g * amount), 0f, 1f);
        float b = Tools.Calc.inBounds(color.b + (color.b * amount), 0f, 1f);
        return create(r, g, b, color.a);
    }

    public static FColor createFromString(String colorString) {
        if(colorString == null) return FColor.BLACK;
        String[] colors = colorString.split(",");
        float r = 0f;
        float g = 0f;
        float b = 0f;
        float a = 1f;
        try {
            if(colors.length >= 1) r = Float.parseFloat(colors[0]);
            if(colors.length >= 2) g = Float.parseFloat(colors[1]);
            if(colors.length >= 3) b = Float.parseFloat(colors[2]);
            if(colors.length >= 4) a = Float.parseFloat(colors[3]);
        }finally {
            return create(r,g,b,a);
        }
    }

    public static FColor createFromHex(String hex) {
        if (hex.startsWith("#")) hex = hex.substring(1);
        float r = 0f;
        float g = 0f;
        float b = 0f;
        float a = 1f;
        try {
            if(hex.length() >= 2) r = Integer.parseInt(hex.substring(0, 2), 16) / 255f;
            if(hex.length() >= 4) g = Integer.parseInt(hex.substring(2, 4), 16) / 255f;
            if(hex.length() >= 6) b = Integer.parseInt(hex.substring(4, 6), 16) / 255f;
            if(hex.length() >= 8) a = Integer.parseInt(hex.substring(6, 8), 16) / 255f;
        }finally {
            return create(r,g,b,a);
        }
    }

    public static float getBrightness(FColor fColor) {
        return (0.299f * fColor.r) + (0.587f * fColor.g) + (0.114f * fColor.b);
    }

    public static FColor createFromInt(int rgb8888) {
        return create((float) ((rgb8888 & -16777216) >>> 24) / 255.0F,
                (float) ((rgb8888 & 16711680) >>> 16) / 255.0F,
                (float) ((rgb8888 & '\uff00') >>> 8) / 255.0F,
                (float) (rgb8888 & 255) / 255.0F
        );
    }

    public static String getAsString(FColor fColor) {
        return fColor.r + "," + fColor.g + "," + fColor.b + "," + fColor.a;
    }

    public static String getAsHex(FColor fColor) {
        return getAsHex(fColor.r, fColor.g, fColor.b);
    }

    private static String getAsHex(float r, float g, float b) {
        return String.format("#%02x%02x%02x", MathUtils.round(r * 255), MathUtils.round(g * 255), MathUtils.round(b * 255));
    }

    public static boolean matches(FColor color1, FColor color2) {
        return (color1.r == color2.r &&
                color1.g == color2.g &&
                color1.b == color2.b &&
                color1.a == color2.a);
    }


    public static FColor createRandom() {
        return create(MathUtils.random(0f, 1f), MathUtils.random(0f, 1f), MathUtils.random(0f, 1f));
    }



}
