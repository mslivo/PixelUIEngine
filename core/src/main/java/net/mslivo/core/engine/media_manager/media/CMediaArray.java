package net.mslivo.core.engine.media_manager.media;

import java.io.Serializable;

public class CMediaArray extends CMediaGFX implements Serializable {

    public int tile_width;

    public int tile_height;

    public int frameOffset;

    public int frameLength;

    public CMediaArray(String filename) {
        super(filename);
    }

}
