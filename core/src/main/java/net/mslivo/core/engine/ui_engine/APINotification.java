package net.mslivo.core.engine.ui_engine;

import com.badlogic.gdx.graphics.Color;
import net.mslivo.core.engine.media_manager.MediaManager;
import net.mslivo.core.engine.media_manager.CMediaFont;
import net.mslivo.core.engine.tools.Tools;
import net.mslivo.core.engine.ui_engine.constants.STATE_NOTIFICATION;
import net.mslivo.core.engine.ui_engine.state.config.UIConfig;
import net.mslivo.core.engine.ui_engine.state.UIEngineState;
import net.mslivo.core.engine.ui_engine.ui.actions.NotificationAction;
import net.mslivo.core.engine.ui_engine.ui.notification.Notification;

public final class APINotification {
    private final API api;
    private final UIEngineState uiEngineState;
    private final MediaManager mediaManager;
    private final UIConfig uiConfig;

    APINotification(API api, UIEngineState uiEngineState, MediaManager mediaManager) {
        this.api = api;
        this.uiEngineState = uiEngineState;
        this.mediaManager = mediaManager;
        this.uiConfig = uiEngineState.config;
    }


    private NotificationAction defaultNotificationAction() {
        return new NotificationAction() {
        };
    }

    public Notification create(String text) {
        return create(text, defaultNotificationAction(), false, uiConfig.notification_defaultDisplayTime);
    }

    public Notification create(String text, NotificationAction notificationAction) {
        return create(text, notificationAction, false, uiConfig.notification_defaultDisplayTime);
    }

    public Notification create(String text, NotificationAction notificationAction, boolean clickAble) {
        return create(text, notificationAction,clickAble, uiConfig.notification_defaultDisplayTime);
    }

    public Notification create(String text, NotificationAction notificationAction, boolean clickAble, int displayTime) {
        Notification notification = new Notification();
        notification.text = Tools.Text.validString(text);
        notification.clickAble = clickAble;
        notification.displayTime = Math.max(displayTime,0);
        notification.color = new Color(uiConfig.notification_defaultColor);
        notification.fontColor = uiConfig.ui_font_defaultColor.cpy();
        notification.notificationAction = notificationAction;
        notification.timer = 0;
        int textWidth = mediaManager.getCMediaFontTextWidth(uiConfig.ui_font, notification.text);
        if (textWidth > uiEngineState.resolutionWidth) {
            int tooMuch = (textWidth - uiEngineState.resolutionWidth);
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
        notification.displayTime = Math.max(displayTime, 0);
    }

    public void setColor(Notification notification, Color color) {
        if (notification == null || color == null) return;
        notification.color.set(color);
    }

    public void setFontColor(Notification notification, Color color) {
        if (notification == null) return;
        notification.fontColor.set(color);
    }

    public void setText(Notification notification, String text) {
        if (notification == null) return;
        notification.text = Tools.Text.validString(text);
    }

}
