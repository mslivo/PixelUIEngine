package net.mslivo.core.engine.ui_engine.ui.components;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Array;
import net.mslivo.core.engine.ui_engine.ui.actions.ContextMenuAction;

public class ContextMenu {
    public int x, y;
    public Color color;
    public Array<ContextMenuItem> items;
    public ContextMenuAction contextMenuAction;
}
