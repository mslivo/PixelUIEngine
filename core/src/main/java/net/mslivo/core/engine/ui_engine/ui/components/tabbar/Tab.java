package net.mslivo.core.engine.ui_engine.ui.components.tabbar;

import net.mslivo.core.engine.media_manager.media.CMediaFont;
import net.mslivo.core.engine.media_manager.media.CMediaSprite;
import net.mslivo.core.engine.ui_engine.ui.actions.TabAction;
import net.mslivo.core.engine.ui_engine.ui.components.Component;

import java.util.ArrayList;

public class Tab {
    public String title;
    public int width;
    public TabAction tabAction;
    public Tabbar addedToTabBar;
    public CMediaFont font;
    public CMediaSprite icon;
    public ArrayList<Component> components;
    public int iconIndex;
    public String name;
    public Object data;
}
