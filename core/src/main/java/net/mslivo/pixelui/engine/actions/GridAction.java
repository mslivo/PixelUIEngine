package net.mslivo.pixelui.engine.actions;

import net.mslivo.pixelui.engine.actions.common.CommonActions;
import net.mslivo.pixelui.engine.actions.common.ItemCellColor;
import net.mslivo.pixelui.engine.Grid;
import net.mslivo.pixelui.engine.List;
import net.mslivo.pixelui.engine.Tooltip;

public interface GridAction<T> extends CommonActions, HasItemIcons<T>, ItemCellColor<T> {

    default Tooltip toolTip(T gridItem) {
        return null;
    }

    default boolean onItemSelected(T gridItem) {
        return true;
    }

    default void onDragFromGrid(Grid fromGrid, int from_x, int from_y, int to_x, int to_y) {
    }

    default void onDragFromList(List fromList, int fromIndex, int to_x, int to_y) {
    }

    default boolean canDragFromGrid(Grid fromGrid) {
        return false;
    }

    default boolean canDragFromList(List fromList) {
        return false;
    }

    default void onDragIntoApp(T gridItem, int from_x, int from_y, int screenX, int screenY) {
    }

    default boolean canDragIntoApp() {
        return false;
    }

}
