package org.mslivo.core.engine.ui_engine.gui.ostextinput;

import org.mslivo.core.engine.media_manager.media.CMediaFont;
import org.mslivo.core.engine.ui_engine.misc.FColor;

import java.util.function.BooleanSupplier;

public class OnScreenTextInput {

    /**
     * Special Character
     * Enter = Confirm
     * \b = Backspace -> Delete
     * \b = Tab -> Delete
     */
    public int x,y;
    public char[] charactersLC;
    public char[] charactersUC;

    public CMediaFont font;
    public int selectedIndex;
    public boolean upperCase;
    public BooleanSupplier onConfirm;

    public FColor color;
}
