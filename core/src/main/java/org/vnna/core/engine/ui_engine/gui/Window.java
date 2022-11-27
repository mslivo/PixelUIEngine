package org.vnna.core.engine.ui_engine.gui;

import org.vnna.core.engine.media_manager.color.CColor;
import org.vnna.core.engine.media_manager.media.CMediaFont;
import org.vnna.core.engine.media_manager.media.CMediaGFX;
import org.vnna.core.engine.ui_engine.gui.actions.MessageReceiverAction;
import org.vnna.core.engine.ui_engine.gui.actions.UpdateAction;
import org.vnna.core.engine.ui_engine.gui.actions.WindowAction;
import org.vnna.core.engine.ui_engine.gui.components.Component;

import java.util.ArrayList;
import java.util.Deque;

/**
 * Created by Admin on 09.03.2019.
 */
public class Window {

    public int x, y, width, height;

    public String title;

    public CMediaFont font;

    public ArrayList<Component> components;

    public Deque<Component> addComponentsQueue;

    public Deque<Component> removeComponentsQueue;

    public ArrayList<UpdateAction> updateActions;

    public String name;

    public Object data;

    public CColor color;

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

}
