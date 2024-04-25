package net.mslivo.core.engine.ui_engine.ui;

import net.mslivo.core.engine.ui_engine.API;

public interface WindowGenerator {

    Window create(API api, Object... params);

}
