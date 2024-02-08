package org.mslivo.core.engine.ui_engine.misc.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ImmediateModeRenderer20;
import com.badlogic.gdx.math.Matrix4;

public class ImmediateRenderer {
    private Matrix4 projection;
    private ImmediateModeRenderer20 renderer20;
    private Color color;
    private boolean blend;
    private final int meshResizeStep;

    public ImmediateRenderer(int resolutionWidth, int resolutionHeight) {
        meshResizeStep = resolutionWidth * resolutionHeight;
        this.renderer20 = new ImmediateModeRenderer20(meshResizeStep, false, true, 0);
        this.color = new Color(Color.WHITE);
        renderer20.color(this.color);
    }

    public void setProjectionMatrix(Matrix4 projection) {
        this.projection = projection;
    }

    public void begin() {
        blend = Gdx.gl.glIsEnabled(GL20.GL_BLEND);
        Gdx.gl.glEnable(GL20.GL_BLEND);
        renderer20.begin(this.projection, GL20.GL_POINTS);
    }

    public void end() {
        renderer20.end();
        if(blend){
            Gdx.gl.glEnable(GL20.GL_BLEND);
        }else{
            Gdx.gl.glDisable(GL20.GL_BLEND);
        }
    }

    public void setColor(Color color) {
        setColor(color.r, color.g, color.b, color.a);
    }

    public void setColor(float r, float g, float b, float a) {
        this.color.set(r, g, b, a);
    }

    public Color getColor() {
        return this.color;
    }

    public void dispose() {
        projection = null;
        renderer20.dispose();
    }

    public void drawPixel(float x, float y) {
        checkMeshSize(1);
        renderer20.color(color);
        renderer20.vertex(x, y, 0);
    }

    public void drawPixel(int x, int y) {
        checkMeshSize(1);
        renderer20.color(color);
        renderer20.vertex(x, y, 0);
    }

    private void checkMeshSize(int vertices){
        // Resize ImmediateRenderer Mesh if needed
        if((renderer20.getNumVertices()+vertices) > renderer20.getMaxVertices()){
            this.renderer20.dispose();
            this.renderer20 = new ImmediateModeRenderer20(renderer20.getMaxVertices()+meshResizeStep, false, true, 0);
        }
    }

}
