package net.mslivo.core.engine.ui_engine.ui.components.combobox;

import net.mslivo.core.engine.ui_engine.ui.actions.ComboBoxAction;
import net.mslivo.core.engine.ui_engine.ui.components.Component;

import java.util.ArrayList;

public class ComboBox extends Component {
    public ArrayList<ComboBoxItem> comboBoxItems;
    public ComboBoxAction comboBoxAction;
    public ComboBoxItem selectedItem;
    public boolean useIcons;
}
