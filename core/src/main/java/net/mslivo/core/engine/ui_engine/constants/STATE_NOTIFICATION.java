package net.mslivo.core.engine.ui_engine.constants;

public enum STATE_NOTIFICATION {
    INIT_SCROLL("Init Scroll"),
    INIT_DISPLAY("Init Display"),
    SCROLL("Scroll"),
    DISPLAY("Display"),
    FADEOUT("Fadeout"),
    FINISHED("Finished");

    public final String text;

    STATE_NOTIFICATION(String text) {
        this.text = text;
    }
}
