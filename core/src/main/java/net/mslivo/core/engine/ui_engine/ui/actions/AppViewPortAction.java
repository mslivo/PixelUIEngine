package net.mslivo.core.engine.ui_engine.ui.actions;

import net.mslivo.core.engine.ui_engine.ui.actions.extendable.CommonActions;

public interface AppViewPortAction extends CommonActions {

    default void onPress(int x, int y) {
    }

    default void onRelease() {
    }


}
