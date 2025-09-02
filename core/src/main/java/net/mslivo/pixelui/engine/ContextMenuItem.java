package net.mslivo.pixelui.engine;

import com.badlogic.gdx.graphics.Color;
import net.mslivo.pixelui.engine.actions.ContextMenuItemAction;

public class ContextMenuItem {
    public String text;
    public Color fontColor;
    public ContextMenuItemAction contextMenuItemAction;
    public ContextMenu addedToContextMenu;
    public String name;
    public Object data;

    ContextMenuItem() {
    }
}
