package net.mslivo.core.engine.ui_engine.ui;

import net.mslivo.core.engine.media_manager.media.CMediaFont;
import net.mslivo.core.engine.media_manager.media.CMediaSprite;
import net.mslivo.core.engine.ui_engine.ui.actions.UpdateAction;
import net.mslivo.core.engine.ui_engine.ui.actions.WindowAction;
import net.mslivo.core.engine.ui_engine.ui.components.Component;

import java.util.ArrayList;

/**
 * Created by Admin on 09.03.2019.
 */
public class Window {
    public int x, y, width, height;
    public String title;
    public CMediaFont font;
    public ArrayList<Component> components;
    public ArrayList<UpdateAction> updateActions;
    public String name;
    public Object data;
    public float color_r, color_g, color_b, color_a;
    public boolean alwaysOnTop;
    public boolean folded;
    public boolean moveAble;
    public boolean hasTitleBar;
    public boolean visible;
    public boolean enforceScreenBounds;
    public WindowAction windowAction;
    public CMediaSprite icon;
    public int iconIndex;
    public boolean addedToScreen;
}
