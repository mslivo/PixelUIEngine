package net.mslivo.pixelui.engine.actions;

import net.mslivo.pixelui.engine.actions.common.CellColor;
import net.mslivo.pixelui.engine.actions.common.Icon;

public interface ContextMenuItemAction extends Icon, CellColor {

    default void onSelect() {
    }

}