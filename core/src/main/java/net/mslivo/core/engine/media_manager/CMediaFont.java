package net.mslivo.core.engine.media_manager;

import java.io.Serializable;

public final class CMediaFont extends CMedia implements Serializable {

    public final int offset_x;
    public final int offset_y;

    CMediaFont(String file, int offset_x, int offset_y) {
        super(file);
        this.offset_x = offset_x;
        this.offset_y = offset_y;
    }
}
