package net.mslivo.core.engine.ui_engine.ui.actions;

/**
 * Created by Admin on 16.03.2019.
 */
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
        this.timer = updateOnInit ? 0 : interval;
    }

    public void onUpdate() {
    }

}
