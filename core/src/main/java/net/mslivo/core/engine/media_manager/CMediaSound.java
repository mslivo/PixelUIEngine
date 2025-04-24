package net.mslivo.core.engine.media_manager;

import java.io.Serializable;

public sealed abstract class CMediaSound extends CMedia implements Serializable permits CMediaSoundEffect, CMediaMusic {

    public CMediaSound(){
    }

    public CMediaSound(CMediaSound other) {
        super(other);
    }

    public CMediaSound(String filename) {
        super(filename);
    }

    public static CMediaSound copyOf(CMediaSound cMediaSound){
        return switch (cMediaSound){
            case CMediaMusic cMediaMusic -> new CMediaMusic(cMediaMusic);
            case CMediaSoundEffect cMediaSoundEffect -> new CMediaSoundEffect(cMediaSoundEffect);
        };
    }

}
