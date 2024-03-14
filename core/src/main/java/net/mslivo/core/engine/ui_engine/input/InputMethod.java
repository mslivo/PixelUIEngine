package net.mslivo.core.engine.ui_engine.input;

public enum InputMethod {
    HARDWARE_MOUSE("Mouse"), KEYBOARD("Keyboard"), GAMEPAD("Gamepad"),NONE("None");
    public final String text;
    InputMethod(String text){
        this.text = text;
    }
}
