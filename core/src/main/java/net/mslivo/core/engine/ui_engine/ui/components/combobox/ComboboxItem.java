package net.mslivo.core.engine.ui_engine.ui.components.combobox;

import net.mslivo.core.engine.media_manager.CMediaFont;
import net.mslivo.core.engine.media_manager.CMediaSprite;
import net.mslivo.core.engine.ui_engine.ui.actions.ComboBoxItemAction;

public class ComboboxItem {
    public String text;
    public float color_r, color_g, color_b;
    public CMediaFont font;
    public CMediaSprite icon;
    public int iconIndex;
    public ComboBoxItemAction comboBoxItemAction;
    public Combobox addedToComboBox;
    public String name;
    public Object data;
}
