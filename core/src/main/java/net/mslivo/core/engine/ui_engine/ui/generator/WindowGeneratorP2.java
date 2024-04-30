package net.mslivo.core.engine.ui_engine.ui.generator;

import net.mslivo.core.engine.ui_engine.API;
import net.mslivo.core.engine.ui_engine.ui.Window;

public interface WindowGeneratorP2<P1, P2> {

    Window createWindow(API api, P1 p1, P2 p2);

}

