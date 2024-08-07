package net.mslivo.core.engine.ui_engine.constants;

public enum INPUT_METHOD {
    HARDWARE_MOUSE("Mouse"),
    KEYBOARD("Keyboard"),
    GAMEPAD("Gamepad"),
    NONE("None");

    public final String text;

    INPUT_METHOD(String text) {
        this.text = text;
    }
}
