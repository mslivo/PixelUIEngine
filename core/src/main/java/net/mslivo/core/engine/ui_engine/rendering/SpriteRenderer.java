package net.mslivo.core.engine.ui_engine.rendering;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFontCache;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.*;
import com.badlogic.gdx.math.Affine2;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.NumberUtils;
import com.badlogic.gdx.utils.ObjectIntMap;
import net.mslivo.core.engine.media_manager.*;
import net.mslivo.core.engine.ui_engine.rendering.shader.SpriteShader;

import java.nio.ShortBuffer;
import java.util.Arrays;
import java.util.HashMap;

/**
 * A substitute for {@link com.badlogic.gdx.graphics.g2d.SpriteBatch} that adds better coloring.
 * This class is based on code from https://github.com/tommyettinger/colorful-gdx
 */
public class SpriteRenderer implements Batch {

    public static final String TWEAK_ATTRIBUTE = "a_tweak";
    public static final String POSITION_ATTRIBUTE = "a_position";
    public static final String COLOR_ATTRIBUTE = "a_color";
    public static final String TEXCOORD_ATTRIBUTE = "a_texCoord";

    public static final String TEXTURE_UNIFORM = "u_texture";
    public static final String TEXTURE_SIZE_UNIFORM = "u_textureSize";
    public static final String PROJTRANS_UNIFORM = "u_projTrans";

    public static final int SIZE_DEFAULT = 16383;
    public static final int SIZE_MAX = 16383;

    private static final String ERROR_END_BEGIN = "SpriteRenderer.end must be called before begin.";
    private static final String ERROR_BEGIN_END = "SpriteRenderer.begin must be called before end.";
    private static final int VERTEX_SIZE = 6;
    private static final int INDICES_SIZE = 6;
    private static final int SPRITE_SIZE = 24;
    private static final int RGB_SRC = 0, RGB_DST = 1, ALPHA_SRC = 2, ALPHA_DST = 3;
    private static final String FLUSH_WARNING = "%d intermediate flushes detected | vertices.length=%d | %s";
    private static final SpriteShader DEFAULT_SHADER = new SpriteShader("""
            VERTEX:
            FRAGMENT:import colorTintAdd
            void main(){
            	gl_FragColor = colorTintAdd(texture2D(u_texture, v_texCoord),v_color);
            }
            """);

    private final Color tempColor;
    private VertexData vertexData;
    private IndexData indexData;
    private float[] vertices;
    private int idx;
    private Texture lastTexture;
    private float invTexWidth, invTexHeight;
    private boolean drawing;

    private final Matrix4 projectionMatrix;
    private final Matrix4 transformMatrix;
    private final Matrix4 combinedMatrix;
    private ShaderProgram shader;
    private ShaderProgram defaultShader;

    private MediaManager mediaManager;
    private int renderCalls;
    private int totalRenderCalls;
    private int maxSpritesInBatch;
    private boolean flushWarning;
    private int intermediateFlushes;
    private float color;
    private float tweak;
    private final int[] blend;

    private float backup_tweak;
    private float backup_color;
    private int[] backup_blend;
    private int nextSamplerTextureUnit;

    private float reset_tweak;
    private float reset_color;
    private final int[] reset_blend;

    private final HashMap<ShaderProgram, ObjectIntMap<String>> uniformLocationCache;

    public SpriteRenderer() {
        this(null, null, SIZE_DEFAULT, false);
    }

    public SpriteRenderer(final MediaManager mediaManager) {
        this(mediaManager, null, SIZE_DEFAULT, false);
    }

    public SpriteRenderer(final MediaManager mediaManager, final ShaderProgram shader) {
        this(mediaManager, shader, SIZE_DEFAULT, false);
    }

    public SpriteRenderer(final MediaManager mediaManager, final ShaderProgram shader, final int size) {
        this(mediaManager, shader, size, false);
    }

    private ShaderProgram defaultShader() {
        if (this.defaultShader == null)
            this.defaultShader = DEFAULT_SHADER.compile();
        return this.defaultShader;
    }

    public SpriteRenderer(final MediaManager mediaManager,final ShaderProgram defaultShader, final int size,final boolean flushWarning) {
        if (size > SIZE_MAX)
            throw new IllegalArgumentException("Can't have more than " + SIZE_MAX + " sprites per batch: " + size);
        this.flushWarning = flushWarning;
        this.drawing = false;
        this.idx = 0;
        this.intermediateFlushes = 0;
        this.lastTexture = null;
        this.transformMatrix = new Matrix4();
        this.combinedMatrix = new Matrix4();
        this.tempColor = new Color(Color.GRAY);
        this.renderCalls = this.totalRenderCalls = this.maxSpritesInBatch = 0;
        this.invTexWidth = this.invTexHeight = 0;
        this.vertexData = createVertexData(size);
        this.indexData = createIndexData(size);
        this.vertices = createVerticesArray(size);
        this.projectionMatrix = new Matrix4().setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        this.reset_tweak = colorPackedRGBA(0f, 0f, 0f, 0.0f);
        this.reset_color = colorPackedRGBA(0.5f, 0.5f, 0.5f, 1f);
        this.reset_blend = new int[]{GL32.GL_SRC_ALPHA, GL32.GL_ONE_MINUS_SRC_ALPHA, GL32.GL_ONE, GL32.GL_ONE_MINUS_SRC_ALPHA};

        this.color = reset_color;
        this.tweak = reset_tweak;
        this.blend = new int[]{this.reset_blend[RGB_SRC], this.reset_blend[RGB_DST], this.reset_blend[ALPHA_SRC], this.reset_blend[ALPHA_DST]};

        this.backup_color = this.color;
        this.backup_tweak = this.tweak;
        this.backup_blend = new int[]{this.blend[RGB_SRC], this.blend[RGB_DST], this.blend[ALPHA_SRC], this.blend[ALPHA_DST]};
        this.nextSamplerTextureUnit = 1;
        this.uniformLocationCache = new HashMap<>();

        this.mediaManager = mediaManager;
        this.defaultShader = defaultShader;

        setShader(defaultShader()); // null = default shader
    }

    private IndexBufferObject createIndexData(int size) {
        int len = size * INDICES_SIZE;
        int j = 0;
        short[] indices = new short[len];

        for (int i = 0; i < len; i += 6, j += 4) {
            indices[i] = intToUnsignedShort(j);
            indices[i + 1] = intToUnsignedShort(j + 1);
            indices[i + 2] = intToUnsignedShort(j + 2);
            indices[i + 3] = intToUnsignedShort(j + 2);
            indices[i + 4] = intToUnsignedShort(j + 3);
            indices[i + 5] = intToUnsignedShort(j);
        }

        IndexBufferObject indexBufferObject = new IndexBufferObject(true, size * INDICES_SIZE);
        indexBufferObject.setIndices(indices, 0, indices.length);

        return indexBufferObject;
    }

    public static short intToUnsignedShort(int value) {
        return (short) (value & 0xFFFF);
    }

    private float[] createVerticesArray(int size) {
        float[] newVertices = new float[size * SPRITE_SIZE];
        return newVertices;
    }

    private VertexData createVertexData(int size) {
        return new VertexBufferObjectWithVAO(true, size * VERTEX_SIZE,
                new VertexAttribute(VertexAttributes.Usage.Position, 2, POSITION_ATTRIBUTE),
                new VertexAttribute(VertexAttributes.Usage.ColorPacked, 4, COLOR_ATTRIBUTE),
                new VertexAttribute(VertexAttributes.Usage.TextureCoordinates, 2, TEXCOORD_ATTRIBUTE),
                new VertexAttribute(VertexAttributes.Usage.ColorPacked, 4, TWEAK_ATTRIBUTE));
    }

    @Override
    public void begin() {
        if (drawing) throw new IllegalStateException(ERROR_END_BEGIN);
        renderCalls = 0;
        Gdx.gl.glDepthMask(false);

        shader.bind();
        setupMatrices();

        Gdx.gl.glEnable(GL32.GL_BLEND);
        Gdx.gl.glBlendFuncSeparate(this.blend[RGB_SRC], this.blend[RGB_DST], this.blend[ALPHA_SRC], this.blend[ALPHA_DST]);

        drawing = true;
    }

    @Override
    public void end() {
        if (!drawing) throw new IllegalStateException(ERROR_BEGIN_END);
        if (idx > 0) flush();
        lastTexture = null;
        Gdx.gl.glDepthMask(true);
        if (flushWarning && this.intermediateFlushes > 0) {
            printFlushWarning();
        }
        this.intermediateFlushes = 0;
        this.nextSamplerTextureUnit = 1;
        drawing = false;
    }

    private void printFlushWarning() {
        System.err.println(String.format(FLUSH_WARNING, (this.intermediateFlushes + 1), this.vertices.length, Thread.currentThread().getStackTrace()[2].toString()));
    }

    @Override
    public void draw(final Texture texture, final float x, final float y, final float originX, final float originY, final float width, final float height, final float scaleX,
                     final float scaleY, final float rotation, final int srcX, final int srcY, final int srcWidth, final int srcHeight, final boolean flipX, final boolean flipY) {
        if (!drawing) throw new IllegalStateException("SpriteRenderer.begin must be called before draw.");

        float[] vertices = this.vertices;

        if (texture != lastTexture)
            switchTexture(texture);
        else if (idx == vertices.length) {
            this.intermediateFlushes++;
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

        vertices[idx] = x1;
        vertices[idx + 1] = y1;
        vertices[idx + 2] = color;
        vertices[idx + 3] = u;
        vertices[idx + 4] = v;
        vertices[idx + 5] = tweak;

        vertices[idx + 6] = x2;
        vertices[idx + 7] = y2;
        vertices[idx + 8] = color;
        vertices[idx + 9] = u;
        vertices[idx + 10] = v2;
        vertices[idx + 11] = tweak;

        vertices[idx + 12] = x3;
        vertices[idx + 13] = y3;
        vertices[idx + 14] = color;
        vertices[idx + 15] = u2;
        vertices[idx + 16] = v2;
        vertices[idx + 17] = tweak;

        vertices[idx + 18] = x4;
        vertices[idx + 19] = y4;
        vertices[idx + 20] = color;
        vertices[idx + 21] = u2;
        vertices[idx + 22] = v;
        vertices[idx + 23] = tweak;

        idx += SPRITE_SIZE;

    }

    @Override
    public void draw(final Texture texture, final float x, final float y, final float width, final float height, final int srcX, final int srcY, final int srcWidth,
                     final int srcHeight, final boolean flipX, final boolean flipY) {
        if (!drawing) throw new IllegalStateException("SpriteRenderer.begin must be called before draw.");

        float[] vertices = this.vertices;

        if (texture != lastTexture)
            switchTexture(texture);
        else if (idx == vertices.length) {
            this.intermediateFlushes++;
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

        vertices[idx] = x;
        vertices[idx + 1] = y;
        vertices[idx + 2] = color;
        vertices[idx + 3] = u;
        vertices[idx + 4] = v;
        vertices[idx + 5] = tweak;

        vertices[idx + 6] = x;
        vertices[idx + 7] = fy2;
        vertices[idx + 8] = color;
        vertices[idx + 9] = u;
        vertices[idx + 10] = v2;
        vertices[idx + 11] = tweak;

        vertices[idx + 12] = fx2;
        vertices[idx + 13] = fy2;
        vertices[idx + 14] = color;
        vertices[idx + 15] = u2;
        vertices[idx + 16] = v2;
        vertices[idx + 17] = tweak;

        vertices[idx + 18] = fx2;
        vertices[idx + 19] = y;
        vertices[idx + 20] = color;
        vertices[idx + 21] = u2;
        vertices[idx + 22] = v;
        vertices[idx + 23] = tweak;

        idx += SPRITE_SIZE;

    }

    @Override
    public void draw(final Texture texture, final float x, final float y, final int srcX, final int srcY, final int srcWidth, final int srcHeight) {
        if (!drawing) throw new IllegalStateException("SpritRenderer.begin must be called before draw.");

        final float[] vertices = this.vertices;

        if (texture != lastTexture)
            switchTexture(texture);
        else if (idx == vertices.length) {
            this.intermediateFlushes++;
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

        vertices[idx] = x;
        vertices[idx + 1] = y;
        vertices[idx + 2] = color;
        vertices[idx + 3] = u;
        vertices[idx + 4] = v;
        vertices[idx + 5] = tweak;

        vertices[idx + 6] = x;
        vertices[idx + 7] = fy2;
        vertices[idx + 8] = color;
        vertices[idx + 9] = u;
        vertices[idx + 10] = v2;
        vertices[idx + 11] = tweak;

        vertices[idx + 12] = fx2;
        vertices[idx + 13] = fy2;
        vertices[idx + 14] = color;
        vertices[idx + 15] = u2;
        vertices[idx + 16] = v2;
        vertices[idx + 17] = tweak;

        vertices[idx + 18] = fx2;
        vertices[idx + 19] = y;
        vertices[idx + 20] = color;
        vertices[idx + 21] = u2;
        vertices[idx + 22] = v;
        vertices[idx + 23] = tweak;

        idx += SPRITE_SIZE;

    }

    @Override
    public void draw(final Texture texture, final float x, final float y, final float width, final float height, final float u, final float v, final float u2, final float v2) {
        if (!drawing) throw new IllegalStateException("SpriteRenderer.begin must be called before draw.");

        final float[] vertices = this.vertices;

        if (texture != lastTexture)
            switchTexture(texture);
        else if (idx == vertices.length) {
            this.intermediateFlushes++;
            flush();
        }

        final float fx2 = x + width;
        final float fy2 = y + height;

        final float color = this.color;
        final float tweak = this.tweak;

        vertices[idx] = x;
        vertices[idx + 1] = y;
        vertices[idx + 2] = color;
        vertices[idx + 3] = u;
        vertices[idx + 4] = v;
        vertices[idx + 5] = tweak;

        vertices[idx + 6] = x;
        vertices[idx + 7] = fy2;
        vertices[idx + 8] = color;
        vertices[idx + 9] = u;
        vertices[idx + 10] = v2;
        vertices[idx + 11] = tweak;

        vertices[idx + 12] = fx2;
        vertices[idx + 13] = fy2;
        vertices[idx + 14] = color;
        vertices[idx + 15] = u2;
        vertices[idx + 16] = v2;
        vertices[idx + 17] = tweak;

        vertices[idx + 18] = fx2;
        vertices[idx + 19] = y;
        vertices[idx + 20] = color;
        vertices[idx + 21] = u2;
        vertices[idx + 22] = v;
        vertices[idx + 23] = tweak;

        idx += SPRITE_SIZE;

    }

    @Override
    public void draw(final Texture texture, final float x, final float y) {
        draw(texture, x, y, texture.getWidth(), texture.getHeight());
    }

    @Override
    public void draw(final Texture texture, final float x, final float y, final float width, final float height) {
        if (!drawing) throw new IllegalStateException("SpriteRenderer.begin must be called before draw.");

        float[] vertices = this.vertices;

        if (texture != lastTexture)
            switchTexture(texture);
        else if (idx == vertices.length) {
            this.intermediateFlushes++;
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

        vertices[idx] = x;
        vertices[idx + 1] = y;
        vertices[idx + 2] = color;
        vertices[idx + 3] = u;
        vertices[idx + 4] = v;
        vertices[idx + 5] = tweak;

        vertices[idx + 6] = x;
        vertices[idx + 7] = fy2;
        vertices[idx + 8] = color;
        vertices[idx + 9] = u;
        vertices[idx + 10] = v2;
        vertices[idx + 11] = tweak;

        vertices[idx + 12] = fx2;
        vertices[idx + 13] = fy2;
        vertices[idx + 14] = color;
        vertices[idx + 15] = u2;
        vertices[idx + 16] = v2;
        vertices[idx + 17] = tweak;

        vertices[idx + 18] = fx2;
        vertices[idx + 19] = y;
        vertices[idx + 20] = color;
        vertices[idx + 21] = u2;
        vertices[idx + 22] = v;
        vertices[idx + 23] = tweak;

        idx += SPRITE_SIZE;

    }

    @Override
    public void draw(final Texture texture, final float[] spriteVertices, int offset, int count) {
        if (!drawing) throw new IllegalStateException("SpriteRenderer.begin must be called before draw.");

        count = (count / 5) * 6;
        final int verticesLength = vertices.length;

        int remainingVertices = verticesLength;
        if (texture != lastTexture)
            switchTexture(texture);
        else {
            remainingVertices -= idx;
            if (remainingVertices == 0) {
                flush();
                remainingVertices = verticesLength;
            }
        }
        int copyCount = Math.min(remainingVertices, count);
        final float tweak = this.tweak;

        for (int s = offset, v = idx, i = 0; i < copyCount; i += 6) {
            vertices[v++] = spriteVertices[s++];
            vertices[v++] = spriteVertices[s++];
            vertices[v++] = spriteVertices[s++];
            vertices[v++] = spriteVertices[s++];
            vertices[v++] = spriteVertices[s++];
            vertices[v++] = this.tweak;
        }
        idx += copyCount;
        count -= copyCount;
        while (count > 0) {
            offset += (copyCount / 6) * 5;
            flush();
            copyCount = Math.min(verticesLength, count);
            for (int s = offset, v = 0, i = 0; i < copyCount; i += 6) {
                vertices[v++] = spriteVertices[s++];
                vertices[v++] = spriteVertices[s++];
                vertices[v++] = spriteVertices[s++];
                vertices[v++] = spriteVertices[s++];
                vertices[v++] = spriteVertices[s++];
                vertices[v++] = tweak;
            }
            idx += copyCount;
            count -= copyCount;
        }
    }


    public void drawExactly(final Texture texture, final float[] spriteVertices, int offset, int count) {
        if (!drawing) throw new IllegalStateException("SpriteRenderer.begin must be called before draw.");

        int verticesLength = vertices.length;
        int remainingVertices = verticesLength;
        if (texture != lastTexture)
            switchTexture(texture);
        else {
            remainingVertices -= idx;
            if (remainingVertices == 0) {
                flush();
                remainingVertices = verticesLength;
            }
        }
        int copyCount = Math.min(remainingVertices, count);

        System.arraycopy(spriteVertices, offset, vertices, idx, copyCount);
        idx += copyCount;
        count -= copyCount;
        while (count > 0) {
            offset += copyCount;
            flush();
            copyCount = Math.min(verticesLength, count);
            System.arraycopy(spriteVertices, offset, vertices, 0, copyCount);
            idx += copyCount;
            count -= copyCount;
        }
    }

    @Override
    public void draw(final TextureRegion region, final float x, final float y) {
        draw(region, x, y, region.getRegionWidth(), region.getRegionHeight());
    }

    @Override
    public void draw(final TextureRegion region, final float x, final float y, final float width, final float height) {
        if (!drawing) throw new IllegalStateException("SpriteRenderer.begin must be called before draw.");

        final float[] vertices = this.vertices;
        final Texture texture = region.getTexture();
        if (texture != lastTexture) {
            switchTexture(texture);
        } else if (idx == vertices.length) {
            this.intermediateFlushes++;
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

        vertices[idx] = x;
        vertices[idx + 1] = y;
        vertices[idx + 2] = color;
        vertices[idx + 3] = u;
        vertices[idx + 4] = v;
        vertices[idx + 5] = tweak;

        vertices[idx + 6] = x;
        vertices[idx + 7] = fy2;
        vertices[idx + 8] = color;
        vertices[idx + 9] = u;
        vertices[idx + 10] = v2;
        vertices[idx + 11] = tweak;

        vertices[idx + 12] = fx2;
        vertices[idx + 13] = fy2;
        vertices[idx + 14] = color;
        vertices[idx + 15] = u2;
        vertices[idx + 16] = v2;
        vertices[idx + 17] = tweak;

        vertices[idx + 18] = fx2;
        vertices[idx + 19] = y;
        vertices[idx + 20] = color;
        vertices[idx + 21] = u2;
        vertices[idx + 22] = v;
        vertices[idx + 23] = tweak;

        idx += SPRITE_SIZE;

    }

    @Override
    public void draw(final TextureRegion region, final float x, final float y, final float originX, final float originY, final float width, final float height,
                     float scaleX, final float scaleY, final float rotation) {
        if (!drawing) throw new IllegalStateException("SpriteRenderer.begin must be called before draw.");

        float[] vertices = this.vertices;

        final Texture texture = region.getTexture();
        if (texture != lastTexture) {
            switchTexture(texture);
        } else if (idx == vertices.length) {
            this.intermediateFlushes++;
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

        vertices[idx] = x1;
        vertices[idx + 1] = y1;
        vertices[idx + 2] = color;
        vertices[idx + 3] = u;
        vertices[idx + 4] = v;
        vertices[idx + 5] = tweak;

        vertices[idx + 6] = x2;
        vertices[idx + 7] = y2;
        vertices[idx + 8] = color;
        vertices[idx + 9] = u;
        vertices[idx + 10] = v2;
        vertices[idx + 11] = tweak;

        vertices[idx + 12] = x3;
        vertices[idx + 13] = y3;
        vertices[idx + 14] = color;
        vertices[idx + 15] = u2;
        vertices[idx + 16] = v2;
        vertices[idx + 17] = tweak;

        vertices[idx + 18] = x4;
        vertices[idx + 19] = y4;
        vertices[idx + 20] = color;
        vertices[idx + 21] = u2;
        vertices[idx + 22] = v;
        vertices[idx + 23] = tweak;

        idx += SPRITE_SIZE;

    }

    @Override
    public void draw(final TextureRegion region, final float x, final float y, final float originX, final float originY, final float width, final float height,
                     float scaleX, final float scaleY, final float rotation, boolean clockwise) {
        if (!drawing) throw new IllegalStateException("SpriteRenderer.begin must be called before draw.");

        float[] vertices = this.vertices;

        final Texture texture = region.getTexture();
        if (texture != lastTexture) {
            switchTexture(texture);
        } else if (idx == vertices.length) {
            this.intermediateFlushes++;
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


        vertices[idx] = x1;
        vertices[idx + 1] = y1;
        vertices[idx + 2] = color;
        vertices[idx + 3] = u1;
        vertices[idx + 4] = v1;
        vertices[idx + 5] = tweak;

        vertices[idx + 6] = x2;
        vertices[idx + 7] = y2;
        vertices[idx + 8] = color;
        vertices[idx + 9] = u2;
        vertices[idx + 10] = v2;
        vertices[idx + 11] = tweak;

        vertices[idx + 12] = x3;
        vertices[idx + 13] = y3;
        vertices[idx + 14] = color;
        vertices[idx + 15] = u3;
        vertices[idx + 16] = v3;
        vertices[idx + 17] = tweak;

        vertices[idx + 18] = x4;
        vertices[idx + 19] = y4;
        vertices[idx + 20] = color;
        vertices[idx + 21] = u4;
        vertices[idx + 22] = v4;
        vertices[idx + 23] = tweak;

        idx += SPRITE_SIZE;

    }

    @Override
    public void draw(final TextureRegion region, final float width, final float height, Affine2 transform) {
        if (!drawing) throw new IllegalStateException("SpriteRenderer.begin must be called before draw.");

        float[] vertices = this.vertices;

        final Texture texture = region.getTexture();
        if (texture != lastTexture) {
            switchTexture(texture);
        } else if (idx == vertices.length) {
            this.intermediateFlushes++;
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
        float u2 = region.getV2();
        float v2 = region.getV();

        final float color = this.color;
        final float tweak = this.tweak;

        vertices[idx] = x1;
        vertices[idx + 1] = y1;
        vertices[idx + 2] = color;
        vertices[idx + 3] = u;
        vertices[idx + 4] = v;
        vertices[idx + 5] = tweak;

        vertices[idx + 6] = x2;
        vertices[idx + 7] = y2;
        vertices[idx + 8] = color;
        vertices[idx + 9] = u;
        vertices[idx + 10] = v2;
        vertices[idx + 11] = tweak;

        vertices[idx + 12] = x3;
        vertices[idx + 13] = y3;
        vertices[idx + 14] = color;
        vertices[idx + 15] = u2;
        vertices[idx + 16] = v2;
        vertices[idx + 17] = tweak;

        vertices[idx + 18] = x4;
        vertices[idx + 19] = y4;
        vertices[idx + 20] = color;
        vertices[idx + 21] = u2;
        vertices[idx + 22] = v;
        vertices[idx + 23] = tweak;

        idx += SPRITE_SIZE;

    }

    @SuppressWarnings("RedundantCast") // These casts are absolutely not redundant! Java 9 changed Buffer ABI.
    @Override
    public void flush() {
        if (idx == 0) return;

        renderCalls++;
        totalRenderCalls++;

        int spritesInBatch = idx / SPRITE_SIZE;
        if (spritesInBatch > maxSpritesInBatch) maxSpritesInBatch = spritesInBatch;
        int count = spritesInBatch * 6;

        lastTexture.bind();

        vertexData.setVertices(this.vertices, 0, this.idx);
        vertexData.bind(shader);

        ShortBuffer indexBuffer = indexData.getBuffer(true);
        indexBuffer.position(0);
        indexBuffer.limit(count);
        indexData.bind();

        Gdx.gl32.glDrawElements(GL32.GL_TRIANGLES, count, GL32.GL_UNSIGNED_SHORT, 0);

        idx = 0;
    }

    @Override
    public void disableBlending() {
        throw new RuntimeException("not supported");
    }

    @Override
    public void enableBlending() {
        throw new RuntimeException("not supported");
    }

    @Override
    public void setBlendFunction(final int srcFunc, final int dstFunc) {
        setBlendFunctionSeparate(srcFunc, dstFunc, srcFunc, dstFunc);
    }

    @Override
    public void setBlendFunctionSeparate(final int srcFuncColor, final int dstFuncColor, final int srcFuncAlpha, final int dstFuncAlpha) {
        if (this.blend[RGB_SRC] == srcFuncColor && this.blend[RGB_DST] == dstFuncColor && this.blend[ALPHA_SRC] == srcFuncAlpha && this.blend[ALPHA_DST] == dstFuncAlpha)
            return;

        this.blend[RGB_SRC] = srcFuncColor;
        this.blend[RGB_DST] = dstFuncColor;
        this.blend[ALPHA_SRC] = srcFuncAlpha;
        this.blend[ALPHA_DST] = dstFuncAlpha;
        if (drawing) {
            flush();
            Gdx.gl.glBlendFuncSeparate(blend[RGB_SRC], blend[RGB_DST], blend[ALPHA_SRC], blend[ALPHA_DST]);
        }
    }

    @Override
    public int getBlendSrcFunc() {
        return blend[RGB_SRC];
    }

    @Override
    public int getBlendDstFunc() {
        return blend[RGB_DST];
    }

    @Override
    public int getBlendSrcFuncAlpha() {
        return blend[ALPHA_SRC];
    }

    @Override
    public int getBlendDstFuncAlpha() {
        return blend[ALPHA_DST];
    }

    @Override
    public void dispose() {
        vertexData.dispose();
        indexData.dispose();
        if (this.shader == defaultShader())
            this.shader.dispose();
    }

    @Override
    public Matrix4 getProjectionMatrix() {
        return projectionMatrix;
    }

    @Override
    public Matrix4 getTransformMatrix() {
        return transformMatrix;
    }

    @Override
    public void setProjectionMatrix(Matrix4 projection) {
        if (Arrays.equals(projectionMatrix.val, projection.val)) return;
        if (drawing) flush();
        this.projectionMatrix.set(projection);
        if (drawing) setupMatrices();
    }

    @Override
    public void setTransformMatrix(Matrix4 transform) {
        if (drawing) flush();
        transformMatrix.set(transform);
        if (drawing) setupMatrices();
    }

    protected void setupMatrices() {
        combinedMatrix.set(projectionMatrix).mul(transformMatrix);
        shader.setUniformMatrix(uniformLocation(PROJTRANS_UNIFORM), combinedMatrix);
        shader.setUniformi(uniformLocation(TEXTURE_UNIFORM), 0);
    }

    protected void switchTexture(final Texture texture) {
        flush();
        lastTexture = texture;
        invTexWidth = 1.0f / texture.getWidth();
        invTexHeight = 1.0f / texture.getHeight();
        shader.setUniformf(uniformLocation(TEXTURE_SIZE_UNIFORM), texture.getWidth(), texture.getHeight());
    }


    @Override
    public ShaderProgram getShader() {
        return this.shader;
    }

    @Override
    public boolean isBlendingEnabled() {
        throw new RuntimeException("not supported");
    }

    public boolean isDrawing() {
        return drawing;
    }

    @Override
    public void setShader(ShaderProgram shader) {
        ShaderProgram nextShader = shader != null ? shader : defaultShader();
        if (this.shader == nextShader)
            return;

        this.shader = nextShader;

        this.nextSamplerTextureUnit = 1;
        if (drawing) {
            flush();
            this.shader.bind();
            setupMatrices();
        }
    }

    public void bindCMediaImageToUniform(CMediaImage cMediaImage, String uniform) {
        bindCMediaImageToUniform(cMediaImage, uniform, null);
    }

    public void bindCMediaImageToUniform(CMediaImage cMediaImage, String uniform, String sizeUniform) {
        if(cMediaImage.useAtlas)
            throw new RuntimeException("Binded textues should not be TextureAtlas");
        bindTextureToUniform(mediaManager.image(cMediaImage).getTexture(), uniform, sizeUniform);
    }

    public void bindTextureToUniform(Texture texture, String uniform) {
        bindTextureToUniform(texture, uniform, null);
    }

    public void bindTextureToUniform(Texture texture, String uniform, String sizeUniform) {
        Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0 + this.nextSamplerTextureUnit);
        texture.bind();
        this.shader.setUniformi(uniformLocation(uniform), this.nextSamplerTextureUnit);
        if (sizeUniform != null) {
            this.shader.setUniformf(uniformLocation(sizeUniform), texture.getWidth(), texture.getHeight());
        }
        Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0);
        this.nextSamplerTextureUnit++;
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

    public void drawCMediaFont(final CMediaFont cMediaFont, final float x, final float y, String text) {
        this.drawCMediaFont(cMediaFont, x, y, text, false, false, 0);
    }

    public void drawCMediaFont(final CMediaFont cMediaFont, final float x, final float y, String text, final boolean centerX, final boolean centerY) {
        this.drawCMediaFont(cMediaFont, x, y, text, centerX, centerY, 0);
    }

    public void drawCMediaFont(final CMediaFont cMediaFont, final float x, final float y, String text, final boolean centerX, final boolean centerY, final int maxWidth) {
        if (cMediaFont == null) return;
        final float x_draw = centerX ? (x - MathUtils.round(mediaManager.fontTextWidth(cMediaFont, text) / 2f)) : x;
        final float y_draw = centerY ? (y - MathUtils.round(mediaManager.fontTextHeight(cMediaFont, text) / 2f)) : y;
        final BitmapFontCache fontCache = mediaManager.font(cMediaFont).getCache();

        fontCache.clear();
        fontCache.addText(text, x_draw, y_draw, 0, text.length(), maxWidth, Align.left, false, maxWidth > 0 ? "" : null);

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
            fontVertices[idx] = colorPackedRGBA(tempColor.r, tempColor.g, tempColor.b, tempColor.a);
        }

        fontCache.draw(this);
    }


    // ####### Getter / Setters #######

    // ----- Tint Color -----

    @Override
    public void setColor(Color color) {
        this.color = colorPackedRGBA(color.r, color.g, color.b, color.a);
    }

    public void setColor(Color color, final float alpha) {
        this.color = colorPackedRGBA(color.r, color.g, color.b, alpha);
    }

    @Override
    public void setColor(final float r, final float g, final float b, final float alpha) {
        this.color = colorPackedRGBA(r, g, b, alpha);
    }

    @Override
    public void setPackedColor(final float color) {
        this.color = color;
    }

    @Override
    public Color getColor() {
        Color.abgr8888ToColor(tempColor, color);
        return tempColor;
    }

    @Override
    public float getPackedColor() {
        return this.color;
    }

    public float getR() {
        int c = NumberUtils.floatToIntColor(this.color);
        return ((c & 0x000000ff)) / 255f;
    }

    public float getG() {
        int c = NumberUtils.floatToIntColor(this.color);
        return ((c & 0x0000ff00) >>> 8) / 255f;
    }

    public float getB() {
        int c = NumberUtils.floatToIntColor(this.color);
        return ((c & 0x00ff0000) >>> 16) / 255f;
    }

    public float getAlpha() {
        int c = NumberUtils.floatToIntColor(this.color);
        return ((c & 0xff000000) >>> 24) / 255f;
    }

    // ----- Tweak -----

    public void setTweak(final float t1, final float t2, final float t3, final float t4) {
        tweak = colorPackedRGBA(t1, t2, t3, t4);
    }

    public void setPackedTweak(final float tweak) {
        this.tweak = tweak;
    }

    public float getTweak1() {
        int c = NumberUtils.floatToIntColor(this.tweak);
        return ((c & 0x000000ff)) / 255f;
    }

    public float getTweak2() {
        int c = NumberUtils.floatToIntColor(this.tweak);
        return ((c & 0x0000ff00) >>> 8) / 255f;
    }

    public float getTweak3() {
        int c = NumberUtils.floatToIntColor(this.tweak);
        return ((c & 0x00ff0000) >>> 16) / 255f;
    }

    public float getTweak4() {
        int c = NumberUtils.floatToIntColor(this.tweak);
        return ((c & 0xff000000) >>> 24) / 255f;
    }

    public float getPackedTweak() {
        return this.tweak;
    }

    public Color getTweak() {
        Color.abgr8888ToColor(tempColor, tweak);
        return tempColor;
    }

    // ---- RESET & STATE ----

    public void setTweakReset() {
        setPackedTweak(reset_tweak);
    }

    public void setColorReset() {
        setPackedColor(reset_color);
    }

    public void setBlendFunctionReset() {
        setBlendFunctionSeparate(reset_blend[RGB_SRC], reset_blend[RGB_DST], reset_blend[ALPHA_SRC], reset_blend[ALPHA_DST]);
    }

    public void setTweakAndColorReset() {
        setTweakReset();
        setColorReset();
    }

    public void setAllReset() {
        setTweakReset();
        setColorReset();
        setBlendFunctionReset();
    }

    public void saveState() {
        this.backup_color = this.color;
        this.backup_tweak = this.tweak;
        System.arraycopy(this.blend, 0, this.backup_blend, 0, 4);
    }

    public void loadState() {
        setPackedColor(this.backup_color);
        setPackedTweak(this.backup_tweak);
        setBlendFunctionSeparate(this.backup_blend[RGB_SRC], this.backup_blend[RGB_DST], this.backup_blend[ALPHA_SRC], this.backup_blend[ALPHA_DST]);
    }


    public void setColorResetValues(final float r, final float g, final float b, final float a) {
        this.reset_color = colorPackedRGBA(r, g, b, a);
        this.setColorReset();
    }

    public void setTweakResetValues(final float h, final float s, final float l, final float c) {
        this.reset_tweak = colorPackedRGBA(h, s, l, c);
        this.setTweakReset();
    }

    public void setBlendFunctionResetValuesSeparate(final int blend_rgb_src, final int blend_rgb_dst, final int blend_alpha_src, final int blend_alpha_blend) {
        this.reset_blend[RGB_SRC] = blend_rgb_src;
        this.reset_blend[RGB_DST] = blend_rgb_dst;
        this.reset_blend[ALPHA_SRC] = blend_alpha_src;
        this.reset_blend[ALPHA_DST] = blend_alpha_blend;
        this.setBlendFunctionReset();
    }

    public void setBlendFunctionResetValues(final int blend_src, final int blend_dst) {
        this.reset_blend[RGB_SRC] = blend_src;
        this.reset_blend[RGB_DST] = blend_dst;
        this.reset_blend[ALPHA_SRC] = blend_src;
        this.reset_blend[ALPHA_DST] = blend_dst;
        this.setBlendFunctionReset();
    }

    public void setBlendFunctionLayer() {
        this.setBlendFunctionSeparate(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA, GL20.GL_ONE, GL20.GL_ONE_MINUS_SRC_ALPHA);

    }

    public void setBlendFunctionComposite() {
        this.setBlendFunction(GL20.GL_ONE, GL20.GL_ONE_MINUS_SRC_ALPHA);
    }

    private static float colorPackedRGBA(final float red, final float green, final float blue, final float alpha) {
        return NumberUtils.intBitsToFloat(((int) (alpha * 255) << 24 & 0xFE000000) | ((int) (blue * 255) << 16 & 0xFF0000)
                | ((int) (green * 255) << 8 & 0xFF00) | ((int) (red * 255) & 0xFF));
    }

    public int getRenderCalls() {
        return this.renderCalls;
    }

    public int getTotalRenderCalls() {
        return this.totalRenderCalls;
    }
}