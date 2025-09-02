package net.mslivo.core.engine.ui_engine.ui.components;

import net.mslivo.core.engine.ui_engine.ui.actions.GridAction;

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
}
