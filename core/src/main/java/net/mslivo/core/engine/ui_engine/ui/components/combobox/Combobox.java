package net.mslivo.core.engine.ui_engine.ui.components.combobox;

import net.mslivo.core.engine.ui_engine.ui.actions.ComboBoxAction;
import net.mslivo.core.engine.ui_engine.ui.components.Component;

import java.util.ArrayList;

public class Combobox extends Component {
    public ArrayList<ComboboxItem> comboBoxItems;
    public ComboBoxAction comboBoxAction;
    public ComboboxItem selectedItem;
    public boolean useIcons;
}
