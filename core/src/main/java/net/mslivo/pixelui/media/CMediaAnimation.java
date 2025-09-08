package net.mslivo.pixelui.media;

import net.mslivo.pixelui.rendering.ExtendedAnimation;

import java.io.Serializable;

public final class CMediaAnimation extends CMediaSprite implements Serializable {

    public int frameWidth;
    public int frameHeight;
    public int frameOffset;
    public int frameLength;
    public float animationSpeed;
    public ExtendedAnimation.PlayMode playMode;

    public CMediaAnimation() {
        super();
        this.frameWidth = 0;
        this.frameHeight = 0;
        this.frameOffset = 0;
        this.frameLength = Integer.MAX_VALUE;
        this.animationSpeed = 0f;
        this.playMode = ExtendedAnimation.PlayMode.LOOP;
    }

    public CMediaAnimation(String file, int tileWidth, int tileHeight) {
        this(file, tileWidth, tileHeight, 0.1f, 0, Integer.MAX_VALUE, ExtendedAnimation.PlayMode.LOOP, true);
    }

    public CMediaAnimation(String file, int tileWidth, int tileHeight, float animationSpeed) {
        this(file, tileWidth, tileHeight, animationSpeed, 0, Integer.MAX_VALUE, ExtendedAnimation.PlayMode.LOOP, true);
    }

    public CMediaAnimation(String file, int tileWidth, int tileHeight, float animation_speed, int frameOffset, int frameLength) {
        this(file, tileWidth, tileHeight, animation_speed, frameOffset, frameLength, ExtendedAnimation.PlayMode.LOOP, true);
    }

    public CMediaAnimation(String filename, int frameWidth, int frameHeight, float animationSpeed, int frameOffset, int frameLength, ExtendedAnimation.PlayMode playMode) {
        this(filename, frameWidth, frameHeight, animationSpeed, frameOffset, frameLength, playMode, true);
    }

    public CMediaAnimation(String filename, int frameWidth, int frameHeight, float animationSpeed, int frameOffset, int frameLength, ExtendedAnimation.PlayMode playMode, boolean useAtlas) {
        super(filename, useAtlas);
        this.frameWidth = frameWidth;
        this.frameHeight = frameHeight;
        this.animationSpeed = animationSpeed;
        this.frameOffset = frameOffset;
        this.frameLength = frameLength;
        this.playMode = playMode;
    }

    public CMediaAnimation copy() {
        CMediaAnimation copy = new CMediaAnimation();
        copy.copyFields(this);
        copy.frameWidth = this.frameWidth;
        copy.frameHeight = this.frameHeight;
        copy.animationSpeed = this.animationSpeed;
        copy.frameOffset = this.frameOffset;
        copy.frameLength = this.frameLength;
        copy.playMode = this.playMode;
        return copy;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        CMediaAnimation that = (CMediaAnimation) o;
        return frameWidth == that.frameWidth && frameHeight == that.frameHeight && Float.compare(animationSpeed, that.animationSpeed) == 0 && frameOffset == that.frameOffset && frameLength == that.frameLength && playMode == that.playMode;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + frameWidth;
        result = 31 * result + frameHeight;
        result = 31 * result + Float.hashCode(animationSpeed);
        result = 31 * result + frameOffset;
        result = 31 * result + frameLength;
        result = 31 * result + playMode.hashCode();
        return result;
    }
}