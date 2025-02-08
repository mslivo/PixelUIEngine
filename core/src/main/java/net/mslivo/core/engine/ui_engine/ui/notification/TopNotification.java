package net.mslivo.core.engine.ui_engine.ui.notification;

import com.badlogic.gdx.graphics.Color;
import net.mslivo.core.engine.ui_engine.constants.TOP_NOTIFICATION_STATE;
import net.mslivo.core.engine.ui_engine.ui.actions.TopNotificationAction;

public final class TopNotification extends Notification {
    public String text;
    public TOP_NOTIFICATION_STATE state;
    public boolean clickAble;
    public Color color;
    public Color fontColor;
    public int scroll;
    public int scrollMax;
    public TopNotificationAction topNotificationAction;
}
