package net.mslivo.core.engine.ui_engine.rendering.renderer;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

public abstract class BasicColorTweakRenderer extends BasicRenderer {
    public static final String COLOR_ATTRIBUTE = "a_color";
    public static final String TWEAK_ATTRIBUTE = "a_tweak";

    protected float tweak;
    protected float tweak_save;
    protected float tweak_reset;

    protected float color;
    protected float color_save;
    protected float color_reset;

    public BasicColorTweakRenderer(final int size, final ShaderProgram defaultShader, final boolean printRenderCalls) {
        super(size, defaultShader, printRenderCalls);

        this.tweak_reset = colorPackedRGBA(0f, 0f, 0f, 0.0f);
        this.tweak_save = tweak_reset;
        this.tweak = tweak_reset;

        this.color_reset = colorPackedRGBA(0.5f, 0.5f, 0.5f, 1f);
        this.color_save = color_reset;
        this.color = color_reset;
    }

    @Override
    protected void onSaveState() {
        this.color_save = this.color;
        this.tweak_save = this.tweak;
    }


    @Override
    protected void onLoadState() {
        setPackedColor(this.color_save);
        setPackedTweak(this.tweak_save);
    }

    public void setColor(Color color) {
        this.color = colorPackedRGBA(color.r, color.g, color.b, color.a);
    }

    public void setColor(Color color, float alpha) {
        this.color = colorPackedRGBA(color.r, color.g, color.b, alpha);
    }

    public void setColor(final float r, final float g, final float b, final float alpha) {
        this.color = colorPackedRGBA(r, g, b, alpha);
    }

    public void setPackedColor(final float color) {
        this.color = color;
    }

    public Color getColor() {
        Color.abgr8888ToColor(tempColor, color);
        return tempColor;
    }

    public void setColorReset() {
        setPackedColor(color_reset);
    }

    public float getPackedColor() {
        return this.color;
    }

    public void setTweak(final float t1, final float t2, final float t3, final float t4) {
        tweak = colorPackedRGBA(t1, t2, t3, t4);
    }

    public void setPackedTweak(final float tweak) {
        this.tweak = tweak;
    }

    public float getPackedTweak() {
        return this.tweak;
    }

    public Color getTweak() {
        Color.abgr8888ToColor(tempColor, tweak);
        return tempColor;
    }

    public void setTweakReset() {
        setPackedTweak(tweak_reset);
    }

    public void setAllReset() {
        setTweakReset();
        setColorReset();
        setBlendFunctionReset();
    }

    public void setColorResetValues(final float r, final float g, final float b, final float a) {
        this.color_reset = colorPackedRGBA(r, g, b, a);
        this.setColorReset();
    }

    public void setTweakResetValues(final float h, final float s, final float l, final float c) {
        this.tweak_reset = colorPackedRGBA(h, s, l, c);
        this.setTweakReset();
    }

}
