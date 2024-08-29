package net.mslivo.core.engine.ui_engine.ui.actions;

import com.badlogic.gdx.graphics.Color;
import net.mslivo.core.engine.ui_engine.ui.contextmenu.ContextmenuItem;

public abstract class ContextmenuAction extends CommonActions {

    public void onItemSelected(ContextmenuItem selectedItem) {
    }

    public void onOpen() {
    }

    public void onClose() {
    }

    public Color cellColor(ContextmenuItem contextMenuItem) {
        return null;
    }

}
