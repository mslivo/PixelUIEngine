package net.mslivo.core.engine.media_manager;

import java.io.Serializable;
import java.util.Objects;

public final class CMediaImage extends CMediaSprite implements Serializable {
    private final int hash;

    CMediaImage(String filename) {
        super(filename);
        this.hash = Objects.hash(filename);
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
