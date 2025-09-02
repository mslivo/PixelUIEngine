package net.mslivo.core.engine.ui_engine.ui.actions;

import net.mslivo.core.engine.ui_engine.ui.actions.extendable.ItemCellColor;
import net.mslivo.core.engine.ui_engine.ui.actions.extendable.CommonActions;
import net.mslivo.core.engine.ui_engine.ui.components.Grid;
import net.mslivo.core.engine.ui_engine.ui.components.List;
import net.mslivo.core.engine.ui_engine.ui.tooltip.Tooltip;

public interface ListAction<T> extends CommonActions, HasItemIcons<T>, ItemCellColor<T> {

    default Tooltip toolTip(T listItem) {
        return null;
    }

    default String text(T listItem) {
        return listItem.toString();
    }

    default boolean onItemSelected(T listItem) {
        return true;
    }

    default void onScrolled(float scrolled) {
    }

    /* Drag */

    default void onDragFromList(List fromList, int fromIndex, int toIndex) {
    }

    default void onDragFromGrid(Grid fromGrid, int from_x, int from_y, int toIndex) {
    }

    default boolean canDragFromList(List list) {
        return false;
    }

    default boolean canDragFromGrid(Grid fromGrid) {
        return false;
    }

    default void onDragIntoApp(T listItem, int mouseX, int mouseY) {
    }

    default boolean canDragIntoApp() {
        return false;
    }

}
