package net.mslivo.pixelui.engine.actions;

import net.mslivo.pixelui.engine.actions.common.CommonActions;

public interface CheckboxAction extends CommonActions {

    default void onCheck(boolean checked) {
    }

}
