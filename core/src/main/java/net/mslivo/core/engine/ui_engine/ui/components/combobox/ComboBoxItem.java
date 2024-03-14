package net.mslivo.core.engine.ui_engine.ui.components.combobox;

import net.mslivo.core.engine.media_manager.media.CMediaFont;
import net.mslivo.core.engine.media_manager.media.CMediaGFX;
import net.mslivo.core.engine.ui_engine.ui.actions.ComboBoxItemAction;

public class ComboBoxItem {
    public String text;
    public float color_r, color_g, color_b;
    public CMediaFont font;
    public CMediaGFX icon;
    public int iconIndex;
    public ComboBoxItemAction comboBoxItemAction;
    public ComboBox addedToComboBox;
    public String name;
    public Object data;
}
