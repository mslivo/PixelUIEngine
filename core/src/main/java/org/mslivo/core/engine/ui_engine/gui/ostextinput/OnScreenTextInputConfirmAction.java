package org.mslivo.core.engine.ui_engine.gui.ostextinput;

import org.mslivo.core.engine.media_manager.media.CMediaFont;
import org.mslivo.core.engine.ui_engine.misc.FColor;

import java.util.function.BooleanSupplier;

public interface OnScreenTextInputConfirmAction {

    /*Return = close yes/no */
    default boolean confirmPressed(){
        return true;
    }
}
