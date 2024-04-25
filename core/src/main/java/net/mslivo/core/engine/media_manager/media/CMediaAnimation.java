package net.mslivo.core.engine.media_manager.media;

import com.badlogic.gdx.graphics.g2d.Animation;

import java.io.Serializable;

public class CMediaAnimation extends CMediaSprite implements Serializable {

    public int regionWidth;

    public int regionHeight;

    public float animation_speed;
    public int frameOffset;

    public int frameLength;
    public Animation.PlayMode playMode;
    public CMediaAnimation(String filename) {
        super(filename);
    }

}