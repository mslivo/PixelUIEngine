package net.mslivo.core.engine.media_manager;

import java.io.Serializable;
import java.util.Objects;

public abstract class CMediaSprite extends CMedia implements Serializable {
    private final int hash;

    CMediaSprite(String filename) {
        super(filename);
        this.hash = Objects.hash(filename);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o instanceof CMediaSprite cMediaSprite) {
            return cMediaSprite.hash == this.hash;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return hash;
    }
}
