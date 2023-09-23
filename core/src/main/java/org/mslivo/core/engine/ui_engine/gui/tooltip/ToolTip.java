package org.mslivo.core.engine.ui_engine.gui.tooltip;

import org.mslivo.core.engine.media_manager.media.CMediaFont;
import org.mslivo.core.engine.ui_engine.gui.actions.ToolTipAction;

import java.util.ArrayList;

public class ToolTip {

    public String[] lines;

    public boolean displayFistLineAsTitle;

    public CMediaFont font;

    public float color_r, color_g, color_b, color_a;

    public ToolTipAction toolTipAction;
    public int minWidth, minHeight;
    public ArrayList<ToolTipImage> images;

}
