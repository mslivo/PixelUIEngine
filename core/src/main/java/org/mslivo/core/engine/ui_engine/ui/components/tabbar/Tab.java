package org.mslivo.core.engine.ui_engine.ui.components.tabbar;

import org.mslivo.core.engine.media_manager.media.CMediaFont;
import org.mslivo.core.engine.media_manager.media.CMediaGFX;
import org.mslivo.core.engine.ui_engine.ui.actions.TabAction;
import org.mslivo.core.engine.ui_engine.ui.components.Component;

import java.util.ArrayList;

public class Tab {
    public String title;
    public int width;
    public TabAction tabAction;
    public TabBar addedToTabBar;
    public CMediaFont font;
    public CMediaGFX icon;
    public ArrayList<Component> components;
    public int iconIndex;
    public int content_offset_x;
    public String name;
    public Object data;
}