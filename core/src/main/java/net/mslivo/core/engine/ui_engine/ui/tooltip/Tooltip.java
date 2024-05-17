package net.mslivo.core.engine.ui_engine.ui.tooltip;

import net.mslivo.core.engine.media_manager.media.CMediaFont;
import net.mslivo.core.engine.ui_engine.ui.actions.ToolTipAction;

import java.util.ArrayList;

public class Tooltip {
    public String title;
    public ToolTipAction toolTipAction;
    public int minWidth, minHeight;
    public ArrayList<TooltipSegment> segments;

}
