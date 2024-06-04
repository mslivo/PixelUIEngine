package net.mslivo.core.engine.media_manager.media;

import java.io.Serializable;

public class CMediaCursor extends CMediaSprite implements Serializable {
    public int hotspot_x;
    public int hotspot_y;

    public CMediaCursor(String filename) {
        super(filename);
    }

}
