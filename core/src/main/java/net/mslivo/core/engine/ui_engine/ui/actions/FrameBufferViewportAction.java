package net.mslivo.core.engine.ui_engine.ui.actions;

public interface FrameBufferViewportAction extends CommonActions {

    default void onPress(int x, int y) {
    }

    default void onRelease() {
    }

}
