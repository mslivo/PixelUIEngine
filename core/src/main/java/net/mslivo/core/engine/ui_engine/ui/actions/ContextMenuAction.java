package net.mslivo.core.engine.ui_engine.ui.actions;

import net.mslivo.core.engine.ui_engine.ui.actions.support.CommonActions;
import net.mslivo.core.engine.ui_engine.ui.actions.support.Displayable;
import net.mslivo.core.engine.ui_engine.ui.contextmenu.ContextMenuItem;

public interface ContextMenuAction extends CommonActions, Displayable {

    default void onItemSelected(ContextMenuItem selectedItem) {
    }

}
