package net.mslivo.core.engine.ui_engine.constants;

public enum VIEWPORT_MODE {
    PIXEL_PERFECT("Pixel Perfect", false),
    FIT("Fit", true),
    STRETCH("Stretch", true);

    public final String text;
    public final boolean upscale;

    VIEWPORT_MODE(String text, boolean upscale) {
        this.text = text;
        this.upscale= upscale;
    }
}
