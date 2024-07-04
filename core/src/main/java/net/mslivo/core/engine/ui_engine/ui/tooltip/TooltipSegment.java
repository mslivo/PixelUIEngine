package net.mslivo.core.engine.ui_engine.ui.tooltip;

import com.badlogic.gdx.graphics.Color;
import net.mslivo.core.engine.ui_engine.constants.SEGMENT_ALIGNMENT;

public abstract class TooltipSegment {
    public Tooltip addedToTooltip;
    public Color color;
    public SEGMENT_ALIGNMENT alignment;
    public int width;
    public int height;
    public boolean border;
    public boolean merge;
    public boolean clear;
}
