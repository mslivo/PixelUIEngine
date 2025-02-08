package net.mslivo.core.engine.ui_engine.constants;

public enum TOP_NOTIFICATION_STATE {
    INIT_SCROLL("Init Scroll"),
    INIT_DISPLAY("Init Display"),
    SCROLL("Scroll"),
    DISPLAY("Display"),
    FOLD("Fold"),
    FINISHED("Finished");

    public final String text;

    TOP_NOTIFICATION_STATE(String text) {
        this.text = text;
    }
}
