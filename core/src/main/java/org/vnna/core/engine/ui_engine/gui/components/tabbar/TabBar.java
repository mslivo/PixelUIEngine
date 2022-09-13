package org.vnna.core.engine.ui_engine.gui.components.tabbar;

import org.vnna.core.engine.ui_engine.gui.actions.TabBarAction;
import org.vnna.core.engine.ui_engine.gui.components.Component;

import java.util.ArrayList;

public class TabBar extends Component {

    public ArrayList<Tab> tabs;

    public int selectedTab;

    public TabBarAction tabBarAction;

    public boolean border;

    public int borderHeight;

    public int tabOffset;

}
