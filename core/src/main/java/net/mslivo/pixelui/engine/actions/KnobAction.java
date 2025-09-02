package net.mslivo.pixelui.engine.actions;

import net.mslivo.pixelui.engine.actions.common.CommonActions;

public interface KnobAction extends CommonActions {

    default void onTurned(float turned, float amount){
    }

    default void onPress(){
    }

    default void onRelease(){
    }


}
