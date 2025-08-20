package net.mslivo.core.engine.ui_engine.ui.actions;

import com.badlogic.gdx.utils.Disposable;
import net.mslivo.core.engine.ui_engine.ui.contextmenu.ContextMenuItem;

public interface ContextMenuAction extends CommonActions, Displayable {

    default void onItemSelected(ContextMenuItem selectedItem) {
    }

}
