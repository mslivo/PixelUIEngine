package net.mslivo.core.engine.ui_engine.ui.components;

import com.badlogic.gdx.utils.Array;
import net.mslivo.core.engine.ui_engine.ui.actions.TabBarAction;

public class Tabbar extends Component {

    public Array<Tab> tabs;

    public int selectedTab;

    public TabBarAction tabBarAction;

    public boolean border;

    public int borderHeight;

    public int tabOffset;

    public boolean bigIconMode;

}
