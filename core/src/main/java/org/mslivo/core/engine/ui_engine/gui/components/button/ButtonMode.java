package org.mslivo.core.engine.ui_engine.gui.components.button;

public enum ButtonMode {
    DEFAULT("Default"),HOLD("Hold"),TOGGLE("Toggle");
    private final String text;
    ButtonMode(String text) {
        this.text = text;
    }
}
