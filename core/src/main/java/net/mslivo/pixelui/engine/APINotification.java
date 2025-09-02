package net.mslivo.pixelui.engine;

import com.badlogic.gdx.graphics.Color;
import net.mslivo.pixelui.media_manager.MediaManager;
import net.mslivo.pixelui.utils.Tools;
import net.mslivo.pixelui.engine.constants.TOOLTIP_NOTIFICATION_STATE;
import net.mslivo.pixelui.engine.constants.TOP_NOTIFICATION_STATE;
import net.mslivo.pixelui.engine.actions.NotificationAction;

public final class APINotification {
    private final API api;
    private final UIEngineState uiEngineState;
    private final UICommonUtils uiCommonUtils;
    private final MediaManager mediaManager;
    private final UIEngineConfig uiEngineConfig;

    public final APITooltipNotification tooltip;

    APINotification(API api, UIEngineState uiEngineState, UICommonUtils uiCommonUtils, MediaManager mediaManager) {
        this.api = api;
        this.uiEngineState = uiEngineState;
        this.uiCommonUtils = uiCommonUtils;
        this.mediaManager = mediaManager;
        this.uiEngineConfig = uiEngineState.config;

        this.tooltip = new APITooltipNotification();
    }

    public final NotificationAction DEFAULT_NOTIFICATION_ACTION = new NotificationAction() {};

    public Notification create(String text) {
        return create(text, DEFAULT_NOTIFICATION_ACTION, false, uiEngineConfig.notification_defaultDisplayTime);
    }

    public Notification create(String text, NotificationAction notificationAction) {
        return create(text, notificationAction, false, uiEngineConfig.notification_defaultDisplayTime);
    }

    public Notification create(String text, NotificationAction notificationAction, boolean uiInteractionEnabled) {
        return create(text, notificationAction,uiInteractionEnabled, uiEngineConfig.notification_defaultDisplayTime);
    }

    public Notification create(String text, NotificationAction notificationAction, boolean uiInteractionEnabled, int displayTime) {
        Notification notification = new Notification();
        notification.text = Tools.Text.validString(text);
        notification.uiInteractionEnabled = uiInteractionEnabled;
        notification.displayTime = Math.max(displayTime,0);
        notification.color = new Color(uiEngineConfig.notification_defaultColor);
        notification.fontColor = uiEngineConfig.ui_font_defaultColor.cpy();
        notification.notificationAction = notificationAction != null ? notificationAction : DEFAULT_NOTIFICATION_ACTION;
        notification.timer = 0;
        int textWidth = mediaManager.fontTextWidth(uiEngineConfig.ui_font, notification.text);
        if (textWidth > uiEngineState.resolutionWidth) {
            int tooMuch = (textWidth - uiEngineState.resolutionWidth);
            notification.state = TOP_NOTIFICATION_STATE.INIT_SCROLL;
            notification.scroll = -(tooMuch / 2) - 4;
            notification.scrollMax = (tooMuch / 2) + 4;
        } else {
            notification.state = TOP_NOTIFICATION_STATE.INIT_DISPLAY;
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
        notification.notificationAction = notificationAction != null ? notificationAction : DEFAULT_NOTIFICATION_ACTION;
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

    public final class APITooltipNotification {

        public TooltipNotification create(int x, int y, Tooltip tooltip){
            return create(x,y,tooltip, uiEngineConfig.notification_tooltip_defaultDisplayTime);
        }

        public TooltipNotification create(int x, int y, Tooltip tooltip, int displayTime){
            TooltipNotification tooltipNotification= new TooltipNotification();
            tooltipNotification.x = x;
            tooltipNotification.y = y;
            tooltipNotification.tooltip = tooltip;
            tooltipNotification.displayTime = displayTime;
            tooltipNotification.timer = 0;
            tooltipNotification.state = TOOLTIP_NOTIFICATION_STATE.INIT;
            return tooltipNotification;
        }

        public void setPosition(TooltipNotification tooltipNotification, int x, int y){
            if(tooltipNotification == null)
                return;
            tooltipNotification.x = x;
            tooltipNotification.y = y;
        }

        public void setTooltip(TooltipNotification tooltipNotification, Tooltip tooltip){
            if(tooltipNotification == null)
                return;
            tooltipNotification.tooltip = tooltip;
        }

        public void setName(TooltipNotification tooltipNotification, String name) {
            if (tooltipNotification == null) return;
            tooltipNotification.name = Tools.Text.validString(name);
        }

        public void setData(TooltipNotification tooltipNotification, Object data) {
            if (tooltipNotification == null) return;
            tooltipNotification.data = data;
        }

    }






}
