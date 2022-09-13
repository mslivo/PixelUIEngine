package org.vnna.core.engine.ui_engine.gui;

import org.vnna.core.engine.ui_engine.API;

public abstract class WindowGenerator {

    protected API api;

    public WindowGenerator(API api){
        this.api = api;
    }

    public abstract Window create(Object[] p);

}
