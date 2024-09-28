package net.mslivo.core.engine.media_manager;

import java.io.Serializable;
import java.util.Objects;

public final class CMediaFont extends CMedia implements Serializable {
    private final int hash;

    public final int offset_x;
    public final int offset_y;
    public final boolean markupEnabled;

    CMediaFont(String filename, int offset_x, int offset_y, boolean markupEnabled) {
        super(filename);
        this.offset_x = offset_x;
        this.offset_y = offset_y;
        this.markupEnabled = markupEnabled;
        this.hash = Objects.hash(filename, offset_x, offset_y, markupEnabled);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o instanceof CMediaFont cMediaFont) {
            return cMediaFont.hash == this.hash;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return hash;
    }
}
