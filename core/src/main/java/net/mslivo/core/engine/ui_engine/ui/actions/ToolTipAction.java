package net.mslivo.core.engine.ui_engine.ui.actions;

import net.mslivo.core.engine.ui_engine.ui.actions.extendable.Displayable;

public interface ToolTipAction extends Displayable {

    default void onUpdate() {
    }

}
