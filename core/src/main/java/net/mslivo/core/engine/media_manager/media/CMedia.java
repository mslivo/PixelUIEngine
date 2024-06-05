package net.mslivo.core.engine.media_manager.media;

import net.mslivo.core.engine.media_manager.MediaManager;

import java.io.Serializable;
import java.util.Objects;

public abstract class CMedia implements Serializable {
    public final String file;
    private int hash;
    private int mediaManagerIndex;

    protected CMedia(String file) {
        this.file = file;
        this.mediaManagerIndex = MediaManager.MEDIAMANGER_INDEX_NONE;
        this.hash = Objects.hash(file);
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
        if (o instanceof CMedia cMedia) {
            return cMedia.hash == this.hash;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return hash;
    }

}