package net.mslivo.pixelui.rendering;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL32;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.VertexBufferObjectWithVAO;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.IntArray;
import net.mslivo.pixelui.utils.Tools;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public class PrimitiveRenderer extends CommonRenderer implements Disposable {

    public static final String VERTEX_COLOR_ATTRIBUTE = "a_vertexColor";
    public static final String POSITION_ATTRIBUTE = "a_position";

    private static final float VERTEX_COLOR_RESET = colorPackedRGBA(0.5f, 0.5f, 0.5f, 1f);

    private static final int VERTEX_SIZE = 3;
    private static final int INDICES_SIZE = 1;
    private static final int VERTEXES_INDICES_RATIO = 1;
    public static final int MAX_VERTEXES_DEFAULT = 65534;
    private static final float[] DUMMY_VERTEX = new float[]{0f, 0f, 0f};
    private static final int PRIMITIVE_RESTART = -1;

    private final int sizeMaxVertexes;
    private final int sizeMaxIndices;
    private final int sizeMaxVertexesFloats;
    private final float[] vertices;
    private int idx;
    private final FloatBuffer vertexBuffer;
    private final IntBuffer indexBuffer;
    private final VertexBufferObjectWithVAO vertexBufferObject;
    private final IntegerIndexBufferObject indexBufferObject;
    private final IntArray indexResets;
    private float vertexColor,vertexColor_save;
    private int primitiveType;

    public PrimitiveRenderer() {
        this(null, MAX_VERTEXES_DEFAULT);
    }

    public PrimitiveRenderer(final ShaderProgram shaderProgram) {
        this(shaderProgram, MAX_VERTEXES_DEFAULT);
    }

    public PrimitiveRenderer(final ShaderProgram defaultShader, final int maxVertexes) {
        int vertexAbsoluteLimit = Integer.MAX_VALUE / (VERTEX_SIZE * 4);
        if (maxVertexes > vertexAbsoluteLimit)
            throw new IllegalArgumentException("size " + maxVertexes + " bigger than mix allowed size " + vertexAbsoluteLimit);
        if (maxVertexes % VERTEXES_INDICES_RATIO != 0)
            throw new IllegalArgumentException("size is not multiple of ratio " + VERTEXES_INDICES_RATIO);
        super(defaultShader);

        this.sizeMaxVertexes = maxVertexes;
        this.sizeMaxVertexesFloats = this.sizeMaxVertexes * VERTEX_SIZE;
        this.sizeMaxIndices = this.sizeMaxVertexes / VERTEXES_INDICES_RATIO;
        this.vertexBufferObject = createVertexBufferObject(this.sizeMaxVertexes);
        this.vertexBuffer = this.vertexBufferObject.getBuffer(true);
        this.vertexBuffer.limit(this.sizeMaxVertexesFloats);
        this.vertices = new float[this.sizeMaxVertexesFloats];
        this.idx = 0;
        this.indexBufferObject = createIndexBufferObject(this.sizeMaxVertexes);
        this.indexBuffer = this.indexBufferObject.getBuffer(true);

        this.indexResets = new IntArray();
        this.vertexColor = VERTEX_COLOR_RESET;
        this.vertexColor_save = this.vertexColor;
        this.primitiveType = GL32.GL_POINTS;

    }

    public void begin() {
        begin(GL32.GL_POINTS);
    }

    public void begin(int primitiveType) {
        if (isDrawing()) throw new IllegalStateException(ERROR_END_BEGIN);
        Gdx.gl.glDepthMask(false);
        this.shader.bind();
        setupMatrices();
        // Blending
        if (this.blendingEnabled) {
            Gdx.gl.glEnable(GL32.GL_BLEND);
            Gdx.gl.glBlendFuncSeparate(this.blend[RGB_SRC], this.blend[RGB_DST], this.blend[ALPHA_SRC], this.blend[ALPHA_DST]);
        } else {
            Gdx.gl.glDisable(GL32.GL_BLEND);
        }
        this.setPrimitiveType(primitiveType);
        Gdx.gl.glEnable(GL32.GL_PRIMITIVE_RESTART_FIXED_INDEX);
        setDrawing(true);
    }

    protected void setPrimitiveType(int primitiveType) {
        if (primitiveType == this.primitiveType)
            return;
        this.flush();
        this.primitiveType = primitiveType;
    }

    public void end() {
        flush();
        Gdx.gl.glDepthMask(true);
        setDrawing(false);
        Gdx.gl.glDisable(GL32.GL_PRIMITIVE_RESTART_FIXED_INDEX);
    }

    protected IntegerIndexBufferObject createIndexBufferObject(final int size) {
        final int[] indices = new int[size * INDICES_SIZE];

        for (int i = 0; i < size; i++)
            indices[i] = i;

        IntegerIndexBufferObject indexBufferObject = new IntegerIndexBufferObject(true, size);
        indexBufferObject.setIndices(indices, 0, indices.length);

        return indexBufferObject;
    }

    public void primitiveRestart() {
        if (!isDrawing()) throw new IllegalStateException(ERROR_BEGIN_END);

        if (this.idx >= sizeMaxVertexesFloats) {
            flush();
        }

        // Insert Restart Index
        final int currentIndex = this.idx / VERTEX_SIZE;
        IntBuffer indicesBuffer = indexBufferObject.getBuffer(true);
        indicesBuffer.put(currentIndex, PRIMITIVE_RESTART);

        // Insert Dummy Vertex
        vertexPush(DUMMY_VERTEX,0,VERTEX_SIZE);

        this.indexResets.add(currentIndex);
    }

    private void vertexPush(float x, float y, float vertexColor) {
        this.vertices[idx] = x;
        this.vertices[idx + 1] = y;
        this.vertices[idx + 2] = vertexColor;
        idx += VERTEX_SIZE;
    }

    private void vertexPush(float[] value, int offset, int count) {
        System.arraycopy(value, offset, this.vertices, idx, count);
        idx += count;
    }

    private boolean isVertexBufferLimitReached() {
        return this.idx >= this.sizeMaxVertexesFloats;
    }

    public void flush() {
        if (idx == 0) return;

        // Bind Vertices
        this.vertexBufferObject.setVertices(vertices, 0, idx);
        this.vertexBufferObject.bind(this.shader);

        // Bind Indices
        final int verticesCount = (idx / VERTEX_SIZE);
        final int indicesCount = (verticesCount / VERTEXES_INDICES_RATIO) * INDICES_SIZE;

        this.indexBuffer.position(0);
        if (this.indexBuffer.limit() != indicesCount) {
            this.indexBuffer.limit(indicesCount);
            this.indexBufferObject.getBuffer(true); // Make Dirty
        }
        this.indexBufferObject.bind();

        // Draw
        Gdx.gl32.glDrawElements(this.primitiveType, indicesCount, GL32.GL_UNSIGNED_INT, 0);

        // reset
        this.indexBuffer.limit(this.sizeMaxIndices);
        this.idx = 0;

        if (!indexResets.isEmpty()) {
            for (int i = indexResets.size - 1; i >= 0; i--) {
                final int resetIndex = indexResets.items[i];
                indexBufferObject.getBuffer(true).put(resetIndex, resetIndex);
                indexResets.removeIndex(i);
            }
        }

    }

    public void vertex(final float x, final float y) {
        if (!isDrawing()) throw new IllegalStateException(ERROR_BEGIN_END);

        // Vertex 1
        if (isVertexBufferLimitReached())
            flush();

        vertexPush((x + 0.5f),(y + 0.5f),this.vertexColor);

    }

    public void vertex(final float x1, final float y1, final float x2, final float y2) {
        if (!isDrawing()) throw new IllegalStateException(ERROR_BEGIN_END);

        // Vertex 1
        if (isVertexBufferLimitReached())
            flush();


        vertexPush((x1 + 0.5f),(y1 + 0.5f),this.vertexColor);


        // Vertex 2
        if (this.idx >= this.sizeMaxVertexesFloats) {
            flush();
        }

        vertexPush((x2 + 0.5f),(y2 + 0.5f),this.vertexColor);

    }

    public void vertex(final float x1, final float y1, final float x2, final float y2, final float x3, final float y3) {
        if (!isDrawing()) throw new IllegalStateException(ERROR_BEGIN_END);

        // Vertex 1
        if (isVertexBufferLimitReached())
            flush();


        vertexPush((x1 + 0.5f),(y1 + 0.5f),this.vertexColor);


        // Vertex 2
        if (this.idx >= this.sizeMaxVertexesFloats) {
            flush();
        }

        vertexPush((x2 + 0.5f),(y2 + 0.5f),this.vertexColor);

        // Vertex 3
        if (this.idx >= this.sizeMaxVertexesFloats) {
            flush();
        }

        vertexPush((x3 + 0.5f),(y3 + 0.5f),this.vertexColor);

    }

    protected ShaderProgram provideDefaultShader() {
        return ShaderParser.parse(Tools.File.findResource("shaders/pixelui/default.primitive.glsl"));
    }


    protected VertexBufferObjectWithVAO createVertexBufferObject(final int size) {
        return new VertexBufferObjectWithVAO( true, size * VERTEX_SIZE,
                new VertexAttribute(VertexAttributes.Usage.Position, 2, POSITION_ATTRIBUTE),
                new VertexAttribute(VertexAttributes.Usage.ColorPacked, 4, VERTEX_COLOR_ATTRIBUTE)
        );
    }

    public void setVertexColor(Color color) {
        this.vertexColor = colorPackedRGBA(color.r, color.g, color.b, color.a);
    }

    public void setVertexColor(Color color, final float alpha) {
        this.vertexColor = colorPackedRGBA(color.r, color.g, color.b, alpha);
    }

    public void setVertexColor(final float r, final float g, final float b, final float alpha) {
        this.vertexColor = colorPackedRGBA(r, g, b, alpha);
    }

    public void setPackedVertexColor(final float vertexColor) {
        this.vertexColor = vertexColor;
    }

    public Color getVertexColor() {
        Color.abgr8888ToColor(tempColor, vertexColor);
        return tempColor;
    }

    public float getPackedVertexColor() {
        return this.vertexColor;
    }

    @Override
    protected void setBlendFuncSeparateImpl(int srcColor, int dstColor, int srcAlpha, int dstAlpha) {
        Gdx.gl.glBlendFuncSeparate(srcColor, dstColor,srcAlpha,dstAlpha);
    }

    @Override
    protected void setBlendFuncImpl(int srcColor, int dstColor) {
        Gdx.gl.glBlendFunc(srcColor, dstColor);
    }

    @Override
    protected void saveStateImpl() {
        this.vertexColor_save = this.vertexColor;
    }

    @Override
    protected void loadStateImpl() {
        this.vertexColor = this.vertexColor_save;
    }

    @Override
    protected void resetImpl() {
        this.vertexColor = VERTEX_COLOR_RESET;
    }

    public int getPrimitiveType() {
        return primitiveType;
    }

    @Override
    public void dispose() {
        vertexBufferObject.dispose();
        indexBufferObject.dispose();
        if (this.shader == this.defaultShader)
            this.shader.dispose();
    }


}
