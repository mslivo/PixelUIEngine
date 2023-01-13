package org.mslivo.core.engine.media_manager.media;

import java.io.Serializable;

public class CMediaImageCursor extends CMediaCursor implements Serializable {

    public int hotspot_x;

    public int hotspot_y;

    public CMediaImageCursor(String filename) {
        super(filename);
    }

}
