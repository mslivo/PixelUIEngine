package net.mslivo.pixelui.engine;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Array;
import net.mslivo.pixelui.engine.actions.UpdateActionSupport;
import net.mslivo.pixelui.engine.actions.WindowAction;

/**
 * Created by Admin on 09.03.2019.
 */
public class Window extends UpdateActionSupport {
    public int x, y, width, height;
    public String title;
    public Color fontColor;
    public Array<Component> components;
    public String name;
    public Object data;
    public Color color;
    public boolean alwaysOnTop;
    public boolean folded;
    public boolean moveAble;
    public boolean hasTitleBar;
    public boolean visible;
    public boolean enforceScreenBounds;
    public WindowAction windowAction;
    public boolean addedToScreen;

}
