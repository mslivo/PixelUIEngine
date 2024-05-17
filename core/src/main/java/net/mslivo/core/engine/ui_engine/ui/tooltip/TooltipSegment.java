package net.mslivo.core.engine.ui_engine.ui.tooltip;

import com.badlogic.gdx.graphics.Color;
import net.mslivo.core.engine.media_manager.media.CMediaFont;
import net.mslivo.core.engine.ui_engine.constants.TOOLTIP_SEGMENT_ALIGNMENT;
import net.mslivo.core.engine.ui_engine.ui.actions.ToolTipAction;

public abstract class TooltipSegment {
    public float color_r, color_g, color_b, color_a;
    public CMediaFont font;
    public int width;
    public int height;
    public TOOLTIP_SEGMENT_ALIGNMENT alignment;
    public Tooltip addedToTooltip;
}
