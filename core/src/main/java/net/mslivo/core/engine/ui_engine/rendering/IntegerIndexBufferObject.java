package net.mslivo.core.engine.ui_engine.rendering;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL32;
import com.badlogic.gdx.graphics.glutils.IndexBufferObject;
import com.badlogic.gdx.graphics.glutils.IndexData;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.GdxRuntimeException;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

public class IntegerIndexBufferObject {
    final IntBuffer buffer;
    final ByteBuffer byteBuffer;
    final boolean ownsBuffer;
    int bufferHandle;
    final boolean isDirect;
    boolean isDirty = true;
    boolean isBound = false;
    final int usage;

    // used to work around bug: https://android-review.googlesource.com/#/c/73175/
    private final boolean empty;

    /**
     * Creates a new static IndexBufferObject to be used with vertex arrays.
     *
     * @param maxIndices the maximum number of indices this buffer can hold
     */
    public IntegerIndexBufferObject(int maxIndices) {
        this(true, maxIndices);
    }

    /**
     * Creates a new IndexBufferObject.
     *
     * @param isStatic   whether the index buffer is static
     * @param maxIndices the maximum number of indices this buffer can hold
     */
    public IntegerIndexBufferObject(boolean isStatic, int maxIndices) {

        empty = maxIndices == 0;
        if (empty) {
            maxIndices = 1; // avoid allocating a zero-sized buffer because of bug in Android's ART < Android 5.0
        }

        byteBuffer = BufferUtils.newUnsafeByteBuffer(maxIndices * Integer.BYTES);
        isDirect = true;

        buffer = byteBuffer.asIntBuffer();
        ownsBuffer = true;
        ((Buffer) buffer).flip();
        ((Buffer) byteBuffer).flip();
        bufferHandle = Gdx.gl32.glGenBuffer();
        usage = isStatic ? GL32.GL_STATIC_DRAW : GL32.GL_DYNAMIC_DRAW;
    }

    public IntegerIndexBufferObject(boolean isStatic, ByteBuffer data) {

        empty = data.limit() == 0;
        byteBuffer = data;
        isDirect = true;

        buffer = byteBuffer.asIntBuffer();
        ownsBuffer = false;
        bufferHandle = Gdx.gl32.glGenBuffer();
        usage = isStatic ? GL32.GL_STATIC_DRAW : GL32.GL_DYNAMIC_DRAW;
    }

    /**
     * @return the number of indices currently stored in this buffer
     */
    public int getNumIndices() {
        return empty ? 0 : buffer.limit();
    }

    /**
     * @return the maximum number of indices this IndexBufferObject can store.
     */
    public int getNumMaxIndices() {
        return empty ? 0 : buffer.capacity();
    }

    /**
     * <p>
     * Sets the indices of this IndexBufferObject, discarding the old indices. The count must equal the number of indices to be
     * copied to this IndexBufferObject.
     * </p>
     *
     * <p>
     * This can be called in between calls to {@link #bind()} and {@link #unbind()}. The index data will be updated instantly.
     * </p>
     *
     * @param indices the vertex data
     * @param offset  the offset to start copying the data from
     * @param count   the number of ints to copy
     */
    public void setIndices(int[] indices, int offset, int count) {
        isDirty = true;
        ((Buffer) buffer).clear();
        buffer.put(indices, offset, count);
        ((Buffer) buffer).flip();
        ((Buffer) byteBuffer).position(0);
        ((Buffer) byteBuffer).limit(count << 1);

        if (isBound) {
            Gdx.gl32.glBufferData(GL32.GL_ELEMENT_ARRAY_BUFFER, byteBuffer.limit(), byteBuffer, usage);
            isDirty = false;
        }
    }

    public void setIndices(IntBuffer indices) {
        isDirty = true;
        int pos = indices.position();
        ((Buffer) buffer).clear();
        buffer.put(indices);
        ((Buffer) buffer).flip();
        ((Buffer) indices).position(pos);
        ((Buffer) byteBuffer).position(0);
        ((Buffer) byteBuffer).limit(buffer.limit() << 1);

        if (isBound) {
            Gdx.gl32.glBufferData(GL32.GL_ELEMENT_ARRAY_BUFFER, byteBuffer.limit(), byteBuffer, usage);
            isDirty = false;
        }
    }

    public void updateIndices(int targetOffset, int[] indices, int offset, int count) {
        isDirty = true;
        final int pos = byteBuffer.position();
        ((Buffer) byteBuffer).position(targetOffset * Integer.BYTES);
        BufferUtils.copy(indices, offset, byteBuffer, count);
        ((Buffer) byteBuffer).position(pos);
        ((Buffer) buffer).position(0);

        if (isBound) {
            Gdx.gl32.glBufferData(GL32.GL_ELEMENT_ARRAY_BUFFER, byteBuffer.limit(), byteBuffer, usage);
            isDirty = false;
        }
    }

    /**
     * @deprecated use {@link #getBuffer(boolean)} instead
     */

    @Deprecated
    public IntBuffer getBuffer() {
        isDirty = true;
        return buffer;
    }

    public IntBuffer getBuffer(boolean forWriting) {
        isDirty |= forWriting;
        return buffer;
    }

    /**
     * Binds this IndexBufferObject for rendering with glDrawElements.
     */
    public void bind() {
        if (bufferHandle == 0) throw new GdxRuntimeException("No buffer allocated!");

        Gdx.gl32.glBindBuffer(GL32.GL_ELEMENT_ARRAY_BUFFER, bufferHandle);
        if (isDirty) {
            ((Buffer) byteBuffer).limit(buffer.limit() * Integer.BYTES);
            Gdx.gl32.glBufferData(GL32.GL_ELEMENT_ARRAY_BUFFER, byteBuffer.limit(), byteBuffer, usage);
            isDirty = false;
        }
        isBound = true;
    }

    /**
     * Unbinds this IndexBufferObject.
     */
    public void unbind() {
        Gdx.gl32.glBindBuffer(GL32.GL_ELEMENT_ARRAY_BUFFER, 0);
        isBound = false;
    }

    /**
     * Invalidates the IndexBufferObject so a new OpenGL buffer handle is created. Use this in case of a context loss.
     */
    public void invalidate() {
        bufferHandle = Gdx.gl32.glGenBuffer();
        isDirty = true;
    }

    /**
     * Disposes this IndexBufferObject and all its associated OpenGL resources.
     */
    public void dispose() {
        Gdx.gl32.glBindBuffer(GL32.GL_ELEMENT_ARRAY_BUFFER, 0);
        Gdx.gl32.glDeleteBuffer(bufferHandle);
        bufferHandle = 0;

        if (ownsBuffer) {
            BufferUtils.disposeUnsafeByteBuffer(byteBuffer);
        }
    }
}
