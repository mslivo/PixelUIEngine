package net.mslivo.pixelui.engine;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Array;
import net.mslivo.pixelui.engine.actions.ContextMenuAction;

public class ContextMenu {
    public int x, y;
    public Color color;
    public Array<ContextMenuItem> items;
    public ContextMenuAction contextMenuAction;

    ContextMenu() {
    }
}
