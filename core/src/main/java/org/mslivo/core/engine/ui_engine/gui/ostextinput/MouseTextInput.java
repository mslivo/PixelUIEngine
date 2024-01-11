package org.mslivo.core.engine.ui_engine.gui.ostextinput;

import org.mslivo.core.engine.media_manager.media.CMediaFont;

public class MouseTextInput {

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
    public MouseTextInputAction mouseTextInputAction;

    public float color_r,color_g,color_b,color_a;
}
