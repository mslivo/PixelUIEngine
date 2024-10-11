package net.mslivo.core.engine.media_manager;

import java.io.Serializable;
import java.util.Objects;

public final class CMediaMusic extends CMedia implements Serializable {
    private final int hash;

    CMediaMusic(String filename) {
        super(filename);
        this.hash = Objects.hash(filename);
    }

}
