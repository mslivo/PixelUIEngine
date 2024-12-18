package net.mslivo.core.engine.ui_engine.ui.tooltip;

import com.badlogic.gdx.graphics.Color;
import net.mslivo.core.engine.ui_engine.constants.DIRECTION;
import net.mslivo.core.engine.ui_engine.ui.UpdateActionSupport;
import net.mslivo.core.engine.ui_engine.ui.actions.ToolTipAction;

import java.util.ArrayList;

public class Tooltip extends UpdateActionSupport {
    public ToolTipAction toolTipAction;
    public ArrayList<TooltipSegment> segments;
    public int minWidth;
    public Color color_border;
    public Color color_line;
    public int lineLength;
    public DIRECTION direction;
}
