package net.mslivo.core.engine.ui_engine.ui.actions;

import com.badlogic.gdx.graphics.Color;
import net.mslivo.core.engine.media_manager.CMediaSprite;
import net.mslivo.core.engine.ui_engine.ui.components.grid.Grid;
import net.mslivo.core.engine.ui_engine.ui.components.list.List;
import net.mslivo.core.engine.ui_engine.ui.tooltip.Tooltip;

public interface GridAction<T> extends CommonActions, ItemIconSupport<T>, ItemCellColorSupport<T>{

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
