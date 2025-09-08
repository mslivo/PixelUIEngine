package net.mslivo.pixelui.media;

import java.io.Serializable;

public final class CMediaMusic extends CMediaSound implements Serializable {

    public CMediaMusic() {
        super();
    }

    public CMediaMusic(String filename) {
        super(filename);
    }

    public CMediaMusic copy(){
        CMediaMusic copy = new CMediaMusic();
        return copy;
    }

}
