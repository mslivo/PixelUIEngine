package net.mslivo.pixelui.engine.actions;

import net.mslivo.pixelui.engine.actions.common.CommonActions;
import net.mslivo.pixelui.engine.actions.common.Displayable;
import net.mslivo.pixelui.engine.ContextMenuItem;

public interface ContextMenuAction extends CommonActions, Displayable {

    default void onItemSelected(ContextMenuItem selectedItem) {
    }

}
