package net.mslivo.pixelui.engine;

import com.badlogic.gdx.graphics.Color;
import net.mslivo.pixelui.engine.actions.common.UpdateActionSupport;

public abstract sealed class Component extends UpdateActionSupport
        permits AppViewport, Button, Checkbox, ComboBox, FrameBufferViewport, Grid, Image, Knob, List, Progressbar, Scrollbar, Shape, Tabbar, Text, TextField {
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
