package net.mslivo.pixelui.engine.actions;

import net.mslivo.pixelui.engine.actions.common.CommonActions;
import net.mslivo.pixelui.engine.actions.common.Displayable;
import net.mslivo.pixelui.engine.actions.common.Icon;

public interface WindowAction extends CommonActions, Icon, Displayable {

    default void onMove(int x, int y) {
    }

    default void onFold() {
    }

    default void onUnfold() {
    }

    default void onMessageReceived(int type, Object... parameters) {
    }

}
