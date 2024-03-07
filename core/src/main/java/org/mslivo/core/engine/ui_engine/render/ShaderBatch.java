package org.mslivo.core.engine.ui_engine.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.ByteArray;
import com.badlogic.gdx.utils.FloatArray;

public class ShaderBatch {
    private static final byte OPERATION_SETCOLOR = 1;
    private static final byte OPERATION_POINTS = 2;
    private static final byte OPERATION_LINE = 3;
    private static final byte OPERATION_TRIANGLE = 4;
    private ShaderProgram shader;
    private Mesh mesh;
    private Matrix4 projection;
    private Color currentColor;
    private boolean blendEnabled;
    private ByteArray opType;
    private FloatArray opParams;

    private static final String VERTEX = """
            attribute vec4 a_position;
                        uniform mat4 u_projTrans;

                        void main() {
                            gl_Position = u_projTrans * a_position;
                            gl_PointSize = 1.0; // Needed for WebGL/ANGLE issue
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


    public ShaderBatch() {
        this.shader = new ShaderProgram(VERTEX, FRAGMENT);
        this.mesh = new Mesh(true, 3, 0, new VertexAttribute(VertexAttributes.Usage.Position, 2, "a_position"));
        this.projection = new Matrix4();
        this.currentColor = new Color(Color.WHITE);
        this.opType = new ByteArray();
        this.opParams = new FloatArray();
    }

    public void setProjectionMatrix(Matrix4 projection) {
        this.projection.set(projection);
    }

    public void begin() {
        blendEnabled = Gdx.gl.glIsEnabled(GL20.GL_BLEND);
        shader.bind();
        if(!blendEnabled) Gdx.gl.glEnable(GL20.GL_BLEND);
        shader.setUniformMatrix("u_projTrans", this.projection);
    }

    public void end() {
        int paramIndex = 0;

        // Render to Screen
        for(int i = 0; i< opType.size; i++){
            byte type = opType.get(i);
            switch (type){
                case OPERATION_SETCOLOR -> {
                    currentColor.set(opParams.get(paramIndex), opParams.get(paramIndex+1), opParams.get(paramIndex+2), opParams.get(paramIndex+3));
                    paramIndex += 4;
                }
                case OPERATION_POINTS -> {
                    shader.setUniformf("u_color", currentColor.r, currentColor.g, currentColor.b, currentColor.a);
                    mesh.setVertices(opParams.items,paramIndex,2);
                    mesh.render(shader, GL20.GL_POINTS);
                    paramIndex += 2;
                }
                case OPERATION_LINE -> {
                    shader.setUniformf("u_color", currentColor.r, currentColor.g, currentColor.b, currentColor.a);
                    mesh.setVertices(opParams.items,paramIndex,4);
                    mesh.render(shader, GL20.GL_LINES);
                    paramIndex += 4;
                }
                case OPERATION_TRIANGLE -> {
                    shader.setUniformf("u_color", currentColor.r, currentColor.g, currentColor.b, currentColor.a);
                    mesh.setVertices(opParams.items,paramIndex,6);
                    mesh.render(shader, GL20.GL_TRIANGLES);
                    paramIndex += 6;
                }
            }
        }

        opType.clear();
        opParams.clear();
        if(!blendEnabled) Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    public void dispose() {
        projection = null;
        currentColor = null;
        opParams.clear();
        opParams.items = null;
        opParams.clear();
        opType.items = null;
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
        if(currentColor.r == r && currentColor.g == g && currentColor.b == b && currentColor.a == a) return;
        opType.add(OPERATION_SETCOLOR);
        opParams.add(r,g,b,a);
        currentColor.set(r,g,b,a);
    }

    public void drawPoint(float x, float y){
        opType.add(OPERATION_POINTS);
        opParams.add(x,y);
    }

    public void drawLine(float x1, float y1, float x2, float y2){
        opType.add(OPERATION_LINE);
        opParams.add(x1,y1,x2,y2);
    }

    public void drawTriangle(float x1, float y1, float x2, float y2, float x3, float y3){
        opType.add(OPERATION_TRIANGLE);
        opParams.add(x1,y1);
        opParams.add(x2,y2);
        opParams.add(x3,y3);
    }

}
