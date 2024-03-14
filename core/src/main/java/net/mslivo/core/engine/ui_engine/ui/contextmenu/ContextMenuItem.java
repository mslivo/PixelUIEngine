package net.mslivo.core.engine.ui_engine.ui.contextmenu;

import net.mslivo.core.engine.media_manager.media.CMediaFont;
import net.mslivo.core.engine.media_manager.media.CMediaGFX;
import net.mslivo.core.engine.ui_engine.ui.actions.ContextMenuItemAction;

public class ContextMenuItem {

    public String text;

    public float color_r, color_g, color_b;

    public CMediaFont font;

    public CMediaGFX icon;

    public int iconIndex;

    public ContextMenuItemAction contextMenuItemAction;

    public ContextMenu addedToContextMenu;

    public String name;

    public Object data;

}
