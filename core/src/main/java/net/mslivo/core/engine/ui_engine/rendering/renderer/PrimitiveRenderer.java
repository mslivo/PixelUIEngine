package net.mslivo.core.engine.ui_engine.rendering.renderer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL32;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.VertexBufferObjectWithVAO;
import com.badlogic.gdx.graphics.glutils.VertexData;
import com.badlogic.gdx.utils.IntArray;
import net.mslivo.core.engine.ui_engine.rendering.IntegerIndexBufferObject;
import net.mslivo.core.engine.ui_engine.rendering.shader.PrimitiveShader;

import java.nio.IntBuffer;

public class PrimitiveRenderer extends BasicColorTweakRenderer {

    public static final String VERTEX_COLOR_ATTRIBUTE = "a_vertexColor";

    private static final int VERTEX_SIZE = 5;
    private static final int INDICES_SIZE = 1;
    private static final int VERTEXES_INDICES_RATIO = 1;

    public static final int MAX_VERTEXES_DEFAULT = 65534;

    private static final int PRIMITIVE_RESTART = -1;
    private static final PrimitiveShader DEFAULT_SHADER = new PrimitiveShader("""
            VERTEX:
            
            vec4 colorTintAdd(vec4 color, vec4 modColor){
                 color.rgb = clamp(color.rgb+(modColor.rgb-0.5),0.0,1.0);
                 color.a *= modColor.a;
                 return color;
            }
            
            void main(){
            	 v_vertexColor = colorTintAdd(v_vertexColor, v_color);
            }
            FRAGMENT:
            void main(){
            	 gl_FragColor = v_vertexColor;
            }
            """);


    private final IntArray indexResets;
    private boolean restartInsertedLast;
    private float vertexColor;
    private float vertexColor_reset;
    private float vertexColor_save;

    public PrimitiveRenderer(final ShaderProgram shaderProgram) {
        this(shaderProgram, MAX_VERTEXES_DEFAULT, false);
    }

    public PrimitiveRenderer(final ShaderProgram shaderProgram, final int maxVertexes) {
        this(shaderProgram, maxVertexes,false);
    }

    public PrimitiveRenderer(final ShaderProgram shaderProgram, final int maxVertexes, final boolean printRenderCalls) {
        super(maxVertexes, shaderProgram, printRenderCalls);
        this.indexResets = new IntArray();
        this.restartInsertedLast = false;
        this.vertexColor_reset = colorPackedRGBA(1f, 1f, 1f, 1f);
        this.vertexColor_save = vertexColor_reset;
        this.vertexColor = vertexColor_reset;
    }

    public void begin() {
        begin(GL32.GL_POINTS);
    }

    public void begin(int primitiveType) {
        this.setPrimitiveType(primitiveType);
        super.begin();
        Gdx.gl.glEnable(GL32.GL_PRIMITIVE_RESTART_FIXED_INDEX);
    }

    @Override
    protected int getVertexSize() {
        return VERTEX_SIZE;
    }

    @Override
    protected int getIndicesSize() {
        return INDICES_SIZE;
    }

    @Override
    protected int getVertexIndicesRatio() {
        return VERTEXES_INDICES_RATIO;
    }

    @Override
    protected IntegerIndexBufferObject createIndexData(final int size) {
        final int[] indices = new int[size*INDICES_SIZE];

        for (int i = 0; i < size; i++)
            indices[i] = i;

        IntegerIndexBufferObject indexBufferObject = new IntegerIndexBufferObject(true, size);
        indexBufferObject.setIndices(indices, 0, indices.length);

        return indexBufferObject;
    }

    public void primitiveRestart() {
        if (!drawing) throw new IllegalStateException(ERROR_BEGIN_END);
        if (this.restartInsertedLast)
            return;
        if (isVertexLimitReached()) {
            flush();
        }

        // Insert Restart Index

        final int currentIndex = idx / vertexSize;

        IntBuffer intBuffer = indexData.getBuffer(true);
        intBuffer.limit(this.maxVertexes);
        intBuffer.put(currentIndex, PRIMITIVE_RESTART);

        // Insert Dummy Vertex

        vertices[idx] = 0f;
        vertices[idx + 1] = 0f;
        vertices[idx + 2] = 0f;
        vertices[idx + 3] = 0f;
        vertices[idx + 4] = 0f;
        idx += VERTEX_SIZE;

        this.indexResets.add(currentIndex);
        this.restartInsertedLast = true;
    }


    public void flush() {
        if (idx == 0) return;
        super.flush();

        if (!indexResets.isEmpty()) {
            final IntBuffer indexBuffer = indexData.getBuffer(true);
            for (int i = indexResets.size - 1; i >= 0; i--) {
                final int resetIndex = indexResets.items[i];
                indexBuffer.put(resetIndex, resetIndex);
                indexResets.removeIndex(i);
            }
        }

    }

    public void vertex(final float x, final float y) {
        if (!drawing) throw new IllegalStateException(ERROR_BEGIN_END);

        // Vertex 1
        if (isVertexLimitReached()) {
            flush();
        }

        vertices[idx] = (x + 0.5f);
        vertices[idx + 1] = (y + 0.5f);
        vertices[idx + 2] = this.vertexColor;
        vertices[idx + 3] = super.color;
        vertices[idx + 4] = super.tweak;

        idx += VERTEX_SIZE;
        this.restartInsertedLast = false;
    }

    public void vertex(final float x1, final float y1, final float x2, final float y2) {
        if (!drawing) throw new IllegalStateException(ERROR_BEGIN_END);

        // Vertex 1
        if (isVertexLimitReached()) {
            flush();
        }

        vertices[idx] = (x1 + 0.5f);
        vertices[idx + 1] = (y1 + 0.5f);
        vertices[idx + 2] = this.vertexColor;
        vertices[idx + 3] = super.color;
        vertices[idx + 4] = super.tweak;

        idx += VERTEX_SIZE;

        // Vertex 2
        if (isVertexLimitReached()) {
            flush();
        }

        vertices[idx] = (x2 + 0.5f);
        vertices[idx + 1] = (y2 + 0.5f);
        vertices[idx + 2] = this.vertexColor;
        vertices[idx + 3] = super.color;
        vertices[idx + 4] = super.tweak;

        idx += VERTEX_SIZE;
        this.restartInsertedLast = false;
    }

    public void vertex(final float x1, final float y1, final float x2, final float y2, final float x3, final float y3) {
        if (!drawing) throw new IllegalStateException(ERROR_BEGIN_END);

        // Vertex 1
        if (isVertexLimitReached()) {
            flush();
        }

        vertices[idx] = (x1 + 0.5f);
        vertices[idx + 1] = (y1 + 0.5f);
        vertices[idx + 2] = this.vertexColor;
        vertices[idx + 3] = super.color;
        vertices[idx + 4] = super.tweak;

        idx += VERTEX_SIZE;

        // Vertex 2
        if (isVertexLimitReached()) {
            flush();
        }

        vertices[idx] = (x2 + 0.5f);
        vertices[idx + 1] = (y2 + 0.5f);
        vertices[idx + 2] = this.vertexColor;
        vertices[idx + 3] = super.color;
        vertices[idx + 4] = super.tweak;

        idx += VERTEX_SIZE;

        // Vertex 3
        if (isVertexLimitReached()) {
            flush();
        }

        vertices[idx] = (x3 + 0.5f);
        vertices[idx + 1] = (y3 + 0.5f);
        vertices[idx + 2] = this.vertexColor;
        vertices[idx + 3] = super.color;
        vertices[idx + 4] = super.tweak;

        idx += VERTEX_SIZE;
        this.restartInsertedLast = false;
    }

    @Override
    protected ShaderProgram provideDefaultShader() {
        return DEFAULT_SHADER.compile();
    }

    @Override
    protected VertexData createVertexData(final int size) {
        final VertexData vertexData = new VertexBufferObjectWithVAO(true, size * VERTEX_SIZE,
                new VertexAttribute(VertexAttributes.Usage.Position, 2, POSITION_ATTRIBUTE),
                new VertexAttribute(VertexAttributes.Usage.ColorPacked, 4, VERTEX_COLOR_ATTRIBUTE),
                new VertexAttribute(VertexAttributes.Usage.ColorPacked, 4, COLOR_ATTRIBUTE),
                new VertexAttribute(VertexAttributes.Usage.ColorPacked, 4, TWEAK_ATTRIBUTE));
        return vertexData;
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

}
