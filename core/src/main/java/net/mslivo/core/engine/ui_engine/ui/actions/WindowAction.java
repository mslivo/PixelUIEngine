package net.mslivo.core.engine.ui_engine.ui.actions;

import net.mslivo.core.engine.ui_engine.ui.actions.support.CommonActions;
import net.mslivo.core.engine.ui_engine.ui.actions.support.Displayable;
import net.mslivo.core.engine.ui_engine.ui.actions.support.Icon;

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
