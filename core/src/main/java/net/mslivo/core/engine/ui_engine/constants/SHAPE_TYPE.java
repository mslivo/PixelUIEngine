package net.mslivo.core.engine.ui_engine.constants;

public enum SHAPE_TYPE {
    RECT("Rect"),
    DIAMOND("Diamond"),
    OVAL("Oval"),
    RIGHT_TRIANGLE("Right Triangle"),
    ISOSCELES_TRIANGLE("Isosceles Triangle");

    public final String text;

    SHAPE_TYPE(String text) {
        this.text = text;
    }
}
