package net.mslivo.core.engine.ui_engine.ui;

import com.badlogic.gdx.graphics.Color;
import net.mslivo.core.engine.media_manager.CMediaFont;
import net.mslivo.core.engine.media_manager.CMediaSprite;
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
    public Color fontColor;
    public ArrayList<Component> components;
    public ArrayList<UpdateAction> updateActions;
    public String name;
    public Object data;
    public Color color;
    public boolean alwaysOnTop;
    public boolean folded;
    public boolean moveAble;
    public boolean hasTitleBar;
    public boolean visible;
    public boolean enforceScreenBounds;
    public WindowAction windowAction;
    public boolean addedToScreen;
}
