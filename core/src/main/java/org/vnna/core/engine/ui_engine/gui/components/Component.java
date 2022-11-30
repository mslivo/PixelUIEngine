package org.vnna.core.engine.ui_engine.gui.components;

import org.vnna.core.engine.ui_engine.gui.Window;
import org.vnna.core.engine.ui_engine.gui.actions.UpdateAction;
import org.vnna.core.engine.ui_engine.gui.components.tabbar.Tab;
import org.vnna.core.engine.ui_engine.gui.tooltip.ToolTip;

import java.util.ArrayList;

public abstract class Component {

    public int x, y, width, height;

    public int offset_x, offset_y;

    public ToolTip toolTip;

    public boolean updateToolTip;

    public float color_r, color_g, color_b, color_a;

    public float color2_r, color2_g, color2_b, color2_a;

    public boolean disabled;

    public boolean visible;

    public Tab addedToTab;

    public ArrayList<UpdateAction> updateActions;

    public String name;

    public Object data;

    public Window addedToWindow; // set by engine

}
