package net.mslivo.core.engine.ui_engine.ui.actions.support;

import net.mslivo.core.engine.ui_engine.ui.tooltip.Tooltip;

public interface CommonActions {

    default void onMousePress(int button) {
    }

    default void onMouseRelease(int button) {
    }

    default void onMouseDoubleClick(int button) {
    }

    default void onMouseScroll(float scrolled) {
    }

    default Tooltip onShowTooltip(){
        return null;
    }

}
