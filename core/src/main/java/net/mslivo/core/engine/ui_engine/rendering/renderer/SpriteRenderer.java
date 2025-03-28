package net.mslivo.core.engine.ui_engine.rendering.renderer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.BitmapFontCache;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.VertexBufferObjectWithVAO;
import com.badlogic.gdx.math.Affine2;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Align;
import net.mslivo.core.engine.media_manager.*;
import net.mslivo.core.engine.ui_engine.rendering.IntegerIndexBufferObject;
import net.mslivo.core.engine.ui_engine.rendering.shader.SpriteShader;

public class SpriteRenderer extends BasicColorTweakRenderer {

    public static final String TEXCOORD_ATTRIBUTE = "a_texCoord";
    public static final String TEXTURE_UNIFORM = "u_texture";
    public static final String TEXTURE_SIZE_UNIFORM = "u_textureSize";

    private static final int VERTEX_SIZE = 6;
    private static final int INDICES_SIZE = 6;
    private static final int VERTEXES_INDICES_RATIO = 4;
    private static final int SPRITE_SIZE = VERTEX_SIZE * 4;

    public static final int MAX_VERTEXES_DEFAULT = 65532 * 4; // 65532 sprites

    private static final SpriteShader DEFAULT_SHADER = new SpriteShader("""
            VERTEX:
            FRAGMENT:
            
            vec4 colorTintAdd(vec4 color, vec4 modColor){
                 color.rgb = clamp(color.rgb+(modColor.rgb-0.5),0.0,1.0);
                 color.a *= modColor.a;
                 return color;
            }
            
            void main(){
            	gl_FragColor = colorTintAdd(texture2D(u_texture, v_texCoord),v_color);
            }
            """);

    private Texture lastTexture;
    private float invTexWidth, invTexHeight;
    private MediaManager mediaManager;
    private int nextSamplerTextureUnit;

    @Override
    protected ShaderProgram provideDefaultShader() {
        return DEFAULT_SHADER.compile();
    }

    public SpriteRenderer() {
        this(null, null, MAX_VERTEXES_DEFAULT, false);
    }

    public SpriteRenderer(final MediaManager mediaManager) {
        this(mediaManager, null, MAX_VERTEXES_DEFAULT, false);
    }


    public SpriteRenderer(final MediaManager mediaManager, final ShaderProgram shaderProgram) {
        this(mediaManager, shaderProgram, MAX_VERTEXES_DEFAULT, false);
    }

    public SpriteRenderer(final MediaManager mediaManager, final ShaderProgram shaderProgram, final int maxVertexes) {
        this(mediaManager, shaderProgram, maxVertexes, false);
    }

    public SpriteRenderer(final MediaManager mediaManager, final ShaderProgram shaderProgram, final int maxVertexes, final boolean printRenderCalls) {
        super(maxVertexes, shaderProgram, printRenderCalls);
        this.invTexWidth = this.invTexHeight = 0;
        this.nextSamplerTextureUnit = 1;
        this.mediaManager = mediaManager;
    }

    @Override
    protected IntegerIndexBufferObject createIndexBufferObject(int size) {
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


    @Override
    protected VertexBufferObjectWithVAO createVertexBufferObject(int size) {
        return new VertexBufferObjectWithVAO(true, size,
                new VertexAttribute(VertexAttributes.Usage.Position, 2, POSITION_ATTRIBUTE),
                new VertexAttribute(VertexAttributes.Usage.ColorPacked, 4, COLOR_ATTRIBUTE),
                new VertexAttribute(VertexAttributes.Usage.TextureCoordinates, 2, TEXCOORD_ATTRIBUTE),
                new VertexAttribute(VertexAttributes.Usage.ColorPacked, 4, TWEAK_ATTRIBUTE));
    }

    @Override
    public void begin() {
        super.setPrimitiveType(GL32.GL_TRIANGLES);
        super.begin();
    }

    @Override
    public void end() {
        super.end();
        lastTexture = null;
        this.nextSamplerTextureUnit = 1;
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

        vertexPush(x1,y1,color,u,v,tweak);
        vertexPush(x2,y2,color,u,v2,tweak);
        vertexPush(x3,y3,color,u2,v2,tweak);
        vertexPush(x4,y4,color,u2,v,tweak);

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

        vertexPush(x,y,color,u,v,tweak);
        vertexPush(x,fy2,color,u,v2,tweak);
        vertexPush(fx2,fy2,color,u2,v2,tweak);
        vertexPush(fx2,y,color,u2,v,tweak);


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

        vertexPush(x,y,color,u,v,tweak);
        vertexPush(x,fy2,color,u,v2,tweak);
        vertexPush(fx2,fy2,color,u2,v2,tweak);
        vertexPush(fx2,y,color,u2,v,tweak);


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

        vertexPush(x,y,color,u,v,tweak);
        vertexPush(x,fy2,color,u,v2,tweak);
        vertexPush(fx2,fy2,color,u2,v2,tweak);
        vertexPush(fx2,y,color,u2,v,tweak);


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

        vertexPush(x,y,color,u,v,tweak);
        vertexPush(x,fy2,color,u,v2,tweak);
        vertexPush(fx2,fy2,color,u2,v2,tweak);
        vertexPush(fx2,y,color,u2,v,tweak);


    }

    public void draw(final Texture texture, final float[] spriteVertices, int offset, int count) {
        if (!drawing) throw new IllegalStateException(ERROR_BEGIN_END);

        count = (count / 5) * 6;
        final int verticesLength = getSizeMaxVertexes();

        int remainingVertices = verticesLength;
        if (texture != lastTexture)
            switchTexture(texture);
        else {
            remainingVertices -= vertexBufferPosition();
            if (remainingVertices == 0) {
                flush();
                remainingVertices = verticesLength;
            }
        }
        int copyCount = Math.min(remainingVertices, count);
        final float tweak = this.tweak;

        for (int s = offset, i = 0; i < copyCount; i += 6) {
            vertexPush(spriteVertices[s++],spriteVertices[s++],spriteVertices[s++],spriteVertices[s++],spriteVertices[s++],this.tweak);
        }
        count -= copyCount;
        while (count > 0) {
            offset += (copyCount / 6) * 5;
            flush();
            copyCount = Math.min(verticesLength, count);
            for (int s = offset, v = 0, i = 0; i < copyCount; i += 6) {
                vertexPush(spriteVertices[s++],spriteVertices[s++],spriteVertices[s++],spriteVertices[s++],spriteVertices[s++],this.tweak);
            }
            count -= copyCount;
        }
    }


    public void drawExactly(final Texture texture, final float[] spriteVertices, int offset, int count) {
        if (!drawing) throw new IllegalStateException(ERROR_BEGIN_END);

        int remainingVertices = this.getSizeMaxVertexesFloats();
        if (texture != lastTexture)
            switchTexture(texture);
        else {
            remainingVertices -= vertexBufferPosition();
            if (remainingVertices == 0) {
                flush();
                remainingVertices = this.getSizeMaxVertexesFloats();
            }
        }
        int copyCount = Math.min(remainingVertices, count);

        vertexPush(spriteVertices, offset, copyCount);
        count -= copyCount;
        while (count > 0) {
            offset += copyCount;
            flush();
            copyCount = Math.min(getSizeMaxVertexes(), count);
            vertexPush(spriteVertices, offset, copyCount);
            count -= copyCount;
        }
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

        vertexPush(x,y,color,u,v,tweak);
        vertexPush(x,fy2,color,u,v2,tweak);
        vertexPush(fx2,fy2,color,u2,v2,tweak);
        vertexPush(fx2,y,color,u2,v,tweak);

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

        vertexPush(x1,y1,color,u,v,tweak);
        vertexPush(x2,y2,color,u,v2,tweak);
        vertexPush(x3,y3,color,u2,v2,tweak);
        vertexPush(x4,y4,color,u2,v,tweak);


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


        vertexPush(x1,y1,color,u1,v1,tweak);
        vertexPush(x2,y2,color,u2,v2,tweak);
        vertexPush(x3,y3,color,u3,v3,tweak);
        vertexPush(x4,y4,color,u4,v4,tweak);


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

        vertexPush(x1,y1,color,u,v,tweak);
        vertexPush(x2,y2,color,u,v2,tweak);
        vertexPush(x3,y3,color,u2,v2,tweak);
        vertexPush(x4,y4,color,u2,v,tweak);


    }

    @Override
    public void flush() {
        if (!isAnyVertexesInBuffer()) return;
        lastTexture.bind();
        super.flush();
    }

    protected void switchTexture(final Texture texture) {
        flush();
        lastTexture = texture;
        invTexWidth = 1.0f / texture.getWidth();
        invTexHeight = 1.0f / texture.getHeight();
        shader.setUniformi(uniformLocation(TEXTURE_UNIFORM), 0);
        shader.setUniformf(uniformLocation(TEXTURE_SIZE_UNIFORM), texture.getWidth(), texture.getHeight());
    }

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
            fontVertices[idx] = (colorPackedRGBA(tempColor.r, tempColor.g, tempColor.b, tempColor.a));
        }

        this.draw(fontCache.getFont().getRegion().getTexture(), fontVertices, 0, fontCache.getVertexCount(0));
    }

}