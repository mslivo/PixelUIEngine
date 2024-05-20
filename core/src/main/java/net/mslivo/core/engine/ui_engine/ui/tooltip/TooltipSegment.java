package net.mslivo.core.engine.ui_engine.ui.tooltip;

import net.mslivo.core.engine.ui_engine.constants.SEGMENT_ALIGNMENT;

public abstract class TooltipSegment {
    public float color_r, color_g, color_b, color_a;
    public int width;
    public int height;
    public SEGMENT_ALIGNMENT alignment;
    public boolean border;
    public boolean merge;
    public Tooltip addedToTooltip;
}
