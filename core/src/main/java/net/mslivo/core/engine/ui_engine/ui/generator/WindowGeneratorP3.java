package net.mslivo.core.engine.ui_engine.ui.generator;

import net.mslivo.core.engine.ui_engine.API;
import net.mslivo.core.engine.ui_engine.ui.window.Window;

public interface WindowGeneratorP3<P1, P2, P3> {

    Window createWindow(API api, P1 p1, P2 p2, P3 p3);

}

