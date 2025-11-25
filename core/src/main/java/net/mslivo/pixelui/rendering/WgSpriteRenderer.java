package net.mslivo.pixelui.rendering;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.BitmapFontCache;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.VertexBufferObjectWithVAO;
import com.badlogic.gdx.math.Affine2;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.github.xpenatan.webgpu.WGPUBlendFactor;
import com.monstrous.gdx.webgpu.application.WebGPUContext;
import com.monstrous.gdx.webgpu.application.WgGraphics;
import com.monstrous.gdx.webgpu.graphics.Binder;
import com.monstrous.gdx.webgpu.graphics.WgShaderProgram;
import com.monstrous.gdx.webgpu.wrappers.PipelineCache;
import com.monstrous.gdx.webgpu.wrappers.PipelineSpecification;
import net.mslivo.pixelui.media.*;

import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Map;

public class WgSpriteRenderer implements Disposable {

    public static final String TEXCOORD_ATTRIBUTE = "a_texCoord";
    public static final String TEXTURE_UNIFORM = "u_texture";
    public static final String TEXTURE_SIZE_UNIFORM = "u_textureSize";
    public static final String COLOR_ATTRIBUTE = "a_color";
    public static final String TWEAK_ATTRIBUTE = "a_tweak";
    public static final String POSITION_ATTRIBUTE = "a_position";

    private static final int VERTEX_SIZE = 6;
    private static final int INDICES_SIZE = 6;
    private static final int VERTEXES_INDICES_RATIO = 4;
    public static final int MAX_VERTEXES_DEFAULT = 65532 * 4; // 65532 sprites

    private final WgGraphics gfx;
    private final WebGPUContext webgpu;
    private final int maxSprites;
    private final VertexAttributes vertexAttributes;
    private final int vertexSize;
    private MediaManager mediaManager;
    private final Map<Integer, WGPUBlendFactor> blendConstantMap = new HashMap<>(); // mapping GL vs WebGPU constants
    private final Map<WGPUBlendFactor, Integer> blendGLConstantMap = new HashMap<>(); // vice versa
    public int maxFlushes;
    private float invTexWidth;
    private float invTexHeight;

    public WgSpriteRenderer(MediaManager mediaManager, int maxSprites, WgShaderProgram specificShader, int maxFlushes) {
        super(specificShader);
        this.mediaManager = mediaManager;
        gfx = (WgGraphics) Gdx.graphics;
        webgpu = gfx.getContext();

        if (maxSprites > 16384)
            throw new GdxRuntimeException("Too many sprites. Max is 16384.");

        this.maxSprites = maxSprites;

        vertexAttributes = new VertexAttributes(new VertexAttribute(VertexAttributes.Usage.Position, 2, POSITION_ATTRIBUTE),
                new VertexAttribute(VertexAttributes.Usage.ColorPacked, 4, COLOR_ATTRIBUTE),
                new VertexAttribute(VertexAttributes.Usage.TextureCoordinates, 2, TEXCOORD_ATTRIBUTE),
                new VertexAttribute(VertexAttributes.Usage.ColorPacked, 4, TWEAK_ATTRIBUTE));

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

        tint = new Color(Color.WHITE);

        invTexWidth = 0f;
        invTexHeight = 0f;

        bindGroupLayout = createBindGroupLayout();

        binder = new Binder();
        // define group
        binder.defineGroup(0, bindGroupLayout);
        // define bindings in the group
        binder.defineBinding("uniforms", 0, 0);
        binder.defineBinding("texture", 0, 1);
        binder.defineBinding("textureSampler", 0, 2);
        // define uniforms in uniform buffer (binding 0) with their offset
        binder.defineUniform("projectionViewTransform", 0, 0, 0);

        // set binding 0 to uniform buffer
        binder.setBuffer("uniforms", uniformBuffer, 0, uniformBufferSize);
        // bindings 1 and 2 are done in switchTexture()

        // get pipeline layout which aggregates all the bind group layouts
        pipelineLayout = binder.getPipelineLayout("SpriteBatch pipeline layout");

        pipelines = new PipelineCache();
        pipelineSpec = new PipelineSpecification(vertexAttributes, this.specificShader);
        pipelineSpec.name = "SpriteBatch pipeline";

        // default blending values
        pipelineSpec.enableBlending();
        pipelineSpec.setBlendFactor(WGPUBlendFactor.SrcAlpha, WGPUBlendFactor.OneMinusSrcAlpha);
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
        initialPipeline = currentPipeline;

        projectionMatrix.setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), 0, 100);
        transformMatrix.idt();

        drawing = false;
        frameNumber = -1;
    }

    private IntegerIndexBufferObject createIndexBufferObject(int size) {
        int len = (size / VERTEXES_INDICES_RATIO) * INDICES_SIZE;
        int j = 0;
        int[] indices = new int[len];
        for (int i = 0; i < len; i += 6, j += 4) {
            indices[i] = j;
            indices[i + 1] = j + 1;
            indices[i + 2] = j + 2;
            indices[i + 3] = j + 2;
            indices[i + 4] = j + 3;
            indices[i + 5] = j;
        }

        IntegerIndexBufferObject indexBufferObject = new IntegerIndexBufferObject(true, size * INDICES_SIZE);
        indexBufferObject.setIndices(indices, 0, indices.length);

        return indexBufferObject;
    }


    private VertexBufferObjectWithVAO createVertexBufferObject(int size) {
        return new VertexBufferObjectWithVAO(true, size,
                new VertexAttribute(VertexAttributes.Usage.Position, 2, POSITION_ATTRIBUTE),
                new VertexAttribute(VertexAttributes.Usage.ColorPacked, 4, COLOR_ATTRIBUTE),
                new VertexAttribute(VertexAttributes.Usage.TextureCoordinates, 2, TEXCOORD_ATTRIBUTE),
                new VertexAttribute(VertexAttributes.Usage.ColorPacked, 4, TWEAK_ATTRIBUTE));
    }

    public void begin() {
        if (drawing) throw new IllegalStateException(ERROR_END_BEGIN);
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


    public void end() {
        flush();
        Gdx.gl.glDepthMask(true);
        this.drawing = false;
        lastTexture = null;
        this.nextSamplerTextureUnit = 1;
    }

    private boolean isVertexBufferLimitReached() {
        return this.idx >= this.sizeMaxVertexesFloats;
    }

    public void draw(final Texture texture, final float x, final float y, final float originX, final float originY, final float width, final float height, final float scaleX,
                     final float scaleY, final float rotation, final int srcX, final int srcY, final int srcWidth, final int srcHeight, final boolean flipX, final boolean flipY) {
        if (!drawing) throw new IllegalStateException(ERROR_BEGIN_END);

        if (texture != lastTexture)
            switchTexture(texture);
        else if (isVertexBufferLimitReached()) {
            flush();
        }

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

        final float color = this.color;
        final float tweak = this.tweak;

        vertexPush(x1, y1, color, u, v, tweak);
        vertexPush(x2, y2, color, u, v2, tweak);
        vertexPush(x3, y3, color, u2, v2, tweak);
        vertexPush(x4, y4, color, u2, v, tweak);

    }

    public void vertexPush(float value1, float value2, float value3, float value4, float value5, float value6) {
        this.vertices[idx] = value1;
        this.vertices[idx + 1] = value2;
        this.vertices[idx + 2] = value3;
        this.vertices[idx + 3] = value4;
        this.vertices[idx + 4] = value5;
        this.vertices[idx + 5] = value6;
        idx += 6;
    }

    public void draw(final Texture texture, final float x, final float y, final float width, final float height, final int srcX, final int srcY, final int srcWidth,
                     final int srcHeight, final boolean flipX, final boolean flipY) {
        if (!drawing) throw new IllegalStateException(ERROR_BEGIN_END);

        if (texture != lastTexture)
            switchTexture(texture);
        else if (isVertexBufferLimitReached()) {
            flush();
        }

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

        final float color = this.color;
        final float tweak = this.tweak;

        vertexPush(x, y, color, u, v, tweak);
        vertexPush(x, fy2, color, u, v2, tweak);
        vertexPush(fx2, fy2, color, u2, v2, tweak);
        vertexPush(fx2, y, color, u2, v, tweak);


    }

    public void draw(final Texture texture, final float x, final float y, final int srcX, final int srcY, final int srcWidth, final int srcHeight) {
        if (!drawing) throw new IllegalStateException("SpritRenderer.begin must be called before draw.");


        if (texture != lastTexture)
            switchTexture(texture);
        else if (isVertexBufferLimitReached()) {
            flush();
        }

        final float u = srcX * invTexWidth;
        final float v = (srcY + srcHeight) * invTexHeight;
        final float u2 = (srcX + srcWidth) * invTexWidth;
        final float v2 = srcY * invTexHeight;
        final float fx2 = x + srcWidth;
        final float fy2 = y + srcHeight;

        final float color = this.color;
        final float tweak = this.tweak;

        vertexPush(x, y, color, u, v, tweak);
        vertexPush(x, fy2, color, u, v2, tweak);
        vertexPush(fx2, fy2, color, u2, v2, tweak);
        vertexPush(fx2, y, color, u2, v, tweak);


    }

    public void draw(final Texture texture, final float x, final float y, final float width, final float height, final float u, final float v, final float u2, final float v2) {
        if (!drawing) throw new IllegalStateException(ERROR_BEGIN_END);


        if (texture != lastTexture)
            switchTexture(texture);
        else if (isVertexBufferLimitReached()) {
            flush();
        }

        final float fx2 = x + width;
        final float fy2 = y + height;

        final float color = this.color;
        final float tweak = this.tweak;

        vertexPush(x, y, color, u, v, tweak);
        vertexPush(x, fy2, color, u, v2, tweak);
        vertexPush(fx2, fy2, color, u2, v2, tweak);
        vertexPush(fx2, y, color, u2, v, tweak);


    }

    public void draw(final Texture texture, final float x, final float y) {
        draw(texture, x, y, texture.getWidth(), texture.getHeight());
    }

    public void draw(final Texture texture, final float x, final float y, final float width, final float height) {
        if (!drawing) throw new IllegalStateException(ERROR_BEGIN_END);


        if (texture != lastTexture)
            switchTexture(texture);
        else if (isVertexBufferLimitReached()) {
            flush();
        }

        final float fx2 = x + width;
        final float fy2 = y + height;
        final float u = 0;
        final float v = 1;
        final float u2 = 1;
        final float v2 = 0;

        final float color = this.color;
        final float tweak = this.tweak;

        vertexPush(x, y, color, u, v, tweak);
        vertexPush(x, fy2, color, u, v2, tweak);
        vertexPush(fx2, fy2, color, u2, v2, tweak);
        vertexPush(fx2, y, color, u2, v, tweak);


    }

    public void draw(final Texture texture, final float[] spriteVertices, int offset, int count) {
        if (!drawing) throw new IllegalStateException(ERROR_BEGIN_END);

        count = (count / 5) * 6;
        final int verticesLength = sizeMaxVertexesFloats;

        int remainingVertices = verticesLength;
        if (texture != lastTexture)
            switchTexture(texture);
        else {
            remainingVertices -= this.idx;
            if (remainingVertices == 0) {
                flush();
                remainingVertices = verticesLength;
            }
        }
        int copyCount = Math.min(remainingVertices, count);
        final float tweak = this.tweak;

        for (int s = offset, i = 0; i < copyCount; i += 6) {
            vertexPush(spriteVertices[s++], spriteVertices[s++], spriteVertices[s++], spriteVertices[s++], spriteVertices[s++], this.tweak);
        }
        count -= copyCount;
        while (count > 0) {
            offset += (copyCount / 6) * 5;
            flush();
            copyCount = Math.min(verticesLength, count);
            for (int s = offset, v = 0, i = 0; i < copyCount; i += 6) {
                vertexPush(spriteVertices[s++], spriteVertices[s++], spriteVertices[s++], spriteVertices[s++], spriteVertices[s++], this.tweak);
            }
            count -= copyCount;
        }
    }


    public void drawExactly(final Texture texture, final float[] spriteVertices, int offset, int count) {
        if (!drawing) throw new IllegalStateException(ERROR_BEGIN_END);

        int remainingVertices = sizeMaxVertexesFloats;
        if (texture != lastTexture)
            switchTexture(texture);
        else {
            remainingVertices -= this.idx;
            if (remainingVertices == 0) {
                flush();
                remainingVertices = sizeMaxVertexesFloats;
            }
        }
        int copyCount = Math.min(remainingVertices, count);

        vertexPush(spriteVertices, offset, copyCount);
        count -= copyCount;
        while (count > 0) {
            offset += copyCount;
            flush();
            copyCount = Math.min(sizeMaxVertexes, count);
            vertexPush(spriteVertices, offset, copyCount);
            count -= copyCount;
        }
    }


    public void vertexPush(float[] value, int offset, int count) {
        System.arraycopy(value, offset, this.vertices, idx, count);
        idx += count;
    }

    public void draw(final TextureRegion region, final float x, final float y) {
        draw(region, x, y, region.getRegionWidth(), region.getRegionHeight());
    }

    public void draw(final TextureRegion region, final float x, final float y, final float width, final float height) {
        if (!drawing) throw new IllegalStateException(ERROR_BEGIN_END);

        final Texture texture = region.getTexture();
        if (texture != lastTexture) {
            switchTexture(texture);
        } else if (isVertexBufferLimitReached()) {
            flush();
        }

        final float fx2 = x + width;
        final float fy2 = y + height;
        final float u = region.getU();
        final float v = region.getV2();
        final float u2 = region.getU2();
        final float v2 = region.getV();

        final float color = this.color;
        final float tweak = this.tweak;

        vertexPush(x, y, color, u, v, tweak);
        vertexPush(x, fy2, color, u, v2, tweak);
        vertexPush(fx2, fy2, color, u2, v2, tweak);
        vertexPush(fx2, y, color, u2, v, tweak);

    }

    public void draw(final TextureRegion region, final float x, final float y, final float originX, final float originY, final float width, final float height,
                     float scaleX, final float scaleY, final float rotation) {
        if (!drawing) throw new IllegalStateException(ERROR_BEGIN_END);


        final Texture texture = region.getTexture();
        if (texture != lastTexture) {
            switchTexture(texture);
        } else if (isVertexBufferLimitReached()) {
            flush();
        }

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

        final float color = this.color;
        final float tweak = this.tweak;

        vertexPush(x1, y1, color, u, v, tweak);
        vertexPush(x2, y2, color, u, v2, tweak);
        vertexPush(x3, y3, color, u2, v2, tweak);
        vertexPush(x4, y4, color, u2, v, tweak);


    }

    public void draw(final TextureRegion region, final float x, final float y, final float originX, final float originY, final float width, final float height,
                     float scaleX, final float scaleY, final float rotation, boolean clockwise) {
        if (!drawing) throw new IllegalStateException(ERROR_BEGIN_END);


        final Texture texture = region.getTexture();
        if (texture != lastTexture) {
            switchTexture(texture);
        } else if (isVertexBufferLimitReached()) {
            flush();
        }

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

        final float color = this.color;
        final float tweak = this.tweak;


        vertexPush(x1, y1, color, u1, v1, tweak);
        vertexPush(x2, y2, color, u2, v2, tweak);
        vertexPush(x3, y3, color, u3, v3, tweak);
        vertexPush(x4, y4, color, u4, v4, tweak);


    }

    public void draw(final TextureRegion region, final float width, final float height, Affine2 transform) {
        if (!drawing) throw new IllegalStateException(ERROR_BEGIN_END);

        final Texture texture = region.getTexture();
        if (texture != lastTexture) {
            switchTexture(texture);
        } else if (isVertexBufferLimitReached()) {
            flush();
        }

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

        final float color = this.color;
        final float tweak = this.tweak;

        vertexPush(x1, y1, color, u, v, tweak);
        vertexPush(x2, y2, color, u, v2, tweak);
        vertexPush(x3, y3, color, u2, v2, tweak);
        vertexPush(x4, y4, color, u2, v, tweak);

    }

    public void flush() {
        if (idx == 0) return;

        lastTexture.bind();

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
        Gdx.gl32.glDrawElements(GL32.GL_TRIANGLES, indicesCount, GL32.GL_UNSIGNED_INT, 0);

        // reset
        this.indexBuffer.limit(this.sizeMaxIndices);
        this.idx = 0;
    }

    private void switchTexture(final Texture texture) {
        flush();
        lastTexture = texture;
        invTexWidth = 1.0f / texture.getWidth();
        invTexHeight = 1.0f / texture.getHeight();
        shader.setUniformi(uniformLocation(TEXTURE_UNIFORM), 0);
        shader.setUniformf(uniformLocation(TEXTURE_SIZE_UNIFORM), texture.getWidth(), texture.getHeight());
    }

    @Override
    public void setShader(ShaderProgram shader) {
        super.setShader(shader);
        this.nextSamplerTextureUnit = 1;
    }

    public void bindCMediaImageToUniform(CMediaImage cMediaImage, String uniform) {
        bindCMediaImageToUniform(cMediaImage, uniform, null);
    }

    public void bindCMediaImageToUniform(CMediaImage cMediaImage, String uniform, String sizeUniform) {
        if (cMediaImage.useAtlas)
            throw new RuntimeException("Texures used as samplers should not be in a TextureAtlas");
        bindTextureToUniform(mediaManager.image(cMediaImage).getTexture(), uniform, sizeUniform);
    }

    public void bindTextureToUniform(Texture texture, String uniform) {
        bindTextureToUniform(texture, uniform, null);
    }

    public void bindTextureToUniform(Texture texture, String uniform, String sizeUniform) {
        Gdx.gl.glActiveTexture(GL32.GL_TEXTURE0 + this.nextSamplerTextureUnit);
        texture.bind();
        this.shader.setUniformi(uniformLocation(uniform), this.nextSamplerTextureUnit);
        if (sizeUniform != null) {
            this.shader.setUniformf(uniformLocation(sizeUniform), texture.getWidth(), texture.getHeight());
        }
        Gdx.gl.glActiveTexture(GL32.GL_TEXTURE0);
        this.nextSamplerTextureUnit++;
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
        this.drawCMediaFont(cMediaFont, x, y, text, 0, text.length(), false, false, 0);
    }

    public void drawCMediaFont(final CMediaFont cMediaFont, final float x, final float y, CharSequence text, final int start, final int end) {
        this.drawCMediaFont(cMediaFont, x, y, text, start, end, false, false, 0);
    }

    public void drawCMediaFont(final CMediaFont cMediaFont, final float x, final float y, CharSequence text, final int start, final int end, final boolean centerX, final boolean centerY) {
        this.drawCMediaFont(cMediaFont, x, y, text, start, end, centerX, centerY, 0);
    }

    public void drawCMediaFont(final CMediaFont cMediaFont, final float x, final float y, CharSequence text, final int start, final int end, final boolean centerX, final boolean centerY, final int maxWidth) {
        if (cMediaFont == null) return;
        final float x_draw = centerX ? (x - MathUtils.round(mediaManager.fontTextWidth(cMediaFont, text) / 2f)) : x;
        final float y_draw = centerY ? (y - MathUtils.round(mediaManager.fontTextHeight(cMediaFont, text) / 2f)) : y;
        final BitmapFontCache fontCache = mediaManager.font(cMediaFont).getCache();
        final String truncate = maxWidth > 0 ? "" : null;

        fontCache.clear();
        fontCache.addText(text, x_draw, y_draw, Math.min(start, end), Math.min(end, text.length()), maxWidth, Align.left, false, truncate);

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

    @Override
    protected void setBlendFuncSeparate(int srcColor, int dstColor, int srcAlpha, int dstAlpha) {
        Gdx.gl.glBlendFuncSeparate(srcColor, dstColor, srcAlpha, dstAlpha);
    }

    @Override
    protected void setBlendFunc(int srcColor, int dstColor) {
        Gdx.gl.glBlendFunc(srcColor, dstColor);
    }

    @Override
    protected void saveStateRenderer() {
    }

    @Override
    protected void loadStateRenderer() {
    }


    @Override
    public void dispose() {
        vertexBufferObject.dispose();
        indexBufferObject.dispose();
        if (this.shader == this.defaultShader)
            this.shader.dispose();
    }


    @Override
    protected WgShaderProgram provideDefaultShader() {
        return new WgShaderProgram("wgSpriteRendererDefault", """
                                struct VertexInput {
                    @location(0) a_position : vec4<f32>,
                    @location(1) a_color    : vec4<f32>,
                    @location(2) a_tweak    : vec4<f32>,
                    @location(3) a_texCoord : vec2<f32>,
                };
                
                struct Uniforms {
                    projectionViewTransform : mat4x4<f32>,
                };
                
                @group(0) @binding(0)
                var<uniform> uniforms : Uniforms;
                
                struct VertexOutput {
                    @builtin(position) Position : vec4<f32>,
                    @location(0) v_color        : vec4<f32>,
                    @location(1) v_tweak        : vec4<f32>,
                    @location(2) v_texCoord     : vec2<f32>,
                };
                
                const FLOAT_CORRECTION : f32 = 255.0 / 254.0;
                
                @vertex
                fn vs_main(input : VertexInput) -> VertexOutput {
                    var out : VertexOutput;
                
                    out.v_color    = input.a_color * FLOAT_CORRECTION;
                    out.v_tweak    = input.a_tweak * FLOAT_CORRECTION;
                    out.v_texCoord = input.a_texCoord;
                
                    out.Position = uniforms.u_projTrans * input.a_position;
                
                    return out;
                }
                
                struct FragmentInput {
                    @location(0) v_color    : vec4<f32>,
                    @location(1) v_tweak    : vec4<f32>,
                    @location(2) v_texCoord : vec2<f32>,
                };
                
                @group(0) @binding(1)
                var u_texture : texture_2d<f32>;
                
                @group(0) @binding(2)
                var u_sampler : sampler;
                
                const FLOAT_CORRECTION : f32 = 255.0 / 254.0;
                
                fn colorTintAdd(color : vec4<f32>, modColor : vec4<f32>) -> vec4<f32> {
                    var c = color;
                    c.rgb = clamp(c.rgb + (modColor.rgb - vec3<f32>(0.5)), vec3<f32>(0.0), vec3<f32>(1.0));
                    c.a = c.a * modColor.a;
                    return c;
                }
                
                @fragment
                fn fs_main(input : FragmentInput) -> @location(0) vec4<f32> {
                
                    let texColor = textureSample(u_texture, u_sampler, input.v_texCoord);
                    var color = colorTintAdd(texColor, input.v_color);
                
                    var hsl = rgb2hsl(color);
                
                    hsl.x = fract(hsl.x + (input.v_tweak.x - 0.5));
                    hsl.y = clamp(hsl.y + ((input.v_tweak.y - 0.5) * 2.0), 0.0, 1.0);
                    hsl.z = clamp(hsl.z + ((input.v_tweak.z - 0.5) * 2.0), 0.0, 1.0);
                
                    return hsl2rgb(hsl);
                }
                """, "");
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
}