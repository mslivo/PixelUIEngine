package net.mslivo.core.engine.ui_engine.ui.actions;

public interface WindowAction extends CommonActions, IconSupport, Displayable {

    default void onMove(int x, int y) {
    }

    default void onFold() {
    }

    default void onUnfold() {
    }

    default void onMessageReceived(int type, Object... parameters) {
    }

}
