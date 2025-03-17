package net.mslivo.core.engine.ui_engine.ui.actions;

import net.mslivo.core.engine.ui_engine.ui.components.combobox.ComboboxItem;

public interface ComboBoxAction extends CommonActions {

    default void onItemSelected(ComboboxItem selectedItem) {
    }

    default void onOpen() {
    }

    default void onClose() {
    }

}
