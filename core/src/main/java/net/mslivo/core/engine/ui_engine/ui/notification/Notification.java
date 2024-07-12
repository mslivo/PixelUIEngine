package net.mslivo.core.engine.ui_engine.ui.notification;

import com.badlogic.gdx.graphics.Color;
import net.mslivo.core.engine.media_manager.CMediaFont;
import net.mslivo.core.engine.ui_engine.constants.STATE_NOTIFICATION;
import net.mslivo.core.engine.ui_engine.ui.actions.NotificationAction;

public class Notification {
    public STATE_NOTIFICATION state;
    public long timer;
    public String text;
    public Color color;
    public boolean clickAble;
    public CMediaFont font;
    public int displayTime;
    public int scroll;
    public int scrollMax;
    public NotificationAction notificationAction;
    public String name;
    public Object data;
    public boolean addedToScreen;
}
