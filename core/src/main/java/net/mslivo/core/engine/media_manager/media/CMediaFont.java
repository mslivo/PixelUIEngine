package net.mslivo.core.engine.media_manager.media;

import java.io.Serializable;

public class CMediaFont extends CMedia implements Serializable {

    public int offset_x;
    public int offset_y;

    public CMediaFont(String filename) {
        super(filename);
    }

}
