package net.mslivo.core.engine.ui_engine.ui.actions;

import com.badlogic.gdx.graphics.Color;
import net.mslivo.core.engine.media_manager.media.CMediaSprite;
import net.mslivo.core.engine.ui_engine.ui.components.grid.Grid;
import net.mslivo.core.engine.ui_engine.ui.components.list.List;
import net.mslivo.core.engine.ui_engine.ui.tooltip.ToolTip;

public abstract class ListAction<T> extends CommonActions {

    public CMediaSprite icon(T listItem) {
        return null;
    }

    public ToolTip toolTip(T listItem) {
        return null;
    }

    public int iconIndex(T listItem) { // if CMedia is CMediaArray
        return 0;
    }

    public String text(T listItem) {
        return listItem.toString();
    }

    public boolean onItemSelected(T listItem) {
        return true;
    }

    public void onScrolled(float scrolled) {

    }
    /* Drag */

    public void onDragFromList(List fromList, int fromIndex, int toIndex) {
    }

    public void onDragFromGrid(Grid fromGrid, int from_x, int from_y, int toIndex) {
    }

    public boolean canDragFromList(List list) {
        return false;
    }

    public boolean canDragFromGrid(Grid fromGrid) {
        return false;
    }

    public Color cellColor(T listItem) {
        return null;
    }

    public void onDragIntoApp(T listItem, int index, int mouseX, int mouseY) {
    }

    public boolean canDragIntoApp() {
        return false;
    }

}
