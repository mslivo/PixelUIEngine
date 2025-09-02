package net.mslivo.core.engine.ui_engine.ui.actions;

import net.mslivo.core.engine.ui_engine.ui.actions.extendable.CommonActions;

public interface TextFieldAction extends CommonActions {

    default boolean isContentValid(String newContent) {
        return true;
    }

    default void onContentChange(String newContent, boolean valid) {
    }

    default void onTyped(char character) {
    }

    default void onEnter(String content, boolean valid) {
    }

    default void onFocus() {
    }

    default void onUnFocus() {
    }

}
