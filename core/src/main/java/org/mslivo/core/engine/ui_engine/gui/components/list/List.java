package org.mslivo.core.engine.ui_engine.gui.components.list;

import org.mslivo.core.engine.media_manager.media.CMediaFont;
import org.mslivo.core.engine.ui_engine.gui.actions.ListAction;
import org.mslivo.core.engine.ui_engine.gui.components.Component;

import java.util.ArrayList;
import java.util.HashSet;

public class List extends Component {

    public ArrayList items;

    public float scrolled;

    public ListAction listAction;

    public CMediaFont font;

    public Object selectedItem; // singleselect

    public boolean multiSelect;

    public HashSet<Object> selectedItems; // multiselect

    public boolean dragEnabled;

    public boolean dragOutEnabled;

    public boolean dragInEnabled;

}
