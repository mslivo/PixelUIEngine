package net.mslivo.core.engine.ui_engine.render.misc;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

/**
 * An implementation of Framebuffer allows nesting.
 * Adapted from: https://github.com/crykn/libgdx-screenmanager/wiki/Custom-FrameBuffer-implementation
 */
public class NestedFrameBuffer extends FrameBuffer {
    private int previousFBOHandle = -1;
    private int[] previousViewport = new int[4];
    private boolean isBound = false;
    private static final IntBuffer INT_BUFF = ByteBuffer
            .allocateDirect(16 * Integer.BYTES).order(ByteOrder.nativeOrder())
            .asIntBuffer();


    public NestedFrameBuffer(Pixmap.Format format, int width, int height) {
        this(format, width, height, false, false);
    }

    public NestedFrameBuffer(Pixmap.Format format, int width, int height, boolean hasDepth) {
        this(format, width, height, hasDepth, false);
    }

    public NestedFrameBuffer(Pixmap.Format format, int width, int height, boolean hasDepth, boolean hasStencil) {
        super(format, width, height, hasDepth, hasStencil);
    }

    protected NestedFrameBuffer(NestableFrameBufferBuilder bufferBuilder) {
        super(bufferBuilder);
    }


    private int getBoundFboHandle() {
        IntBuffer intBuf = INT_BUFF;
        Gdx.gl.glGetIntegerv(GL20.GL_FRAMEBUFFER_BINDING, intBuf);
        return intBuf.get(0);
    }

    private int[] getViewport() {
        IntBuffer intBuf = INT_BUFF;
        Gdx.gl.glGetIntegerv(GL20.GL_VIEWPORT, intBuf);
        return new int[]{intBuf.get(0), intBuf.get(1), intBuf.get(2),
                intBuf.get(3)};
    }



    @Override
    public void begin() {
        if (isBound) throw new RuntimeException("end() has to be called before another draw can begin!");
        isBound = true;

        previousFBOHandle = getBoundFboHandle();
        bind();

        previousViewport = getViewport();
        setFrameBufferViewport();
    }

    @Deprecated
    @Override
    public void bind() {
        Gdx.gl20.glBindFramebuffer(GL20.GL_FRAMEBUFFER, framebufferHandle);
    }

    @Override
    public void end() {
        end(previousViewport[0], previousViewport[1], previousViewport[2],
                previousViewport[3]);
    }

    @Override
    public void end(int x, int y, int width, int height) {
        if (!isBound) throw new RuntimeException("begin() has to be called first!");
        isBound = false;

        if (getBoundFboHandle() != framebufferHandle) {
            throw new IllegalStateException("The currently bound framebuffer ("
                    + getBoundFboHandle()
                    + ") doesn't match this one. Make sure the nested framebuffers are closed in the same order they were opened in!");
        }

        Gdx.gl20.glBindFramebuffer(GL20.GL_FRAMEBUFFER, previousFBOHandle);
        Gdx.gl20.glViewport(x, y, width, height);
    }

    @Override
    protected void build() {
        int previousFBOHandle = getBoundFboHandle();
        super.build();
        Gdx.gl20.glBindFramebuffer(GL20.GL_FRAMEBUFFER, previousFBOHandle);
    }

    public boolean isBound() {
        return isBound;
    }

    public static class NestableFrameBufferBuilder extends FrameBufferBuilder {
        public NestableFrameBufferBuilder(int width, int height) {
            super(width, height);
        }

        @Override
        public FrameBuffer build() {
            return new NestedFrameBuffer(this);
        }

        boolean hasDepthRenderBuffer() {
            return hasDepthRenderBuffer;
        }
    }

}