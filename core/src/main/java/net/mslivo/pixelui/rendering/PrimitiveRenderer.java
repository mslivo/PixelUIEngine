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

public class PrimitiveRenderer implements Disposable {

    public static final String VERTEX_COLOR_ATTRIBUTE = "a_vertexColor";
    public static final String COLOR_ATTRIBUTE = "a_color";
    public static final String TWEAK_ATTRIBUTE = "a_tweak";
    public static final String POSITION_ATTRIBUTE = "a_position";
    public static final String PROJTRANS_UNIFORM = "u_projTrans";

    private static final String ERROR_END_BEGIN = ".end() must be called before begin.";
    private static final String ERROR_BEGIN_END = ".begin() must be called before end.";
    private static final int RGB_SRC = 0, RGB_DST = 1, ALPHA_SRC = 2, ALPHA_DST = 3;
    private static final int VERTEX_SIZE = 5;
    private static final int INDICES_SIZE = 1;
    private static final int VERTEXES_INDICES_RATIO = 1;
    public static final int MAX_VERTEXES_DEFAULT = 65534;
    private static final float[] DUMMY_VERTEX = new float[]{0f, 0f, 0f, 0f, 0f};
    private static final int PRIMITIVE_RESTART = -1;

    protected float tweak,tweak_save,tweak_reset;
    protected float color,color_save,color_reset;
    private int[] blend,blend_save,blend_reset;
    private boolean blendingEnabled;
    private final int sizeMaxVertexes;
    private final int sizeMaxIndices;
    private final int sizeMaxVertexesFloats;
    private final float[] vertices;
    private int idx;
    private final FloatBuffer vertexBuffer;
    private final IntBuffer indexBuffer;
    private final VertexBufferObjectWithVAO vertexBufferObject;
    private final IntegerIndexBufferObject indexBufferObject;
    private final Color tempColor;
    private final Matrix4 projectionMatrix, transformMatrix, combinedMatrix;
    private final ObjectMap<ShaderProgram, ObjectIntMap<String>> uniformLocationCache;
    private ShaderProgram shader;
    private ShaderProgram defaultShader;
    private boolean drawing;
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

        this.tweak_reset = colorPackedRGBA(0.5f, 0.5f, 0.5f, 1f);
        this.tweak_save = tweak_reset;
        this.tweak = tweak_reset;

        this.color_reset = colorPackedRGBA(0.5f, 0.5f, 0.5f, 1f);
        this.color_save = color_reset;
        this.color = color_reset;

        this.sizeMaxVertexes = maxVertexes;
        this.sizeMaxVertexesFloats = this.sizeMaxVertexes * VERTEX_SIZE;
        this.sizeMaxIndices = this.sizeMaxVertexes / VERTEXES_INDICES_RATIO;
        this.tempColor = new Color(Color.CLEAR);
        this.transformMatrix = new Matrix4();
        this.combinedMatrix = new Matrix4();
        this.projectionMatrix = new Matrix4().setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        this.vertexBufferObject = createVertexBufferObject(this.sizeMaxVertexes);
        this.vertexBuffer = this.vertexBufferObject.getBuffer(true);
        this.vertexBuffer.limit(this.sizeMaxVertexesFloats);
        this.vertices = new float[this.sizeMaxVertexesFloats];
        this.idx = 0;
        this.indexBufferObject = createIndexBufferObject(this.sizeMaxVertexes);
        this.indexBuffer = this.indexBufferObject.getBuffer(true);
        this.blend_reset = new int[]{GL32.GL_SRC_ALPHA, GL32.GL_ONE_MINUS_SRC_ALPHA, GL32.GL_ONE, GL32.GL_ONE_MINUS_SRC_ALPHA};
        this.blend = new int[]{this.blend_reset[RGB_SRC], this.blend_reset[RGB_DST], this.blend_reset[ALPHA_SRC], this.blend_reset[ALPHA_DST]};
        this.blend_save = Arrays.copyOf(this.blend_reset, this.blend_reset.length);
        this.uniformLocationCache = new ObjectMap<>();
        this.drawing = false;
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

    private void setupMatrices() {
        combinedMatrix.set(projectionMatrix).mul(transformMatrix);
        shader.setUniformMatrix(uniformLocation(PROJTRANS_UNIFORM), combinedMatrix);
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

    protected int getVertexSize() {
        return VERTEX_SIZE;
    }

    protected int getIndicesSize() {
        return INDICES_SIZE;
    }

    protected int getVertexIndicesRatio() {
        return VERTEXES_INDICES_RATIO;
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

    private int uniformLocation(String uniform) {
        ObjectIntMap uniformMap = uniformLocationCache.get(this.shader);
        if (uniformMap == null) {
            uniformMap = new ObjectIntMap<>();
            uniformLocationCache.put(this.shader, uniformMap);
        }

        int location = uniformMap.get(uniform, -1);
        if (location == -1) {
            location = this.shader.getUniformLocation(uniform);
            uniformMap.put(uniform, location);
        }
        return location;
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

    public void saveState() {
        this.color_save = this.color;
        this.tweak_save = this.tweak;
        System.arraycopy(this.blend, 0, this.blend_save, 0, 4);
        this.vertexColor_save = this.vertexColor;
    }

    public void loadState() {
        setPackedColor(this.color_save);
        setPackedTweak(this.tweak_save);
        setBlendFunctionSeparate(this.blend_save[RGB_SRC], this.blend_save[RGB_DST], this.blend_save[ALPHA_SRC], this.blend_save[ALPHA_DST]);
        setPackedVertexColor(this.vertexColor_save);
    }


    public void setColor(Color color) {
        this.color = colorPackedRGBA(color.r, color.g, color.b, color.a);
    }

    public void setColor(Color color, float alpha) {
        this.color = colorPackedRGBA(color.r, color.g, color.b, alpha);
    }

    public void setColor(final float r, final float g, final float b, final float alpha) {
        this.color = colorPackedRGBA(r, g, b, alpha);
    }

    public void setPackedColor(final float color) {
        this.color = color;
    }

    public Color getColor() {
        Color.abgr8888ToColor(tempColor, color);
        return tempColor;
    }

    public void setColorReset() {
        setPackedColor(color_reset);
    }

    public float getPackedColor() {
        return this.color;
    }

    public void setTweak(final float t1, final float t2, final float t3, final float t4) {
        tweak = colorPackedRGBA(t1, t2, t3, t4);
    }

    public void setPackedTweak(final float tweak) {
        this.tweak = tweak;
    }

    public float getPackedTweak() {
        return this.tweak;
    }

    public Color getTweak() {
        Color.abgr8888ToColor(tempColor, tweak);
        return tempColor;
    }

    public void setTweakReset() {
        setPackedTweak(tweak_reset);
    }

    public void setAllReset() {
        setTweakReset();
        setColorReset();
        setBlendFunctionReset();
    }

    public void setColorResetValues(final float r, final float g, final float b, final float a) {
        this.color_reset = colorPackedRGBA(r, g, b, a);
        this.setColorReset();
    }

    public void setTweakResetValues(final float h, final float s, final float l, final float c) {
        this.tweak_reset = colorPackedRGBA(h, s, l, c);
        this.setTweakReset();
    }

    public void setBlendFunctionResetValuesSeparate(final int blend_rgb_src, final int blend_rgb_dst, final int blend_alpha_src, final int blend_alpha_blend) {
        this.blend_reset[RGB_SRC] = blend_rgb_src;
        this.blend_reset[RGB_DST] = blend_rgb_dst;
        this.blend_reset[ALPHA_SRC] = blend_alpha_src;
        this.blend_reset[ALPHA_DST] = blend_alpha_blend;
        this.setBlendFunctionReset();
    }

    public void setBlendFunctionResetValues(final int blend_src, final int blend_dst) {
        this.blend_reset[RGB_SRC] = blend_src;
        this.blend_reset[RGB_DST] = blend_dst;
        this.blend_reset[ALPHA_SRC] = blend_src;
        this.blend_reset[ALPHA_DST] = blend_dst;
        this.setBlendFunctionReset();
    }

    public void setBlendFunctionLayer() {
        this.setBlendFunctionSeparate(GL32.GL_SRC_ALPHA, GL32.GL_ONE_MINUS_SRC_ALPHA, GL32.GL_ONE, GL32.GL_ONE_MINUS_SRC_ALPHA);
    }

    public void setBlendFunctionComposite() {
        this.setBlendFunction(GL32.GL_ONE, GL32.GL_ONE_MINUS_SRC_ALPHA);
    }

    public void setBlendFunctionReset() {
        setBlendFunctionSeparate(blend_reset[RGB_SRC], blend_reset[RGB_DST], blend_reset[ALPHA_SRC], blend_reset[ALPHA_DST]);
    }

    public void setBlendFunction(final int srcFunc, final int dstFunc) {
        if (this.blend[RGB_SRC] == srcFunc && this.blend[RGB_DST] == dstFunc && this.blend[ALPHA_SRC] == srcFunc && this.blend[ALPHA_DST] == dstFunc)
            return;
        this.blend[RGB_SRC] = srcFunc;
        this.blend[RGB_DST] = dstFunc;
        this.blend[ALPHA_SRC] = srcFunc;
        this.blend[ALPHA_DST] = dstFunc;
        if (drawing) {
            flush();
            Gdx.gl.glBlendFunc(srcFunc, dstFunc);
        }
    }

    public boolean isBlendingEnabled() {
        return this.blendingEnabled;
    }

    public void setBlendingEnabled(boolean enabled) {
        this.blendingEnabled = enabled;
    }

    public void setBlendFunctionSeparate(final int srcFuncColor, final int dstFuncColor, final int srcFuncAlpha, final int dstFuncAlpha) {
        if (this.blend[RGB_SRC] == srcFuncColor && this.blend[RGB_DST] == dstFuncColor && this.blend[ALPHA_SRC] == srcFuncAlpha && this.blend[ALPHA_DST] == dstFuncAlpha)
            return;
        this.blend[RGB_SRC] = srcFuncColor;
        this.blend[RGB_DST] = dstFuncColor;
        this.blend[ALPHA_SRC] = srcFuncAlpha;
        this.blend[ALPHA_DST] = dstFuncAlpha;
        if (drawing) {
            flush();
            Gdx.gl.glBlendFuncSeparate(srcFuncColor, dstFuncColor, srcFuncAlpha, dstFuncAlpha);
        }
    }

    public int getBlendSrcFunc() {
        return blend[RGB_SRC];
    }

    public int getBlendDstFunc() {
        return blend[RGB_DST];
    }

    public int getBlendSrcFuncAlpha() {
        return blend[ALPHA_SRC];
    }

    public int getBlendDstFuncAlpha() {
        return blend[ALPHA_DST];
    }

    public void setProjectionMatrix(Matrix4 projection) {
        if (Arrays.equals(projectionMatrix.val, projection.val)) return;
        if (drawing) flush();
        this.projectionMatrix.set(projection);
        if (drawing) setupMatrices();
    }

    private float colorPackedRGBA(final float red, final float green, final float blue, final float alpha) {
        return NumberUtils.intBitsToFloat(((int) (alpha * 255) << 24 & 0xFE000000) | ((int) (blue * 255) << 16 & 0xFF0000)
                | ((int) (green * 255) << 8 & 0xFF00) | ((int) (red * 255) & 0xFF));
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
