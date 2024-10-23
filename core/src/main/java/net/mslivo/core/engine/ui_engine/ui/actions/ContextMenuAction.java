package net.mslivo.core.engine.ui_engine.ui.actions;

import net.mslivo.core.engine.ui_engine.ui.contextmenu.ContextMenuItem;

public interface ContextMenuAction extends CommonActions {

    default void onItemSelected(ContextMenuItem selectedItem) {
    }

    default void onOpen() {
    }

    default void onClose() {
    }

}
