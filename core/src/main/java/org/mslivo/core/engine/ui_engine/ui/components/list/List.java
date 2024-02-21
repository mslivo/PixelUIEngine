package org.mslivo.core.engine.ui_engine.ui.components.list;

import org.mslivo.core.engine.media_manager.media.CMediaFont;
import org.mslivo.core.engine.ui_engine.ui.actions.ListAction;
import org.mslivo.core.engine.ui_engine.ui.components.Component;

import java.util.ArrayList;
import java.util.HashSet;

public class List<T> extends Component {
    public ArrayList<T> items;
    public float scrolled;
    public ListAction listAction;
    public CMediaFont font;
    public T selectedItem; // singleselect
    public boolean multiSelect;
    public HashSet<T> selectedItems; // multiselect
    public boolean dragEnabled;
    public boolean dragOutEnabled;
    public boolean dragInEnabled;
}
