package net.mslivo.pixelui.engine.actions.common;

public abstract class UpdateAction {

    public final int interval;

    public long timer;

    public UpdateAction() {
        this(0, false);
    }

    public UpdateAction(int interval) {
        this(interval, false);
    }

    public UpdateAction(int interval, boolean updateOnInit) {
        this.interval = interval;
        this.timer = updateOnInit ? interval: 0 ;
    }

    public void onUpdate() {
    }

}
