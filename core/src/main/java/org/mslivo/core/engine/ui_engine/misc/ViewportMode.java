package org.mslivo.core.engine.ui_engine.misc;

public enum ViewportMode {
    PIXEL_PERFECT("Pixel Perfect"), FIT("Fit"), STRETCH("Stretch");

    public final String text;

    ViewportMode(String text){
        this.text = text;
    }

}
