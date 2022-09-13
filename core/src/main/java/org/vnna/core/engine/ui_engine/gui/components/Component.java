package org.vnna.core.engine.ui_engine.gui.components;

import org.vnna.core.engine.media_manager.color.CColor;
import org.vnna.core.engine.ui_engine.gui.Window;
import org.vnna.core.engine.ui_engine.gui.actions.UpdateAction;
import org.vnna.core.engine.ui_engine.gui.components.tabbar.Tab;
import org.vnna.core.engine.ui_engine.gui.tooltip.ToolTip;

public abstract class Component {

    public int x,y,width,height;

    public int offset_x, offset_y;

    public ToolTip toolTip;

    public boolean updateToolTip;

    public CColor color;

    public CColor color2;

    public boolean disabled;

    public boolean visible;

    public Tab addedToTab;

    public UpdateAction updateAction;

    public String customFlag;

    public Object customData;

    public Window addedToWindow; // set by engine

}
