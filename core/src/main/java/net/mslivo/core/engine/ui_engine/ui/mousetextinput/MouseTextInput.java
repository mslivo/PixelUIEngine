package net.mslivo.core.engine.ui_engine.ui.mousetextinput;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.IntArray;
import net.mslivo.core.engine.ui_engine.ui.actions.MouseTextInputAction;

public class MouseTextInput {
    public int x,y;
    public char[] charactersLC;
    public char[] charactersUC;
    public Color color;
    public Color color2;
    public Color fontColor;
    public int selectedIndex;
    public boolean upperCase;
    public MouseTextInputAction mouseTextInputAction;
    public IntArray enterCharacterQueue;

}
