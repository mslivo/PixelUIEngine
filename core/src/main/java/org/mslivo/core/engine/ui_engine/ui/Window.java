package org.mslivo.core.engine.ui_engine.ui;

import org.mslivo.core.engine.media_manager.media.CMediaFont;
import org.mslivo.core.engine.media_manager.media.CMediaGFX;
import org.mslivo.core.engine.ui_engine.ui.actions.MessageReceiverAction;
import org.mslivo.core.engine.ui_engine.ui.actions.UpdateAction;
import org.mslivo.core.engine.ui_engine.ui.actions.WindowAction;
import org.mslivo.core.engine.ui_engine.ui.components.Component;

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
    public float color_r, color_g, color_b,color_a;
    public boolean alwaysOnTop;
    public boolean folded;
    public boolean moveAble;
    public boolean hasTitleBar;
    public boolean visible;
    public boolean enforceScreenBounds;
    public WindowAction windowAction;
    public CMediaGFX icon;
    public int iconIndex;
    public ArrayList<MessageReceiverAction> messageReceiverActions;
    public boolean addedToScreen;
}
