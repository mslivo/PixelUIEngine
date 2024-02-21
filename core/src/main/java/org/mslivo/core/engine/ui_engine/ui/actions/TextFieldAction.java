package org.mslivo.core.engine.ui_engine.ui.actions;

public abstract class TextFieldAction extends CommonActions {

    public boolean isContentValid(String newContent) {
        return true;
    }

    public void onContentChange(String newContent, boolean valid) {
    }

    public void onTyped(char character) {
    }

    public void onEnter(String content, boolean valid) {
    }

    public void onFocus() {
    }

    public void onUnFocus() {
    }

}
