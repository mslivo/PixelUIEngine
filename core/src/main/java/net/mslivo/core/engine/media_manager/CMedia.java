package net.mslivo.core.engine.media_manager;

import java.io.Serializable;
import java.util.Objects;

public abstract class CMedia implements Serializable {
    public final String file;
    private int mediaManagerIndex;
    private final int hash;

    CMedia(String file) {
        this.file = file;
        this.mediaManagerIndex = MediaManager.MEDIAMANGER_INDEX_NONE;
        this.hash = Objects.hashCode(this);
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        return o.hashCode() == this.hash;
    }

    @Override
    public int hashCode() {
        return hash;
    }


}