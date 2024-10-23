package net.mslivo.core.engine.ui_engine.ui.actions;

public interface KnobAction extends CommonActions {

    default void onTurned(float turned, float amount){
    }

    default void onPress(){
    }

    default void onRelease(){
    }


}
