package net.mslivo.core.engine.ui_engine.constants;

public enum VIEWPORT_MODE {
    PIXEL_PERFECT("Pixel Perfect"),
    FIT("Fit"),
    STRETCH("Stretch");

    public final String text;

    VIEWPORT_MODE(String text) {
        this.text = text;
    }
}
