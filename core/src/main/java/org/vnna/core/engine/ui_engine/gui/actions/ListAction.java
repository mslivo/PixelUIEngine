package org.vnna.core.engine.ui_engine.gui.actions;

import org.vnna.core.engine.media_manager.color.FColor;
import org.vnna.core.engine.media_manager.media.CMediaGFX;
import org.vnna.core.engine.ui_engine.gui.components.inventory.Inventory;
import org.vnna.core.engine.ui_engine.gui.components.list.List;
import org.vnna.core.engine.ui_engine.gui.tooltip.ToolTip;

import java.util.HashSet;

public interface ListAction<T extends Object> {

    default CMediaGFX icon(T listItem){
        return null;
    };

    default ToolTip toolTip(T listItem){ return null;}

    default int iconArrayIndex(T listItem){ // if CMedia is CMediaArray
        return 0;
    };

    default String text(T listItem){
        return listItem.toString();
    };

    default void onItemSelected(T listItem){
        return;
    };

    default void onItemsSelected(HashSet<T> listItems){
        return;
    }; // Multiselect

    /* Drag */

    default void onDragFromList(List fromList, int fromIndex, int toIndex){ return; }

    default void onDragFromInventory(Inventory fromInventory, int from_x, int from_y, int toIndex){ return; }

    default boolean canDragFromList(List list){ return false;}

    default boolean canDragFromInventory(Inventory fromInventory){ return false;}

    default FColor cellColor(T listItem){
        return null;
    }

    default void onMouseClick(int button){
        return;
    }

    default void onMouseDoubleClick(int button){
        return;
    }

    default void onDragIntoScreen(T listItem, int index, int mouseX, int mouseY) {
        return;
    }

    default void onMouseScroll(float scrolled){return;}
}
