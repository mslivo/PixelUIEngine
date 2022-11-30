package org.vnna.core.engine.ui_engine.gui.contextmenu;

import org.vnna.core.engine.media_manager.media.CMediaFont;
import org.vnna.core.engine.media_manager.media.CMediaGFX;
import org.vnna.core.engine.ui_engine.gui.actions.ContextMenuItemAction;

public class ContextMenuItem {

    public String text;

    public float color_r, color_g, color_b, color_a;

    public CMediaFont font;

    public CMediaGFX icon;

    public int iconIndex;

    public ContextMenuItemAction contextMenuItemAction;

    public String name;

    public Object data;

}
