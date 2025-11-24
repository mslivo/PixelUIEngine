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
import com.badlogic.gdx.utils.*;
import net.mslivo.pixelui.media.*;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Arrays;

public class SpriteRenderer implements Disposable {

    public static final String PROJTRANS_UNIFORM = "u_projTrans";
    public static final String TEXCOORD_ATTRIBUTE = "a_texCoord";
    public static final String TEXTURE_UNIFORM = "u_texture";
    public static final String TEXTURE_SIZE_UNIFORM = "u_textureSize";
    public static final String COLOR_ATTRIBUTE = "a_color";
    public static final String TWEAK_ATTRIBUTE = "a_tweak";
    public static final String POSITION_ATTRIBUTE = "a_position";

    private static final String ERROR_END_BEGIN = ".end() must be called before begin.";
    private static final String ERROR_BEGIN_END = ".begin() must be called before end.";

    private static final int RGB_SRC = 0, RGB_DST = 1, ALPHA_SRC = 2, ALPHA_DST = 3;

    private static final int VERTEX_SIZE = 6;
    private static final int INDICES_SIZE = 6;
    private static final int VERTEXES_INDICES_RATIO = 4;
    public static final int MAX_VERTEXES_DEFAULT = 65532 * 4; // 65532 sprites

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

    private Texture lastTexture;
    private float invTexWidth, invTexHeight;
    private MediaManager mediaManager;
    private int nextSamplerTextureUnit;




    private int primitiveType;

    private ShaderProgram provideDefaultShader() {
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
        this(null, null, MAX_VERTEXES_DEFAULT, false);
    }

    public SpriteRenderer(final MediaManager mediaManager) {
        this(mediaManager, null, MAX_VERTEXES_DEFAULT, false);
    }


    public SpriteRenderer(final MediaManager mediaManager, final ShaderProgram defaultShader) {
        this(mediaManager, defaultShader, MAX_VERTEXES_DEFAULT, false);
    }

    public SpriteRenderer(final MediaManager mediaManager, final ShaderProgram defaultShader, final int maxVertexes) {
        this(mediaManager, defaultShader, maxVertexes, false);
    }

    public SpriteRenderer(final MediaManager mediaManager, final ShaderProgram defaultShader, final int maxVertexes, final boolean printRenderCalls) {
        int vertexAbsoluteLimit = Integer.MAX_VALUE / (VERTEX_SIZE * 4);
        if (maxVertexes > vertexAbsoluteLimit)
            throw new IllegalArgumentException("size " + maxVertexes + " bigger than mix allowed size " + vertexAbsoluteLimit);
        if (maxVertexes % VERTEXES_INDICES_RATIO != 0)
            throw new IllegalArgumentException("size is not multiple of ratio " + VERTEXES_INDICES_RATIO);

        this.tweak_reset = colorPackedRGBA(0f, 0f, 0f, 0.0f);
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
        this.invTexWidth = this.invTexHeight = 0;
        this.nextSamplerTextureUnit = 1;
        this.mediaManager = mediaManager;
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


    private void setupMatrices() {
        combinedMatrix.set(projectionMatrix).mul(transformMatrix);
        shader.setUniformMatrix(uniformLocation(PROJTRANS_UNIFORM), combinedMatrix);
    }

    public void setShader(ShaderProgram shader) {
        ShaderProgram nextShader = shader != null ? shader : this.defaultShader;
        if (this.shader == nextShader)
            return;

        this.shader = nextShader;

        if (drawing) {
            flush();
            this.shader.bind();
            setupMatrices();
        }

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


    private float colorPackedRGBA(final float red, final float green, final float blue, final float alpha) {
        return NumberUtils.intBitsToFloat(((int) (alpha * 255) << 24 & 0xFE000000) | ((int) (blue * 255) << 16 & 0xFF0000)
                | ((int) (green * 255) << 8 & 0xFF00) | ((int) (red * 255) & 0xFF));
    }

    public void saveState() {
        this.color_save = this.color;
        this.tweak_save = this.tweak;
        System.arraycopy(this.blend, 0, this.blend_save, 0, 4);
    }


    public boolean isDrawing(){
        return drawing;
    }

    public void loadState() {
        setPackedColor(this.color_save);
        setPackedTweak(this.tweak_save);
        setBlendFunctionSeparate(this.blend_save[RGB_SRC], this.blend_save[RGB_DST], this.blend_save[ALPHA_SRC], this.blend_save[ALPHA_DST]);
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

    @Override
    public void dispose() {
        vertexBufferObject.dispose();
        indexBufferObject.dispose();
        if (this.shader == this.defaultShader)
            this.shader.dispose();
    }
}