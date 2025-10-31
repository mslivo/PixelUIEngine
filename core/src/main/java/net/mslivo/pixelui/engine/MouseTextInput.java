package net.mslivo.pixelui.engine;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.IntArray;
import net.mslivo.pixelui.engine.actions.MouseTextInputAction;

public final class MouseTextInput {
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

    MouseTextInput() {
    }
}
