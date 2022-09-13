package org.vnna.core.engine.ui_engine.gui.actions;

public interface TabBarAction {

    default void onChangeTab(int tab){};

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
}
