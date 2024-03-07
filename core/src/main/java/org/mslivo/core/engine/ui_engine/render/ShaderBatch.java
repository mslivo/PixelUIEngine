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
    private FloatBuffer vertices;
    private float offsetX;
    private float offsetY;

    private static final String VERTEX = """
               attribute vec4 a_position;
               uniform mat4 u_projTrans;
            
               void main() {
                   gl_Position = u_projTrans * a_position;
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
        this.vertices = BufferUtils.newFloatBuffer(6);
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

    public void drawPoint(float x, float y){
        Gdx.gl.glUniform4f(shader.getUniformLocation("u_color"), currentColor.r, currentColor.g, currentColor.b, currentColor.a);
        vertices.put(x+offsetX);
        vertices.put(y+offsetY);
        vertices.position(0);
        Gdx.gl.glEnableVertexAttribArray(0);
        Gdx.gl.glVertexAttribPointer(0, 2, GL20.GL_FLOAT, false, 0, vertices);
        Gdx.gl.glDrawArrays(GL20.GL_POINTS, 0, 1);
    }

    public void drawLine(float x1, float y1,float x2, float y2){
        Gdx.gl.glUniform4f(shader.getUniformLocation("u_color"), currentColor.r, currentColor.g, currentColor.b, currentColor.a);
        vertices.put(x1+offsetX);
        vertices.put(y1+offsetY);
        vertices.put(x2+offsetX);
        vertices.put(y2+offsetY);
        vertices.position(0);
        Gdx.gl.glEnableVertexAttribArray(0);
        Gdx.gl.glVertexAttribPointer(0, 2, GL20.GL_FLOAT, false, 0, vertices);
        Gdx.gl.glDrawArrays(GL20.GL_LINES, 0, 2);
    }

    public void drawTriangle(float x1, float y1,float x2, float y2,float x3, float y3){
        Gdx.gl.glUniform4f(shader.getUniformLocation("u_color"), currentColor.r, currentColor.g, currentColor.b, currentColor.a);
        vertices.put(x1+offsetX);
        vertices.put(y1+offsetY);
        vertices.put(x2+offsetX);
        vertices.put(y2+offsetY);
        vertices.put(x3+offsetX);
        vertices.put(y3+offsetY);
        vertices.position(0);
        Gdx.gl.glEnableVertexAttribArray(0);
        Gdx.gl.glVertexAttribPointer(0, 2, GL20.GL_FLOAT, false, 0, vertices);
        Gdx.gl.glDrawArrays(GL20.GL_TRIANGLES, 0, 3);
    }

    public void setOffset(float x, float y) {
        this.offsetX = offsetX;
    }

    public float getOffsetX() {
        return offsetX;
    }
    public float getOffsetY() {
        return offsetX;
    }

}