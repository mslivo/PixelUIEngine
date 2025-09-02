package net.mslivo.pixelui.engine;

import com.badlogic.gdx.graphics.Color;
import net.mslivo.pixelui.engine.actions.ComboBoxItemAction;

public class ComboboxItem {
    public String text;
    public Color fontColor;
    public ComboBoxItemAction comboBoxItemAction;
    public Combobox addedToComboBox;
    public String name;
    public Object data;

    ComboboxItem() {
    }
}
