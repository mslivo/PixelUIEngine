package net.mslivo.core.engine.ui_engine.ui.actions;

import net.mslivo.core.engine.ui_engine.ui.actions.extendable.CommonActions;
import net.mslivo.core.engine.ui_engine.ui.components.Tab;

public interface TabBarAction extends CommonActions {

    default void onChangeTab(int index, Tab tab){}

}
