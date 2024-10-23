package net.mslivo.core.engine.ui_engine.ui.actions;

public interface AppViewPortAction extends CommonActions {

    default void onPress(int x, int y) {
    }

    default void onRelease() {
    }


}
