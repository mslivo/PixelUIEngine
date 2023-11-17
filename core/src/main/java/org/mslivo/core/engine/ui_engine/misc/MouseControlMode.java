package org.mslivo.core.engine.ui_engine.misc;

public enum MouseControlMode {
    HARDWARE_MOUSE("Mouse"), KEYBOARD("Keyboard"), GAMEPAD("Gamepad"), DISABLED("Disabled");

    public final String text;
    MouseControlMode(String text){
        this.text = text;
    }
}
