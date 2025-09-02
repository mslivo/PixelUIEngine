package net.mslivo.pixelui.engine;

import com.badlogic.gdx.graphics.Color;
import net.mslivo.pixelui.engine.constants.TOP_NOTIFICATION_STATE;
import net.mslivo.pixelui.engine.actions.NotificationAction;

public final class Notification extends GenericNotification {
    public String text;
    public TOP_NOTIFICATION_STATE state;
    public boolean uiInteractionEnabled;
    public Color color;
    public Color fontColor;
    public int scroll;
    public int scrollMax;
    public NotificationAction notificationAction;

    Notification() {
    }
}
