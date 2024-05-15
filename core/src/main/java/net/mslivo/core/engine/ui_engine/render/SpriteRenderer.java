package net.mslivo.core.engine.ui_engine.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Affine2;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.NumberUtils;
import net.mslivo.core.engine.media_manager.MediaManager;
import net.mslivo.core.engine.media_manager.media.*;

/**
 * A substitute for {@link com.badlogic.gdx.graphics.g2d.SpriteBatch} that adds an extra attribute to store another
 * color's worth of channels, called the "hslt" and used to modify the color with HSL changes, while the primary color
 * tints the color. Additionaly this can be used to draw MediaManager CMediaSprite files.
 * This class is based on
 * https://github.com/tommyettinger/colorful-gdx/blob/master/colorful/src/test/java/com/github/tommyettinger/colorful/rgb/UISpriteBatch.java
 */
public class SpriteRenderer implements Batch {
    private static final String VERTEX_SHADER = """
            attribute vec4 a_position;
            attribute vec4 a_color;
            attribute vec2 a_texCoord0;
            attribute vec4 a_hslt;
            uniform mat4 u_projTrans;
            varying vec4 v_color;
            varying vec4 v_hslt;
            varying vec2 v_texCoords;
                            
            void main()
            {
               v_color = a_color;
               v_color.a = v_color.a * (255.0/254.0);
               
               v_hslt = a_hslt;
               v_hslt.a = v_hslt.a * (255.0/254.0);
               
               v_texCoords = a_texCoord0;
               gl_Position =  u_projTrans * a_position;
            }
                            
            """;
    private static final String FRAGMENT_SHADER = """
            #ifdef GL_ES
            #define LOWP lowp
            precision mediump float;
            #else
            #define LOWP
            #endif
            varying vec2 v_texCoords;
            varying LOWP vec4 v_color;
            varying LOWP vec4 v_hslt;
            uniform sampler2D u_texture;
            const float eps = 1.0e-10;
                                
            vec4 rgb2hsl(vec4 c)
            {
                const vec4 J = vec4(0.0, -1.0 / 3.0, 2.0 / 3.0, -1.0);
                vec4 p = mix(vec4(c.bg, J.wz), vec4(c.gb, J.xy), step(c.b, c.g));
                vec4 q = mix(vec4(p.xyw, c.r), vec4(c.r, p.yzx), step(p.x, c.r));
                float d = q.x - min(q.w, q.y);
                float l = q.x * (1.0 - 0.5 * d / (q.x + eps));
                return vec4(abs(q.z + (q.w - q.y) / (6.0 * d + eps)), (q.x - l) / (min(l, 1.0 - l) + eps), l, c.a);
            }
                        
            vec4 hsl2rgb(vec4 c)
            {
                const vec4 K = vec4(1.0, 2.0 / 3.0, 1.0 / 3.0, 3.0);
                vec3 p = abs(fract(c.x + K.xyz) * 6.0 - K.www);
                float v = (c.z + c.y * min(c.z, 1.0 - c.z));
                return vec4(v * mix(K.xxx, clamp(p - K.xxx, 0.0, 1.0), 2.0 * (1.0 - c.z / (v + eps))), c.w);
            }
                            
            void main()
            {
              vec4 tgt = rgb2hsl(texture2D( u_texture, v_texCoords )); // convert to HSL
              
              tgt.x = fract(tgt.x+v_hslt.x); // hslt Hue
              tgt.y *= (v_hslt.y*2.0); // hslt Saturation
              tgt.z += (v_hslt.z-0.5) * 2.0; // hslt Lightness
              
              vec4 color = hsl2rgb(tgt); // convert back to RGB 
              color = mix(color, (color*v_color), v_hslt.w); // mixed with tinted color based on hslt Tint
              color.rgb = mix(vec3(dot(color.rgb, vec3(0.3333))), color.rgb,  (v_hslt.y*2.0));  // remove colors based on hslt.saturation
              
              gl_FragColor = color;
            }       
            """;
    private static final String ERROR_END_BEGIN = "SpriteRenderer.end must be called before begin.";
    private static final String ERROR_BEGIN_END = "SpriteRenderer.begin must be called before end.";
    public static final int SPRITE_SIZE = 24;
    public static final String HSLT_ATTRIBUTE = "a_hslt";

    private static final float HSLT_RESET = Color.toFloatBits(0f, 0.5f, 0.5f, 1f);
    private static final float COLOR_RESET = Color.toFloatBits(1f, 1f, 1f, 1f);

    private final Color tempColor;
    private final Mesh mesh;
    private final float[] vertices;
    private float hslt;
    private int idx;
    private Texture lastTexture;
    private float invTexWidth, invTexHeight;
    private boolean drawing;
    private final Matrix4 transformMatrix;
    private final Matrix4 projectionMatrix;
    private final Matrix4 combinedMatrix;
    private int srcRGB;
    private int dstRGB;
    private int srcAlpha;
    private int dstAlpha;
    private ShaderProgram shader;
    private boolean defaultShader;
    private float backup_hslt;
    private float backup_color;
    private int backup_srcRGB;
    private int backup_dstRGB;
    private int backup_srcAlpha;
    private int backup_dstAlpha;

    protected float color;
    private MediaManager mediaManager;
    private int u_projTrans;
    private int u_texture;
    public int renderCalls;
    public int totalRenderCalls;
    public int maxSpritesInBatch;



    public SpriteRenderer() {
        this(null, 1024, null);
    }

    public SpriteRenderer(MediaManager mediaManager) {
        this(mediaManager, 1024, null);
    }

    public SpriteRenderer(MediaManager mediaManager, int size) {
        this(mediaManager, size, null);
    }

    public SpriteRenderer(MediaManager mediaManager, int size, ShaderProgram shader) {
        if (size > 16383) throw new IllegalArgumentException("Can't have more than 16383 sprites per batch: " + size);
        if (shader == null) {
            this.shader = new ShaderProgram(VERTEX_SHADER, FRAGMENT_SHADER);
            if (!this.shader.isCompiled())
                throw new IllegalArgumentException("Error compiling shader: " + this.shader.getLog());
            defaultShader = true;
        } else {
            this.shader = shader;
        }
        this.u_projTrans = this.shader.getUniformLocation("u_projTrans");
        this.u_texture = this.shader.getUniformLocation("u_texture");
        this.drawing = false;
        this.idx = 0;
        this.lastTexture = null;
        this.transformMatrix = new Matrix4();
        this.projectionMatrix = new Matrix4().setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        this.combinedMatrix = new Matrix4();
        this.tempColor = new Color(Color.WHITE);
        this.color = COLOR_RESET;
        this.renderCalls = this.totalRenderCalls = this.maxSpritesInBatch = 0;
        this.invTexWidth = this.invTexHeight = 0;
        this.hslt = HSLT_RESET;
        this.srcRGB = GL20.GL_SRC_ALPHA;
        this.dstRGB = GL20.GL_ONE_MINUS_SRC_ALPHA;
        this.srcAlpha = GL20.GL_SRC_ALPHA;
        this.dstAlpha = GL20.GL_ONE_MINUS_SRC_ALPHA;
        this.vertices = new float[size * SPRITE_SIZE];
        this.backup_color = COLOR_RESET;
        this.backup_hslt = HSLT_RESET;
        this.backup_srcRGB = GL20.GL_SRC_ALPHA;
        this.backup_dstRGB = GL20.GL_ONE_MINUS_SRC_ALPHA;
        this.backup_srcAlpha = GL20.GL_SRC_ALPHA;
        this.backup_dstAlpha = GL20.GL_ONE_MINUS_SRC_ALPHA;
        this.mesh = new Mesh((Gdx.gl30 != null) ? Mesh.VertexDataType.VertexBufferObjectWithVAO : Mesh.VertexDataType.VertexArray,
                false, size * 4, size * 6,
                new VertexAttribute(VertexAttributes.Usage.Position, 2, ShaderProgram.POSITION_ATTRIBUTE),
                new VertexAttribute(VertexAttributes.Usage.ColorPacked, 4, ShaderProgram.COLOR_ATTRIBUTE),
                new VertexAttribute(VertexAttributes.Usage.TextureCoordinates, 2, ShaderProgram.TEXCOORD_ATTRIBUTE + "0"),
                new VertexAttribute(VertexAttributes.Usage.ColorPacked, 4, HSLT_ATTRIBUTE));
        int len = size * 6;
        short[] indices = new short[len];
        short j = 0;
        for (int i = 0; i < len; i += 6, j += 4) {
            indices[i] = j;
            indices[i + 1] = (short) (j + 1);
            indices[i + 2] = (short) (j + 2);
            indices[i + 3] = (short) (j + 2);
            indices[i + 4] = (short) (j + 3);
            indices[i + 5] = j;
        }
        this.mesh.setIndices(indices);
        this.mediaManager = mediaManager;
    }

    public void saveBackup(){
        this.backup_color = this.color;
        this.backup_hslt = this.hslt;
        this.backup_srcRGB = this.srcRGB;
        this.backup_dstRGB = this.dstRGB;
        this.backup_srcAlpha = this.srcAlpha;
        this.backup_dstAlpha = this.dstAlpha;
    }

    public void loadBackup(){
        setPackedColor(this.backup_color);
        setPackedHSLT(this.backup_hslt);
        setBlendFunctionSeparate(backup_srcRGB,backup_dstRGB, backup_srcAlpha, backup_dstAlpha);
    }

    @Override
    public void begin() {
        if (drawing) throw new IllegalStateException(ERROR_END_BEGIN);
        renderCalls = 0;
        Gdx.gl.glDepthMask(false);

        shader.bind();
        setupMatrices();

        drawing = true;
    }

    @Override
    public void end() {
        if (!drawing) throw new IllegalStateException(ERROR_BEGIN_END);
        if (idx > 0) flush();
        lastTexture = null;
        drawing = false;

        Gdx.gl.glDepthMask(true);
    }

    @Override
    public void setColor(Color color) {
        setColor(color.r,color.g,color.b,color.a);
    }

    @Override
    public void setColor(float r, float g, float b, float a) {
        color = rgbPacked(r, g, b, a);
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
        return color;
    }

    public float getHue() {
        int c = NumberUtils.floatToIntColor(hslt);
        float a = ((c & 0xff000000) >>> 24) / 255f;
        float b = ((c & 0x00ff0000) >>> 16) / 255f;
        float g = ((c & 0x0000ff00) >>> 8) / 255f;
        float r = ((c & 0x000000ff)) / 255f;
        return ((c & 0x000000ff)) / 255f;
    }

    public float getSaturation() {
        int c = NumberUtils.floatToIntColor(hslt);
        return ((c & 0x0000ff00) >>> 8) / 255f;
    }

    public float getLightness() {
        int c = NumberUtils.floatToIntColor(hslt);
        return ((c & 0x00ff0000) >>> 16) / 255f;
    }

    public float getTint() {
        int c = NumberUtils.floatToIntColor(hslt);
        return ((c & 0xff000000) >>> 24) / 255f;
    }

    public void setHue(float hue) {
        int c = NumberUtils.floatToIntColor(hslt);
        float a = ((c & 0xff000000) >>> 24) / 255f;
        float b = ((c & 0x00ff0000) >>> 16) / 255f;
        float g = ((c & 0x0000ff00) >>> 8) / 255f;
        hslt = rgbPacked(hue, g, b, a);
    }

    public void setSaturation(float saturation) {
        int c = NumberUtils.floatToIntColor(hslt);
        float a = ((c & 0xff000000) >>> 24) / 255f;
        float b = ((c & 0x00ff0000) >>> 16) / 255f;
        float r = ((c & 0x000000ff)) / 255f;
        hslt = rgbPacked(r, saturation, b, a);
    }

    public void setLightness(float lightness) {
        int c = NumberUtils.floatToIntColor(hslt);
        float a = ((c & 0xff000000) >>> 24) / 255f;
        float g = ((c & 0x0000ff00) >>> 8) / 255f;
        float r = ((c & 0x000000ff)) / 255f;
        hslt = rgbPacked(r, g, lightness, a);
    }

    public void setTint(float tint) {
        int c = NumberUtils.floatToIntColor(hslt);
        float b = ((c & 0x00ff0000) >>> 16) / 255f;
        float g = ((c & 0x0000ff00) >>> 8) / 255f;
        float r = ((c & 0x000000ff)) / 255f;
        hslt = rgbPacked(r, g, b, tint);
    }

    public void setHSLT(float hue, float saturation, float lightness, float tint) {
        hslt = rgbPacked(hue, saturation, lightness, tint);
    }

    public void setPackedHSLT(final float hslt) {
        this.hslt = hslt;
    }

    public void setHSLTReset() {
        setPackedHSLT(HSLT_RESET);
    }

    public void setColorReset() {
        setPackedColor(COLOR_RESET);
    }

    public void setBlendFunctionReset(){
        setBlendFunctionSeparate(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA,GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
    }

    public void setHSLTAndColorReset() {
        setHSLTReset();
        setColorReset();
    }

    public void setAllReset() {
        setHSLTAndColorReset();
        setBlendFunctionReset();
    }

    public float getPackedHSLT() {
        return hslt;
    }

    private float rgbPacked(float red, float green, float blue, float alpha) {
        return NumberUtils.intBitsToFloat(((int) (alpha * 255) << 24 & 0xFE000000) | ((int) (blue * 255) << 16 & 0xFF0000)
                | ((int) (green * 255) << 8 & 0xFF00) | ((int) (red * 255) & 0xFF));
    }

    @Override
    public void draw(Texture texture, float x, float y, float originX, float originY, float width, float height, float scaleX,
                     float scaleY, float rotation, int srcX, int srcY, int srcWidth, int srcHeight, boolean flipX, boolean flipY) {
        if (!drawing) throw new IllegalStateException("SpriteRenderer.begin must be called before draw.");

        float[] vertices = this.vertices;

        if (texture != lastTexture)
            switchTexture(texture);
        else if (idx == vertices.length)
            flush();

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
        final float hslt = this.hslt;
        final int idx = this.idx;
        vertices[idx] = x1;
        vertices[idx + 1] = y1;
        vertices[idx + 2] = color;
        vertices[idx + 3] = u;
        vertices[idx + 4] = v;
        vertices[idx + 5] = hslt;

        vertices[idx + 6] = x2;
        vertices[idx + 7] = y2;
        vertices[idx + 8] = color;
        vertices[idx + 9] = u;
        vertices[idx + 10] = v2;
        vertices[idx + 11] = hslt;

        vertices[idx + 12] = x3;
        vertices[idx + 13] = y3;
        vertices[idx + 14] = color;
        vertices[idx + 15] = u2;
        vertices[idx + 16] = v2;
        vertices[idx + 17] = hslt;

        vertices[idx + 18] = x4;
        vertices[idx + 19] = y4;
        vertices[idx + 20] = color;
        vertices[idx + 21] = u2;
        vertices[idx + 22] = v;
        vertices[idx + 23] = hslt;
        this.idx = idx + 24;
    }

    @Override
    public void draw(Texture texture, float x, float y, float width, float height, int srcX, int srcY, int srcWidth,
                     int srcHeight, boolean flipX, boolean flipY) {
        if (!drawing) throw new IllegalStateException("SpriteRenderer.begin must be called before draw.");

        float[] vertices = this.vertices;

        if (texture != lastTexture)
            switchTexture(texture);
        else if (idx == vertices.length) //
            flush();

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
        final float hslt = this.hslt;
        final int idx = this.idx;
        vertices[idx] = x;
        vertices[idx + 1] = y;
        vertices[idx + 2] = color;
        vertices[idx + 3] = u;
        vertices[idx + 4] = v;
        vertices[idx + 5] = hslt;

        vertices[idx + 6] = x;
        vertices[idx + 7] = fy2;
        vertices[idx + 8] = color;
        vertices[idx + 9] = u;
        vertices[idx + 10] = v2;
        vertices[idx + 11] = hslt;

        vertices[idx + 12] = fx2;
        vertices[idx + 13] = fy2;
        vertices[idx + 14] = color;
        vertices[idx + 15] = u2;
        vertices[idx + 16] = v2;
        vertices[idx + 17] = hslt;

        vertices[idx + 18] = fx2;
        vertices[idx + 19] = y;
        vertices[idx + 20] = color;
        vertices[idx + 21] = u2;
        vertices[idx + 22] = v;
        vertices[idx + 23] = hslt;
        this.idx = idx + 24;
    }

    @Override
    public void draw(Texture texture, float x, float y, int srcX, int srcY, int srcWidth, int srcHeight) {
        if (!drawing) throw new IllegalStateException("SpritRenderer.begin must be called before draw.");

        float[] vertices = this.vertices;

        if (texture != lastTexture)
            switchTexture(texture);
        else if (idx == vertices.length) //
            flush();

        final float u = srcX * invTexWidth;
        final float v = (srcY + srcHeight) * invTexHeight;
        final float u2 = (srcX + srcWidth) * invTexWidth;
        final float v2 = srcY * invTexHeight;
        final float fx2 = x + srcWidth;
        final float fy2 = y + srcHeight;

        final float color = this.color;
        final float hslt = this.hslt;
        final int idx = this.idx;
        vertices[idx] = x;
        vertices[idx + 1] = y;
        vertices[idx + 2] = color;
        vertices[idx + 3] = u;
        vertices[idx + 4] = v;
        vertices[idx + 5] = hslt;

        vertices[idx + 6] = x;
        vertices[idx + 7] = fy2;
        vertices[idx + 8] = color;
        vertices[idx + 9] = u;
        vertices[idx + 10] = v2;
        vertices[idx + 11] = hslt;

        vertices[idx + 12] = fx2;
        vertices[idx + 13] = fy2;
        vertices[idx + 14] = color;
        vertices[idx + 15] = u2;
        vertices[idx + 16] = v2;
        vertices[idx + 17] = hslt;

        vertices[idx + 18] = fx2;
        vertices[idx + 19] = y;
        vertices[idx + 20] = color;
        vertices[idx + 21] = u2;
        vertices[idx + 22] = v;
        vertices[idx + 23] = hslt;
        this.idx = idx + 24;
    }

    @Override
    public void draw(Texture texture, float x, float y, float width, float height, float u, float v, float u2, float v2) {
        if (!drawing) throw new IllegalStateException("SpriteRenderer.begin must be called before draw.");

        float[] vertices = this.vertices;

        if (texture != lastTexture)
            switchTexture(texture);
        else if (idx == vertices.length) //
            flush();

        final float fx2 = x + width;
        final float fy2 = y + height;

        final float color = this.color;
        final float hslt = this.hslt;
        final int idx = this.idx;
        vertices[idx] = x;
        vertices[idx + 1] = y;
        vertices[idx + 2] = color;
        vertices[idx + 3] = u;
        vertices[idx + 4] = v;
        vertices[idx + 5] = hslt;

        vertices[idx + 6] = x;
        vertices[idx + 7] = fy2;
        vertices[idx + 8] = color;
        vertices[idx + 9] = u;
        vertices[idx + 10] = v2;
        vertices[idx + 11] = hslt;

        vertices[idx + 12] = fx2;
        vertices[idx + 13] = fy2;
        vertices[idx + 14] = color;
        vertices[idx + 15] = u2;
        vertices[idx + 16] = v2;
        vertices[idx + 17] = hslt;

        vertices[idx + 18] = fx2;
        vertices[idx + 19] = y;
        vertices[idx + 20] = color;
        vertices[idx + 21] = u2;
        vertices[idx + 22] = v;
        vertices[idx + 23] = hslt;
        this.idx = idx + 24;
    }

    @Override
    public void draw(Texture texture, float x, float y) {
        draw(texture, x, y, texture.getWidth(), texture.getHeight());
    }

    @Override
    public void draw(Texture texture, float x, float y, float width, float height) {
        if (!drawing) throw new IllegalStateException("SpriteRenderer.begin must be called before draw.");

        float[] vertices = this.vertices;

        if (texture != lastTexture)
            switchTexture(texture);
        else if (idx == vertices.length) //
            flush();

        final float fx2 = x + width;
        final float fy2 = y + height;
        final float u = 0;
        final float v = 1;
        final float u2 = 1;
        final float v2 = 0;

        final float color = this.color;
        final float hslt = this.hslt;
        final int idx = this.idx;
        vertices[idx] = x;
        vertices[idx + 1] = y;
        vertices[idx + 2] = color;
        vertices[idx + 3] = u;
        vertices[idx + 4] = v;
        vertices[idx + 5] = hslt;

        vertices[idx + 6] = x;
        vertices[idx + 7] = fy2;
        vertices[idx + 8] = color;
        vertices[idx + 9] = u;
        vertices[idx + 10] = v2;
        vertices[idx + 11] = hslt;

        vertices[idx + 12] = fx2;
        vertices[idx + 13] = fy2;
        vertices[idx + 14] = color;
        vertices[idx + 15] = u2;
        vertices[idx + 16] = v2;
        vertices[idx + 17] = hslt;

        vertices[idx + 18] = fx2;
        vertices[idx + 19] = y;
        vertices[idx + 20] = color;
        vertices[idx + 21] = u2;
        vertices[idx + 22] = v;
        vertices[idx + 23] = hslt;
        this.idx = idx + 24;
    }

    /**
     * This is very different from the other overloads in this class; it assumes the float array it is given is in the
     * format libGDX uses to give to SpriteBatch, that is, in groups of 20 floats per sprite. UISpriteBatch uses 24
     * floats per sprite, to add hslt per color, so this does some conversion.
     *
     * @param texture        the Texture being drawn from; usually an atlas or some parent Texture with lots of TextureRegions
     * @param spriteVertices not the same format as {@link #vertices} in this class; should have a length that's a multiple of 20
     * @param offset         where to start drawing vertices from {@code spriteVertices}
     * @param count          how many vertices to draw from {@code spriteVertices} (20 vertices is one sprite)
     */
    @Override
    public void draw(Texture texture, float[] spriteVertices, int offset, int count) {
        if (!drawing) throw new IllegalStateException("SpriteRenderer.begin must be called before draw.");

        count = (count / 5) * 6;
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
        final float hslt = this.hslt;

        ////old way, breaks when libGDX code expects SPRITE_SIZE to be 20
        //System.arraycopy(spriteVertices, offset, vertices, idx, copyCount);
        ////new way, thanks mgsx
        for (int s = offset, v = idx, i = 0; i < copyCount; i += 6) {
            vertices[v++] = spriteVertices[s++];
            vertices[v++] = spriteVertices[s++];
            vertices[v++] = spriteVertices[s++];
            vertices[v++] = spriteVertices[s++];
            vertices[v++] = spriteVertices[s++];
            vertices[v++] = hslt;
        }
        idx += copyCount;
        count -= copyCount;
        while (count > 0) {
            offset += (copyCount / 6) * 5;
            flush();
            copyCount = Math.min(verticesLength, count);
            ////old way, breaks when libGDX code expects SPRITE_SIZE to be 20
            //System.arraycopy(spriteVertices, offset, vertices, 0, copyCount);
            ////new way, thanks mgsx
            for (int s = offset, v = 0, i = 0; i < copyCount; i += 6) {
                vertices[v++] = spriteVertices[s++];
                vertices[v++] = spriteVertices[s++];
                vertices[v++] = spriteVertices[s++];
                vertices[v++] = spriteVertices[s++];
                vertices[v++] = spriteVertices[s++];
                vertices[v++] = hslt;
            }
            idx += copyCount;
            count -= copyCount;
        }
    }

    /**
     * Meant for code that uses UISpriteBatch specifically and can set an extra float (for the color hslt) per vertex,
     * this is just like {@link #draw(Texture, float[], int, int)} when used in other Batch implementations, but expects
     * {@code spriteVertices} to have a length that is a multiple of 24 instead of 20.
     *
     * @param texture        the Texture being drawn from; usually an atlas or some parent Texture with lots of TextureRegions
     * @param spriteVertices vertices formatted as this class uses them; length should be a multiple of 24
     * @param offset         where to start drawing vertices from {@code spriteVertices}
     * @param count          how many vertices to draw from {@code spriteVertices} (24 vertices is one sprite)
     */
    public void drawExactly(Texture texture, float[] spriteVertices, int offset, int count) {
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
    public void draw(TextureRegion region, float x, float y) {
        draw(region, x, y, region.getRegionWidth(), region.getRegionHeight());
    }

    @Override
    public void draw(TextureRegion region, float x, float y, float width, float height) {
        if (!drawing) throw new IllegalStateException("SpriteRenderer.begin must be called before draw.");

        float[] vertices = this.vertices;

        Texture texture = region.getTexture();
        if (texture != lastTexture) {
            switchTexture(texture);
        } else if (idx == vertices.length) {
            flush();
        }
        final float fx2 = x + width;
        final float fy2 = y + height;
        final float u = region.getU();
        final float v = region.getV2();
        final float u2 = region.getU2();
        final float v2 = region.getV();

        final float color = this.color;
        final float hslt = this.hslt;
        final int idx = this.idx;
        vertices[idx] = x;
        vertices[idx + 1] = y;
        vertices[idx + 2] = color;
        vertices[idx + 3] = u;
        vertices[idx + 4] = v;
        vertices[idx + 5] = hslt;

        vertices[idx + 6] = x;
        vertices[idx + 7] = fy2;
        vertices[idx + 8] = color;
        vertices[idx + 9] = u;
        vertices[idx + 10] = v2;
        vertices[idx + 11] = hslt;

        vertices[idx + 12] = fx2;
        vertices[idx + 13] = fy2;
        vertices[idx + 14] = color;
        vertices[idx + 15] = u2;
        vertices[idx + 16] = v2;
        vertices[idx + 17] = hslt;

        vertices[idx + 18] = fx2;
        vertices[idx + 19] = y;
        vertices[idx + 20] = color;
        vertices[idx + 21] = u2;
        vertices[idx + 22] = v;
        vertices[idx + 23] = hslt;
        this.idx = idx + 24;
    }

    @Override
    public void draw(TextureRegion region, float x, float y, float originX, float originY, float width, float height,
                     float scaleX, float scaleY, float rotation) {
        if (!drawing) throw new IllegalStateException("SpriteRenderer.begin must be called before draw.");

        float[] vertices = this.vertices;

        Texture texture = region.getTexture();
        if (texture != lastTexture) {
            switchTexture(texture);
        } else if (idx == vertices.length) //
            flush();

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
        final float hslt = this.hslt;
        final int idx = this.idx;
        vertices[idx] = x1;
        vertices[idx + 1] = y1;
        vertices[idx + 2] = color;
        vertices[idx + 3] = u;
        vertices[idx + 4] = v;
        vertices[idx + 5] = hslt;

        vertices[idx + 6] = x2;
        vertices[idx + 7] = y2;
        vertices[idx + 8] = color;
        vertices[idx + 9] = u;
        vertices[idx + 10] = v2;
        vertices[idx + 11] = hslt;

        vertices[idx + 12] = x3;
        vertices[idx + 13] = y3;
        vertices[idx + 14] = color;
        vertices[idx + 15] = u2;
        vertices[idx + 16] = v2;
        vertices[idx + 17] = hslt;

        vertices[idx + 18] = x4;
        vertices[idx + 19] = y4;
        vertices[idx + 20] = color;
        vertices[idx + 21] = u2;
        vertices[idx + 22] = v;
        vertices[idx + 23] = hslt;
        this.idx = idx + 24;
    }

    @Override
    public void draw(TextureRegion region, float x, float y, float originX, float originY, float width, float height,
                     float scaleX, float scaleY, float rotation, boolean clockwise) {
        if (!drawing) throw new IllegalStateException("SpriteRenderer.begin must be called before draw.");

        float[] vertices = this.vertices;

        Texture texture = region.getTexture();
        if (texture != lastTexture) {
            switchTexture(texture);
        } else if (idx == vertices.length) //
            flush();

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
        final float hslt = this.hslt;
        final int idx = this.idx;
        vertices[idx] = x1;
        vertices[idx + 1] = y1;
        vertices[idx + 2] = color;
        vertices[idx + 3] = u1;
        vertices[idx + 4] = v1;
        vertices[idx + 5] = hslt;

        vertices[idx + 6] = x2;
        vertices[idx + 7] = y2;
        vertices[idx + 8] = color;
        vertices[idx + 9] = u2;
        vertices[idx + 10] = v2;
        vertices[idx + 11] = hslt;

        vertices[idx + 12] = x3;
        vertices[idx + 13] = y3;
        vertices[idx + 14] = color;
        vertices[idx + 15] = u3;
        vertices[idx + 16] = v3;
        vertices[idx + 17] = hslt;

        vertices[idx + 18] = x4;
        vertices[idx + 19] = y4;
        vertices[idx + 20] = color;
        vertices[idx + 21] = u4;
        vertices[idx + 22] = v4;
        vertices[idx + 23] = hslt;
        this.idx = idx + 24;
    }

    @Override
    public void draw(TextureRegion region, float width, float height, Affine2 transform) {
        if (!drawing) throw new IllegalStateException("SpriteRenderer.begin must be called before draw.");

        float[] vertices = this.vertices;

        Texture texture = region.getTexture();
        if (texture != lastTexture) {
            switchTexture(texture);
        } else if (idx == vertices.length) {
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
        final float hslt = this.hslt;
        final int idx = this.idx;
        vertices[idx] = x1;
        vertices[idx + 1] = y1;
        vertices[idx + 2] = color;
        vertices[idx + 3] = u;
        vertices[idx + 4] = v;
        vertices[idx + 5] = hslt;

        vertices[idx + 6] = x2;
        vertices[idx + 7] = y2;
        vertices[idx + 8] = color;
        vertices[idx + 9] = u;
        vertices[idx + 10] = v2;
        vertices[idx + 11] = hslt;

        vertices[idx + 12] = x3;
        vertices[idx + 13] = y3;
        vertices[idx + 14] = color;
        vertices[idx + 15] = u2;
        vertices[idx + 16] = v2;
        vertices[idx + 17] = hslt;

        vertices[idx + 18] = x4;
        vertices[idx + 19] = y4;
        vertices[idx + 20] = color;
        vertices[idx + 21] = u2;
        vertices[idx + 22] = v;
        vertices[idx + 23] = hslt;
        this.idx = idx + 24;
    }

    @SuppressWarnings("RedundantCast") // These casts are absolutely not redundant! Java 9 changed Buffer ABI.
    @Override
    public void flush() {
        if (idx == 0) return;

        renderCalls++;
        totalRenderCalls++;

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFuncSeparate(this.srcRGB, this.dstRGB, this.srcAlpha, this.dstAlpha);

        int spritesInBatch = idx / SPRITE_SIZE;
        if (spritesInBatch > maxSpritesInBatch) maxSpritesInBatch = spritesInBatch;
        int count = spritesInBatch * 6;

        lastTexture.bind();

        mesh.setVertices(vertices, 0, idx);
        mesh.getIndicesBuffer(true).position(0);
        mesh.getIndicesBuffer(true).limit(count);
        mesh.render(shader, GL20.GL_TRIANGLES, 0, count);

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
    public void setBlendFunction(int srcFunc, int dstFunc) {
        setBlendFunctionSeparate(srcFunc, dstFunc, srcFunc, dstFunc);
    }

    @Override
    public void setBlendFunctionSeparate(int srcFuncColor, int dstFuncColor, int srcFuncAlpha, int dstFuncAlpha) {
        if (srcRGB == srcFuncColor && dstRGB == dstFuncColor && srcAlpha == srcFuncAlpha && dstAlpha == dstFuncAlpha)
            return;
        flush();
        this.srcRGB = srcFuncColor;
        this.dstRGB = dstFuncColor;
        this.srcAlpha = srcFuncAlpha;
        this.dstAlpha = dstFuncAlpha;
    }

    @Override
    public int getBlendSrcFunc() {
        return srcRGB;
    }

    @Override
    public int getBlendDstFunc() {
        return dstRGB;
    }

    @Override
    public int getBlendSrcFuncAlpha() {
        return srcAlpha;
    }

    @Override
    public int getBlendDstFuncAlpha() {
        return dstAlpha;
    }

    @Override
    public void dispose() {
        mesh.dispose();
        if (defaultShader && shader != null) shader.dispose();
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
        if (drawing) flush();
        projectionMatrix.set(projection);
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
        shader.setUniformMatrix(u_projTrans, combinedMatrix);
        shader.setUniformi(u_texture, 0);
    }

    protected void switchTexture(Texture texture) {
        flush();
        lastTexture = texture;
        invTexWidth = 1.0f / texture.getWidth();
        invTexHeight = 1.0f / texture.getHeight();
    }

    @Override
    public void setShader(ShaderProgram shader) {
        if (drawing) {
            flush();
        }
        this.shader = shader;
        this.u_projTrans = shader.getUniformLocation("u_projTrans");
        this.u_texture = shader.getUniformLocation("u_texture");
        this.shader.bind();
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

    /* -- Additional MediaManager draw methods */

    /* ----- CMediaImage ----- */

    public void drawCMediaImage(CMediaImage cMedia, float x, float y) {
        TextureRegion texture = mediaManager.getCMediaImage(cMedia);
        this.draw(texture, x, y, 0, 0, texture.getRegionWidth(), texture.getRegionHeight(), 1, 1, 0);
    }

    public void drawCMediaImage(CMediaImage cMedia, float x, float y, float origin_x, float origin_y) {
        TextureRegion texture = mediaManager.getCMediaImage(cMedia);
        this.draw(texture, x, y, origin_x, origin_y, texture.getRegionWidth(), texture.getRegionHeight(), 1, 1, 0);
    }

    public void drawCMediaImage(CMediaImage cMedia, float x, float y, float origin_x, float origin_y, float width, float height) {
        TextureRegion texture = mediaManager.getCMediaImage(cMedia);
        this.draw(texture, x, y, origin_x, origin_y, width, height, 1, 1, 0);
    }

    public void drawCMediaImage(CMediaImage cMedia, float x, float y, float origin_x, float origin_y, float width, float height, float rotation) {
        TextureRegion texture = mediaManager.getCMediaImage(cMedia);
        this.draw(texture, x, y, origin_x, origin_y, width, height, 1, 1, rotation);
    }

    public void drawCMediaImageCut(CMediaImage cMedia, float x, float y, int widthCut, int heightCut) {
        TextureRegion texture = mediaManager.getCMediaImage(cMedia);
        this.draw(texture.getTexture(), x, y, texture.getRegionX(), texture.getRegionY(), widthCut, heightCut);
    }

    public void drawCMediaImageCut(CMediaImage cMedia, float x, float y, int srcX, int srcY, int widthCut, int heightCut) {
        TextureRegion texture = mediaManager.getCMediaImage(cMedia);
        this.draw(texture.getTexture(), x, y, texture.getRegionX() + srcX, texture.getRegionY() + srcY, widthCut, heightCut);
    }

    public void drawCMediaImageScale(CMediaImage cMedia, float x, float y, float origin_x, float origin_y, float scaleX, float scaleY) {
        TextureRegion texture = mediaManager.getCMediaImage(cMedia);
        this.draw(texture, x, y, origin_x, origin_y, texture.getRegionWidth(), texture.getRegionHeight(), scaleX, scaleY, 0);
    }

    public void drawCMediaImageScale(CMediaImage cMedia, float x, float y, float origin_x, float origin_y, float scaleX, float scaleY, float rotation) {
        TextureRegion texture = mediaManager.getCMediaImage(cMedia);
        this.draw(texture, x, y, origin_x, origin_y, texture.getRegionWidth(), texture.getRegionHeight(), scaleX, scaleY, rotation);
    }

    /* --- CMediaAnimation  --- */

    public void drawCMediaAnimation(CMediaAnimation cMedia, float x, float y, float animationTimer) {
        TextureRegion textureRegion = mediaManager.getCMediaAnimation(cMedia).getKeyFrame(animationTimer);
        this.draw(textureRegion, x, y, 0, 0, textureRegion.getRegionWidth(), textureRegion.getRegionHeight(), 1, 1, 0);
    }

    public void drawCMediaAnimation(CMediaAnimation cMedia, float x, float y, float animationTimer, float origin_x, float origin_y) {
        TextureRegion textureRegion = mediaManager.getCMediaAnimation(cMedia).getKeyFrame(animationTimer);
        this.draw(textureRegion, x, y, origin_x, origin_y, textureRegion.getRegionWidth(), textureRegion.getRegionHeight(), 1, 1, 0);
    }

    public void drawCMediaAnimation(CMediaAnimation cMedia, float x, float y, float animationTimer, float origin_x, float origin_y, float width, float height) {
        TextureRegion textureRegion = mediaManager.getCMediaAnimation(cMedia).getKeyFrame(animationTimer);
        this.draw(textureRegion, x, y, origin_x, origin_y, width, height, 1, 1, 0);
    }

    public void drawCMediaAnimation(CMediaAnimation cMedia, float x, float y, float animationTimer, float origin_x, float origin_y, float width, float height, float rotation) {
        TextureRegion textureRegion = mediaManager.getCMediaAnimation(cMedia).getKeyFrame(animationTimer);
        this.draw(textureRegion, x, y, origin_x, origin_y, width, height, 1, 1, rotation);
    }

    public void drawCMediaAnimationCut(CMediaAnimation cMedia, float x, float y, float animationTimer, int widthCut, int heightCut) {
        drawCMediaAnimationCut(cMedia, x, y, animationTimer, 0, 0, widthCut, heightCut);
    }

    public void drawCMediaAnimationCut(CMediaAnimation cMedia, float x, float y, float animationTimer, int srcX, int srcY, int widthCut, int heightCut) {
        TextureRegion textureRegion = mediaManager.getCMediaAnimation(cMedia).getKeyFrame(animationTimer);
        this.draw(textureRegion.getTexture(), x, y, textureRegion.getRegionX() + srcX, textureRegion.getRegionY() + srcY, widthCut, heightCut);
    }

    public void drawCMediaAnimationScale(CMediaAnimation cMedia, float x, float y, float animationTimer, float origin_x, float origin_y, float scaleX, float scaleY) {
        TextureRegion textureRegion = mediaManager.getCMediaAnimation(cMedia).getKeyFrame(animationTimer);
        this.draw(textureRegion, x, y, origin_x, origin_y, textureRegion.getRegionWidth(), textureRegion.getRegionHeight(), scaleX, scaleY, 0);
    }

    public void drawCMediaAnimationScale(CMediaAnimation cMedia, float x, float y, float animationTimer, float origin_x, float origin_y, float scaleX, float scaleY, float rotation) {
        TextureRegion textureRegion = mediaManager.getCMediaAnimation(cMedia).getKeyFrame(animationTimer);
        this.draw(textureRegion, x, y, origin_x, origin_y, textureRegion.getRegionWidth(), textureRegion.getRegionHeight(), scaleX, scaleY, rotation);
    }
    /* --- CMediaArray  --- */

    public void drawCMediaArray(CMediaArray cMedia, float x, float y, int arrayIndex) {
        TextureRegion textureRegion = mediaManager.getCMediaArray(cMedia, arrayIndex);
        this.draw(textureRegion, x, y, 0, 0, textureRegion.getRegionWidth(), textureRegion.getRegionHeight(), 1, 1, 0);
    }

    public void drawCMediaArray(CMediaArray cMedia, float x, float y, int arrayIndex, float origin_x, float origin_y) {
        TextureRegion textureRegion = mediaManager.getCMediaArray(cMedia, arrayIndex);
        this.draw(textureRegion, x, y, origin_x, origin_y, textureRegion.getRegionWidth(), textureRegion.getRegionHeight(), 1, 1, 0);
    }

    public void drawCMediaArray(CMediaArray cMedia, float x, float y, int arrayIndex, float origin_x, float origin_y, float width, float height) {
        TextureRegion textureRegion = mediaManager.getCMediaArray(cMedia, arrayIndex);
        this.draw(textureRegion, x, y, origin_x, origin_y, width, height, 1, 1, 0);
    }

    public void drawCMediaArray(CMediaArray cMedia, float x, float y, int arrayIndex, float origin_x, float origin_y, float width, float height, float rotation) {
        TextureRegion textureRegion = mediaManager.getCMediaArray(cMedia, arrayIndex);
        this.draw(textureRegion, x, y, origin_x, origin_y, width, height, 1, 1, rotation);
    }

    public void drawCMediaArrayCut(CMediaArray cMedia, float x, float y, int arrayIndex, int widthCut, int heightCut) {
        drawCMediaArrayCut(cMedia, x, y, arrayIndex, 0, 0, widthCut, heightCut);
    }

    public void drawCMediaArrayCut(CMediaArray cMedia, float x, float y, int arrayIndex, int srcX, int srcY, int widthCut, int heightCut) {
        TextureRegion textureRegion = mediaManager.getCMediaArray(cMedia, arrayIndex);
        this.draw(textureRegion.getTexture(), x, y, textureRegion.getRegionX() + srcX, textureRegion.getRegionY() + srcY, widthCut, heightCut);
    }

    public void drawCMediaArrayScale(CMediaArray cMedia, float x, float y, int arrayIndex, float origin_x, float origin_y, float scaleX, float scaleY) {
        TextureRegion textureRegion = mediaManager.getCMediaArray(cMedia, arrayIndex);
        this.draw(textureRegion, x, y, origin_x, origin_y, textureRegion.getRegionWidth(), textureRegion.getRegionHeight(), scaleX, scaleY, 0);
    }

    public void drawCMediaArrayScale(CMediaArray cMedia, float x, float y, int arrayIndex, float origin_x, float origin_y, float scaleX, float scaleY, float rotation) {
        TextureRegion textureRegion = mediaManager.getCMediaArray(cMedia, arrayIndex);
        this.draw(textureRegion, x, y, origin_x, origin_y, textureRegion.getRegionWidth(), textureRegion.getRegionHeight(), scaleX, scaleY, rotation);
    }

    /* --- CMediaFont  --- */

    public void drawCMediaFont(CMediaFont cMedia, float x, float y, String text) {
        BitmapFont bitmapFont = mediaManager.getCMediaFont(cMedia);
        bitmapFont.draw(this, text, (x + cMedia.offset_x), (y + cMedia.offset_y));
    }

    public void drawCMediaFont(CMediaFont cMedia, float x, float y, String text, int maxWidth) {
        BitmapFont bitmapFont = mediaManager.getCMediaFont(cMedia);
        bitmapFont.draw(this, text, (x + cMedia.offset_x), (y + cMedia.offset_y), 0, text.length(), maxWidth, Align.left, true, "");
    }


    /* ----- CMediaCursor ----- */

    public void drawCMediaCursor(CMediaCursor cMedia, float x, float y) {
        TextureRegion texture = mediaManager.getCMediaCursor(cMedia);
        this.draw(texture, x - cMedia.hotspot_x, y - cMedia.hotspot_y, 0, 0, texture.getRegionWidth(), texture.getRegionHeight(), 1, 1, 0);
    }


    /* -----  CMediaSprite ----- */

    public void drawCMediaSprite(CMediaSprite cMedia, float x, float y) {
        drawCMediaSprite(cMedia, x, y, 0, 0);
    }

    public void drawCMediaSprite(CMediaSprite cMedia, float x, float y, int arrayIndex, float animationTimer) {
        if (cMedia == null) return;
        switch (cMedia) {
            case CMediaImage cMediaImage -> drawCMediaImage(cMediaImage, x, y);
            case CMediaAnimation cMediaAnimation -> drawCMediaAnimation(cMediaAnimation, x, y, animationTimer);
            case CMediaArray cMediaArray -> drawCMediaArray(cMediaArray, x, y, arrayIndex);
            case CMediaCursor cMediaCursor -> drawCMediaCursor(cMediaCursor, x, y);
            default -> {
            }
        }
    }

    public void drawCMediaSprite(CMediaSprite cMedia, float x, float y, float origin_x, float origin_y) {
        drawCMediaSprite(cMedia, x, y, origin_x, origin_y, 0, 0);
    }

    public void drawCMediaSprite(CMediaSprite cMedia, float x, float y, float origin_x, float origin_y, int arrayIndex, float animationTimer) {
        if (cMedia == null) return;
        switch (cMedia) {
            case CMediaImage cMediaImage -> drawCMediaImage(cMediaImage, x, y, origin_x, origin_y);
            case CMediaAnimation cMediaAnimation ->
                    drawCMediaAnimation(cMediaAnimation, x, y, animationTimer, origin_x, origin_y);
            case CMediaArray cMediaArray -> drawCMediaArray(cMediaArray, x, y, arrayIndex, origin_x, origin_y);
            case CMediaCursor cMediaCursor -> drawCMediaCursor(cMediaCursor, x, y);
            default -> {
            }
        }
    }

    public void drawCMediaSprite(CMediaSprite cMedia, float x, float y, float origin_x, float origin_y, float width, float height) {
        drawCMediaSprite(cMedia, x, y, origin_x, origin_y, width, height, 0, 0);
    }

    public void drawCMediaSprite(CMediaSprite cMedia, float x, float y, float origin_x, float origin_y, float width, float height, int arrayIndex, float animationTimer) {
        if (cMedia == null) return;
        switch (cMedia) {
            case CMediaImage cMediaImage -> drawCMediaImage(cMediaImage, x, y, origin_x, origin_y, width, height);
            case CMediaAnimation cMediaAnimation ->
                    drawCMediaAnimation(cMediaAnimation, x, y, animationTimer, origin_x, origin_y, width, height);
            case CMediaArray cMediaArray ->
                    drawCMediaArray(cMediaArray, x, y, arrayIndex, origin_x, origin_y, width, height);
            case CMediaCursor cMediaCursor -> drawCMediaCursor(cMediaCursor, x, y);
            default -> {
            }
        }
    }

    public void drawCMediaSprite(CMediaSprite cMedia, float x, float y, float origin_x, float origin_y, float width, float height, float rotation) {
        drawCMediaSprite(cMedia, x, y, origin_x, origin_y, width, height, rotation, 0, 0);
    }

    public void drawCMediaSprite(CMediaSprite cMedia, float x, float y, float origin_x, float origin_y, float width, float height, float rotation, float animationTimer, int arrayIndex) {
        if (cMedia == null) return;
        switch (cMedia) {
            case CMediaImage cMediaImage ->
                    drawCMediaImage(cMediaImage, x, y, origin_x, origin_y, width, height, rotation);
            case CMediaAnimation cMediaAnimation ->
                    drawCMediaAnimation(cMediaAnimation, x, y, animationTimer, origin_x, origin_y, width, height, rotation);
            case CMediaArray cMediaArray ->
                    drawCMediaArray(cMediaArray, x, y, arrayIndex, origin_x, origin_y, width, height, rotation);
            case CMediaCursor cMediaCursor -> drawCMediaCursor(cMediaCursor, x, y);
            default -> {
            }
        }
    }

    public void drawCMediaSpriteCut(CMediaSprite cMedia, float x, float y, int widthCut, int heightCut) {
        drawCMediaSpriteCut(cMedia, x, y, widthCut, heightCut, 0, 0);
    }

    public void drawCMediaSpriteCut(CMediaSprite cMedia, float x, float y, int widthCut, int heightCut, float animationTimer, int arrayIndex) {
        drawCMediaSpriteCut(cMedia, x, y, 0, 0, widthCut, heightCut, animationTimer, arrayIndex);
    }

    public void drawCMediaSpriteCut(CMediaSprite cMedia, float x, float y, int srcX, int srcY, int widthCut, int heightCut) {
        drawCMediaSpriteCut(cMedia, x, y, srcX, srcY, widthCut, heightCut, 0, 0);
    }

    public void drawCMediaSpriteCut(CMediaSprite cMedia, float x, float y, int srcX, int srcY, int widthCut, int heightCut, float animationTimer, int arrayIndex) {
        if (cMedia == null) return;
        switch (cMedia) {
            case CMediaImage cMediaImage -> drawCMediaImageCut(cMediaImage, x, y, srcX, srcY, widthCut, heightCut);
            case CMediaAnimation cMediaAnimation ->
                    drawCMediaAnimationCut(cMediaAnimation, x, y, animationTimer, srcX, srcY, widthCut, heightCut);
            case CMediaArray cMediaArray ->
                    drawCMediaArrayCut(cMediaArray, x, y, arrayIndex, srcX, srcY, widthCut, heightCut);
            case CMediaCursor cMediaCursor -> drawCMediaCursor(cMediaCursor, x, y);
            default -> {
            }
        }
    }

    public void drawCMediaSpriteScale(CMediaSprite cMedia, float x, float y, float origin_x, float origin_y, float scaleX, float scaleY) {
        drawCMediaSprite(cMedia, x, y, origin_x, origin_y, scaleX, scaleY, 0, 0, 0);
    }

    public void drawCMediaSpriteScale(CMediaSprite cMedia, float x, float y, float origin_x, float origin_y, float scaleX, float scaleY, float animationTimer, int arrayIndex) {
        drawCMediaSprite(cMedia, x, y, origin_x, origin_y, scaleX, scaleY, 0, animationTimer, arrayIndex);
    }

    public void drawCMediaSpriteScale(CMediaSprite cMedia, float x, float y, float origin_x, float origin_y, float scaleX, float scaleY, float rotation) {
        drawCMediaSprite(cMedia, x, y, origin_x, origin_y, scaleX, scaleY, rotation, 0, 0);
    }

    public void drawCMediaSpriteScale(CMediaSprite cMedia, float x, float y, float origin_x, float origin_y, float scaleX, float scaleY, float rotation, float animationTimer, int arrayIndex) {
        if (cMedia == null) return;
        switch (cMedia) {
            case CMediaImage cMediaImage ->
                    drawCMediaImageScale(cMediaImage, x, y, origin_x, origin_y, scaleX, scaleY, rotation);
            case CMediaAnimation cMediaAnimation ->
                    drawCMediaAnimationScale(cMediaAnimation, x, y, animationTimer, origin_x, origin_y, scaleX, scaleY, rotation);
            case CMediaArray cMediaArray ->
                    drawCMediaArrayScale(cMediaArray, x, y, arrayIndex, origin_x, origin_y, scaleX, scaleY, rotation);
            case CMediaCursor cMediaCursor -> drawCMediaCursor(cMediaCursor, x, y);
            default -> {
            }
        }
    }
}