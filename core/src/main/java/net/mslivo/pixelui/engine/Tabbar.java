package net.mslivo.pixelui.engine;

import com.badlogic.gdx.utils.Array;
import net.mslivo.pixelui.engine.actions.TabBarAction;

public class Tabbar extends Component {
    public Array<Tab> tabs;
    public int selectedTab;
    public TabBarAction tabBarAction;
    public boolean border;
    public int borderHeight;
    public int tabOffset;
    public boolean bigIconMode;

    Tabbar() {
    }
}
