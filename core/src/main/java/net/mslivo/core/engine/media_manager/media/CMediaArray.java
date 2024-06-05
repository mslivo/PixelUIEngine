package net.mslivo.core.engine.media_manager.media;

import java.io.Serializable;

public class CMediaArray extends CMediaSprite implements Serializable {

    public final int regionWidth;
    public final int regionHeight;
    public final int frameOffset;
    public final int frameLength;

    public CMediaArray(String filename, int regionWidth, int regionHeight, int frameOffset, int frameLength) {
        super(filename);
        this.regionWidth = regionWidth;
        this.regionHeight = regionHeight;
        this.frameOffset = frameOffset;
        this.frameLength = frameLength;
    }
}
