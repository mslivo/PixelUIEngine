package net.mslivo.core.engine.ui_engine.ui.notification;

import net.mslivo.core.engine.media_manager.media.CMediaFont;
import net.mslivo.core.engine.ui_engine.ui.actions.NotificationAction;

public class Notification {
    public STATE_NOTIFICATION state;
    public long timer;
    public String text;
    public float color_r, color_g, color_b, color_a;
    public CMediaFont font;
    public int displayTime;
    public int scroll;
    public int scrollMax;
    public NotificationAction notificationAction;
    public String name;
    public Object data;
    public boolean addedToScreen;
}
