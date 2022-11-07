package org.vnna.core.engine.ui_engine.gui.actions;

public interface ImageAction {

    default void onMouseClick(int button){
        return;
    }

    default void onMouseDoubleClick(int button){
        return;
    }

    default void onKeyDown(int keyCodeUp){
        return;
    }

    default void onKeyUp(int keyCodeDown){
        return;
    }

    default void onMouseScroll(float scrolled){return;}
}
