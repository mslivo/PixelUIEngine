package net.mslivo.pixelui.engine;

import com.badlogic.gdx.graphics.Color;
import net.mslivo.pixelui.engine.actions.UpdateActionSupport;

public abstract class Component extends UpdateActionSupport {
    public int x, y, width, height;
    public Color color;
    public Color color2;
    public boolean disabled;
    public boolean visible;
    public Tab addedToTab;
    public String name;
    public Object data;
    public Window addedToWindow; // set by engine
    public boolean addedToScreen;

}
