package net.mslivo.core.engine.ui_engine.ui.actions;

public interface WindowAction extends CommonActions, IconSupport {

    default void onMove(int x, int y) {
    }

    default void onFold() {
    }

    default void onUnfold() {
    }

    default void onRemove() {
    }

    default void onAdd() {
    }

    default void onMessageReceived(int type, Object... parameters) {
    }

}
