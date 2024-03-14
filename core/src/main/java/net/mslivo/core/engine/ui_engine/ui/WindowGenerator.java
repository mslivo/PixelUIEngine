package net.mslivo.core.engine.ui_engine.ui;

import net.mslivo.core.engine.ui_engine.API;

public abstract class WindowGenerator {

    protected final API api;

    public WindowGenerator(API api){
        this.api = api;
    }

    public abstract Window create(Object[] p);

}
