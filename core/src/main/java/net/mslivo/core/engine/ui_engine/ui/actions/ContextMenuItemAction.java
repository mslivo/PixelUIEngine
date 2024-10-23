package net.mslivo.core.engine.ui_engine.ui.actions;

import com.badlogic.gdx.graphics.Color;
import net.mslivo.core.engine.ui_engine.ui.contextmenu.ContextMenuItem;

public abstract class ContextMenuItemAction implements IconSupport {

    public void onSelect() {
    }


    public Color cellColor() {
        return Color.GRAY;
    }
}
