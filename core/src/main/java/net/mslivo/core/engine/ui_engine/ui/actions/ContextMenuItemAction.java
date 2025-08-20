package net.mslivo.core.engine.ui_engine.ui.actions;

import net.mslivo.core.engine.ui_engine.ui.actions.support.CellColor;
import net.mslivo.core.engine.ui_engine.ui.actions.support.Icon;

public interface ContextMenuItemAction extends Icon, CellColor {

    default void onSelect() {
    }

}