package net.mslivo.core.engine.media_manager;

import net.mslivo.core.engine.ui_engine.rendering.ExtendedAnimation;

import java.io.Serializable;

public final class CMediaAnimation extends CMediaSprite implements Serializable {
    public final int regionWidth;
    public final int regionHeight;
    public final float animation_speed;
    public final int frameOffset;
    public final int frameLength;
    public final ExtendedAnimation.PlayMode playMode;

    CMediaAnimation(String filename, int regionWidth, int regionHeight, float animation_speed, int frameOffset, int frameLength, ExtendedAnimation.PlayMode playMode) {
        super(filename);
        this.regionWidth = regionWidth;
        this.regionHeight = regionHeight;
        this.animation_speed = animation_speed;
        this.frameOffset = frameOffset;
        this.frameLength = frameLength;
        this.playMode = playMode;
    }

}