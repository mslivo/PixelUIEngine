package net.mslivo.core.engine.ui_engine.ui.components.grid;

import net.mslivo.core.engine.ui_engine.ui.actions.GridAction;
import net.mslivo.core.engine.ui_engine.ui.components.Component;

public class Grid<T> extends Component {
    public T[][] items;
    public GridAction gridAction;
    public T selectedItem;
    public boolean dragEnabled;
    public boolean dragOutEnabled;
    public boolean dragInEnabled;
    public boolean doubleSized;
}
