package net.mslivo.pixelui.engine.actions;

import net.mslivo.pixelui.engine.actions.common.CommonActions;
import net.mslivo.pixelui.engine.actions.common.Displayable;
import net.mslivo.pixelui.engine.ComboBoxItem;
import net.mslivo.pixelui.engine.actions.common.ItemCellColor;
import net.mslivo.pixelui.engine.actions.common.ItemIcons;

public interface ComboBoxAction extends CommonActions, Displayable {

    default void onItemSelected(ComboBoxItem selectedItem) {
    }

}
