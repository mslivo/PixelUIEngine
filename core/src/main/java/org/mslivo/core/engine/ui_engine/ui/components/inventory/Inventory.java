package org.mslivo.core.engine.ui_engine.ui.components.inventory;

import org.mslivo.core.engine.ui_engine.ui.actions.InventoryAction;
import org.mslivo.core.engine.ui_engine.ui.components.Component;

public class Inventory<T> extends Component {
    public T[][] items;
    public InventoryAction inventoryAction;
    public T selectedItem;
    public boolean dragEnabled;
    public boolean dragOutEnabled;
    public boolean dragInEnabled;
    public boolean doubleSized;
}
