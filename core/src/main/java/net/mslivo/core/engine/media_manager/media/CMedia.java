package net.mslivo.core.engine.media_manager.media;

import java.io.Serializable;
import java.util.Objects;

public abstract class CMedia implements Serializable {
    public static final int MEDIAMANGER_INDEX_NONE = -1;

    public final String file;
    private final int hash;
    public int mediaManagerIndex;

    protected CMedia(String file) {
        this.file = file;
        this.hash = Objects.hash(file);
        this.mediaManagerIndex = MEDIAMANGER_INDEX_NONE;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CMedia cMedia = (CMedia) o;
        return cMedia.hash == this.hash;
    }

    @Override
    public int hashCode() {
        return hash;
    }
}