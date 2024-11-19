package net.mslivo.core.engine.media_manager;

import java.io.Serializable;
import java.util.Objects;

public sealed abstract class CMediaSprite extends CMedia implements Serializable permits CMediaImage, CMediaArray , CMediaAnimation {
    private final int hash;

    CMediaSprite(String filename) {
        super(filename);
        this.hash = Objects.hash(filename);
    }

}
