package net.mslivo.pixelui.rendering;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.utils.BufferUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

/**
 * An implementation of Framebuffer allows nesting.
 * Adapted from: https://github.com/crykn/libgdx-screenmanager/wiki/Custom-FrameBuffer-implementation
 */
public class NestedFrameBuffer extends FrameBuffer {
    private static final String ERROR_END_BEGIN = "NestedFrameBuffer.end must be called before begin.";
    private static final String ERROR_BEGIN_END = "NestedFrameBuffer.begin must be called before end.";
    private int[] previousViewport;
    private boolean isBound;
    private TextureRegion textureRegionFlippedCache;
    private final IntBuffer intBuffer;
    private int[] getViewPortCache;
    private int previousFBOHandle;
    private int getBoundFBOCache;

    public NestedFrameBuffer(Pixmap.Format format, int width, int height) {
        this(format, width, height, false, false);
    }

    public NestedFrameBuffer(Pixmap.Format format, int width, int height, boolean hasDepth) {
        this(format, width, height, hasDepth, false);
    }

    public NestedFrameBuffer(Pixmap.Format format, int width, int height, boolean hasDepth, boolean hasStencil) {
        super(format, width, height, hasDepth, hasStencil);
        this.isBound = false;
        this.previousFBOHandle = -1;
        this.previousViewport = null;
        this.intBuffer = ByteBuffer
                .allocateDirect(16 * Integer.BYTES).order(ByteOrder.nativeOrder())
                .asIntBuffer();
        this.getBoundFBOCache = -1;
        this.getViewPortCache = null;
        this.textureRegionFlippedCache = null;
    }


    private int getBoundFboHandle() {
        if(this.getBoundFBOCache != -1) return this.getBoundFBOCache;
        Gdx.gl.glGetIntegerv(GL32.GL_FRAMEBUFFER_BINDING, this.intBuffer);
        this.getBoundFBOCache = this.intBuffer.get(0);
        return this.getBoundFBOCache;
    }

    private int[] getViewport() {
        if(this.getViewPortCache != null) return this.getViewPortCache;
        this.getViewPortCache = new int[4];
        IntBuffer intBuf = intBuffer;
        Gdx.gl.glGetIntegerv(GL32.GL_VIEWPORT, intBuf);
        this.getViewPortCache[0] = intBuf.get(0);
        this.getViewPortCache[1] = intBuf.get(1);
        this.getViewPortCache[2] = intBuf.get(2);
        this.getViewPortCache[3] = intBuf.get(3);
        return this.getViewPortCache;
    }


    public void beginGlClear(){
        this.begin();
        Gdx.gl.glClearColor(0f, 0f, 0f, 0f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
    }


    @Override
    public void begin() {
        if (isBound) throw new RuntimeException(ERROR_BEGIN_END);
        isBound = true;

        previousFBOHandle = getBoundFboHandle();
        bind();

        previousViewport = getViewport();
        setFrameBufferViewport();
    }

    @Deprecated
    @Override
    public void bind() {
        Gdx.gl.glBindFramebuffer(GL32.GL_FRAMEBUFFER, framebufferHandle);
    }

    @Override
    public void end() {
        end(previousViewport[0], previousViewport[1], previousViewport[2],
                previousViewport[3]);
    }

    @Override
    public void end(int x, int y, int width, int height) {
        if (!isBound) throw new RuntimeException(ERROR_END_BEGIN);
        isBound = false;
        Gdx.gl.glBindFramebuffer(GL32.GL_FRAMEBUFFER, previousFBOHandle);
    }

    @Override
    protected void build() {
        int previousFBOHandle = getBoundFboHandle();
        super.build();
        Gdx.gl.glBindFramebuffer(GL32.GL_FRAMEBUFFER, previousFBOHandle);
    }

    public boolean isBound() {
        return isBound;
    }

    public void resetCaches(){
        this.getViewPortCache = null;
        this.textureRegionFlippedCache = null;
        this.getBoundFBOCache = -1;
    }

    public TextureRegion getFlippedTextureRegion(){
        if(this.textureRegionFlippedCache == null){
            this.textureRegionFlippedCache = new TextureRegion(this.getColorBufferTexture());
            this.textureRegionFlippedCache.flip(false,true);
        }
        return this.textureRegionFlippedCache;
    }


    public NestedFrameBuffer trimRegionCopy(SpriteRenderer batch,
                                            int srcX, int srcY,
                                            int trimW, int trimH) {

        final Pixmap.Format format = this.getColorBufferTexture().getTextureData().getFormat();
        final Texture.TextureFilter minFilter = this.getColorBufferTexture().getMinFilter();
        final Texture.TextureFilter magFilter = this.getColorBufferTexture().getMagFilter();

        // Create new trimmed FBO
        NestedFrameBuffer out = new NestedFrameBuffer(format, trimW, trimH, false, false);
        out.getColorBufferTexture().setFilter(minFilter, magFilter);

        // Region from original framebuffer texture
        TextureRegion region = new TextureRegion(
                this.getColorBufferTexture(),
                srcX, srcY,
                trimW, trimH
        );
        region.flip(false, true); // FBO textures are Y-flipped in LibGDX

        // Camera for drawing into the new FBO
        OrthographicCamera cam = new OrthographicCamera(trimW, trimH);
        cam.setToOrtho(true, trimW, trimH);

        // Draw region into trimmed framebuffer
        out.beginGlClear();
        batch.setProjectionMatrix(cam.combined);
        batch.begin();
        batch.draw(region, 0, 0, trimW, trimH);
        batch.end();
        out.end();

        return out;
    }

    public NestedFrameBuffer trimAlphaCopy(SpriteRenderer batch, float alphaThreshold) {

        final int w = this.getWidth();
        final int h = this.getHeight();

        ByteBuffer buf = BufferUtils.newByteBuffer(w * h * 4);

        this.begin();
        Gdx.gl.glReadPixels(0, 0, w, h, GL32.GL_RGBA, GL32.GL_UNSIGNED_BYTE, buf);
        this.end();

        int minX = w, minY = h, maxX = -1, maxY = -1;

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int index = (y * w + x) * 4;
                int a = buf.get(index + 3) & 0xFF;

                if (a > alphaThreshold) {
                    if (x < minX) minX = x;
                    if (x > maxX) maxX = x;
                    if (y < minY) minY = y;
                    if (y > maxY) maxY = y;
                }
            }
        }

        // Nothing visible → return 1×1
        if (maxX < 0) {
            return new NestedFrameBuffer(
                    this.getColorBufferTexture().getTextureData().getFormat(),
                    1, 1,
                    false, false
            );
        }

        final int trimW = maxX - minX + 1;
        final int trimH = maxY - minY + 1;

        // Reuse generic trim method:
        return trimRegionCopy(batch, minX, minY, trimW, trimH);
    }


    @Override
    public void dispose() {
        super.dispose();
    }
}