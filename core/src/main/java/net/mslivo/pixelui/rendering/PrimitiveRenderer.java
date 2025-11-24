package net.mslivo.pixelui.rendering;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL32;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.glutils.*;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.*;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Arrays;

public class PrimitiveRenderer extends CommonRenderer implements Disposable {

    public static final String VERTEX_COLOR_ATTRIBUTE = "a_vertexColor";
    public static final String COLOR_ATTRIBUTE = "a_color";
    public static final String TWEAK_ATTRIBUTE = "a_tweak";
    public static final String POSITION_ATTRIBUTE = "a_position";

    private static final int RGB_SRC = 0, RGB_DST = 1, ALPHA_SRC = 2, ALPHA_DST = 3;
    private static final int VERTEX_SIZE = 5;
    private static final int INDICES_SIZE = 1;
    private static final int VERTEXES_INDICES_RATIO = 1;
    public static final int MAX_VERTEXES_DEFAULT = 65534;
    private static final float[] DUMMY_VERTEX = new float[]{0f, 0f, 0f, 0f, 0f};
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
    private float vertexColor,vertexColor_reset,vertexColor_save;
    private int primitiveType;

    public PrimitiveRenderer() {
        this(null, MAX_VERTEXES_DEFAULT, false);
    }

    public PrimitiveRenderer(final ShaderProgram shaderProgram) {
        this(shaderProgram, MAX_VERTEXES_DEFAULT, false);
    }

    public PrimitiveRenderer(final ShaderProgram shaderProgram, final int maxVertexes) {
        this(shaderProgram, maxVertexes, false);
    }

    public PrimitiveRenderer(final ShaderProgram defaultShader, final int maxVertexes, final boolean printRenderCalls) {
        int vertexAbsoluteLimit = Integer.MAX_VALUE / (VERTEX_SIZE * 4);
        if (maxVertexes > vertexAbsoluteLimit)
            throw new IllegalArgumentException("size " + maxVertexes + " bigger than mix allowed size " + vertexAbsoluteLimit);
        if (maxVertexes % VERTEXES_INDICES_RATIO != 0)
            throw new IllegalArgumentException("size is not multiple of ratio " + VERTEXES_INDICES_RATIO);
        super();

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
        this.blendingEnabled = true;
        this.defaultShader = defaultShader != null ? defaultShader : provideDefaultShader();
        this.shader = this.defaultShader;

        this.indexResets = new IntArray();
        this.vertexColor_reset = colorPackedRGBA(0.5f,0.5f,0.5f,1f);
        this.vertexColor_save = vertexColor_reset;
        this.vertexColor = vertexColor_reset;
        this.primitiveType = GL32.GL_POINTS;
    }

    public void begin() {
        begin(GL32.GL_POINTS);
    }

    public void begin(int primitiveType) {
        this.setPrimitiveType(primitiveType);
        if (drawing) throw new IllegalStateException(ERROR_END_BEGIN);
        Gdx.gl.glEnable(GL32.GL_PRIMITIVE_RESTART_FIXED_INDEX);
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

        this.drawing = true;
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
        this.drawing = false;
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
        if (!drawing) throw new IllegalStateException(ERROR_BEGIN_END);

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

    public void vertexPush(float value1, float value2, float value3, float value4, float value5) {
        this.vertices[idx] = value1;
        this.vertices[idx + 1] = value2;
        this.vertices[idx + 2] = value3;
        this.vertices[idx + 3] = value4;
        this.vertices[idx + 4] = value5;
        idx += 5;
    }

    public boolean isDrawing(){
        return drawing;
    }

    public void vertexPush(float[] value, int offset, int count) {
        System.arraycopy(value, offset, this.vertices, idx, count);
        idx += count;
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
        if (!drawing) throw new IllegalStateException(ERROR_BEGIN_END);

        // Vertex 1
        if (this.idx >= this.sizeMaxVertexesFloats) {
            flush();
        }

        vertexPush((x + 0.5f),(y + 0.5f),this.vertexColor,this.color,this.tweak);

    }

    public void vertex(final float x1, final float y1, final float x2, final float y2) {
        if (!drawing) throw new IllegalStateException(ERROR_BEGIN_END);

        // Vertex 1
        if (this.idx >= this.sizeMaxVertexesFloats) {
            flush();
        }

        vertexPush((x1 + 0.5f),(y1 + 0.5f),this.vertexColor,this.color,this.tweak);


        // Vertex 2
        if (this.idx >= this.sizeMaxVertexesFloats) {
            flush();
        }

        vertexPush((x2 + 0.5f),(y2 + 0.5f),this.vertexColor,this.color,this.tweak);

    }

    public void vertex(final float x1, final float y1, final float x2, final float y2, final float x3, final float y3) {
        if (!drawing) throw new IllegalStateException(ERROR_BEGIN_END);

        // Vertex 1
        if (this.idx >= this.sizeMaxVertexesFloats) {
            flush();
        }

        vertexPush((x1 + 0.5f),(y1 + 0.5f),this.vertexColor,this.color,this.tweak);


        // Vertex 2
        if (this.idx >= this.sizeMaxVertexesFloats) {
            flush();
        }

        vertexPush((x2 + 0.5f),(y2 + 0.5f),this.vertexColor,this.color,this.tweak);

        // Vertex 3
        if (this.idx >= this.sizeMaxVertexesFloats) {
            flush();
        }

        vertexPush((x3 + 0.5f),(y3 + 0.5f),this.vertexColor,this.color,this.tweak);

    }

    protected ShaderProgram provideDefaultShader() {
        return ShaderParser.parse(ShaderParser.SHADER_TEMPLATE.PRIMITIVE,"""
            // Usable Vertex Shader Variables: vec4 a_position | vec4 v_color | vec4 v_tweak | vec4 v_vertexColor
            // Usable Fragment Shader Variables: vec4 v_color | vec4 v_tweak | vec4 v_vertexColor
            
            // BEGIN VERTEX
            
            vec4 colorTintAdd(vec4 color, vec4 modColor){
                 color.rgb = clamp(color.rgb+(modColor.rgb-0.5),0.0,1.0);
                 color.a *= modColor.a;
                 return color;
            }
            
            void main(){
            	 v_vertexColor = colorTintAdd(v_vertexColor, v_color);
            }
            
            // END VERTEX

            // BEGIN FRAGMENT
            
            void main(){
            	 gl_FragColor = v_vertexColor;
            }
            // END FRAGMENT
            """);
    }


    protected VertexBufferObjectWithVAO createVertexBufferObject(final int size) {
        return new VertexBufferObjectWithVAO( true, size * VERTEX_SIZE,
                new VertexAttribute(VertexAttributes.Usage.Position, 2, POSITION_ATTRIBUTE),
                new VertexAttribute(VertexAttributes.Usage.ColorPacked, 4, VERTEX_COLOR_ATTRIBUTE),
                new VertexAttribute(VertexAttributes.Usage.ColorPacked, 4, COLOR_ATTRIBUTE),
                new VertexAttribute(VertexAttributes.Usage.ColorPacked, 4, TWEAK_ATTRIBUTE));
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
    public void saveState() {
        super.saveState();
        this.vertexColor_save = this.vertexColor;
    }

    @Override
    public void loadState() {
        super.loadState();
        setPackedVertexColor(this.vertexColor_save);
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
