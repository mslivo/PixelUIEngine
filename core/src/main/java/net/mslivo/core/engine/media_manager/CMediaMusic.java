package net.mslivo.core.engine.media_manager;

import java.io.Serializable;
import java.util.Objects;

public final class CMediaMusic extends CMediaSound implements Serializable {

    public CMediaMusic() {
    }

    public CMediaMusic(String filename) {
        super(filename);
    }

}
