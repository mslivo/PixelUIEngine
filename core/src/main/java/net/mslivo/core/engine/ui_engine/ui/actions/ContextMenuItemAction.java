package net.mslivo.core.engine.ui_engine.ui.actions;

public interface ContextMenuItemAction extends IconSupport, CellColorSupport {

    default void onSelect() {
    }

}