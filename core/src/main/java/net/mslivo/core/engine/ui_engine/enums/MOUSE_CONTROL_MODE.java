package net.mslivo.core.engine.ui_engine.enums;

public enum MOUSE_CONTROL_MODE {
    HARDWARE_MOUSE("Mouse", false),
    KEYBOARD("Keyboard", true),
    GAMEPAD("Gamepad", true),
    DISABLED("Disabled", false);
    public final String text;
    public final boolean emulated;
    MOUSE_CONTROL_MODE(String text, boolean emulated){
        this.text = text;
        this.emulated = emulated;
    }
}
