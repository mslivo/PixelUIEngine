package net.mslivo.core.engine.ui_engine.ui.tooltip;

import com.badlogic.gdx.graphics.Color;
import net.mslivo.core.engine.ui_engine.constants.DIRECTION;
import net.mslivo.core.engine.ui_engine.ui.actions.ToolTipAction;

import java.util.ArrayList;

public class Tooltip {
    public ToolTipAction toolTipAction;
    public ArrayList<TooltipSegment> segments;
    public int minWidth;
    public Color color_border;
    public Color color_line;
    public DIRECTION direction;
}
