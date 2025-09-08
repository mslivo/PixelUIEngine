package net.mslivo.pixelui.media;

import java.io.Serializable;

public final class CMediaArray extends CMediaSprite implements Serializable {
    public int regionWidth;
    public int regionHeight;
    public int frameOffset;
    public int frameLength;

    public CMediaArray() {
        super();
        this.regionWidth = 0;
        this.regionHeight = 0;
        this.frameOffset = 0;
        this.frameLength = 0;
    }

    public CMediaArray(String file, int tileWidth, int tileHeight) {
        this(file, tileWidth, tileHeight, 0, Integer.MAX_VALUE, true);
    }

    public CMediaArray(String file, int regionWidth, int regionHeight, int frameOffset, int frameLength) {
        this(file, regionWidth, regionHeight, frameOffset, frameLength, true);
    }

    public CMediaArray(String file, int regionWidth, int regionHeight, int frameOffset, int frameLength, boolean useAtlas) {
        super(file, useAtlas);
        this.regionWidth = regionWidth;
        this.regionHeight = regionHeight;
        this.frameOffset = frameOffset;
        this.frameLength = frameLength;
    }

    public CMediaArray copy() {
        CMediaArray copy = new CMediaArray();
        copy.copyFields(this);
        copy.regionWidth = this.regionWidth;
        copy.regionHeight = this.regionHeight;
        copy.frameOffset = this.frameOffset;
        copy.frameLength = this.frameLength;
        return copy;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        CMediaArray that = (CMediaArray) o;
        return regionWidth == that.regionWidth && regionHeight == that.regionHeight && frameOffset == that.frameOffset && frameLength == that.frameLength;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + regionWidth;
        result = 31 * result + regionHeight;
        result = 31 * result + frameOffset;
        result = 31 * result + frameLength;
        return result;
    }
}
