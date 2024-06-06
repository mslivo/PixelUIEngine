package net.mslivo.core.engine.media_manager;

import java.io.Serializable;

public abstract class CMediaSprite extends CMedia implements Serializable {
    CMediaSprite(String filename) {
        super(filename);
    }
}
