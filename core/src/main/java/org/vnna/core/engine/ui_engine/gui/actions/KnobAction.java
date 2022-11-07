package org.vnna.core.engine.ui_engine.gui.actions;

public interface KnobAction {

    default void onTurned(float turned, float amount){
        return;
    };

    default void onPress(){
        return;
    };

    default void onRelease(){
        return;
    };

    default void onMouseClick(int button){
        return;
    }

    default void onMouseDoubleClick(int button){
        return;
    }

    default void onMouseScroll(float scrolled){return;}
}
