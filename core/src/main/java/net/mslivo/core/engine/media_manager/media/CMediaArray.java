package net.mslivo.core.engine.media_manager.media;

import java.io.Serializable;

public class CMediaArray extends CMediaSprite implements Serializable {

    public int regionWidth;
    public int regionHeight;
    public int frameOffset;
    public int frameLength;

    public CMediaArray(String filename) {
        super(filename);
    }

}
