package org.mslivo.core.engine.ui_engine.misc.render;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ImmediateModeRenderer20;
import com.badlogic.gdx.math.Matrix4;
import org.lwjgl.opengl.GL30;

public class ImmediateRenderer {
    private Matrix4 projection;
    private ImmediateModeRenderer20 renderer20;
    public ImmediateRenderer(int resolutionWidth, int resolutionHeight) {
        this.renderer20 = new ImmediateModeRenderer20(resolutionWidth*resolutionHeight, false, true, 0);
    }

    public void setProjectionMatrix(Matrix4 projection){
        this.projection = projection;
    }

    public void begin(){
        renderer20.begin(this.projection, GL30.GL_POINTS);
    }

    public void end(){
        renderer20.end();
    }
    public void setColor(Color color){
        renderer20.color(color.r,color.g,color.b,color.a);
    }
    public void setColor(float r, float g, float b, float a){
        renderer20.color(r,g,b,a);
    }
    public void drawPixel(float x, float y){
        renderer20.vertex(x,y,0);
    }

    public void drawPixel(int x, int y){
        renderer20.vertex(x,y,0);
    }

    public void dispose(){
        projection = null;
        renderer20.dispose();
    }

}
