package net.mslivo.core.engine.ui_engine.ui.contextmenu;

import com.badlogic.gdx.graphics.Color;
import net.mslivo.core.engine.ui_engine.ui.actions.ContextMenuAction;

import java.util.ArrayList;

public class Contextmenu {
    public int x, y;
    public Color color;
    public ArrayList<ContextMenuItem> items;
    public ContextMenuAction contextMenuAction;
}
