package net.mslivo.pixelui.engine;

import com.badlogic.gdx.utils.Array;
import net.mslivo.pixelui.engine.actions.ComboBoxAction;

public class Combobox extends Component {
    public Array<ComboboxItem> items;
    public ComboBoxAction comboBoxAction;
    public ComboboxItem selectedItem;

    Combobox() {
    }
}
