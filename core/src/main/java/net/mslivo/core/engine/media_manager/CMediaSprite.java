package net.mslivo.core.engine.media_manager;

import java.io.Serializable;

public sealed abstract class CMediaSprite extends CMedia implements Serializable permits CMediaImage, CMediaArray, CMediaAnimation {

    public CMediaSprite(){
        super();
    }

    public CMediaSprite(CMediaSprite other) {
        super(other);
        this.useAtlas = other.useAtlas;
    }

    public boolean useAtlas;

    public CMediaSprite(String filename, boolean useAtlas) {
        super(filename);
        this.useAtlas = useAtlas;
    }

    public static CMediaSprite copyOf(CMediaSprite cMediaSprite){
        return switch (cMediaSprite){
            case CMediaAnimation cMediaAnimation -> new CMediaAnimation(cMediaAnimation);
            case CMediaArray cMediaArray -> new CMediaArray(cMediaArray);
            case CMediaImage cMediaImage -> new CMediaImage(cMediaImage);
        };
    }
}
