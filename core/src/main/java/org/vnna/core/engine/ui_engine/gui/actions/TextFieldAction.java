package org.vnna.core.engine.ui_engine.gui.actions;

public abstract class TextFieldAction {

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

    public void onMouseClick(int button) {
    }

    public void onMouseScroll(float scrolled) {
    }
}
