package net.mslivo.core.engine.ui_engine.ui.components.list;

import com.badlogic.gdx.graphics.Color;
import net.mslivo.core.engine.media_manager.CMediaFont;
import net.mslivo.core.engine.ui_engine.ui.actions.ListAction;
import net.mslivo.core.engine.ui_engine.ui.components.Component;

import java.util.ArrayList;
import java.util.HashSet;

public class List<T> extends Component {
    public ArrayList<T> items;
    public float scrolled;
    public ListAction listAction;
    public Color fontColor;
    public T selectedItem; // singleselect
    public boolean multiSelect;
    public HashSet<T> selectedItems; // multiselect
    public boolean dragEnabled;
    public boolean dragOutEnabled;
    public boolean dragInEnabled;
}
