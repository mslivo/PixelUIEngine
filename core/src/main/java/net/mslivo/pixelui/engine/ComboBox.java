package net.mslivo.pixelui.engine;

import com.badlogic.gdx.utils.Array;
import net.mslivo.pixelui.engine.actions.ComboBoxAction;

public final class ComboBox extends Component {
    public Array<ComboBoxItem> items;
    public ComboBoxAction comboBoxAction;
    public ComboBoxItem selectedItem;

    ComboBox() {
    }
}
