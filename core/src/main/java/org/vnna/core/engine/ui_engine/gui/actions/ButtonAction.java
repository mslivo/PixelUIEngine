package org.vnna.core.engine.ui_engine.gui.actions;

/**
 * Created by Admin on 10.03.2019.
 */
public interface ButtonAction {

    default void onPress(){
        return;
    };

    default void onRelease(){
        return;
    };

    default void onHold(){
        return;
    };

    default void onToggle(boolean value){
        return;
    }

    default void onMouseClick(int button){
        return;
    }

    default void onMouseDoubleClick(int button){
        return;
    }

    default void onMouseScroll(float scrolled){return;}
}
