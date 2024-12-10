package net.mslivo.core.engine.media_manager;

import java.io.Serializable;
import java.util.Objects;

public final class CMediaImage extends CMediaSprite implements Serializable {

    public CMediaImage(){
    }

    public CMediaImage(String filename) {
        super(filename);
    }

}
