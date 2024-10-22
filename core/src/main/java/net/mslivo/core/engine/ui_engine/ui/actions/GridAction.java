package net.mslivo.core.engine.ui_engine.ui.actions;

import com.badlogic.gdx.graphics.Color;
import net.mslivo.core.engine.media_manager.CMediaSprite;
import net.mslivo.core.engine.ui_engine.ui.components.grid.Grid;
import net.mslivo.core.engine.ui_engine.ui.components.list.List;
import net.mslivo.core.engine.ui_engine.ui.tooltip.Tooltip;

public abstract class GridAction<T> extends CommonActions {

    public CMediaSprite icon(T gridItem) {
        return null;
    }

    public Tooltip toolTip(T gridItem) {
        return null;
    }

    public int iconIndex(T gridItem) {
        return 0;
    }

    public boolean onItemSelected(T gridItem) {
        return true;
    }

    public void onDragFromGrid(Grid fromGrid, int from_x, int from_y, int to_x, int to_y) {
    }

    public void onDragFromList(List fromList, int fromIndex, int to_x, int to_y) {
    }

    public boolean canDragFromGrid(Grid fromGrid) {
        return false;
    }

    public boolean canDragFromList(List fromList) {
        return false;
    }

    public Color cellColor(T gridItem, int x, int y) {
        return null;
    }

    public void onDragIntoApp(T gridItem, int x, int y, int screenX, int screenY) {
    }

    public boolean canDragIntoApp() {
        return false;
    }

}
