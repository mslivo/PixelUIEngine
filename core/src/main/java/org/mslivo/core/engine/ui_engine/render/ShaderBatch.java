package org.mslivo.core.engine.ui_engine.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.BufferUtils;

import java.nio.FloatBuffer;

public class ShaderBatch {
    private ShaderProgram shader;
    private Mesh mesh;
    private Matrix4 projection;
    private Color currentColor;
    private boolean blendEnabled;
    private float offsetX;
    private float offsetY;

    private static final String VERTEX = """
                attribute vec4 a_position;
                uniform mat4 u_projTrans;
                uniform vec2 u_pointPosition;

                void main() {
                    gl_Position = u_projTrans * (a_position + vec4(u_pointPosition, 0.0, 0.0));
                    gl_PointSize = 1.0;
                }
            """;

    private static final String FRAGMENT = """
                #ifdef GL_ES
                    precision mediump float;
                #endif

                uniform vec4 u_color;

                void main() {
                    gl_FragColor = u_color;
                }
            """;


    public ShaderBatch() {
        this.shader = new ShaderProgram(VERTEX, FRAGMENT);
        this.mesh = new Mesh(true, 3, 0, new VertexAttribute(VertexAttributes.Usage.Position, 2, "a_position"));
        this.projection = new Matrix4();
        this.currentColor = new Color(Color.WHITE);
        this.offsetX = 0;
        this.offsetY = 0;
    }

    public void setProjectionMatrix(Matrix4 projection) {
        this.projection.set(projection);
    }

    public void begin() {
        blendEnabled = Gdx.gl.glIsEnabled(GL20.GL_BLEND);
        shader.bind();
        if(!blendEnabled) Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glUniformMatrix4fv(shader.getUniformLocation("u_projTrans"), 1, false, this.projection.val, 0);
    }

    public void end() {
        if(!blendEnabled) Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    public void dispose() {
        projection = null;
        currentColor = null;
        mesh.dispose();
        shader.dispose();
    }

    public Color getColor() {
        return this.currentColor;
    }

    public void setColor(Color lastColor) {
        setColor(lastColor.r, lastColor.g, lastColor.b, lastColor.a);
    }

    public void setColor(float r, float g, float b, float a) {
        currentColor.set(r,g,b,a);
    }

    public void drawPoint(float x, float y) {
        Gdx.gl.glEnable(GL20.GL_BLEND);
        shader.setUniformf("u_color", currentColor.r, currentColor.g, currentColor.b, currentColor.a);
        shader.setUniformf("u_pointPosition", x, y);
        Gdx.gl.glDrawArrays(GL20.GL_POINTS, 0,1);
    }

    public void setOffset(float x, float y) {
        this.offsetX = offsetX;
    }

    public float getOffsetX() {
        return offsetX;
    }
    public float getOffsetY() {
        return offsetY;
    }

}