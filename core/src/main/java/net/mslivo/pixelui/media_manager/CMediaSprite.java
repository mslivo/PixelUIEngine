package net.mslivo.pixelui.media_manager;

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

    public CMediaSprite copy(){
        CMediaSprite copy = switch (this){
            case CMediaAnimation cMediaAnimation -> cMediaAnimation.copy();
            case CMediaArray cMediaArray -> cMediaArray.copy();
            case CMediaImage cMediaImage -> cMediaImage.copy();
        };
        return copy;
    }

    protected void copyFields(CMediaSprite copyFrom){
        super.copyFields(copyFrom);
        this.useAtlas = copyFrom.useAtlas;
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) return false;
        if (!super.equals(object)) return false;

        CMediaSprite that = (CMediaSprite) object;
        return useAtlas == that.useAtlas;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + Boolean.hashCode(useAtlas);
        return result;
    }

}
