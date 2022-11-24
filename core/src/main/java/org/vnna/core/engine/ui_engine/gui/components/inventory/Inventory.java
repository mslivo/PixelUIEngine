package org.vnna.core.engine.ui_engine.gui.components.inventory;

import org.vnna.core.engine.ui_engine.gui.actions.InventoryAction;
import org.vnna.core.engine.ui_engine.gui.components.Component;

import java.util.ArrayList;

public class Inventory extends Component {

    public Object[][] items;

    public InventoryAction inventoryAction;

    public Object selectedItem;

    public boolean dragEnabled;

    public boolean dragOutEnabled;

    public boolean dragInEnabled;

    public boolean doubleSized;

}
