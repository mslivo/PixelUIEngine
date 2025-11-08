package net.mslivo.pixelui.media;

import net.mslivo.pixelui.utils.misc.Copyable;

public final class CMediaSoundEffect extends CMediaSound implements Copyable<CMediaSoundEffect> {

    public CMediaSoundEffect() {
        super();
    }

    public CMediaSoundEffect(String filename) {
        super(filename);
    }

    @Override
    public CMediaSoundEffect copy() {
        CMediaSoundEffect copy = new CMediaSoundEffect();
        copy.copyFields(this);
        return copy;
    }


}
