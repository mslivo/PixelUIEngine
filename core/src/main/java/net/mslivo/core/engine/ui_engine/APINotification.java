package net.mslivo.core.engine.ui_engine;

import com.badlogic.gdx.graphics.Color;
import net.mslivo.core.engine.media_manager.MediaManager;
import net.mslivo.core.engine.media_manager.media.CMediaFont;
import net.mslivo.core.engine.tools.Tools;
import net.mslivo.core.engine.ui_engine.constants.STATE_NOTIFICATION;
import net.mslivo.core.engine.ui_engine.state.UIConfig;
import net.mslivo.core.engine.ui_engine.state.UIEngineState;
import net.mslivo.core.engine.ui_engine.ui.actions.NotificationAction;
import net.mslivo.core.engine.ui_engine.ui.notification.Notification;

public final class APINotification {
    private API api;
    private UIEngineState uiEngineState;
    private MediaManager mediaManager;
    private UIConfig uiConfig;

    APINotification(API api, UIEngineState uiEngineState, MediaManager mediaManager) {
        this.api = api;
        this.uiEngineState = uiEngineState;
        this.mediaManager = mediaManager;
        this.uiConfig = this.uiEngineState.uiEngineConfig;
    }


    private NotificationAction defaultNotificationAction() {
        return new NotificationAction() {
        };
    }

    public Notification create(String text) {
        return create(text, defaultNotificationAction(), uiConfig.notification_defaultDisplayTime);
    }

    public Notification create(String text, NotificationAction notificationAction) {
        return create(text, notificationAction, uiConfig.notification_defaultDisplayTime);
    }

    public Notification create(String text, NotificationAction notificationAction, int displayTime) {
        Notification notification = new Notification();
        notification.text = Tools.Text.validString(text);
        notification.displayTime = displayTime;
        notification.color_r = uiConfig.notification_defaultColor.r;
        notification.color_g = uiConfig.notification_defaultColor.g;
        notification.color_b = uiConfig.notification_defaultColor.b;
        notification.color_a = uiConfig.notification_defaultColor.a;
        notification.font = uiConfig.notification_defaultFont;
        notification.notificationAction = notificationAction;
        notification.timer = 0;
        int textWidth = mediaManager.getCMediaFontTextWidth(notification.font, notification.text);
        if (textWidth > uiEngineState.resolutionWidth_ui) {
            int tooMuch = (textWidth - uiEngineState.resolutionWidth_ui);
            notification.state = STATE_NOTIFICATION.INIT_SCROLL;
            notification.scroll = -(tooMuch / 2) - 4;
            notification.scrollMax = (tooMuch / 2) + 4;
        } else {
            notification.state = STATE_NOTIFICATION.INIT_DISPLAY;
            notification.scroll = notification.scrollMax = 0;
        }
        return notification;
    }

    public void setName(Notification notification, String name) {
        if (notification == null) return;
        notification.name = Tools.Text.validString(name);
    }

    public void setData(Notification notification, Object data) {
        if (notification == null) return;
        notification.data = data;
    }

    public void setNotificationAction(Notification notification, NotificationAction notificationAction) {
        if (notification == null) return;
        notification.notificationAction = notificationAction;
    }

    public void setDisplayTime(Notification notification, int displayTime) {
        if (notification == null) return;
        notification.displayTime = Math.clamp(displayTime, 0, Integer.MAX_VALUE);
    }

    public void setColor(Notification notification, float r, float g, float b, float a) {
        if (notification == null) return;
        notification.color_r = r;
        notification.color_g = g;
        notification.color_b = b;
        notification.color_a = a;
    }

    public void setColor(Notification notification, Color color) {
        if (notification == null || color == null) return;
        setColor(notification, color.r, color.g, color.b, color.a);
    }

    public void setFont(Notification notification, CMediaFont font) {
        if (notification == null) return;
        notification.font = font;
    }

    public void setText(Notification notification, String text) {
        if (notification == null) return;
        notification.text = Tools.Text.validString(text);
    }

}
