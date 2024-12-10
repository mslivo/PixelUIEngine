package net.mslivo.core.engine.media_manager;

import java.io.Serializable;
import java.util.Objects;

public final class CMediaSoundEffect extends CMediaSound implements Serializable {

    public CMediaSoundEffect(){
    }

    public CMediaSoundEffect(String filename) {
        super(filename);
    }

}
