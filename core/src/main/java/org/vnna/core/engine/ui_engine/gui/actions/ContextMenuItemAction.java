package org.vnna.core.engine.ui_engine.gui.actions;

public interface ContextMenuItemAction {

    default void onSelect(){
        return;
    }
}
