package net.mslivo.core.engine.media_manager;

import java.io.Serializable;
import java.util.Objects;

public abstract sealed class CMedia implements Serializable permits CMediaFont, CMediaSound, CMediaSprite {
    public String file;

    public CMedia(){
    }

    CMedia(String file) {
        this.file = file;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        CMedia cMedia = (CMedia) o;
        return Objects.equals(file, cMedia.file);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(file);
    }
}