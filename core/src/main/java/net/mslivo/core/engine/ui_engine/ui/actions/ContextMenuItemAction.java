package net.mslivo.core.engine.ui_engine.ui.actions;

import net.mslivo.core.engine.ui_engine.ui.actions.extendable.CellColor;
import net.mslivo.core.engine.ui_engine.ui.actions.extendable.Icon;

public interface ContextMenuItemAction extends Icon, CellColor {

    default void onSelect() {
    }

}