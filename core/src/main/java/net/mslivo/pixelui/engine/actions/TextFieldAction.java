package net.mslivo.pixelui.engine.actions;

import net.mslivo.pixelui.engine.actions.common.CommonActions;

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
