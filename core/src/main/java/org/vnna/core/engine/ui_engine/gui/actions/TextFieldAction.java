package org.vnna.core.engine.ui_engine.gui.actions;

public interface TextFieldAction {

    default boolean isContentValid(String newContent){return true;};

    default void onContentChange(String newContent, boolean valid){return;};

    default void onTyped(char character){return;};

    default void onEnter(String content, boolean valid){return;};

    default void onFocus(){
        return;
    };

    default void onUnFocus(){
        return;
    };

    default void onMouseClick(int button){
        return;
    }
}
