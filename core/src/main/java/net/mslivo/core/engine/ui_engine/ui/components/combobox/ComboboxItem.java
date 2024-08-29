package net.mslivo.core.engine.ui_engine.ui.components.combobox;

import com.badlogic.gdx.graphics.Color;
import net.mslivo.core.engine.media_manager.CMediaFont;
import net.mslivo.core.engine.media_manager.CMediaSprite;
import net.mslivo.core.engine.ui_engine.ui.actions.ComboBoxItemAction;

public class ComboboxItem {
    public String text;
    public CMediaFont font;
    public CMediaSprite icon;
    public int iconIndex;
    public ComboBoxItemAction comboBoxItemAction;
    public Combobox addedToComboBox;
    public String name;
    public Object data;
}
