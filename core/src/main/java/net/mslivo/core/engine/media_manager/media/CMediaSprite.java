package net.mslivo.core.engine.media_manager.media;

import java.io.Serializable;

public abstract class CMediaSprite extends CMedia implements Serializable {
    public CMediaSprite(String filename) {
        super(filename);
    }
}
