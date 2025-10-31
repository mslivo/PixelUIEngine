package net.mslivo.pixelui.engine;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Array;
import net.mslivo.pixelui.engine.constants.DIRECTION;
import net.mslivo.pixelui.engine.actions.common.UpdateActionSupport;
import net.mslivo.pixelui.engine.actions.ToolTipAction;

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

    Tooltip() {
    }
}
