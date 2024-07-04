package net.mslivo.core.engine.ui_engine.ui.tooltip;

import net.mslivo.core.engine.ui_engine.constants.DIRECTION;
import net.mslivo.core.engine.ui_engine.ui.actions.ToolTipAction;

import java.awt.*;
import java.util.ArrayList;

public class Tooltip {
    public ToolTipAction toolTipAction;
    public ArrayList<TooltipSegment> segments;
    public int minWidth;
    public float color_border_r, color_border_g, color_border_b, color_border_a;
    public DIRECTION direction;
}
