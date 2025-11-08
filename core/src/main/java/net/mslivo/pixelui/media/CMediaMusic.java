package net.mslivo.pixelui.media;

import net.mslivo.pixelui.utils.misc.Copyable;

public final class CMediaMusic extends CMediaSound implements Copyable<CMediaMusic> {

    public CMediaMusic() {
        super();
    }

    public CMediaMusic(String filename) {
        super(filename);
    }

    @Override
    public CMediaMusic copy() {
        CMediaMusic copy = new CMediaMusic();
        return copy;
    }

}
