package net.mslivo.core.engine.ui_engine.ui.tooltip;

import com.badlogic.gdx.graphics.Color;
import net.mslivo.core.engine.ui_engine.constants.SEGMENT_ALIGNMENT;

public abstract class TooltipSegment {
    public Color color;
    public int width;
    public int height;
    public SEGMENT_ALIGNMENT alignment;
    public boolean border;
    public boolean merge;
    public Tooltip addedToTooltip;
}
