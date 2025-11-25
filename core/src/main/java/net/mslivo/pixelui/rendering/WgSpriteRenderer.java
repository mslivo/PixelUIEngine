package net.mslivo.pixelui.rendering;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.BitmapFontCache;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.utils.*;
import com.github.xpenatan.webgpu.*;
import com.monstrous.gdx.webgpu.application.WebGPUContext;
import com.monstrous.gdx.webgpu.application.WgGraphics;
import com.monstrous.gdx.webgpu.graphics.Binder;
import com.monstrous.gdx.webgpu.graphics.WgShaderProgram;
import com.monstrous.gdx.webgpu.graphics.WgTexture;
import com.monstrous.gdx.webgpu.wrappers.*;
import net.mslivo.pixelui.media.*;

import java.nio.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class WgSpriteRenderer implements Disposable {
    public static final String TEXCOORD_ATTRIBUTE = "a_texCoord";
    public static final String COLOR_ATTRIBUTE = "a_color";
    public static final String TWEAK_ATTRIBUTE = "a_tweak";
    public static final String POSITION_ATTRIBUTE = "a_position";

    public static final String TEXTURE_SIZE_UNIFORM = "u_textureSize";
    public static final String PROJTRANS_UNIFORM = "u_projTrans";
    public static final String TEXTURE = "texture";
    public static final String TEXTURE_SAMPLER = "textureSampler";

    private final static int VERTS_PER_SPRITE = 4;
    private final static int INDICES_PER_SPRITE = 6;

    private final WgGraphics gfx;
    private final WebGPUContext webgpu;
    private final WgShaderProgram specificShader;
    private final int maxSprites;
    private boolean drawing;
    private final int vertexSize;
    private final ByteBuffer vertexBB;
    private final FloatBuffer vertexFloats; // float buffer view on byte buffer
    public int numSprites;
    private int numSpritesPerFlush;
    private WebGPUVertexBuffer vertexBuffer;
    private WebGPUIndexBuffer indexBuffer;
    private WebGPUUniformBuffer uniformBuffer;
    private final WebGPUBindGroupLayout bindGroupLayout;
    private final VertexAttributes vertexAttributes;
    private final WGPUPipelineLayout pipelineLayout;
    private final PipelineSpecification pipelineSpec;
    private int uniformBufferSize;
    private WgTexture lastTexture;
    private final Matrix4 projectionMatrix;
    private final Matrix4 transformMatrix;
    private final Matrix4 combinedMatrix;
    private final Matrix4 shiftDepthMatrix;
    private WebGPURenderPass renderPass;
    private int vbOffset;
    private final PipelineCache pipelines;
    public int maxSpritesInBatch; // most nr of sprites in the batch over its lifetime
    public int renderCalls;
    public int pipelineCount;
    public int flushCount; // number of flushes since begin()
    public int maxFlushes;
    private float invTexWidth;
    private float invTexHeight;
    private final Map<Integer, WGPUBlendFactor> blendConstantMap = new HashMap<>(); // mapping GL vs WebGPU constants
    private final Map<WGPUBlendFactor, Integer> blendGLConstantMap = new HashMap<>(); // vice versa
    private final Binder binder;
    private static String defaultShader;
    private int frameNumber;
    //---
    private MediaManager mediaManager;
    protected float tweak, tweak_save, tweak_reset;
    protected static final int RGB_SRC = 0, RGB_DST = 1, ALPHA_SRC = 2, ALPHA_DST = 3;
    private static final float COLOR_RESET = colorPackedRGBA(0.5f, 0.5f, 0.5f, 1f);
    private static final int[] BLEND_RESET =  new int[]{
            GL32.GL_SRC_ALPHA,
            GL32.GL_ONE_MINUS_SRC_ALPHA,
            GL32.GL_ONE,
            GL32.GL_ONE_MINUS_SRC_ALPHA
    };
    protected float color, color_save;
    protected int[] blend, blend_save;
    protected final Color tempColor;

    public WgSpriteRenderer() {
        this(null,2000, null, 100); // default nr
    }

    public WgSpriteRenderer(MediaManager mediaManager) {
        this(mediaManager,2000, null, 100); // default nr
    }

    public WgSpriteRenderer(MediaManager mediaManager, int maxSprites) {
        this(mediaManager,maxSprites, null, 100);
    }

    public WgSpriteRenderer(MediaManager mediaManager, int maxSprites, WgShaderProgram specificShader) {
        this(mediaManager,maxSprites, specificShader, 100);
    }

    /**
     * Create a SpriteBatch.
     *
     * @param maxSprites maximum number of sprite to be supported (default is 1000)
     * @param specificShader specific ShaderProgram to use, must be compatible with "sprite.wgsl". Leave null to use the
     *            default shader.
     * @param maxFlushes maximum number of flushes (e.g. texture changes, blending changes)
     */
    public WgSpriteRenderer(MediaManager mediaManager, int maxSprites, WgShaderProgram specificShader, int maxFlushes) {
        this.mediaManager = mediaManager;
        gfx = (WgGraphics) Gdx.graphics;
        webgpu = gfx.getContext();

        if (maxSprites > 16384)
            throw new GdxRuntimeException("Too many sprites. Max is 16384.");

        this.maxSprites = maxSprites;
        this.specificShader = specificShader;

        vertexAttributes = new VertexAttributes(
                new VertexAttribute(VertexAttributes.Usage.Position, 2, POSITION_ATTRIBUTE),
                new VertexAttribute(VertexAttributes.Usage.ColorPacked, 4, COLOR_ATTRIBUTE),
                new VertexAttribute(VertexAttributes.Usage.TextureCoordinates, 2, TEXCOORD_ATTRIBUTE),
                new VertexAttribute(VertexAttributes.Usage.Tangent, 4, TWEAK_ATTRIBUTE,1));

        // vertex: x, y, rgba, u, v
        vertexSize = vertexAttributes.vertexSize; // bytes

        initBlendMap(); // fill constants mapping table

        // allow for a different projectionView matrix per flush.
        this.maxFlushes = maxFlushes;

        // allocate data buffers based on default vertex attributes which are assumed to be the worst case.
        // i.e. with setVertexAttributes() you can specify a subset
        createBuffers(maxFlushes + 1);
        fillIndexBuffer(maxSprites);

        // Create FloatBuffer to hold vertex data per batch, is reset every flush
        vertexBB = BufferUtils.newUnsafeByteBuffer(maxSprites * VERTS_PER_SPRITE * vertexSize);
        vertexBB.order(ByteOrder.LITTLE_ENDIAN);
        // important, webgpu expects little endian. ByteBuffer defaults to big endian.
        vertexFloats = vertexBB.asFloatBuffer();

        projectionMatrix = new Matrix4();
        transformMatrix = new Matrix4();
        combinedMatrix = new Matrix4();

        // matrix which will transform an opengl ortho matrix to a webgpu ortho matrix
        // by scaling the Z range from [-1..1] to [0..1]
        shiftDepthMatrix = new Matrix4().idt().scl(1, 1, -0.5f).trn(0, 0, 0.5f);


        invTexWidth = 0f;
        invTexHeight = 0f;

        bindGroupLayout = createBindGroupLayout();

        binder = new Binder();
        // define group
        binder.defineGroup(0, bindGroupLayout);
        // define bindings in the group
        binder.defineBinding("uniforms", 0, 0);
        binder.defineBinding(TEXTURE, 0, 1);
        binder.defineBinding(TEXTURE_SAMPLER, 0, 2);
        // define uniforms in uniform buffer (binding 0) with their offset
        binder.defineUniform(PROJTRANS_UNIFORM, 0, 0, 0);
        binder.defineUniform(TEXTURE_SIZE_UNIFORM, 0, 0, 16);

        // set binding 0 to uniform buffer
        binder.setBuffer("uniforms", uniformBuffer, 0, uniformBufferSize);
        // bindings 1 and 2 are done in switchTexture()

        // get pipeline layout which aggregates all the bind group layouts
        pipelineLayout = binder.getPipelineLayout("SpriteBatch pipeline layout");

        pipelines = new PipelineCache();
        pipelineSpec = new PipelineSpecification(vertexAttributes, this.specificShader);
        pipelineSpec.name = "SpriteBatch pipeline";
        pipelineSpec.colorFormat = gfx.getContext().getSurfaceFormat();
        pipelineSpec.invalidateHashCode();

        // default blending values
        pipelineSpec.enableBlending();
        setBlendFunctionSeparate(BLEND_RESET[RGB_SRC],BLEND_RESET[RGB_DST],BLEND_RESET[ALPHA_SRC],BLEND_RESET[ALPHA_DST]);
        pipelineSpec.disableDepthTest();

        pipelineSpec.vertexAttributes = vertexAttributes;
        pipelineSpec.numSamples = webgpu.getSamples();

        // use provided (compiled) shader or else use default shader (source)
        // this can be overruled with setShader()
        pipelineSpec.shader = specificShader;
        if (specificShader == null) {
            pipelineSpec.shaderSource = getDefaultShaderSource();
        }

        setPipeline();

        projectionMatrix.setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), 0, 100);
        transformMatrix.idt();

        drawing = false;
        frameNumber = -1;


        //---
        this.color = COLOR_RESET;
        this.color_save = this.color;
        this.tweak_reset = colorPackedRGBA(0f, 0f, 0f, 0f);
        this.tweak = this.tweak_reset;
        this.tweak_save = this.tweak;
        this.blend = Arrays.copyOf(BLEND_RESET, BLEND_RESET.length);
        this.blend_save = Arrays.copyOf(this.blend, this.blend.length);
        this.tempColor = new Color(Color.CLEAR);
    }

    // the index buffer is fixed and only has to be filled on start-up
    private void fillIndexBuffer(int maxSprites) {
        ByteBuffer bb = BufferUtils.newUnsafeByteBuffer(maxSprites * INDICES_PER_SPRITE * Short.BYTES);
        bb.order(ByteOrder.LITTLE_ENDIAN); // webgpu expects little endian data
        ShortBuffer shorts = bb.asShortBuffer();
        for (int i = 0; i < maxSprites; i++) {
            // note: even if there is overflow above 32767 and the short becomes negative
            // the GPU will interpret it as a uint32, and it will still work.
            // The real limit is where there are more than 16384 * 4 indices (per flush!),
            // and it wraps back to zero.
            short vertexOffset = (short) (i * 4);
            // two triangles per sprite
            shorts.put(vertexOffset);
            shorts.put((short) (vertexOffset + 1));
            shorts.put((short) (vertexOffset + 2));

            shorts.put(vertexOffset);
            shorts.put((short) (vertexOffset + 2));
            shorts.put((short) (vertexOffset + 3));
        }
        // set the limit of the byte buffer to the number of bytes filled
        ((Buffer) bb).limit(shorts.limit() * Short.BYTES);

        indexBuffer.setIndices(bb);
        BufferUtils.disposeUnsafeByteBuffer(bb);
    }

    public void setColor(float r, float g, float b, float a) {
        color = colorPackedRGBA(r,g,b,a);
    }

    public void setColor(Color color) {
        this.setColor(color.r,color.g,color.b,color.a);
    }

    public void setColor(Color color, float alpha) {
        this.setColor(color.r,color.g,color.b,alpha);
    }

    public Color getColor() {
        Color.abgr8888ToColor(this.tempColor, this.color);
        return this.tempColor;
    }

    public void setPackedColor(float packedColor) {
        this.color = packedColor;
    }

    public float getPackedColor() {
        return this.color;
    }

    public void setBlendFactor(WGPUBlendFactor srcFunc, WGPUBlendFactor dstFunc) {
        pipelineSpec.setBlendFactorSeparate(srcFunc, dstFunc, srcFunc, dstFunc);
    }

    public void setBlendFactorSeparate(WGPUBlendFactor srcFuncColor, WGPUBlendFactor dstFuncColor,
                                       WGPUBlendFactor srcFuncAlpha, WGPUBlendFactor dstFuncAlpha) {
        if (pipelineSpec.getBlendSrcFactor() == srcFuncColor && pipelineSpec.getBlendDstFactor() == dstFuncColor
                && pipelineSpec.getBlendSrcFactorAlpha() == srcFuncAlpha
                && pipelineSpec.getBlendDstFactorAlpha() == dstFuncAlpha)
            return;

        flush();
        pipelineSpec.setBlendFactorSeparate(srcFuncColor, dstFuncColor, srcFuncAlpha, dstFuncAlpha);
        setPipeline();
    }

    public WGPUBlendFactor getBlendSrcFactor() {
        return pipelineSpec.getBlendSrcFactor();
    }

    public WGPUBlendFactor getBlendDstFactor() {
        return pipelineSpec.getBlendDstFactor();
    }

    public WGPUBlendFactor getBlendSrcFactorAlpha() {
        return pipelineSpec.getBlendSrcFactorAlpha();
    }

    public WGPUBlendFactor getBlendDstFactorAlpha() {
        return pipelineSpec.getBlendDstFactorAlpha();
    }

    // for compatibility with GL based methods
    public void setBlendFunction(int srcFunc, int dstFunc) {
        setBlendFunctionSeparate(srcFunc, dstFunc, srcFunc, dstFunc);
    }

    public void setBlendFunctionSeparate(int srcFuncColor, int dstFuncColor, int srcFuncAlpha, int dstFuncAlpha) {
        WGPUBlendFactor srcFactorColor = blendConstantMap.get(srcFuncColor);
        WGPUBlendFactor dstFactorColor = blendConstantMap.get(dstFuncColor);
        WGPUBlendFactor srcFactorAlpha = blendConstantMap.get(srcFuncAlpha);
        WGPUBlendFactor dstFactorAlpha = blendConstantMap.get(dstFuncAlpha);
        if (pipelineSpec.getBlendSrcFactor() == srcFactorColor && pipelineSpec.getBlendDstFactor() == dstFactorColor
                && pipelineSpec.getBlendSrcFactorAlpha() == srcFactorAlpha
                && pipelineSpec.getBlendDstFactorAlpha() == dstFactorAlpha)
            return;

        flush();
        pipelineSpec.setBlendFactorSeparate(srcFactorColor, dstFactorColor, srcFactorAlpha, dstFactorAlpha);
        setPipeline();
    }

    public int getBlendSrcFunc() {
        return blendGLConstantMap.get(pipelineSpec.getBlendSrcFactor());
    }

    public int getBlendDstFunc() {
        return blendGLConstantMap.get(pipelineSpec.getBlendDstFactor());
    }

    public int getBlendSrcFuncAlpha() {
        return blendGLConstantMap.get(pipelineSpec.getBlendSrcFactorAlpha());
    }

    public int getBlendDstFuncAlpha() {
        return blendGLConstantMap.get(pipelineSpec.getBlendDstFactorAlpha());
    }

    public void enableBlending() {
        if (pipelineSpec.isBlendingEnabled())
            return;
        flush();
        pipelineSpec.enableBlending();
        setPipeline();
    }

    public void disableBlending() {
        if (!pipelineSpec.isBlendingEnabled())
            return;
        flush();
        pipelineSpec.disableBlending();
        setPipeline();
    }

    /** Apply a scissor rectangle for further sprites. */
    public void setScissorRect(int x, int y, int width, int height) {
        // start a new render pass (flush sprites up till now)
        end();
        begin();
        renderPass.setScissorRect(x, y, width, height);
    }

    public boolean isBlendingEnabled() {
        return pipelineSpec.isBlendingEnabled();
    }

    public boolean isDrawing() {
        return drawing;
    }

    public void begin() {
        begin(null);
    }

    public void begin(Color clearColor) {
        if (drawing)
            throw new RuntimeException("Must end() before begin()");

        pipelineSpec.colorFormat = gfx.getContext().getSurfaceFormat();
        pipelineSpec.invalidateHashCode();


        renderPass = RenderPassBuilder.create("SpriteBatch", clearColor, webgpu.getSamples());

        // default blending values
        pipelineSpec.enableBlending();
        pipelineSpec.setBlendFactor(WGPUBlendFactor.SrcAlpha, WGPUBlendFactor.OneMinusSrcAlpha);
        pipelineSpec.disableDepthTest();

        setPipeline();

        // First begin() call in this render frame?
        if (webgpu.frameNumber != this.frameNumber) {
            this.frameNumber = webgpu.frameNumber;

            Rectangle view = webgpu.getViewportRectangle();
            renderPass.setViewport(view.x, view.y, view.width, view.height, 0, 1);

            uniformBuffer.beginSlices(); // setDynamicOffsetIndex(0); // reset the dynamic offset to the start
            // if the same spritebatch is used multiple times per frame this will overwrite the previous pass
            // to solve this we reset at the start of a new frame.
            numSpritesPerFlush = 0;
            vbOffset = 0;
            vertexFloats.clear();
            maxSpritesInBatch = 0;
            flushCount = 0;
            numSprites = 0;
        }
        renderCalls = 0;

        // set default state

        // don't reset the matrices because setProjectionMatrix() and setTransformMatrix()
        // may be called before begin() and need to be respected.

        drawing = true;
    }

    protected void switchTexture(Texture texture) {
        flush();
        if (!(texture instanceof WgTexture))
            throw new IllegalArgumentException("texture must be WebGPUTexture");
        lastTexture = (WgTexture) texture;
        invTexWidth = 1.0f / texture.getWidth();
        invTexHeight = 1.0f / texture.getHeight();

        binder.setTexture("texture", lastTexture.getTextureView());
        binder.setSampler("textureSampler", lastTexture.getSampler());
        binder.setUniform(TEXTURE_SIZE_UNIFORM, new Vector2(texture.getWidth(), texture.getHeight()));
    }

    public void flush() {
        if (numSpritesPerFlush == 0)
            return;
        if (numSpritesPerFlush > maxSpritesInBatch)
            maxSpritesInBatch = numSpritesPerFlush;
        if (flushCount > maxFlushes - 1) {
            Gdx.app.error("WgSpriteBatch", "Too many flushes (" + flushCount + "). Increase maxFlushes.");
            return;
        }
        renderCalls++;

        // bind group
        int dynamicOffset = uniformBuffer.nextSlice();
        updateMatrices();
        // int dynamicOffset = flushCount *uniformBuffer.getUniformStride();
        WebGPUBindGroup wbg = binder.getBindGroup(0);
        WGPUBindGroup bg = wbg.getBindGroup();
        renderPass.setBindGroup(0, bg, dynamicOffset);

        // append new vertex data to GPU vertex buffer
        int numBytes = numSpritesPerFlush * VERTS_PER_SPRITE * vertexSize;
        vertexBuffer.setVertices(vertexBB, vbOffset, numBytes);

        // Set vertex buffer while encoding the render pass
        // use an offset to set the vertex buffer for this batch
        renderPass.setVertexBuffer(0, vertexBuffer.getBuffer(), vbOffset, numBytes);
        renderPass.setIndexBuffer(indexBuffer.getBuffer(), WGPUIndexFormat.Uint16, 0,
                numSpritesPerFlush * 6 * Short.BYTES);

        renderPass.drawIndexed(numSpritesPerFlush * 6, 1, 0, 0, 0);

        // bg.release();

        vbOffset += numBytes;
        vertexFloats.clear(); // reset fill position for next batch
        numSprites += numSpritesPerFlush;
        numSpritesPerFlush = 0; // reset
        flushCount++;
        // advance the dynamic offset in the uniform buffer ready for the next flush
        // uniformBuffer.setDynamicOffsetIndex(flushCount);
    }

    public void end() {
        if (!drawing) // catch incorrect usage
            throw new RuntimeException("Cannot end() without begin()");
        drawing = false;
        flush();
        uniformBuffer.endSlices();
        renderPass.end();
        renderPass = null;
        pipelineCount = pipelines.size(); // statistics
    }

    // create or reuse pipeline on demand to match the pipeline spec
    private void setPipeline() {
        WebGPUPipeline pipeline = pipelines.findPipeline(pipelineLayout, pipelineSpec);
        if (renderPass != null)
            renderPass.setPipeline(pipeline);
    }

    /**
     * Set shader to use instead of the default shader or the shader provided in the constructor. Use null to reset to
     * default shader
     */
    public void setShader(@Null WgShaderProgram shaderProgram) {
        if (pipelineSpec.shader == shaderProgram)
            return;
        if (drawing)
            flush();
        if (shaderProgram == null) {
            pipelineSpec.shader = specificShader;
            if (specificShader == null)
                pipelineSpec.shaderSource = getDefaultShaderSource();
        } else {
            pipelineSpec.shader = shaderProgram;
            pipelineSpec.shaderSource = "precompiled"; // shaderProgram.getName(); // todo;
        }
        pipelineSpec.invalidateHashCode();
        setPipeline();
    }

    public Matrix4 getProjectionMatrix() {
        return projectionMatrix;
    }

    public Matrix4 getTransformMatrix() {
        return transformMatrix;
    }

    /**
     * Set projection matrix. Expects an OpenGL standard projection matrix, i.e. mapping Z to [-1 .. 1]
     *
     */
    public void setProjectionMatrix(Matrix4 projection) {
        if (drawing)
            flush();
        projectionMatrix.set(projection);
    }

    public void setTransformMatrix(Matrix4 transform) {
        if (drawing)
            flush();
        transformMatrix.set(transform);
    }

    public ShaderProgram getShader() {
        return null;
    }

    public void draw(Texture texture, float x, float y) {
        draw(texture, x, y, texture.getWidth(), texture.getHeight());
    }

    public void draw(Texture texture, float x, float y, float w, float h) {
        this.draw(texture, x, y, w, h, 0f, 1f, 1f, 0f);
    }

    public void draw(TextureRegion region, float x, float y) {
        // note: v2 is top of glyph, v the bottom
        draw(region, x, y, region.getRegionWidth(), region.getRegionHeight());
    }

    private boolean check() {
        if (!drawing)
            throw new RuntimeException("SpriteBatch: Must call begin() before draw().");
        if (numSpritesPerFlush == maxSprites) {
            Gdx.app.error("WgSpriteBatch", "Too many sprites (more than " + maxSprites + "). Enlarge maxSprites.");
            return false;
        }
        return true;
    }

    public void draw(TextureRegion region, float x, float y, float w, float h) {
        if (!check())
            return;

        if (region.getTexture() != lastTexture) { // changing texture, need to flush what we have so far
            switchTexture(region.getTexture());
        }
        addRect(x, y, w, h, region.getU(), region.getV2(), region.getU2(), region.getV(), this.color, this.tweak); // flip v and v2
        numSpritesPerFlush++;
    }

    public void draw(Texture texture, float x, float y, float width, float height, float u, float v, float u2,
                     float v2) {
        if (!check())
            return;

        if (texture != lastTexture) { // changing texture, need to flush what we have so far
            switchTexture(texture);
        }
        addRect(x, y, width, height, u, v, u2, v2, this.color, this.tweak);
        numSpritesPerFlush++;
    }

    public void draw(Texture texture, float x, float y, float originX, float originY, float width, float height,
                     float scaleX, float scaleY, float rotation, int srcX, int srcY, int srcWidth, int srcHeight, boolean flipX,
                     boolean flipY) {
        if (!check())
            return;

        if (texture != lastTexture) { // changing texture, need to flush what we have so far
            switchTexture(texture);
        }

        // if (idx == vertices.length) //
        // flush();

        // bottom left and top right corner points relative to origin
        final float worldOriginX = x + originX;
        final float worldOriginY = y + originY;
        float fx = -originX;
        float fy = -originY;
        float fx2 = width - originX;
        float fy2 = height - originY;

        // scale
        if (scaleX != 1 || scaleY != 1) {
            fx *= scaleX;
            fy *= scaleY;
            fx2 *= scaleX;
            fy2 *= scaleY;
        }

        // construct corner points, start from top left and go counter clockwise
        final float p1x = fx;
        final float p1y = fy;
        final float p2x = fx;
        final float p2y = fy2;
        final float p3x = fx2;
        final float p3y = fy2;
        final float p4x = fx2;
        final float p4y = fy;

        float x1;
        float y1;
        float x2;
        float y2;
        float x3;
        float y3;
        float x4;
        float y4;

        // rotate
        if (rotation != 0) {
            final float cos = MathUtils.cosDeg(rotation);
            final float sin = MathUtils.sinDeg(rotation);

            x1 = cos * p1x - sin * p1y;
            y1 = sin * p1x + cos * p1y;

            x2 = cos * p2x - sin * p2y;
            y2 = sin * p2x + cos * p2y;

            x3 = cos * p3x - sin * p3y;
            y3 = sin * p3x + cos * p3y;

            x4 = x1 + (x3 - x2);
            y4 = y3 - (y2 - y1);
        } else {
            x1 = p1x;
            y1 = p1y;

            x2 = p2x;
            y2 = p2y;

            x3 = p3x;
            y3 = p3y;

            x4 = p4x;
            y4 = p4y;
        }

        x1 += worldOriginX;
        y1 += worldOriginY;
        x2 += worldOriginX;
        y2 += worldOriginY;
        x3 += worldOriginX;
        y3 += worldOriginY;
        x4 += worldOriginX;
        y4 += worldOriginY;

        float u = srcX * invTexWidth;
        float v = (srcY + srcHeight) * invTexHeight;
        float u2 = (srcX + srcWidth) * invTexWidth;
        float v2 = srcY * invTexHeight;

        if (flipX) {
            float tmp = u;
            u = u2;
            u2 = tmp;
        }

        if (flipY) {
            float tmp = v;
            v = v2;
            v2 = tmp;
        }
        addVertex(x1, y1,this.color, u, v,this.tweak);
        addVertex(x2, y2,this.color, u, v2,this.tweak);
        addVertex(x3, y3,this.color, u2, v2,this.tweak);
        addVertex(x4, y4,this.color, u2, v,this.tweak);
        numSpritesPerFlush++;
    }

    public void draw(Texture texture, float x, float y, float width, float height, int srcX, int srcY, int srcWidth,
                     int srcHeight, boolean flipX, boolean flipY) {
        if (!check())
            return;

        if (texture != lastTexture)
            switchTexture(texture);

        float u = srcX * invTexWidth;
        float v = (srcY + srcHeight) * invTexHeight;
        float u2 = (srcX + srcWidth) * invTexWidth;
        float v2 = srcY * invTexHeight;
        final float fx2 = x + width;
        final float fy2 = y + height;

        if (flipX) {
            float tmp = u;
            u = u2;
            u2 = tmp;
        }

        if (flipY) {
            float tmp = v;
            v = v2;
            v2 = tmp;
        }

        addVertex(x, y,this.color, u, v, this.tweak);
        addVertex(x, fy2,this.color, u, v2, this.tweak);
        addVertex(fx2, fy2,this.color, u2, v2, this.tweak);
        addVertex(fx2, y,this.color, u2, v, this.tweak);
        numSpritesPerFlush++;
    }

    public void draw(Texture texture, float x, float y, int srcX, int srcY, int srcWidth, int srcHeight) {
        if (!check())
            return;

        if (texture != lastTexture)
            switchTexture(texture);

        final float u = srcX * invTexWidth;
        final float v = (srcY + srcHeight) * invTexHeight;
        final float u2 = (srcX + srcWidth) * invTexWidth;
        final float v2 = srcY * invTexHeight;
        final float fx2 = x + srcWidth;
        final float fy2 = y + srcHeight;

        addVertex(x, y,this.color, u, v, this.tweak);
        addVertex(x, fy2,this.color, u, v2, this.tweak);
        addVertex(fx2, fy2,this.color, u2, v2, this.tweak);
        addVertex(fx2, y,this.color, u2, v, this.tweak);
        numSpritesPerFlush++;
    }

    // public void draw (Texture texture, float x, float y, float width, float height, float u, float v, float u2, float
    // v2) {
    // if (!drawing) throw new IllegalStateException("SpriteBatch.begin must be called before draw.");
    //
    // if (texture != lastTexture)
    // switchTexture(texture);
    // final float fx2 = x + width;
    // final float fy2 = y + height;
    //
    // addVertex(x, y, u, v);
    // addVertex(x, fy2, u, v2);
    // addVertex(fx2,fy2, u2, v2);
    // addVertex(fx2, y, u2, v);
    // }

    // @Override
    // public void draw (Texture texture, float x, float y) {
    // draw(texture, x, y, texture.getWidth(), texture.getHeight());
    // }

    // used by Sprite class and BitmapFont
    public void draw(Texture texture, float[] spriteVertices, int offset, int numFloats) {
        if (!check())
            return;

        if (texture != lastTexture) { // changing texture, need to flush what we have so far
            switchTexture(texture);
        }
        int remaining = 20 * (maxSprites - numSpritesPerFlush);
        if (numFloats > remaining) // avoid buffer overflow by truncating as needed
            numFloats = remaining;
        vertexFloats.put(spriteVertices, offset, numFloats);
        numSpritesPerFlush += numFloats / 20;
    }

    public void draw(TextureRegion region, float x, float y, float originX, float originY, float width, float height,
                     float scaleX, float scaleY, float rotation) {
        if (!check())
            return;
        Texture texture = region.getTexture();
        if (texture != lastTexture)
            switchTexture(texture);

        // bottom left and top right corner points relative to origin
        final float worldOriginX = x + originX;
        final float worldOriginY = y + originY;
        float fx = -originX;
        float fy = -originY;
        float fx2 = width - originX;
        float fy2 = height - originY;

        // scale
        if (scaleX != 1 || scaleY != 1) {
            fx *= scaleX;
            fy *= scaleY;
            fx2 *= scaleX;
            fy2 *= scaleY;
        }

        // construct corner points, start from top left and go counter clockwise
        final float p1x = fx;
        final float p1y = fy;
        final float p2x = fx;
        final float p2y = fy2;
        final float p3x = fx2;
        final float p3y = fy2;
        final float p4x = fx2;
        final float p4y = fy;

        float x1;
        float y1;
        float x2;
        float y2;
        float x3;
        float y3;
        float x4;
        float y4;

        // rotate
        if (rotation != 0) {
            final float cos = MathUtils.cosDeg(rotation);
            final float sin = MathUtils.sinDeg(rotation);

            x1 = cos * p1x - sin * p1y;
            y1 = sin * p1x + cos * p1y;

            x2 = cos * p2x - sin * p2y;
            y2 = sin * p2x + cos * p2y;

            x3 = cos * p3x - sin * p3y;
            y3 = sin * p3x + cos * p3y;

            x4 = x1 + (x3 - x2);
            y4 = y3 - (y2 - y1);
        } else {
            x1 = p1x;
            y1 = p1y;

            x2 = p2x;
            y2 = p2y;

            x3 = p3x;
            y3 = p3y;

            x4 = p4x;
            y4 = p4y;
        }

        x1 += worldOriginX;
        y1 += worldOriginY;
        x2 += worldOriginX;
        y2 += worldOriginY;
        x3 += worldOriginX;
        y3 += worldOriginY;
        x4 += worldOriginX;
        y4 += worldOriginY;

        final float u = region.getU();
        final float v = region.getV2();
        final float u2 = region.getU2();
        final float v2 = region.getV();

        addVertex(x1, y1,this.color, u, v,this.tweak);
        addVertex(x2, y2,this.color, u, v2,this.tweak);
        addVertex(x3, y3,this.color, u2, v2,this.tweak);
        addVertex(x4, y4,this.color, u2, v,this.tweak);
        numSpritesPerFlush++;
    }

    public void draw(TextureRegion region, float x, float y, float originX, float originY, float width, float height,
                     float scaleX, float scaleY, float rotation, boolean clockwise) {
        if (!check())
            return;
        Texture texture = region.getTexture();
        if (texture != lastTexture)
            switchTexture(texture);

        // bottom left and top right corner points relative to origin
        final float worldOriginX = x + originX;
        final float worldOriginY = y + originY;
        float fx = -originX;
        float fy = -originY;
        float fx2 = width - originX;
        float fy2 = height - originY;

        // scale
        if (scaleX != 1 || scaleY != 1) {
            fx *= scaleX;
            fy *= scaleY;
            fx2 *= scaleX;
            fy2 *= scaleY;
        }

        // construct corner points, start from top left and go counter clockwise
        final float p1x = fx;
        final float p1y = fy;
        final float p2x = fx;
        final float p2y = fy2;
        final float p3x = fx2;
        final float p3y = fy2;
        final float p4x = fx2;
        final float p4y = fy;

        float x1;
        float y1;
        float x2;
        float y2;
        float x3;
        float y3;
        float x4;
        float y4;

        // rotate
        if (rotation != 0) {
            final float cos = MathUtils.cosDeg(rotation);
            final float sin = MathUtils.sinDeg(rotation);

            x1 = cos * p1x - sin * p1y;
            y1 = sin * p1x + cos * p1y;

            x2 = cos * p2x - sin * p2y;
            y2 = sin * p2x + cos * p2y;

            x3 = cos * p3x - sin * p3y;
            y3 = sin * p3x + cos * p3y;

            x4 = x1 + (x3 - x2);
            y4 = y3 - (y2 - y1);
        } else {
            x1 = p1x;
            y1 = p1y;

            x2 = p2x;
            y2 = p2y;

            x3 = p3x;
            y3 = p3y;

            x4 = p4x;
            y4 = p4y;
        }

        x1 += worldOriginX;
        y1 += worldOriginY;
        x2 += worldOriginX;
        y2 += worldOriginY;
        x3 += worldOriginX;
        y3 += worldOriginY;
        x4 += worldOriginX;
        y4 += worldOriginY;

        float u1, v1, u2, v2, u3, v3, u4, v4;
        if (clockwise) {
            u1 = region.getU2();
            v1 = region.getV2();
            u2 = region.getU();
            v2 = region.getV2();
            u3 = region.getU();
            v3 = region.getV();
            u4 = region.getU2();
            v4 = region.getV();
        } else {
            u1 = region.getU();
            v1 = region.getV();
            u2 = region.getU2();
            v2 = region.getV();
            u3 = region.getU2();
            v3 = region.getV2();
            u4 = region.getU();
            v4 = region.getV2();
        }

        addVertex(x1, y1,this.color, u1, v1, this.tweak);
        addVertex(x2, y2,this.color, u2, v2, this.tweak);
        addVertex(x3, y3,this.color, u3, v3, this.tweak);
        addVertex(x4, y4,this.color, u4, v4, this.tweak);
        numSpritesPerFlush++;
    }

    public void draw(TextureRegion region, float width, float height, Affine2 transform) {
        if (!check())
            return;
        Texture texture = region.getTexture();
        if (texture != lastTexture)
            switchTexture(texture);

        // construct corner points
        float x1 = transform.m02;
        float y1 = transform.m12;
        float x2 = transform.m01 * height + transform.m02;
        float y2 = transform.m11 * height + transform.m12;
        float x3 = transform.m00 * width + transform.m01 * height + transform.m02;
        float y3 = transform.m10 * width + transform.m11 * height + transform.m12;
        float x4 = transform.m00 * width + transform.m02;
        float y4 = transform.m10 * width + transform.m12;

        float u = region.getU();
        float v = region.getV2();
        float u2 = region.getU2();
        float v2 = region.getV();

        addVertex(x1, y1, this.color, u, v,this.tweak);
        addVertex(x2, y2, this.color, u, v2,this.tweak);
        addVertex(x3, y3, this.color, u2, v2,this.tweak);
        addVertex(x4, y4, this.color, u2, v,this.tweak);
        numSpritesPerFlush++;
    }

    private void addRect(float x, float y, float w, float h, float u, float v, float u2, float v2, float color, float tweak) {
        addVertex(x, y,color, u, v, tweak);
        addVertex(x, y + h,color, u, v2, tweak);
        addVertex(x + w, y + h,color, u2, v2, tweak);
        addVertex(x + w, y,color, u2, v, tweak);
    }

    private void addVertex(float x, float y, float color, float u, float v, float tweak) {
        boolean hasColor = (vertexAttributes.getMask() & VertexAttributes.Usage.ColorPacked) != 0;
        boolean hasUV = (vertexAttributes.getMask() & VertexAttributes.Usage.TextureCoordinates) != 0;

        vertexFloats.put(x);
        vertexFloats.put(y);
        if (hasColor) {
            vertexFloats.put(color);
        }
        if (hasUV) {
            vertexFloats.put(u);
            vertexFloats.put(v);
        }
        vertexFloats.put(tweak);
    }

    private void createBuffers(int maxFlushes) {
        int indexSize = maxSprites * INDICES_PER_SPRITE * Short.BYTES;

        // Create vertex buffer and index buffer
        vertexBuffer = new WebGPUVertexBuffer(WGPUBufferUsage.CopyDst.or(WGPUBufferUsage.Vertex),
                maxSprites * 4 * vertexSize);
        indexBuffer = new WebGPUIndexBuffer(WGPUBufferUsage.CopyDst.or(WGPUBufferUsage.Index), indexSize, Short.BYTES);

        // Create uniform buffer with dynamic offset for the view projection matrix
        // dynamic offset will be incremented per flush so that it can have a specific view projection matrix
        uniformBufferSize = 24 * Float.BYTES;
        uniformBuffer = new WebGPUUniformBuffer(uniformBufferSize, WGPUBufferUsage.CopyDst.or(WGPUBufferUsage.Uniform),
                maxFlushes);
    }

    private Matrix4 prevMatrix = new Matrix4();

    private void updateMatrices() {
        combinedMatrix.set(shiftDepthMatrix).mul(projectionMatrix).mul(transformMatrix);
        binder.setUniform(PROJTRANS_UNIFORM, combinedMatrix);
    }

    private WebGPUBindGroupLayout createBindGroupLayout() {
        WebGPUBindGroupLayout layout = new WebGPUBindGroupLayout("SpriteBatch bind group layout");
        layout.begin();
        layout.addBuffer(0, WGPUShaderStage.Vertex, WGPUBufferBindingType.Uniform, uniformBufferSize, true);
        layout.addTexture(1, WGPUShaderStage.Fragment, WGPUTextureSampleType.Float, WGPUTextureViewDimension._2D,
                false);
        layout.addSampler(2, WGPUShaderStage.Fragment, WGPUSamplerBindingType.Filtering);
        layout.end();
        return layout;
    }

    @Override
    public void dispose() {
        binder.dispose();
        pipelines.dispose();
        vertexBuffer.dispose();
        indexBuffer.dispose();
        uniformBuffer.dispose();
        bindGroupLayout.dispose();
        // pipelineLayout.dispose();

    }

    private String getDefaultShaderSource() {
        return """
                struct VertexInput {
                    @location(0) a_position : vec2f,
                    @location(1) a_texCoord : vec2f,
                    @location(3) a_tweak    : vec4f,
                    @location(5) a_color    : vec4f,
                };
                
                struct Uniforms {
                    u_projTrans : mat4x4<f32>,
                    u_textureSize : vec2f
                };
                
                @group(0) @binding(0)
                var<uniform> uniforms : Uniforms;
                
                struct VertexOutput {
                    @builtin(position) Position : vec4f,
                    @location(0) v_color        : vec4f,
                    @location(1) v_texCoord     : vec2f,
                    @location(2) v_tweak        : vec4f,
                };
                
                const FLOAT_CORRECTION : f32 = 255.0 / 254.0;
                
                @vertex
                fn vs_main(input : VertexInput) -> VertexOutput {
                    var out : VertexOutput;
                
                    out.v_color    = input.a_color * FLOAT_CORRECTION;
                    out.v_tweak    = input.a_tweak * FLOAT_CORRECTION;
                    out.v_texCoord = input.a_texCoord;
                
                    out.Position = uniforms.u_projTrans * vec4f(input.a_position,0.0,1.0);                
                
                    return out;
                }
                
                struct FragmentInput {
                    @location(0) v_color    : vec4f,
                    @location(1) v_texCoord : vec2f,
                    @location(2) v_tweak    : vec4f,
                };
                
                @group(0) @binding(1)
                var texture : texture_2d<f32>;
                
                @group(0) @binding(2)
                var textureSampler : sampler;
                                
                fn colorTintAdd(color : vec4f, modColor : vec4f) -> vec4f {
                    let newRgb = clamp(color.rgb + (modColor.rgb - vec3<f32>(0.5)),
                                       vec3<f32>(0.0), vec3<f32>(1.0));
                
                    return vec4f(newRgb, color.a * modColor.a);
                }
                
                @fragment
                fn fs_main(input : FragmentInput) -> @location(0) vec4f {
                
                    let texColor = textureSample(texture, textureSampler, input.v_texCoord);
                    var color = colorTintAdd(texColor, input.v_color);
                    
                    return color;
                }
                """;
    }

    private void initBlendMap() {
        blendConstantMap.put(GL20.GL_ZERO, WGPUBlendFactor.Zero);
        blendConstantMap.put(GL20.GL_ONE, WGPUBlendFactor.One);
        blendConstantMap.put(GL20.GL_SRC_ALPHA, WGPUBlendFactor.SrcAlpha);
        blendConstantMap.put(GL20.GL_ONE_MINUS_SRC_ALPHA, WGPUBlendFactor.OneMinusSrcAlpha);
        blendConstantMap.put(GL20.GL_DST_ALPHA, WGPUBlendFactor.DstAlpha);
        blendConstantMap.put(GL20.GL_ONE_MINUS_DST_ALPHA, WGPUBlendFactor.OneMinusDstAlpha);
        blendConstantMap.put(GL20.GL_SRC_COLOR, WGPUBlendFactor.Src);
        blendConstantMap.put(GL20.GL_ONE_MINUS_SRC_COLOR, WGPUBlendFactor.OneMinusSrc);
        blendConstantMap.put(GL20.GL_DST_COLOR, WGPUBlendFactor.Dst);
        blendConstantMap.put(GL20.GL_ONE_MINUS_DST_COLOR, WGPUBlendFactor.OneMinusDst);
        blendConstantMap.put(GL20.GL_SRC_ALPHA_SATURATE, WGPUBlendFactor.SrcAlphaSaturated);

        // and build the inverse mapping
        for (int key : blendConstantMap.keySet()) {
            WGPUBlendFactor factor = blendConstantMap.get(key);
            blendGLConstantMap.put(factor, key);
        }
    }


    // -------- Tweaks --------

    public void setTweakReset(float t1, float t2, float t3, float t4) {
        this.tweak_reset = colorPackedRGBA(t1, t2, t3, t4);
        this.tweak = tweak_reset;
    }

    public void setTweak(float t1, float t2, float t3, float t4) {
        this.tweak = colorPackedRGBA(t1, t2, t3, t4);
    }

    public void setPackedTweak(float packed) {
        this.tweak = packed;
    }

    public float getPackedTweak() {
        return this.tweak;
    }

    public void setTweakReset() {
        this.tweak = this.tweak_reset;
    }

    public void setTweakResetValues(float h, float s, float l, float c) {
        this.tweak_reset = colorPackedRGBA(h, s, l, c);
        this.tweak = this.tweak_reset;
    }


    private static float colorPackedRGBA(float r, float g, float b, float a) {
        return NumberUtils.intBitsToFloat(
                ((int)(a * 255) << 24 & 0xFE000000) |
                        ((int)(b * 255) << 16 & 0xFF0000) |
                        ((int)(g * 255) << 8  & 0xFF00) |
                        ((int)(r * 255)       & 0xFF)
        );
    }

    public void saveState() {
        this.color_save = this.color;
        this.tweak_save = this.tweak;
        System.arraycopy(this.blend, 0, this.blend_save, 0, 4);
    }

    public void loadState() {
        this.color = this.color_save;
        this.tweak = this.tweak_save;
        setBlendFunctionSeparate(
                blend_save[RGB_SRC],
                blend_save[RGB_DST],
                blend_save[ALPHA_SRC],
                blend_save[ALPHA_DST]
        );
    }

    public void reset(){
        this.color = COLOR_RESET;
        this.tweak = this.tweak_reset;
        System.arraycopy(BLEND_RESET,0,this.blend,0,this.blend.length);
    }

    public void setBlendFunctionComposite() {
        this.setBlendFunction(GL32.GL_ONE, GL32.GL_ONE_MINUS_SRC_ALPHA);
    }

    // ####### MediaManager Draw Methods #######

    // ----- CMediaSprite -----

    public void drawCMediaSprite(CMediaSprite cMediaSprite, final int index, final float animationTimer, final float x, final float y) {
        TextureRegion region = mediaManager.sprite(cMediaSprite, index, animationTimer);
        this.draw(region, x, y);
    }

    public void drawCMediaSprite(CMediaSprite cMediaSprite, final int index, final float animationTimer, final float x, final float y, final float width, final float height) {
        TextureRegion region = mediaManager.sprite(cMediaSprite, index, animationTimer);
        this.draw(region, x, y, width, height);
    }

    public void drawCMediaSprite(CMediaSprite cMediaSprite, final int index, final float animationTimer, final float x, final float y, final int srcX, final int srcY, final int srcWidth, final int srcHeight) {
        TextureRegion region = mediaManager.sprite(cMediaSprite, index, animationTimer);
        this.draw(region.getTexture(), x, y, region.getRegionX() + srcX, region.getRegionY() + srcY, srcWidth, srcHeight);
    }

    public void drawCMediaSprite(CMediaSprite cMediaSprite, final int index, final float animationTimer, final float x, final float y, final float originX, final float originY, final float width, final float height, final float scaleX, final float scaleY, final float rotation) {
        TextureRegion region = mediaManager.sprite(cMediaSprite, index, animationTimer);
        this.draw(region, x, y, originX, originY, width, height, scaleX, scaleY, rotation);
    }

    public void drawCMediaSprite(CMediaSprite cMediaSprite, final int index, final float animationTimer, final float x, final float y, final float originX, final float originY, final float width, final float height, final float scaleX, final float scaleY, final float rotation, final boolean clockwise) {
        TextureRegion region = mediaManager.sprite(cMediaSprite, index, animationTimer);
        this.draw(region, x, y, originX, originY, width, height, scaleX, scaleY, rotation, clockwise);
    }

    public void drawCMediaSprite(CMediaSprite cMediaSprite, final int index, final float animationTimer, final float x, final float y, final float originX, final float originY, final float width, final float height, final float scaleX, final float scaleY, final float rotation, final int srcX, final int srcY, final int srcWidth, final int srcHeight, final boolean flipX, final boolean flipY) {
        TextureRegion region = mediaManager.sprite(cMediaSprite, index, animationTimer);
        this.draw(region.getTexture(), x, y, originX, originY, width, height, scaleX, scaleY, rotation, region.getRegionX() + srcX, region.getRegionY() + srcY, srcWidth, srcHeight, flipX, flipY);
    }

    public void drawCMediaSprite(CMediaSprite cMediaSprite, final int index, final float animationTimer, final float x, final float y, final float width, final float height, final int srcX, final int srcY, final int srcWidth, final int srcHeight, final boolean flipX, final boolean flipY) {
        TextureRegion region = mediaManager.sprite(cMediaSprite, index, animationTimer);
        this.draw(region.getTexture(), x, y, width, height, region.getRegionX() + srcX, region.getRegionY() + srcY, srcWidth, srcHeight, flipX, flipY);
    }

    public void drawCMediaSprite(CMediaSprite cMediaSprite, final int index, final float animationTimer, final float x, final float y, final float width, final float height, final float u, final float v, final float u2, final float v2) {
        TextureRegion region = mediaManager.sprite(cMediaSprite, index, animationTimer);
        this.draw(region.getTexture(), x, y, width, height, u, v, u2, v2);
    }

    public void drawCMediaSprite(CMediaSprite cMediaSprite, final int index, final float animationTimer, final float[] spriteVertices, final int offset, final int count) {
        TextureRegion region = mediaManager.sprite(cMediaSprite, index, animationTimer);
        this.draw(region.getTexture(), spriteVertices, offset, count);
    }

    public void drawCMediaSprite(CMediaSprite cMediaSprite, final int index, final float animationTimer, final float width, final float height, final Affine2 transform) {
        TextureRegion region = mediaManager.sprite(cMediaSprite, index, animationTimer);
        this.draw(region, width, height, transform);
    }

    // ----- CMediaimage -----

    public void drawCMediaImage(final CMediaImage cMediaImage, final float x, final float y) {
        final TextureRegion region = mediaManager.image(cMediaImage);
        this.draw(region, x, y);
    }

    public void drawCMediaImage(final CMediaImage cMediaImage, final float x, final float y, final float width, final float height) {
        final TextureRegion region = mediaManager.image(cMediaImage);
        this.draw(region, x, y, width, height);
    }

    public void drawCMediaImage(final CMediaImage cMediaImage, final float x, final float y, final int srcX, final int srcY, final int srcWidth, final int srcHeight) {
        final TextureRegion region = mediaManager.image(cMediaImage);
        this.draw(region.getTexture(), x, y, region.getRegionX() + srcX, region.getRegionY() + srcY, srcWidth, srcHeight);
    }

    public void drawCMediaImage(final CMediaImage cMediaImage, final float x, final float y, final float originX, final float originY, final float width, final float height, final float scaleX, final float scaleY, final float rotation) {
        final TextureRegion region = mediaManager.image(cMediaImage);
        this.draw(region, x, y, originX, originY, width, height, scaleX, scaleY, rotation);
    }

    public void drawCMediaImage(final CMediaImage cMediaImage, final float x, final float y, final float originX, final float originY, final float width, final float height, final float scaleX, final float scaleY, final float rotation, final boolean clockwise) {
        final TextureRegion region = mediaManager.image(cMediaImage);
        this.draw(region, x, y, originX, originY, width, height, scaleX, scaleY, rotation, clockwise);
    }

    public void drawCMediaImage(final CMediaImage cMediaImage, final float x, final float y, final float originX, final float originY, final float width, final float height, final float scaleX, final float scaleY, final float rotation, final int srcX, final int srcY, final int srcWidth, final int srcHeight, final boolean flipX, final boolean flipY) {
        final TextureRegion region = mediaManager.image(cMediaImage);
        this.draw(region.getTexture(), x, y, originX, originY, width, height, scaleX, scaleY, rotation, region.getRegionX() + srcX, region.getRegionY() + srcY, srcWidth, srcHeight, flipX, flipY);
    }

    public void drawCMediaImage(final CMediaImage cMediaImage, final float x, final float y, final float width, final float height, final int srcX, final int srcY, final int srcWidth, final int srcHeight, boolean flipX, boolean flipY) {
        final TextureRegion region = mediaManager.image(cMediaImage);
        this.draw(region.getTexture(), x, y, width, height, region.getRegionX() + srcX, region.getRegionY() + srcY, srcWidth, srcHeight, flipX, flipY);
    }

    public void drawCMediaImage(final CMediaImage cMediaImage, final float x, final float y, final float width, final float height, final float u, final float v, final float u2, final float v2) {
        final TextureRegion region = mediaManager.image(cMediaImage);
        this.draw(region.getTexture(), x, y, width, height, u, v, u2, v2);
    }

    public void drawCMediaImage(final CMediaImage cMediaImage, final float[] spriteVertices, final int offset, final int count) {
        final TextureRegion region = mediaManager.image(cMediaImage);
        this.draw(region.getTexture(), spriteVertices, offset, count);
    }

    public void drawCMediaImage(final CMediaImage cMediaImage, final float width, final float height, final Affine2 transform) {
        final TextureRegion region = mediaManager.image(cMediaImage);
        this.draw(region, width, height, transform);
    }

    // ----- CMediaArray -----

    public void drawCMediaArray(final CMediaArray cMediaArray, final int index, final float x, final float y) {
        final TextureRegion region = mediaManager.array(cMediaArray, index);
        this.draw(region, x, y, region.getRegionWidth(), region.getRegionHeight());
    }

    public void drawCMediaArray(final CMediaArray cMediaArray, final int index, final float x, final float y, final float width, final float height) {
        final TextureRegion region = mediaManager.array(cMediaArray, index);
        this.draw(region, x, y, width, height);
    }

    public void drawCMediaArray(final CMediaArray cMediaArray, final int index, final float x, final float y, final int srcX, final int srcY, final int srcWidth, final int srcHeight) {
        final TextureRegion region = mediaManager.array(cMediaArray, index);
        this.draw(region.getTexture(), x, y, region.getRegionX() + srcX, region.getRegionY() + srcY, srcWidth, srcHeight);
    }

    public void drawCMediaArray(final CMediaArray cMediaArray, final int index, final float x, final float y, final float originX, final float originY, final float width, final float height, final float scaleX, final float scaleY, final float rotation) {
        final TextureRegion region = mediaManager.array(cMediaArray, index);
        this.draw(region, x, y, originX, originY, width, height, scaleX, scaleY, rotation);
    }

    public void drawCMediaArray(final CMediaArray cMediaArray, final int index, final float x, final float y, final float originX, final float originY, final float width, final float height, final float scaleX, final float scaleY, final float rotation, boolean clockwise) {
        final TextureRegion region = mediaManager.array(cMediaArray, index);
        this.draw(region, x, y, originX, originY, width, height, scaleX, scaleY, rotation, clockwise);
    }

    public void drawCMediaArray(final CMediaArray cMediaArray, final int index, final float x, final float y, final float originX, final float originY, final float width, final float height, final float scaleX, final float scaleY, final float rotation, final int srcX, final int srcY, final int srcWidth, final int srcHeight, final boolean flipX, final boolean flipY) {
        final TextureRegion region = mediaManager.array(cMediaArray, index);
        this.draw(region.getTexture(), x, y, originX, originY, width, height, scaleX, scaleY, rotation, region.getRegionX() + srcX, region.getRegionY() + srcY, srcWidth, srcHeight, flipX, flipY);
    }

    public void drawCMediaArray(final CMediaArray cMediaArray, final int index, final float x, final float y, final float width, final float height, final int srcX, final int srcY, final int srcWidth, final int srcHeight, final boolean flipX, final boolean flipY) {
        final TextureRegion region = mediaManager.array(cMediaArray, index);
        this.draw(region.getTexture(), x, y, width, height, region.getRegionX() + srcX, region.getRegionY() + srcY, srcWidth, srcHeight, flipX, flipY);
    }

    public void drawCMediaArray(final CMediaArray cMediaArray, final int index, final float x, final float y, final float width, final float height, final float u, final float v, final float u2, final float v2) {
        final TextureRegion region = mediaManager.array(cMediaArray, index);
        this.draw(region.getTexture(), x, y, width, height, u, v, u2, v2);
    }

    public void drawCMediaArray(final CMediaArray cMediaArray, final int index, final float[] spriteVertices, final int offset, final int count) {
        final TextureRegion region = mediaManager.array(cMediaArray, index);
        this.draw(region.getTexture(), spriteVertices, offset, count);
    }

    public void drawCMediaArray(final CMediaArray cMediaArray, final int index, final float width, final float height, final Affine2 transform) {
        final TextureRegion region = mediaManager.array(cMediaArray, index);
        this.draw(region, width, height, transform);
    }

    // ----- CMediaAnimation -----

    public void drawCMediaAnimation(final CMediaAnimation cMediaAnimation, final float animationTimer, final float x, final float y) {
        final TextureRegion region = mediaManager.animation(cMediaAnimation).getKeyFrame(animationTimer);
        this.draw(region, x, y);
    }

    public void drawCMediaAnimation(final CMediaAnimation cMediaAnimation, final float animationTimer, final float x, final float y, final float width, final float height) {
        final TextureRegion region = mediaManager.animation(cMediaAnimation).getKeyFrame(animationTimer);
        this.draw(region, x, y, width, height);
    }

    public void drawCMediaAnimation(final CMediaAnimation cMediaAnimation, final float animationTimer, final float x, final float y, final int srcX, final int srcY, final int srcWidth, final int srcHeight) {
        final TextureRegion region = mediaManager.animation(cMediaAnimation).getKeyFrame(animationTimer);
        this.draw(region.getTexture(), x, y, region.getRegionX() + srcX, region.getRegionY() + srcY, srcWidth, srcHeight);
    }

    public void drawCMediaAnimation(final CMediaAnimation cMediaAnimation, final float animationTimer, final float x, final float y, final float originX, final float originY, final float width, final float height, final float scaleX, final float scaleY, final float rotation) {
        final TextureRegion region = mediaManager.animation(cMediaAnimation).getKeyFrame(animationTimer);
        this.draw(region, x, y, originX, originY, width, height, scaleX, scaleY, rotation);
    }

    public void drawCMediaAnimation(final CMediaAnimation cMediaAnimation, final float animationTimer, final float x, final float y, final float originX, final float originY, final float width, final float height, final float scaleX, final float scaleY, final float rotation, final boolean clockwise) {
        final TextureRegion region = mediaManager.animation(cMediaAnimation).getKeyFrame(animationTimer);
        this.draw(region, x, y, originX, originY, width, height, scaleX, scaleY, rotation, clockwise);
    }

    public void drawCMediaAnimation(final CMediaAnimation cMediaAnimation, final float animationTimer, final float x, final float y, final float originX, final float originY, final float width, final float height, final float scaleX, final float scaleY, final float rotation, final int srcX, final int srcY, final int srcWidth, final int srcHeight, final boolean flipX, final boolean flipY) {
        final TextureRegion region = mediaManager.animation(cMediaAnimation).getKeyFrame(animationTimer);
        this.draw(region.getTexture(), x, y, originX, originY, width, height, scaleX, scaleY, rotation, region.getRegionX() + srcX, region.getRegionY() + srcY, srcWidth, srcHeight, flipX, flipY);
    }

    public void drawCMediaAnimation(final CMediaAnimation cMediaAnimation, final float animationTimer, final float x, final float y, final float width, final float height, final int srcX, final int srcY, final int srcWidth, final int srcHeight, boolean flipX, boolean flipY) {
        final TextureRegion region = mediaManager.animation(cMediaAnimation).getKeyFrame(animationTimer);
        this.draw(region.getTexture(), x, y, width, height, region.getRegionX() + srcX, region.getRegionY() + srcY, srcWidth, srcHeight, flipX, flipY);
    }

    public void drawCMediaAnimation(final CMediaAnimation cMediaAnimation, final float animationTimer, final float x, final float y, final float width, final float height, final float u, final float v, final float u2, final float v2) {
        final TextureRegion region = mediaManager.animation(cMediaAnimation).getKeyFrame(animationTimer);
        this.draw(region.getTexture(), x, y, width, height, u, v, u2, v2);
    }

    public void drawCMediaAnimation(final CMediaAnimation cMediaAnimation, final float animationTimer, final float[] spriteVertices, final int offset, final int count) {
        TextureRegion region = mediaManager.animation(cMediaAnimation).getKeyFrame(animationTimer);
        this.draw(region.getTexture(), spriteVertices, offset, count);
    }

    public void drawCMediaAnimation(final CMediaAnimation cMediaAnimation, final float animationTimer, final float width, final float height, final Affine2 transform) {
        TextureRegion region = mediaManager.animation(cMediaAnimation).getKeyFrame(animationTimer);
        this.draw(region, width, height, transform);
    }

    // ----- CMediaFont -----

    public void drawCMediaFont(final CMediaFont cMediaFont, final float x, final float y, CharSequence text) {
        this.drawCMediaFont(cMediaFont, x, y, text,0,text.length(), false, false, 0);
    }

    public void drawCMediaFont(final CMediaFont cMediaFont, final float x, final float y, CharSequence text, final int start, final int end) {
        this.drawCMediaFont(cMediaFont, x, y, text,start,end, false, false, 0);
    }

    public void drawCMediaFont(final CMediaFont cMediaFont, final float x, final float y, CharSequence text, final int start, final int end, final boolean centerX, final boolean centerY) {
        this.drawCMediaFont(cMediaFont, x, y, text,start,end, centerX, centerY, 0);
    }

    public void drawCMediaFont(final CMediaFont cMediaFont, final float x, final float y, CharSequence text, final int start, final int end, final boolean centerX, final boolean centerY, final int maxWidth) {
        if (cMediaFont == null) return;
        final float x_draw = centerX ? (x - MathUtils.round(mediaManager.fontTextWidth(cMediaFont, text) / 2f)) : x;
        final float y_draw = centerY ? (y - MathUtils.round(mediaManager.fontTextHeight(cMediaFont, text) / 2f)) : y;
        final BitmapFontCache fontCache = mediaManager.font(cMediaFont).getCache();
        final String truncate = maxWidth > 0 ? "" : null;

        fontCache.clear();
        fontCache.addText(text, x_draw, y_draw, Math.min(start,end), Math.min(end, text.length()), maxWidth, Align.left, false, truncate);

        // Multiply by Batch Color
        Color.abgr8888ToColor(this.tempColor, this.color);
        final float batch_r = this.tempColor.r * 2f; // 0.5 = default -> x 2 = 1f for multiplication
        final float batch_g = this.tempColor.g * 2f;
        final float batch_b = this.tempColor.b * 2f;
        final float batch_a = this.tempColor.a;

        float[] fontVertices = fontCache.getVertices();
        for (int idx = 2; idx < fontVertices.length; idx += 5) {
            float fontColor = fontVertices[idx];
            Color.abgr8888ToColor(this.tempColor, fontColor);
            tempColor.mul(batch_r, batch_g, batch_b, batch_a);
            fontVertices[idx] = (colorPackedRGBA(tempColor.r, tempColor.g, tempColor.b, tempColor.a));
        }

        this.draw(fontCache.getFont().getRegion().getTexture(), fontVertices, 0, fontCache.getVertexCount(0));
    }
}