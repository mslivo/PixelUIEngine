package net.mslivo.core.engine.ui_engine.ui.actions;

import net.mslivo.core.engine.ui_engine.ui.actions.support.CommonActions;

public interface ScrollBarAction extends CommonActions {

    default void onScrolled(float scrolled) {
    }

    default void onPress(float scrolled) {
    }

    default void onRelease(float scrolled) {
    }

}
