package net.mslivo.core.engine.ui_engine.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.NumberUtils;

public class ImmediateRenderer {
    private static final String VERTEX = """
                attribute vec4 a_position;
                attribute vec4 a_color;
                uniform mat4 u_projModelView;
                varying vec4 v_col;
                void main() {
                   gl_Position = u_projModelView * a_position;
                   v_col = a_color;
                   v_col.a *= 255.0 / 254.0;
                   gl_PointSize = 1.0;
                }
            """;
    private static final String FRAGMENT = """
                #ifdef GL_ES
                precision mediump float;
                #endif
                varying vec4 v_col;
                void main() {
                   gl_FragColor = v_col;
                }
            """;
    private int primitiveType;
    private final int MESH_RESIZE_STEP = 5000 * 4;
    private Matrix4 projection;
    private Color color;
    private boolean blend;
    private ShaderProgram shader;
    private Mesh mesh;
    private float vertices[];
    private int colorOffset, vertexIdx, vertexSize;
    private int uProjModelViewLocation;
    private boolean drawing;

    public ImmediateRenderer() {
        this.primitiveType = GL20.GL_POINTS;
        this.blend = false;
        this.color = new Color(Color.WHITE);
        this.shader = new ShaderProgram(VERTEX, FRAGMENT);
        this.uProjModelViewLocation = shader.getUniformLocation("u_projModelView");
        if (!shader.isCompiled()) throw new GdxRuntimeException("Error compiling shader: " + shader.getLog());


        this.vertices = new float[MESH_RESIZE_STEP];
        this.mesh = createMesh(MESH_RESIZE_STEP);
        this.colorOffset = mesh.getVertexAttribute(VertexAttributes.Usage.ColorPacked).offset / 4;
        this.vertexIdx = 0;
        this.vertexSize = mesh.getVertexAttributes().vertexSize / 4;
        this.drawing = false;
    }

    public void setProjectionMatrix(Matrix4 projection) {
        this.projection = projection;
    }

    public void begin() {
        begin(GL20.GL_POINTS);
    }

    public void begin(int primitiveType) {
        if (drawing) throw new IllegalStateException("ImmediateRenderer.end must be called before begin.");
        this.primitiveType = primitiveType;
        this.blend = Gdx.gl.glIsEnabled(GL20.GL_BLEND);
        if (!blend) Gdx.gl.glEnable(GL20.GL_BLEND);
        this.drawing = true;
    }

    public void end() {
        if (!drawing) throw new IllegalStateException("ImmediateRenderer.begin must be called before end.");
        this.drawing = false;
        if (vertexIdx == 0) return;
        shader.bind();
        shader.setUniformMatrix(uProjModelViewLocation, this.projection);
        mesh.setVertices(vertices, 0, vertexIdx);
        mesh.render(shader, this.primitiveType);
        vertexIdx = 0;
        this.drawing = false;
    }

    public void setColor(Color color) {
        setColor(color.r, color.g, color.b, color.a);
    }

    public void setColor(float r, float g, float b) {
        setColor(r, g, b, 1f);
    }

    public void setColor(float r, float g, float b, float a) {
        this.color.set(r, g, b, a);
    }

    public Color getColor() {
        return this.color;
    }

    public void dispose() {
        this.mesh.dispose();
        projection = null;
    }

    public void vertex(float x, float y) {
        if (!drawing) throw new IllegalStateException("ImmediateRenderer.begin must be called before draw.");
        checkMeshSize(vertexSize);
        vertices[vertexIdx + colorOffset] = NumberUtils.intToFloatColor(((int)(255 * this.color.a) << 24) | ((int)(255 * this.color.b) << 16) | ((int)(255 * this.color.g) << 8) | ((int)(255 * this.color.r)));
        vertices[vertexIdx] = x;
        vertices[vertexIdx + 1] = y;
        vertices[vertexIdx + 2] = 0;
        vertexIdx += vertexSize;
    }

    public void vertex(float x, float y, float x2, float y2) {
        vertex(x, y);
        vertex(x2, y2);
    }

    public void vertex(float x, float y, float x2, float y2, float x3, float y3) {
        vertex(x, y);
        vertex(x2, y2);
        vertex(x3, y3);
    }

    public boolean isDrawing() {
        return drawing;
    }

    private void checkMeshSize(int size) {
        if ((vertexIdx + size) > mesh.getMaxVertices()) {
            int newSize = mesh.getMaxVertices() + MESH_RESIZE_STEP;
            float[] newVertices = new float[newSize];
            System.arraycopy(vertices, 0, newVertices, 0, vertices.length);
            this.vertices = newVertices;
            Mesh newMesh = createMesh(newSize);
            mesh.dispose();
            mesh = newMesh;
        }
    }

    private Mesh createMesh(int maxVertices) {
        return new Mesh(false, maxVertices, 0,
                new VertexAttribute(VertexAttributes.Usage.Position, 3, ShaderProgram.POSITION_ATTRIBUTE),
                new VertexAttribute(VertexAttributes.Usage.ColorPacked, 4, ShaderProgram.COLOR_ATTRIBUTE)
        );
    }

}
