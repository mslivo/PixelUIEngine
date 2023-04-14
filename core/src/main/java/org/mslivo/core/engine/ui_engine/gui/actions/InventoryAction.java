package org.mslivo.core.engine.ui_engine.gui.actions;

import org.mslivo.core.engine.media_manager.color.FColor;
import org.mslivo.core.engine.media_manager.media.CMediaGFX;
import org.mslivo.core.engine.ui_engine.gui.components.inventory.Inventory;
import org.mslivo.core.engine.ui_engine.gui.components.list.List;
import org.mslivo.core.engine.ui_engine.gui.tooltip.ToolTip;

public abstract class InventoryAction<T> extends CommonActions {

    public CMediaGFX icon(T listItem) {
        return null;
    }

    public ToolTip toolTip(T listItem) {
        return null;
    }

    public int iconArrayIndex(T listItem) {
        return 0;
    }

    public void onItemSelected(T listItem, int x, int y) {
    }

    public void onDragFromInventory(Inventory fromInventory, int from_x, int from_y, int to_x, int to_y) {
    }

    public void onDragFromList(List fromList, int fromIndex, int to_x, int to_y) {
    }

    public boolean canDragFromInventory(Inventory fromInventory) {
        return false;
    }

    public boolean canDragFromList(List fromList) {
        return false;
    }

    public boolean canDragIntoScreen() {return false;}

    public FColor cellColor(T listItem, int x, int y) {
        return null;
    }

    public void onDragIntoScreen(T listItem, int x, int y, int screenX, int screenY) {
    }



}
