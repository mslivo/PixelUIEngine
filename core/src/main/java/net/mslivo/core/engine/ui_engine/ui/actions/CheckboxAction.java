package net.mslivo.core.engine.ui_engine.ui.actions;

import net.mslivo.core.engine.ui_engine.ui.actions.extendable.CommonActions;

public interface CheckboxAction extends CommonActions {

    default void onCheck(boolean checked) {
    }

}
