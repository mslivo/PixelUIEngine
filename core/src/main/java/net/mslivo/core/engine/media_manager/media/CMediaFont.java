package net.mslivo.core.engine.media_manager.media;

import java.io.Serializable;

public class CMediaFont extends CMedia implements Serializable {

    public final int offset_x;
    public final int offset_y;

    public CMediaFont(String file, int offset_x, int offset_y) {
        super(file);
        this.offset_x = offset_x;
        this.offset_y = offset_y;
    }
}
