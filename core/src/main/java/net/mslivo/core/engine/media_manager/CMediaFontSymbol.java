package net.mslivo.core.engine.media_manager;

import java.io.Serializable;

public final class CMediaFontSymbol implements Serializable {

    public final int id;
    public final String file;

    CMediaFontSymbol(int id, String file) {
        this.id = id;
        this.file = file;
    }
}
