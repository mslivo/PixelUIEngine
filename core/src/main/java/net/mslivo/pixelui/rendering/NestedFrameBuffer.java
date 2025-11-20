package net.mslivo.pixelui.rendering;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.GL32;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;

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
        Gdx.gl.glViewport(x, y, width, height);
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

    @Override
    public void dispose() {
        super.dispose();
    }
}