package net.mslivo.core.engine.ui_engine.constants;

public enum SHAPE_TYPE {
    RECT("Rect"),
    DIAMOND("Diamond"),
    OVAL("Oval"),
    TRIANGLE_LEFT_DOWN("Trangle"),
    TRIANGLE_RIGHT_DOWN("Triangle"),
    TRIANGLE_LEFT_UP("Triangle"),
    TRIANGLE_RIGHT_UP("Triangle");

    public final String text;

    SHAPE_TYPE(String text) {
        this.text = text;
    }
}
