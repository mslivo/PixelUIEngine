package net.mslivo.core.engine.ui_engine.ui.mousetextinput;

import net.mslivo.core.engine.media_manager.CMediaFont;
import net.mslivo.core.engine.ui_engine.ui.actions.MouseTextInputAction;

public class MouseTextInput {
    public int x,y;
    public char[] charactersLC;
    public char[] charactersUC;
    public float color_r,color_g,color_b,color_a;
    public float color2_r,color2_g,color2_b;
    public CMediaFont font;
    public int selectedIndex;
    public boolean upperCase;
    public MouseTextInputAction mouseTextInputAction;
}
