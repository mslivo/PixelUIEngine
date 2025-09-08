package net.mslivo.pixelui.media;

import java.io.Serializable;

public final class CMediaSoundEffect extends CMediaSound implements Serializable {

    public CMediaSoundEffect(){
        super();
    }

    public CMediaSoundEffect(String filename) {
        super(filename);
    }

    public CMediaSoundEffect copy(){
        CMediaSoundEffect copy = new CMediaSoundEffect();
        copy.copyFields(this);
        return copy;
    }


}
