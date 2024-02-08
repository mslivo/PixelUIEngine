package org.mslivo.core.engine.ui_engine.misc.render;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ImmediateModeRenderer20;
import com.badlogic.gdx.math.Matrix4;

public class ImmediateRenderer {
    private Matrix4 projection;
    private ImmediateModeRenderer20 renderer20;
    private Color color;

    public ImmediateRenderer(int resolutionWidth, int resolutionHeight) {
        this.renderer20 = new ImmediateModeRenderer20(resolutionWidth * resolutionHeight, false, true, 0);
        this.color = new Color(Color.WHITE);
        renderer20.color(this.color);
    }

    public void setProjectionMatrix(Matrix4 projection) {
        this.projection = projection;
    }

    public void begin() {
        renderer20.begin(this.projection, GL20.GL_POINTS);
    }

    public void end() {
        renderer20.end();
    }

    public void setColor(Color color) {
        setColor(color.r, color.g, color.b, color.a);
    }

    public void setColor(float r, float g, float b, float a) {
        this.color.set(r, g, b, a);
        renderer20.color(this.color.r, this.color.g, this.color.b, this.color.a);
    }

    public Color getColor() {
        return this.color;
    }

    public void dispose() {
        projection = null;
        renderer20.dispose();
    }

    public void drawPixel(float x, float y) {
        renderer20.vertex(x, y, 0);
    }

    public void drawPixel(int x, int y) {
        renderer20.vertex(x, y, 0);
    }

}
