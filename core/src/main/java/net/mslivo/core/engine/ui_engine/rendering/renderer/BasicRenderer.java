package net.mslivo.core.engine.ui_engine.rendering.renderer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL32;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.VertexBufferObjectWithVAO;
import com.badlogic.gdx.graphics.glutils.VertexData;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.NumberUtils;
import com.badlogic.gdx.utils.ObjectIntMap;
import net.mslivo.core.engine.ui_engine.rendering.IntegerIndexBufferObject;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Arrays;
import java.util.HashMap;

public abstract class BasicRenderer {

    public static final String POSITION_ATTRIBUTE = "a_position";
    public static final String PROJTRANS_UNIFORM = "u_projTrans";
    protected static final int RGB_SRC = 0, RGB_DST = 1, ALPHA_SRC = 2, ALPHA_DST = 3;
    protected static final String ERROR_END_BEGIN = ".end() must be called before begin.";
    protected static final String ERROR_BEGIN_END = ".begin() must be called before end.";

    protected final int vertexSize;
    protected final int sizeMaxVertexes;
    protected final int sizeMaxVertexFloats;
    protected final int indicesSize;
    protected final int vertexIndicesRatio;
    protected final int sizeMaxIndicesInts;
    protected final boolean printRenderCalls;

    protected final FloatBuffer vertexBuffer;
    protected final IntBuffer indexBuffer;
    protected final VertexBufferObjectWithVAO vertexBufferObject;
    protected final IntegerIndexBufferObject indexBufferObject;

    protected final Color tempColor;
    protected final Matrix4 projectionMatrix, transformMatrix, combinedMatrix;
    private final HashMap<ShaderProgram, ObjectIntMap<String>> uniformLocationCache;

    protected boolean drawing;
    protected ShaderProgram shader;
    protected ShaderProgram defaultShader;
    protected int renderCalls;

    protected int[] blend;
    private int[] blend_save;
    private int[] blend_reset;

    protected int primitiveType;


    public BasicRenderer(final int sizeMaxVertexes, final ShaderProgram defaultShader, boolean printRenderCalls) {
        this.vertexSize = getVertexSize();
        this.indicesSize = getIndicesSize();
        this.vertexIndicesRatio = getVertexIndicesRatio();
        this.sizeMaxVertexes = sizeMaxVertexes;
        this.sizeMaxVertexFloats = this.sizeMaxVertexes * vertexSize;
        this.sizeMaxIndicesInts = (this.sizeMaxVertexes / this.vertexIndicesRatio) * indicesSize;
        this.printRenderCalls = printRenderCalls;
        int vertexAbsoluteLimit = Integer.MAX_VALUE / (this.vertexSize * 4);
        if (sizeMaxVertexes > vertexAbsoluteLimit)
            throw new IllegalArgumentException("size " + sizeMaxVertexes + " bigger than mix allowed size " + vertexAbsoluteLimit);
        if (sizeMaxVertexes % this.vertexIndicesRatio != 0)
            throw new IllegalArgumentException("size is not multiple of ratio " + vertexIndicesRatio);


        this.tempColor = new Color(Color.CLEAR);
        this.transformMatrix = new Matrix4();
        this.combinedMatrix = new Matrix4();
        this.projectionMatrix = new Matrix4().setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        this.vertexBufferObject = createVertexBufferObject(this.sizeMaxVertexes);
        this.vertexBuffer = this.vertexBufferObject.getBuffer(true);
        this.vertexBuffer.limit(this.sizeMaxVertexFloats);

        this.indexBufferObject = createIndexBufferObject(this.sizeMaxVertexes);
        this.indexBuffer = this.indexBufferObject.getBuffer(true);

        this.blend_reset = new int[]{GL32.GL_SRC_ALPHA, GL32.GL_ONE_MINUS_SRC_ALPHA, GL32.GL_ONE, GL32.GL_ONE_MINUS_SRC_ALPHA};
        this.blend = new int[]{this.blend_reset[RGB_SRC], this.blend_reset[RGB_DST], this.blend_reset[ALPHA_SRC], this.blend_reset[ALPHA_DST]};
        this.blend_save = Arrays.copyOf(this.blend_reset, this.blend_reset.length);

        this.uniformLocationCache = new HashMap<>();

        this.defaultShader = defaultShader;

        this.drawing = false;
        setShader(defaultShader());
    }

    public void begin() {
        if (drawing) throw new IllegalStateException(ERROR_END_BEGIN);
        this.renderCalls = 0;
        Gdx.gl.glDepthMask(false);

        this.shader.bind();
        this.setupMatrices();

        Gdx.gl.glEnable(GL32.GL_BLEND);
        Gdx.gl.glBlendFuncSeparate(this.blend[RGB_SRC], this.blend[RGB_DST], this.blend[ALPHA_SRC], this.blend[ALPHA_DST]);

        this.drawing = true;
    }

    public void end() {
        if (!drawing) throw new IllegalStateException(ERROR_BEGIN_END);
        flush();
        Gdx.gl.glDepthMask(true);
        this.drawing = false;
        if (printRenderCalls)
            System.out.println("renderFlushes: " + this.renderCalls);
    }

    private ShaderProgram defaultShader() {
        if (this.defaultShader == null)
            this.defaultShader = provideDefaultShader();
        return this.defaultShader;
    }

    public void setShader(ShaderProgram shader) {
        ShaderProgram nextShader = shader != null ? shader : defaultShader();
        if (this.shader == nextShader)
            return;

        this.shader = nextShader;

        if (drawing) {
            flush();
            this.shader.bind();
            setupMatrices();
        }
    }

    public ShaderProgram getShader() {
        return this.shader;
    }


    public void flush() {
        final int vertexIndex = vertexBuffer.position();
        if (vertexBuffer.position() == 0) return;

        // Bind Vertices
        this.vertexBuffer.position(0);
        this.vertexBuffer.limit(vertexIndex);
        this.vertexBufferObject.getBuffer(true); // Make Dirty
        this.vertexBufferObject.bind(this.shader);

        // Bind Indices
        final int verticesCount = (vertexIndex / this.vertexSize);
        final int indicesCount = (verticesCount / this.vertexIndicesRatio) * this.indicesSize;

        this.indexBuffer.position(0);
        if(this.indexBuffer.limit() != indicesCount) {
            this.indexBuffer.limit(indicesCount);
            this.indexBufferObject.getBuffer(true); // Make Dirty
        }
        this.indexBufferObject.bind();

        // Draw
        Gdx.gl32.glDrawElements(this.primitiveType, indicesCount, GL32.GL_UNSIGNED_INT, 0);

        // Reset Buffers
        this.vertexBuffer.position(0);
        this.vertexBuffer.limit(sizeMaxVertexFloats);
        this.indexBuffer.limit(sizeMaxIndicesInts);

        this.renderCalls++;
    }

    public int getSizeMaxIndicesInts() {
        return sizeMaxIndicesInts;
    }

    public int vertexBufferPosition() {
        return this.vertexBuffer.position();
    }

    public int vertexBufferLimit() {
        return this.vertexBuffer.limit();
    }

    public void vertexBufferPush(float value) {
        this.vertexBuffer.put(value);
    }

    public void vertexBufferPush(float[] values) {
        this.vertexBuffer.put(values);
    }

    public void setIndexBufferObject() {
        this.indexBufferObject.getBuffer(true);
    }

    public void vertexBufferPush(float[] values, int offset, int length) {
        this.vertexBuffer.put(values, offset, length);
    }

    public int getRenderCalls() {
        return renderCalls;
    }

    public IntegerIndexBufferObject getIndexBuffer() {
        return this.indexBufferObject;
    }

    public VertexData getVertexBuffer() {
        return this.vertexBufferObject;
    }

    public Matrix4 getProjectionMatrix() {
        return projectionMatrix;
    }

    public Matrix4 getTransformMatrix() {
        return transformMatrix;
    }

    public int getSizeMaxVertexFloats() {
        return sizeMaxVertexFloats;
    }

    protected void setPrimitiveType(int primitiveType) {
        if (primitiveType == this.primitiveType)
            return;
        this.flush();
        this.primitiveType = primitiveType;
    }

    public int getPrimitiveType() {
        return primitiveType;
    }

    public void setProjectionMatrix(Matrix4 projection) {
        if (Arrays.equals(projectionMatrix.val, projection.val)) return;
        if (drawing) flush();
        this.projectionMatrix.set(projection);
        if (drawing) setupMatrices();
    }

    public void setTransformMatrix(Matrix4 transform) {
        if (drawing) flush();
        transformMatrix.set(transform);
        if (drawing) setupMatrices();
    }

    public void dispose() {
        vertexBufferObject.dispose();
        indexBufferObject.dispose();
        if (this.shader == defaultShader())
            this.shader.dispose();
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

    public void saveState() {
        System.arraycopy(this.blend, 0, this.blend_reset, 0, 4);
    }

    public void loadState() {
        setBlendFunctionSeparate(this.blend_save[RGB_SRC], this.blend_save[RGB_DST], this.blend_save[ALPHA_SRC], this.blend_save[ALPHA_DST]);
    }

    public boolean isDrawing() {
        return drawing;
    }

    protected void setupMatrices() {
        combinedMatrix.set(projectionMatrix).mul(transformMatrix);
        shader.setUniformMatrix(uniformLocation(PROJTRANS_UNIFORM), combinedMatrix);
    }

    protected float colorPackedRGBA(final float red, final float green, final float blue, final float alpha) {
        return NumberUtils.intBitsToFloat(((int) (alpha * 255) << 24 & 0xFE000000) | ((int) (blue * 255) << 16 & 0xFF0000)
                | ((int) (green * 255) << 8 & 0xFF00) | ((int) (red * 255) & 0xFF));
    }

    protected boolean isVertexLimitReached() {
        return this.vertexBuffer.position() >= this.vertexBuffer.limit();
    }

    public int getSizeMaxVertexes() {
        return sizeMaxVertexes;
    }

    protected int uniformLocation(String uniform) {
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

    protected abstract int getVertexSize();

    protected abstract int getIndicesSize();

    protected abstract int getVertexIndicesRatio();

    protected abstract IntegerIndexBufferObject createIndexBufferObject(int size);

    protected abstract VertexBufferObjectWithVAO createVertexBufferObject(int size);

    protected abstract ShaderProgram provideDefaultShader();

}
