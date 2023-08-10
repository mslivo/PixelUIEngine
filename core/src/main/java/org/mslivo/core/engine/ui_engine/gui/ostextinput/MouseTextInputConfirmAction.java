package org.mslivo.core.engine.ui_engine.gui.ostextinput;

public interface MouseTextInputConfirmAction {

    /*Return = close yes/no */
    default boolean confirmPressed(){
        return true;
    }

    default void onEnterCharacter(char c){
        return;
    }
}
