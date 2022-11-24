package org.vnna.core.engine.ui_engine.gui.actions;

import org.vnna.core.engine.media_manager.color.FColor;
import org.vnna.core.engine.media_manager.media.CMediaGFX;
import org.vnna.core.engine.ui_engine.gui.components.inventory.Inventory;
import org.vnna.core.engine.ui_engine.gui.components.list.List;
import org.vnna.core.engine.ui_engine.gui.tooltip.ToolTip;

public abstract class InventoryAction<T extends Object> {

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

    public FColor cellColor(T listItem, int x, int y) {
        return null;
    }

    public void onMouseClick(int button) {
    }

    public void onMouseDoubleClick(int button) {
    }

    public void onDragIntoScreen(T listItem, int x, int y, int screenX, int screenY) {
    }

    public void onMouseScroll(float scrolled) {
    }

}
