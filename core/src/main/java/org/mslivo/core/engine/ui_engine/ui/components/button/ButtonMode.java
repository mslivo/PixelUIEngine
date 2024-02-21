package org.mslivo.core.engine.ui_engine.ui.components.button;

public enum ButtonMode {
    DEFAULT("Default"),HOLD("Hold"),TOGGLE("Toggle");
    private final String text;
    ButtonMode(String text) {
        this.text = text;
    }
}
