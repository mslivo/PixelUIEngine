package org.mslivo.core.engine.ui_engine.gui.components.combobox;

import org.mslivo.core.engine.media_manager.media.CMediaFont;
import org.mslivo.core.engine.ui_engine.gui.actions.ComboBoxAction;
import org.mslivo.core.engine.ui_engine.gui.components.Component;

import java.util.ArrayList;

public class ComboBox extends Component {

    public ArrayList items;

    public ComboBoxAction comboBoxAction;

    public Object selectedItem;

    public boolean useIcons;

    public CMediaFont font;

}
