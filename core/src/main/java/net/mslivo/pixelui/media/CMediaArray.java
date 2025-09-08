package net.mslivo.pixelui.media;

import java.io.Serializable;

public final class CMediaArray extends CMediaSprite implements Serializable {
    public int frameWidth;
    public int frameHeight;
    public int frameOffset;
    public int frameLength;

    public CMediaArray() {
        super();
        this.frameWidth = 0;
        this.frameHeight = 0;
        this.frameOffset = 0;
        this.frameLength = Integer.MAX_VALUE;
    }

    public CMediaArray(String file, int frameWidth, int frameHeight) {
        this(file, frameWidth, frameHeight, 0, Integer.MAX_VALUE, true);
    }

    public CMediaArray(String file, int frameWidth, int frameHeight, int frameOffset, int frameLength) {
        this(file, frameWidth, frameHeight, frameOffset, frameLength, true);
    }

    public CMediaArray(String file, int frameWidth, int frameHeight, int frameOffset, int frameLength, boolean useAtlas) {
        super(file, useAtlas);
        this.frameWidth = frameWidth;
        this.frameHeight = frameHeight;
        this.frameOffset = frameOffset;
        this.frameLength = frameLength;
    }

    public CMediaArray copy() {
        CMediaArray copy = new CMediaArray();
        copy.copyFields(this);
        copy.frameWidth = this.frameWidth;
        copy.frameHeight = this.frameHeight;
        copy.frameOffset = this.frameOffset;
        copy.frameLength = this.frameLength;
        return copy;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        CMediaArray that = (CMediaArray) o;
        return frameWidth == that.frameWidth && frameHeight == that.frameHeight && frameOffset == that.frameOffset && frameLength == that.frameLength;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + frameWidth;
        result = 31 * result + frameHeight;
        result = 31 * result + frameOffset;
        result = 31 * result + frameLength;
        return result;
    }
}
