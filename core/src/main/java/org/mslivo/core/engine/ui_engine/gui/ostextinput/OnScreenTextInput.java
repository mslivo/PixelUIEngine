package org.mslivo.core.engine.ui_engine.gui.ostextinput;

import org.mslivo.core.engine.media_manager.media.CMediaFont;
import org.mslivo.core.engine.ui_engine.misc.FColor;

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
    public OnScreenTextInputConfirmAction confirmAction;

    public FColor color;
}
