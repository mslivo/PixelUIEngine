package org.vnna.core.engine.ui_engine.gui.notification;

import org.vnna.core.engine.media_manager.color.CColor;
import org.vnna.core.engine.media_manager.media.CMediaFont;
import org.vnna.core.engine.ui_engine.gui.actions.NotificationAction;

public class Notification {

    public STATE_NOTIFICATION state;

    public long timer;

    public String text;

    public CColor color;

    public CMediaFont font;

    public int displayTime;

    public float scroll;

    public int scrollMax;

    public NotificationAction notificationAction;

}
