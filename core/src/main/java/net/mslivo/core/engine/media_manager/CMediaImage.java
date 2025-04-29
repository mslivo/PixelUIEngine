package net.mslivo.core.engine.media_manager;

import java.io.Serializable;
import java.util.Objects;

public final class CMediaImage extends CMediaSprite implements Serializable {

    public CMediaImage(){
    }

    public CMediaImage(String filename) {
        this(filename, true);
    }

    public CMediaImage(String filename, boolean useAtlas) {
        super(filename, useAtlas);
    }

    public CMediaImage copy(){
        CMediaImage copy = new CMediaImage();
        copy.copyFields(this);
        return copy;
    }

}
