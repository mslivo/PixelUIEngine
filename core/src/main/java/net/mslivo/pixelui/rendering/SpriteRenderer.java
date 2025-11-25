package net.mslivo.pixelui.rendering;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.BitmapFontCache;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.VertexBufferObjectWithVAO;
import com.badlogic.gdx.math.Affine2;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.*;
import net.mslivo.pixelui.media.*;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public class SpriteRenderer extends CommonRenderer implements Disposable {

    public static final String TEXCOORD_ATTRIBUTE = "a_texCoord";
    public static final String TEXTURE_UNIFORM = "u_texture";
    public static final String TEXTURE_SIZE_UNIFORM = "u_textureSize";
    public static final String COLOR_ATTRIBUTE = "a_color";
    public static final String TWEAK_ATTRIBUTE = "a_tweak";
    public static final String POSITION_ATTRIBUTE = "a_position";

    private static final float COLOR_RESET = colorPackedRGBA(0.5f, 0.5f, 0.5f, 1f);

    private static final int VERTEX_SIZE = 6;
    private static final int INDICES_SIZE = 6;
    private static final int VERTEXES_INDICES_RATIO = 4;
    private static final int SPRITE_SIZE_VERTEXES=4;
    public static final int MAX_VERTEXES_DEFAULT = 65532*SPRITE_SIZE_VERTEXES; // 65532 sprites

    private final int sizeMaxVertexes;
    private final int sizeMaxIndices;
    private final int sizeMaxVertexesFloats;
    private final float[] vertices;
    private int idx;
    private final FloatBuffer vertexBuffer;
    private final IntBuffer indexBuffer;
    private final VertexBufferObjectWithVAO vertexBufferObject;
    private final IntegerIndexBufferObject indexBufferObject;

    private Texture lastTexture;
    private float invTexWidth, invTexHeight;
    private MediaManager mediaManager;
    private int nextSamplerTextureUnit;

    protected float color, color_save;
    protected float tweak, tweak_save, tweak_reset;

    @Override
    protected ShaderProgram provideDefaultShader() {
        return ShaderParser.parse(ShaderParser.SHADER_TEMPLATE.SPRITE,"""
            // Usable Vertex Shader Variables: vec4 a_position | vec4 v_color | vec4 v_tweak | vec2 v_texCoord
            // Usable Fragment Shader Variables: vec4 v_color | vec4 v_tweak | vec2 v_texCoord | sampler2D u_texture | vec2 u_textureSize
            
            // BEGIN VERTEX
            
            // END VERTEX
            
            // BEGIN FRAGMENT

            
            vec4 colorTintAdd(vec4 color, vec4 modColor){
                 color.rgb = clamp(color.rgb+(modColor.rgb-0.5),0.0,1.0);
                 color.a *= modColor.a;
                 return color;
            }
            
            void main(){
            	gl_FragColor = colorTintAdd(texture2D(u_texture, v_texCoord),v_color);
            }
            
            // END FRAGMENT
            """);
    }

    public SpriteRenderer() {
        this(null, null, MAX_VERTEXES_DEFAULT);
    }

    public SpriteRenderer(final MediaManager mediaManager) {
        this(mediaManager, null, MAX_VERTEXES_DEFAULT);
    }

    public SpriteRenderer(final MediaManager mediaManager, final ShaderProgram defaultShader) {
        this(mediaManager, defaultShader, MAX_VERTEXES_DEFAULT);
    }

    public SpriteRenderer(final MediaManager mediaManager, final ShaderProgram defaultShader, final int maxVertexes) {
        int vertexAbsoluteLimit = Integer.MAX_VALUE / (VERTEX_SIZE * 4);
        if (maxVertexes > vertexAbsoluteLimit)
            throw new IllegalArgumentException("size " + maxVertexes + " bigger than mix allowed size " + vertexAbsoluteLimit);
        if (maxVertexes % VERTEXES_INDICES_RATIO != 0)
            throw new IllegalArgumentException("size is not multiple of ratio " + VERTEXES_INDICES_RATIO);
        super(defaultShader);

        this.tweak_reset = colorPackedRGBA(0f, 0f, 0f, 0.0f);
        this.tweak_save = tweak_reset;
        this.tweak = tweak_reset;


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
        this.invTexWidth = this.invTexHeight = 0;
        this.nextSamplerTextureUnit = 1;
        this.mediaManager = mediaManager;

        this.color = COLOR_RESET;
        this.color_save = this.color;
        this.tweak_reset = colorPackedRGBA(0f, 0f, 0f, 0f);
        this.tweak = this.tweak_reset;
        this.tweak_save = this.tweak;
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
        setDrawing(true);
    }


    public void end() {
        flush();
        Gdx.gl.glDepthMask(true);
        setDrawing(false);
        lastTexture = null;
        this.nextSamplerTextureUnit = 1;
    }

    private boolean isVertexBufferLimitReached() {
        return this.idx >= this.sizeMaxVertexesFloats;
    }

    public void draw(final Texture texture, final float x, final float y, final float originX, final float originY, final float width, final float height, final float scaleX,
                     final float scaleY, final float rotation, final int srcX, final int srcY, final int srcWidth, final int srcHeight, final boolean flipX, final boolean flipY) {
        if (!isDrawing()) throw new IllegalStateException(ERROR_BEGIN_END);

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

    private void vertexPush(float x, float y, float color, float u, float v, float tweak) {
        this.vertices[idx] = x;
        this.vertices[idx + 1] = y;
        this.vertices[idx + 2] = color;
        this.vertices[idx + 3] = u;
        this.vertices[idx + 4] = v;
        this.vertices[idx + 5] = tweak;
        idx += 6;
    }

    public void draw(final Texture texture, final float x, final float y, final float width, final float height, final int srcX, final int srcY, final int srcWidth,
                     final int srcHeight, final boolean flipX, final boolean flipY) {
        if (!isDrawing()) throw new IllegalStateException(ERROR_BEGIN_END);

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
        if (!isDrawing()) throw new IllegalStateException("SpritRenderer.begin must be called before draw.");


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
        if (!isDrawing()) throw new IllegalStateException(ERROR_BEGIN_END);


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
        if (!isDrawing()) throw new IllegalStateException(ERROR_BEGIN_END);


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
        if (!isDrawing()) throw new IllegalStateException(ERROR_BEGIN_END);

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
        if (!isDrawing()) throw new IllegalStateException(ERROR_BEGIN_END);

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


    private void vertexPush(float[] value, int offset, int count) {
        System.arraycopy(value, offset, this.vertices, idx, count);
        idx += count;
    }

    public void draw(final TextureRegion region, final float x, final float y) {
        draw(region, x, y, region.getRegionWidth(), region.getRegionHeight());
    }

    public void draw(final TextureRegion region, final float x, final float y, final float width, final float height) {
        if (!isDrawing()) throw new IllegalStateException(ERROR_BEGIN_END);

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
        if (!isDrawing()) throw new IllegalStateException(ERROR_BEGIN_END);


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
        if (!isDrawing()) throw new IllegalStateException(ERROR_BEGIN_END);


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
        if (!isDrawing()) throw new IllegalStateException(ERROR_BEGIN_END);

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
        this.color_save = this.color;
        this.tweak_save = this.tweak;
    }

    @Override
    protected void loadStateImpl() {
        this.color = this.color_save;
        this.tweak = this.tweak_save;
    }

    @Override
    protected void resetImpl(){
        this.color = COLOR_RESET;
        this.tweak = this.tweak_reset;
    }

    // -------- Colors --------

    public void setColor(float r, float g, float b, float a) {
        this.color = colorPackedRGBA(r, g, b, a);
    }

    public void setColor(Color color){
        this.color = colorPackedRGBA(color.r,color.g,color.b,color.a);
    }

    public void setColor(Color color, float a){
        this.color = colorPackedRGBA(color.r,color.g,color.b,a);
    }

    public void setPackedColor(float packed) {
        this.color = packed;
    }

    public float getPackedColor() {
        return this.color;
    }

    // -------- Tweaks --------

    public void setTweak(float t1, float t2, float t3, float t4) {
        this.tweak = colorPackedRGBA(t1, t2, t3, t4);
    }

    public void setPackedTweak(float packed) {
        this.tweak = packed;
    }

    public float getPackedTweak() {
        return this.tweak;
    }

    public Color getColor(){
        Color.abgr8888ToColor(this.tempColor,getPackedColor());
        return this.tempColor;
    }

    public void setTweakResetValues(float h, float s, float l, float c) {
        this.tweak_reset = colorPackedRGBA(h, s, l, c);
        this.tweak = this.tweak_reset;
    }

    @Override
    public void dispose() {
        vertexBufferObject.dispose();
        indexBufferObject.dispose();
        if (this.shader == this.defaultShader)
            this.shader.dispose();
    }
}