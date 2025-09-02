package net.mslivo.core.engine.ui_engine.ui.tooltip;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Array;
import net.mslivo.core.engine.ui_engine.constants.DIRECTION;
import net.mslivo.core.engine.ui_engine.ui.actions.UpdateActionSupport;
import net.mslivo.core.engine.ui_engine.ui.actions.ToolTipAction;

public class Tooltip extends UpdateActionSupport {
    public ToolTipAction toolTipAction;
    public Array<TooltipSegment> segments;
    public int minWidth;
    public Color color_border;
    public Color color_line;
    public int lineLength;
    public DIRECTION direction;
    public String name;
    public Object data;

}
