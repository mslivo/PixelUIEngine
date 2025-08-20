package net.mslivo.core.engine.ui_engine.ui.actions;

import net.mslivo.core.engine.ui_engine.ui.actions.support.CommonActions;
import net.mslivo.core.engine.ui_engine.ui.actions.support.Icon;

public interface ButtonAction extends CommonActions, Icon {

    default void onPress() {
    }

    default void onRelease() {
    }

    default void onToggle(boolean value) {
    }

}
