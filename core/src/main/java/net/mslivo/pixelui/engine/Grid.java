package net.mslivo.pixelui.engine;

import net.mslivo.pixelui.engine.actions.GridAction;

import java.util.HashSet;

public class Grid<T> extends Component {
    public T[][] items;
    public GridAction gridAction;
    public T selectedItem;
    public boolean multiSelect;
    public HashSet<T> selectedItems;
    public boolean dragEnabled;
    public boolean dragOutEnabled;
    public boolean dragInEnabled;
    public boolean bigMode;

    Grid() {
    }
}
