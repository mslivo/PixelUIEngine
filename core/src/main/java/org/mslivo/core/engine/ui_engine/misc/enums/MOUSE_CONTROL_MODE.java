package org.mslivo.core.engine.ui_engine.misc.enums;

public enum MOUSE_CONTROL_MODE {
    HARDWARE_MOUSE("Mouse"), KEYBOARD("Keyboard"), GAMEPAD("Gamepad"), DISABLED("Disabled");

    public final String text;
    MOUSE_CONTROL_MODE(String text){
        this.text = text;
    }
}
