package net.mslivo.core.engine.media_manager;

import java.io.Serializable;
import java.util.Objects;

public final class CMediaFontSymbol implements Serializable {
    private final int hash;
    public final int id;
    public final String file;

    CMediaFontSymbol(int id, String file) {
        this.id = id;
        this.file = file;
        this.hash = Objects.hash(id, file);
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
