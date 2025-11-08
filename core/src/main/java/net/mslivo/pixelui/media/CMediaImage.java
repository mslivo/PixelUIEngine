package net.mslivo.pixelui.media;

import net.mslivo.pixelui.utils.misc.Copyable;

public final class CMediaImage extends CMediaSprite implements Copyable<CMediaImage> {

    public CMediaImage() {
        super();
    }

    public CMediaImage(String filename) {
        this(filename, true);
    }

    public CMediaImage(String filename, boolean useAtlas) {
        super(filename, useAtlas);
    }

    public CMediaImage copy() {
        CMediaImage copy = new CMediaImage();
        copy.copyFields(this);
        return copy;
    }

}
