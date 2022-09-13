package org.vnna.core.engine.ui_engine.gui.actions;

public interface WindowAction {

    default void onMove(int x, int y){ return;};

    default void onFold(){ return;};

    default void onUnfold(){ return;};

    default void onRemove(){ return;};

    default void onMouseClick(int button){ return; }

    default void onMouseDoubleClick(int button){ return; }

    default void onKeyDown(int keyCodeUp){
        return;
    }

    default void onKeyUp(int keyCodeDown){
        return;
    }
}
