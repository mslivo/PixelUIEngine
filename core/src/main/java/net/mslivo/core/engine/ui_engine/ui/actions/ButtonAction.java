package net.mslivo.core.engine.ui_engine.ui.actions;

public interface ButtonAction extends CommonActions, IconSupport {

    default void onPress() {
    }

    default void onRelease() {
    }

    default void onToggle(boolean value) {
    }

}
