package net.mslivo.core.engine.ui_engine.ui.components;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Array;
import net.mslivo.core.engine.ui_engine.ui.actions.TabAction;

public class Tab {
    public String title;
    public int width;
    public TabAction tabAction;
    public Tabbar addedToTabBar;
    public Color fontColor;
    public Array<Component> components;
    public String name;
    public Object data;
}
