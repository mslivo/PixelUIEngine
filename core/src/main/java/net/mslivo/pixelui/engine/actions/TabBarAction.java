package net.mslivo.pixelui.engine.actions;

import net.mslivo.pixelui.engine.actions.common.CommonActions;
import net.mslivo.pixelui.engine.Tab;

public interface TabBarAction extends CommonActions {

    default void onChangeTab(int index, Tab tab){}

}
