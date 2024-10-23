package net.mslivo.core.engine.ui_engine.ui.actions;

import com.badlogic.gdx.graphics.Color;

public interface ContextMenuItemAction extends IconSupport, CellColorSupport {

    default void onSelect() {
    }

}
