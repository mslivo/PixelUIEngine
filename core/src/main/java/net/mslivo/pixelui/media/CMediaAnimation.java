package net.mslivo.pixelui.media;

import net.mslivo.pixelui.rendering.ExtendedAnimation;

import java.io.Serializable;

public final class CMediaAnimation extends CMediaSprite implements Serializable {

    public int regionWidth;
    public int regionHeight;
    public int frameOffset;
    public int frameLength;
    public float animationSpeed;
    public ExtendedAnimation.PlayMode playMode;

    public CMediaAnimation() {
        super();
        this.regionWidth = 0;
        this.regionHeight = 0;
        this.frameOffset = 0;
        this.frameLength = 0;
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

    public CMediaAnimation(String filename, int regionWidth, int regionHeight, float animationSpeed, int frameOffset, int frameLength, ExtendedAnimation.PlayMode playMode) {
        this(filename, regionWidth, regionHeight, animationSpeed, frameOffset, frameLength, playMode, true);
    }

    public CMediaAnimation(String filename, int regionWidth, int regionHeight, float animationSpeed, int frameOffset, int frameLength, ExtendedAnimation.PlayMode playMode, boolean useAtlas) {
        super(filename, useAtlas);
        this.regionWidth = regionWidth;
        this.regionHeight = regionHeight;
        this.animationSpeed = animationSpeed;
        this.frameOffset = frameOffset;
        this.frameLength = frameLength;
        this.playMode = playMode;
    }

    public CMediaAnimation copy() {
        CMediaAnimation copy = new CMediaAnimation();
        copy.copyFields(this);
        copy.regionWidth = this.regionWidth;
        copy.regionHeight = this.regionHeight;
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
        return regionWidth == that.regionWidth && regionHeight == that.regionHeight && Float.compare(animationSpeed, that.animationSpeed) == 0 && frameOffset == that.frameOffset && frameLength == that.frameLength && playMode == that.playMode;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + regionWidth;
        result = 31 * result + regionHeight;
        result = 31 * result + Float.hashCode(animationSpeed);
        result = 31 * result + frameOffset;
        result = 31 * result + frameLength;
        result = 31 * result + playMode.hashCode();
        return result;
    }
}