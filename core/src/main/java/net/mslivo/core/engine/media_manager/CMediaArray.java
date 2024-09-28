package net.mslivo.core.engine.media_manager;

import java.io.Serializable;
import java.util.Objects;

public final class CMediaArray extends CMediaSprite implements Serializable {
    private final int hash;

    public final int regionWidth;
    public final int regionHeight;
    public final int frameOffset;
    public final int frameLength;

    CMediaArray(String filename, int regionWidth, int regionHeight, int frameOffset, int frameLength) {
        super(filename);
        this.regionWidth = regionWidth;
        this.regionHeight = regionHeight;
        this.frameOffset = frameOffset;
        this.frameLength = frameLength;
        this.hash = Objects.hash(filename, regionWidth, regionHeight, frameOffset, frameLength);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o instanceof CMediaArray cMediaArray) {
            return cMediaArray.hash == this.hash;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return hash;
    }
}
