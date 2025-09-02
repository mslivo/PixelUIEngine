package net.mslivo.pixelui.engine.actions;

import net.mslivo.pixelui.engine.actions.common.CommonActions;

public interface ScrollBarAction extends CommonActions {

    default void onScrolled(float scrolled) {
    }

    default void onPress(float scrolled) {
    }

    default void onRelease(float scrolled) {
    }

}
