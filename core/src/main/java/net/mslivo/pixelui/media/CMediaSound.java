package net.mslivo.pixelui.media;

public sealed abstract class CMediaSound extends CMedia permits CMediaSoundEffect, CMediaMusic {

    public CMediaSound() {
        super();
    }

    public CMediaSound(String filename) {
        super(filename);
    }

    public CMediaSound copy() {
        CMediaSound copy = switch (this) {
            case CMediaMusic cMediaMusic -> cMediaMusic.copy();
            case CMediaSoundEffect cMediaSoundEffect -> cMediaSoundEffect.copy();
        };
        return copy;
    }

    protected void copyFields(CMediaSound copyFrom) {
        super.copyFields(copyFrom);
    }


}
