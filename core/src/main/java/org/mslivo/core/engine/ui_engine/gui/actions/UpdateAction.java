package org.mslivo.core.engine.ui_engine.gui.actions;

/**
 * Created by Admin on 16.03.2019.
 */
public abstract class UpdateAction {

    public int interval;

    public long lastUpdate;

    public UpdateAction() {
        this(0, false);
    }

    public UpdateAction(int interval) {
        this(interval, false);
    }

    public UpdateAction(int interval, boolean updateOnInit) {
        this.interval = interval;
        this.lastUpdate = updateOnInit ? 0 : System.currentTimeMillis();
    }

    public void onUpdate() {
    }

}
