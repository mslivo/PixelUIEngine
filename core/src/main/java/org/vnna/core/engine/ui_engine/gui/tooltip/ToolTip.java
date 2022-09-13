package org.vnna.core.engine.ui_engine.gui.tooltip;

import org.vnna.core.engine.media_manager.color.CColor;
import org.vnna.core.engine.media_manager.media.CMediaFont;
import org.vnna.core.engine.ui_engine.gui.actions.ToolTipAction;

import java.util.ArrayList;

public class ToolTip {

    public String[] lines;

    public boolean displayFistLineAsTitle;

    public CMediaFont font;

    public CColor cColor;

    public ToolTipAction toolTipAction;

    public ArrayList<ToolTipImage> images;

}
