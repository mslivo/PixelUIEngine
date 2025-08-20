package net.mslivo.core.engine.ui_engine.ui.actions;

import net.mslivo.core.engine.ui_engine.ui.actions.support.CommonActions;
import net.mslivo.core.engine.ui_engine.ui.components.tabbar.Tab;

public interface TabBarAction extends CommonActions {

    default void onChangeTab(int index, Tab tab){}

}
