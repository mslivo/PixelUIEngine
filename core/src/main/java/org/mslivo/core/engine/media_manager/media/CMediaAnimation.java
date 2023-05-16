package org.mslivo.core.engine.media_manager.media;

import java.io.Serializable;

public class CMediaAnimation extends CMediaGFX implements Serializable {

    public int tile_width;

    public int tile_height;

    public float animation_speed;
    public int frameOffset;

    public int frameLength;
    public CMediaAnimation(String filename) {
        super(filename);
    }

}