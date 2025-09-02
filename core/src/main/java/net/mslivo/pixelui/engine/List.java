package net.mslivo.pixelui.engine;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Array;
import net.mslivo.pixelui.engine.actions.ListAction;

import java.util.HashSet;

public class List<T> extends Component {
    public Array<T> items;
    public float scrolled;
    public ListAction listAction;
    public Color fontColor;
    public T selectedItem; // singleselect
    public boolean multiSelect;
    public HashSet<T> selectedItems; // multiselect
    public boolean dragEnabled;
    public boolean dragOutEnabled;
    public boolean dragInEnabled;

    List() {
    }
}
