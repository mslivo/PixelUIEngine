package net.mslivo.pixelui.utils.transitions;

public enum TRANSITION_SPEED {
    IMMEDIATE(0f),
    VERY_SLOW(0.25f),
    SLOW(0.5f),
    DEFAULT(1.0f),
    FAST(2.0f),
    VERY_FAST(3.0f),
    FASTEST(4.0f);

    public final float value;

    TRANSITION_SPEED(float value) {
        this.value = value;
    }
}
