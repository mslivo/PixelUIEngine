package org.mslivo.core.engine.ui_engine.ui.components.combobox;

import org.mslivo.core.engine.ui_engine.ui.actions.ComboBoxAction;
import org.mslivo.core.engine.ui_engine.ui.components.Component;

import java.util.ArrayList;

public class ComboBox extends Component {
    public ArrayList<ComboBoxItem> items;
    public ComboBoxAction comboBoxAction;
    public ComboBoxItem selectedItem;
    public boolean useIcons;
}
