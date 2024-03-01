package org.mslivo.core.engine.ui_engine.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;

public class ShaderRenderer {
    private Matrix4 projection;
    private Color color;
    private ShaderProgram shader;
    private float[] pixel_position;
    private boolean blendEnabled;

    private static final String VERTEX = """
                attribute vec4 a_position;
                uniform mat4 u_projTrans;
                uniform vec2 u_pixelPosition; // New uniform for pixel position
                
                void main() {
                    gl_Position = u_projTrans * (a_position + vec4(u_pixelPosition, 0.0, 0.0));
                }
            """;
    private static final String FRAGMENT = """
                #ifdef GL_ES
                    precision mediump float;
                #endif
                
                uniform vec4 u_color; // Color uniform
                
                void main() {
                    gl_FragColor = u_color;
                }
            """;


    public ShaderRenderer() {
        this.shader = new ShaderProgram(VERTEX, FRAGMENT);
        this.projection = new Matrix4();
        this.color = new Color(Color.WHITE);
        this.pixel_position = new float[2];
    }

    public void setProjectionMatrix(Matrix4 projection) {
        this.projection.set(projection);
    }

    public void begin() {
        blendEnabled = Gdx.gl.glIsEnabled(GL20.GL_BLEND);
        shader.bind();
        Gdx.gl.glEnable(GL20.GL_BLEND);
        shader.setUniformMatrix("u_projTrans", this.projection);
    }

    public void end() {
        if(!blendEnabled){
            Gdx.gl.glEnable(GL20.GL_BLEND);
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
        color = null;
        shader.dispose();
    }

    public void drawPoint(float x, float y) {
        pixel_position[0] = x;
        pixel_position[1] = y;
        shader.setUniformf("u_color", color.r, color.g, color.b, color.a);
        shader.setUniformf("u_pixelPosition", x, y);
        Gdx.gl.glDrawArrays(GL20.GL_POINTS, 0,1);
    }


}
