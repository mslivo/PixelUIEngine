package net.mslivo.core.engine.tools.transitions;

public enum TRANSITION_SPEED {
    IMMEDIATE(0f),
    HALF(0.5f),
    X1(1.0f), X2(2.0f), X3(3.0f);

    public final float value;

    TRANSITION_SPEED(float value) {
        this.value = value;
    }
}
