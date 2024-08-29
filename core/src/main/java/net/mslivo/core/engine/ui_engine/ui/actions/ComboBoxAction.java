package net.mslivo.core.engine.ui_engine.ui.actions;

import com.badlogic.gdx.graphics.Color;
import net.mslivo.core.engine.ui_engine.ui.components.combobox.ComboboxItem;

public abstract class ComboBoxAction extends CommonActions {

    public void onItemSelected(ComboboxItem selectedItem) {
    }

    public void onOpen() {
    }

    public void onClose() {
    }

    public Color cellColor(ComboboxItem comboboxItem) {
        return null;
    }
}
