package net.mslivo.core.engine.media_manager.media;

import com.badlogic.gdx.graphics.g2d.Animation;

import java.io.Serializable;

public class CMediaAnimation extends CMediaSprite implements Serializable {
    public final int regionWidth;
    public final int regionHeight;
    public final float animation_speed;
    public final int frameOffset;
    public final int frameLength;
    public final Animation.PlayMode playMode;

    public CMediaAnimation(String filename, int regionWidth, int regionHeight, float animation_speed, int frameOffset, int frameLength, Animation.PlayMode playMode) {
        super(filename);
        this.regionWidth = regionWidth;
        this.regionHeight = regionHeight;
        this.animation_speed = animation_speed;
        this.frameOffset = frameOffset;
        this.frameLength = frameLength;
        this.playMode = playMode;
    }

}