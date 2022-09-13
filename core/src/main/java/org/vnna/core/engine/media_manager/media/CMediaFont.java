package org.vnna.core.engine.media_manager.media;

import java.io.Serializable;

public class CMediaFont extends CMedia implements Serializable {

    public int offset_x, offset_y;

    public CMediaFont(String filename) {
        super(filename);
    }

}
