package org.mslivo.core.engine.ui_engine.gui.components.combobox;

import org.mslivo.core.engine.ui_engine.gui.actions.ComboBoxAction;
import org.mslivo.core.engine.ui_engine.gui.components.Component;

import java.util.ArrayList;

public class ComboBox extends Component {

    public ArrayList<ComboBoxItem> items;

    public ComboBoxAction comboBoxAction;

    public ComboBoxItem selectedItem;

    public boolean useIcons;

}
