package net.mslivo.pixelui.engine.actions;

import net.mslivo.pixelui.engine.actions.common.CommonActions;

public interface FrameBufferViewportAction extends CommonActions {

    default void onPress(int x, int y) {
    }

    default void onRelease() {
    }

}
