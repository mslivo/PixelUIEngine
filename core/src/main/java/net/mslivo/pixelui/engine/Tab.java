package net.mslivo.pixelui.engine;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Array;
import net.mslivo.pixelui.engine.actions.TabAction;

public class Tab {
    public String title;
    public int width;
    public TabAction tabAction;
    public Tabbar addedToTabBar;
    public Color fontColor;
    public Array<Component> components;
    public String name;
    public Object data;
    public boolean disabled;

    Tab() {
    }
}
