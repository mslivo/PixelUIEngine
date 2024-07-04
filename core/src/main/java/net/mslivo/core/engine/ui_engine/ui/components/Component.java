package net.mslivo.core.engine.ui_engine.ui.components;

import com.badlogic.gdx.graphics.Color;
import net.mslivo.core.engine.ui_engine.ui.Window;
import net.mslivo.core.engine.ui_engine.ui.actions.UpdateAction;
import net.mslivo.core.engine.ui_engine.ui.components.tabbar.Tab;
import net.mslivo.core.engine.ui_engine.ui.tooltip.Tooltip;

import java.util.ArrayList;

public abstract class Component {
    public int x, y, width, height;
    public Tooltip toolTip;
    public boolean updateToolTip;
    public Color color;
    public Color color2;
    public boolean disabled;
    public boolean visible;
    public Tab addedToTab;
    public ArrayList<UpdateAction> updateActions;
    public String name;
    public Object data;
    public Window addedToWindow; // set by engine
    public boolean addedToScreen;
}
