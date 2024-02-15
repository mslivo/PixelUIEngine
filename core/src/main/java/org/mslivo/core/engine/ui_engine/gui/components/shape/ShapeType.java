package org.mslivo.core.engine.ui_engine.gui.components.shape;

public enum ShapeType {
    RECT("Rect"), DIAMOND("Diamond"), OVAL("Oval"),
    TRIANGLE_LEFT_DOWN("Trangle"), TRIANGLE_RIGHT_DOWN("Triangle"), TRIANGLE_LEFT_UP("Triangle"), TRIANGLE_RIGHT_UP("Triangle");

    public final String text;

    ShapeType(String text) {
        this.text = text;
    }
}
