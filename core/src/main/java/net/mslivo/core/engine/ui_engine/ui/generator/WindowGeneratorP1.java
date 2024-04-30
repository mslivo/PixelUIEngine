package net.mslivo.core.engine.ui_engine.ui.generator;

import net.mslivo.core.engine.ui_engine.API;
import net.mslivo.core.engine.ui_engine.ui.Window;

public interface WindowGeneratorP1<P1> {

    Window createWindow(API api, P1 p1);

}

