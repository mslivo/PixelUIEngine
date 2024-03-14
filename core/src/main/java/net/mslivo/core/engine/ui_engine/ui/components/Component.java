package net.mslivo.core.engine.ui_engine.ui.components;

import net.mslivo.core.engine.ui_engine.ui.Window;
import net.mslivo.core.engine.ui_engine.ui.actions.UpdateAction;
import net.mslivo.core.engine.ui_engine.ui.components.tabbar.Tab;
import net.mslivo.core.engine.ui_engine.ui.tooltip.ToolTip;

import java.util.ArrayList;

public abstract class Component {
    public int x, y, width, height;
    public ToolTip toolTip;
    public boolean updateToolTip;
    public float color_r, color_g, color_b,color_a;
    public float color2_r, color2_g, color2_b;
    public boolean disabled;
    public boolean visible;
    public Tab addedToTab;
    public ArrayList<UpdateAction> updateActions;
    public String name;
    public Object data;
    public Window addedToWindow; // set by engine
    public boolean addedToScreen;
}
