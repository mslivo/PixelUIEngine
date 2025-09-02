package net.mslivo.pixelui.engine.actions;

import net.mslivo.pixelui.engine.actions.common.CommonActions;
import net.mslivo.pixelui.engine.actions.common.Icon;

public interface ButtonAction extends CommonActions, Icon {

    default void onPress() {
    }

    default void onRelease() {
    }

    default void onToggle(boolean value) {
    }

}
