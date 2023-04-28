package org.mslivo.core.engine.ui_engine.gui.components.combobox;

import org.mslivo.core.engine.media_manager.media.CMediaFont;
import org.mslivo.core.engine.media_manager.media.CMediaGFX;
import org.mslivo.core.engine.ui_engine.gui.actions.ComboBoxItemAction;
import org.mslivo.core.engine.ui_engine.gui.actions.ContextMenuItemAction;
import org.mslivo.core.engine.ui_engine.gui.contextmenu.ContextMenu;

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
