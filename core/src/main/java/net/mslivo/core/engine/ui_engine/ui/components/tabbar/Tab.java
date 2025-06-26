package net.mslivo.core.engine.ui_engine.ui.components.tabbar;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Array;
import net.mslivo.core.engine.media_manager.CMediaFont;
import net.mslivo.core.engine.media_manager.CMediaSprite;
import net.mslivo.core.engine.ui_engine.ui.actions.TabAction;
import net.mslivo.core.engine.ui_engine.ui.components.Component;

import java.util.ArrayList;

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
