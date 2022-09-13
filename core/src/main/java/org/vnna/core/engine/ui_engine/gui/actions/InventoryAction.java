package org.vnna.core.engine.ui_engine.gui.actions;

import org.vnna.core.engine.media_manager.color.FColor;
import org.vnna.core.engine.media_manager.media.CMediaGFX;
import org.vnna.core.engine.ui_engine.gui.components.inventory.Inventory;
import org.vnna.core.engine.ui_engine.gui.components.list.List;
import org.vnna.core.engine.ui_engine.gui.tooltip.ToolTip;

public interface InventoryAction<T extends Object> {

    default CMediaGFX icon(T listItem) {
        return null;
    }

    ;

    default ToolTip toolTip(T listItem) {
        return null;
    }

    default int iconArrayIndex(T listItem) {
        return 0;
    }

    ;

    default void onItemSelected(T listItem, int x, int y) {
        return;
    }

    default void onDragFromInventory(Inventory fromInventory, int from_x, int from_y, int to_x, int to_y) {
        return;
    }

    default void onDragFromList(List fromList, int fromIndex, int to_x, int to_y) {
        return;
    }

    default boolean canDragFromInventory(Inventory fromInventory) {
        return false;
    }

    default boolean canDragFromList(List fromList) {
        return false;
    }

    default FColor cellColor(T listItem, int x, int y) {
        return null;
    }

    default void onMouseClick(int button) {
        return;
    }

    default void onMouseDoubleClick(int button) {
        return;
    }

    default void onKeyDown(int keyCodeUp) {
        return;
    }

    default void onKeyUp(int keyCodeDown) {
        return;
    }

    default void onDragIntoScreen(T listItem, int x, int y, int screenX, int screenY) {
        return;
    }

}
