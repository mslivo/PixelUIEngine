package net.mslivo.core.engine.media_manager;

import java.io.Serializable;

public sealed abstract class CMediaSound extends CMedia implements Serializable permits CMediaSoundEffect, CMediaMusic {

    public CMediaSound(){
    }

    public CMediaSound(String filename) {
        super(filename);
    }

    public CMediaSound copy(){
        CMediaSound copy =  switch (this){
            case CMediaMusic cMediaMusic -> cMediaMusic.copy();
            case CMediaSoundEffect cMediaSoundEffect -> cMediaSoundEffect.copy();
        };
        return copy;
    }

    protected void copyFields(CMediaSound copyFrom){
        super.copyFields(copyFrom);
    }



}
