package org.vnna.core.engine.media_manager.media;

import java.io.Serializable;

public class CMediaArray extends CMediaGFX implements Serializable {

    public int tile_width;

    public int tile_height;

    public CMediaArray(String filename) {
        super(filename);
    }

}
