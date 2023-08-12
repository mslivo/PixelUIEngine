package org.mslivo.core.engine.ui_engine.gui.ostextinput;

public interface MouseTextInputAction {

    /*Return = close yes/no */
    default boolean onConfirm(){
        return true;
    }

    default void onEnterCharacter(char c){return;}

    default void onChangeCase(boolean upperCase){return;}
    default void onDelete(){return;}

}
