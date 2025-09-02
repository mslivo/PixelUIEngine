package net.mslivo.core.engine.ui_engine.ui.components;

import com.badlogic.gdx.utils.Array;
import net.mslivo.core.engine.ui_engine.ui.actions.ComboBoxAction;

public class Combobox extends Component {
    public Array<ComboboxItem> items;
    public ComboBoxAction comboBoxAction;
    public ComboboxItem selectedItem;
}
