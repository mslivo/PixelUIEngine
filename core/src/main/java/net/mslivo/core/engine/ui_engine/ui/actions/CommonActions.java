package net.mslivo.core.engine.ui_engine.ui.actions;

import net.mslivo.core.engine.ui_engine.ui.tooltip.Tooltip;

public interface CommonActions {

    default void onMouseClick(int button) {
    }

    default void onMouseDoubleClick(int button) {
    }

    default void onMouseScroll(float scrolled) {
    }

    default Tooltip onShowTooltip(){
        return null;
    }

}
