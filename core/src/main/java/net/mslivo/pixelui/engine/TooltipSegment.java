package net.mslivo.pixelui.engine;

import com.badlogic.gdx.graphics.Color;
import net.mslivo.pixelui.engine.constants.SEGMENT_ALIGNMENT;

public abstract sealed class TooltipSegment permits TooltipFramebufferViewportSegment, TooltipImageSegment, TooltipTextSegment {
    public Tooltip addedToTooltip;
    public Color cellColor;
    public Color contentColor;
    public SEGMENT_ALIGNMENT alignment;
    public int width;
    public int height;
    public boolean border;
    public boolean merge;
    public boolean clear;

    TooltipSegment() {
    }
}
