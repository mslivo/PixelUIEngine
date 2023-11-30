package org.mslivo.core.engine.ui_engine.misc.render;

public enum ViewportMode {
    PIXEL_PERFECT("Pixel Perfect"), FIT("Fit"), STRETCH("Stretch");

    public final String text;

    ViewportMode(String text){
        this.text = text;
    }

}
