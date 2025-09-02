package net.mslivo.core.engine.ui_engine.ui.actions;

import net.mslivo.core.engine.ui_engine.ui.actions.extendable.CommonActions;

public interface FrameBufferViewportAction extends CommonActions {

    default void onPress(int x, int y) {
    }

    default void onRelease() {
    }

}
