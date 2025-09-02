package net.mslivo.pixelui.engine.actions;

import net.mslivo.pixelui.engine.actions.common.CommonActions;
import net.mslivo.pixelui.engine.actions.common.Displayable;
import net.mslivo.pixelui.engine.ComboboxItem;

public interface ComboBoxAction extends CommonActions, Displayable {

    default void onItemSelected(ComboboxItem selectedItem) {
    }

}
