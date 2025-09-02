package net.mslivo.pixelui.media_manager;

import java.io.Serializable;

public final class CMediaMusic extends CMediaSound implements Serializable {

    public CMediaMusic() {
    }

    public CMediaMusic(String filename) {
        super(filename);
    }

    public CMediaMusic copy(){
        CMediaMusic copy = new CMediaMusic();
        return copy;
    }

}
