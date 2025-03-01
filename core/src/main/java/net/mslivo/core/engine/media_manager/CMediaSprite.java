package net.mslivo.core.engine.media_manager;

import java.io.Serializable;

public sealed abstract class CMediaSprite extends CMedia implements Serializable permits CMediaImage, CMediaArray, CMediaAnimation {

    public CMediaSprite(){
        super();
    }

    public boolean useAtlas;

    public CMediaSprite(String filename, boolean useAtlas) {
        super(filename);
        this.useAtlas = useAtlas;
    }

}
