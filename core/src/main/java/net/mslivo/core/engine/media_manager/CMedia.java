package net.mslivo.core.engine.media_manager;

import java.io.Serializable;
import java.util.Objects;

public abstract class CMedia implements Serializable {
    public final String file;
    private int mediaManagerIndex;

    CMedia(String file) {
        this.file = file;
        this.mediaManagerIndex = MediaManager.MEDIAMANGER_INDEX_NONE;
    }

    public void setMediaManagerIndex(int mediaManagerIndex) {
        this.mediaManagerIndex = mediaManagerIndex;
    }

    public int mediaManagerIndex() {
        return mediaManagerIndex;
    }

    public String file() {
        return file;
    }

}