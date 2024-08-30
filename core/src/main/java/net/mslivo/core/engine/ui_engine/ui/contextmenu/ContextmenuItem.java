package net.mslivo.core.engine.ui_engine.ui.contextmenu;

import com.badlogic.gdx.graphics.Color;
import net.mslivo.core.engine.media_manager.CMediaFont;
import net.mslivo.core.engine.media_manager.CMediaSprite;
import net.mslivo.core.engine.ui_engine.ui.actions.ContextMenuItemAction;

public class ContextmenuItem {

    public String text;
    public Color fontColor;
    public CMediaSprite icon;
    public int iconIndex;
    public ContextMenuItemAction contextMenuItemAction;
    public Contextmenu addedToContextMenu;
    public String name;
    public Object data;

}
