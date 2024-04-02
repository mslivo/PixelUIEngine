package net.mslivo.core.engine.ui_engine.ui.actions;

import com.badlogic.gdx.graphics.Color;
import net.mslivo.core.engine.ui_engine.ui.tooltip.ToolTip;
import net.mslivo.core.engine.media_manager.media.CMediaSprite;
import net.mslivo.core.engine.ui_engine.ui.components.grid.Grid;
import net.mslivo.core.engine.ui_engine.ui.components.list.List;

public abstract class GridAction<T> extends CommonActions {

    public CMediaSprite icon(T listItem) {
        return null;
    }

    public ToolTip toolTip(T listItem) {
        return null;
    }

    public int iconIndex(T listItem) {
        return 0;
    }

    public void onItemSelected(T listItem) {
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

    public boolean canDragIntoScreen() {return false;}

    public Color cellColor(T listItem, int x, int y) {
        return null;
    }

    public void onDragIntoScreen(T listItem, int x, int y, int screenX, int screenY) {
    }



}
