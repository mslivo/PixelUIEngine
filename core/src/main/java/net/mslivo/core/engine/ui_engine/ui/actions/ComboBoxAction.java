package net.mslivo.core.engine.ui_engine.ui.actions;

import net.mslivo.core.engine.ui_engine.ui.actions.extendable.CommonActions;
import net.mslivo.core.engine.ui_engine.ui.actions.extendable.Displayable;
import net.mslivo.core.engine.ui_engine.ui.components.ComboboxItem;

public interface ComboBoxAction extends CommonActions, Displayable {

    default void onItemSelected(ComboboxItem selectedItem) {
    }

}
