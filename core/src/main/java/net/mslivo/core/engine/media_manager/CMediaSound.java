package net.mslivo.core.engine.media_manager;

import java.io.Serializable;

public sealed abstract class CMediaSound extends CMedia implements Serializable permits CMediaSoundEffect, CMediaMusic {

    CMediaSound(String filename) {
        super(filename);
    }

}
