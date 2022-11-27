package org.vnna.core.engine.ui_engine.gui.components.tabbar;

import org.vnna.core.engine.media_manager.media.CMediaFont;
import org.vnna.core.engine.media_manager.media.CMediaGFX;
import org.vnna.core.engine.ui_engine.gui.actions.TabAction;
import org.vnna.core.engine.ui_engine.gui.components.Component;

import java.util.ArrayList;

public class Tab {

    public String title;

    public int width;

    public TabAction tabAction;

    public TabBar tabBar;

    public CMediaFont font;

    public CMediaGFX icon;

    public ArrayList<Component> components;

    public int iconIndex;

    public int content_offset_x;

    public String name;

    public Object data;

}
