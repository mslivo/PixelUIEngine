package net.mslivo.pixelui.utils.rendering;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;

/**
 * Reflection Free Alternative to Libgdx Animation, supporting a stateTimeOffset
 */
public final class ExtendedAnimation {

    private final TextureRegion[] keyFrames;
    private float animationDuration;
    private float animationSpeed;
    private float stateTimeOffset;
    private int lastFrameNumber;
    private float lastStateTime;
    private PlayMode playMode;

    public enum PlayMode {
        NORMAL, REVERSED, LOOP, LOOP_REVERSED, LOOP_PINGPONG, LOOP_RANDOM,
    }

    public ExtendedAnimation(ExtendedAnimation other) {
        this.keyFrames = new TextureRegion[other.keyFrames.length];
        for (int i = 0; i < this.keyFrames.length; i++)
            this.keyFrames[i] = new TextureRegion(other.keyFrames[i]);
        this.animationDuration = other.animationDuration;
        this.animationSpeed = other.animationSpeed;
        this.stateTimeOffset = other.stateTimeOffset;
        this.lastFrameNumber = other.lastFrameNumber;
        this.lastStateTime = other.lastStateTime;
        this.playMode = other.playMode;
    }

    public ExtendedAnimation(float animationSpeed, TextureRegion[] keyFrames, PlayMode playMode) {
        this.keyFrames = new TextureRegion[keyFrames.length];
        for (int i = 0; i < keyFrames.length; i++)
            this.keyFrames[i] = new TextureRegion(keyFrames[i]);
        this.playMode = playMode;
        this.animationSpeed = animationSpeed;
        this.animationDuration = this.keyFrames.length * animationSpeed;
        this.stateTimeOffset = 0;
    }

    public ExtendedAnimation(float animationSpeed, Array<TextureRegion> keyFrames, PlayMode playMode) {
        this.keyFrames = new TextureRegion[keyFrames.size];
        for (int i = 0; i < keyFrames.size; i++)
            this.keyFrames[i] = keyFrames.get(i);

        this.playMode = playMode;
        this.animationSpeed = animationSpeed;
        this.animationDuration = this.keyFrames.length * animationSpeed;
        this.stateTimeOffset = 0;
    }

    public TextureRegion getKeyFrame(float stateTime) {
        return keyFrames[getKeyFrameIndex(stateTime)];
    }

    public int getKeyFrameIndex(float stateTime) {
        if (keyFrames.length == 1) return 0;

        stateTime = Math.max(stateTime - this.stateTimeOffset, 0);

        int frameNumber = (int) (stateTime / animationSpeed);
        switch (playMode) {
            case NORMAL:
                frameNumber = Math.min(keyFrames.length - 1, frameNumber);
                break;
            case LOOP:
                frameNumber = frameNumber % keyFrames.length;
                break;
            case LOOP_PINGPONG:
                frameNumber = frameNumber % ((keyFrames.length * 2) - 2);
                if (frameNumber >= keyFrames.length)
                    frameNumber = keyFrames.length - 2 - (frameNumber - keyFrames.length);
                break;
            case LOOP_RANDOM:
                int lastFrameNumber = (int) ((lastStateTime) / animationSpeed);
                if (lastFrameNumber != frameNumber) {
                    frameNumber = MathUtils.random(keyFrames.length - 1);
                } else {
                    frameNumber = this.lastFrameNumber;
                }
                break;
            case REVERSED:
                frameNumber = Math.max(keyFrames.length - frameNumber - 1, 0);
                break;
            case LOOP_REVERSED:
                frameNumber = frameNumber % keyFrames.length;
                frameNumber = keyFrames.length - frameNumber - 1;
                break;
        }

        lastFrameNumber = frameNumber;
        lastStateTime = stateTime;
        return frameNumber;
    }

    public PlayMode getPlayMode() {
        return playMode;
    }

    public void setPlayMode(PlayMode playMode) {
        this.playMode = playMode;
    }

    public boolean isAnimationFinished(float stateTime) {
        int frameNumber = (int) (stateTime / animationSpeed);
        return keyFrames.length - 1 < frameNumber;
    }

    public void setAnimationSpeed(float animationSpeed) {
        this.animationSpeed = animationSpeed;
        this.animationDuration = keyFrames.length * animationSpeed;
    }

    public float getAnimationSpeed() {
        return animationSpeed;
    }

    public float getAnimationDuration() {
        return animationDuration;
    }

    public float getStateTimeOffset() {
        return stateTimeOffset;
    }

    public void setStateTimeOffset(float stateTimeOffset) {
        this.stateTimeOffset = stateTimeOffset;
    }
}
