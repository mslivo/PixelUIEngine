package net.mslivo.core.engine.ui_engine;

import com.badlogic.gdx.graphics.Color;
import net.mslivo.core.engine.media_manager.MediaManager;
import net.mslivo.core.engine.tools.Tools;
import net.mslivo.core.engine.ui_engine.constants.TOP_NOTIFICATION_STATE;
import net.mslivo.core.engine.ui_engine.state.config.UIConfig;
import net.mslivo.core.engine.ui_engine.state.UIEngineState;
import net.mslivo.core.engine.ui_engine.ui.actions.TopNotificationAction;
import net.mslivo.core.engine.ui_engine.ui.notification.TooltipNotification;
import net.mslivo.core.engine.ui_engine.ui.notification.TopNotification;
import net.mslivo.core.engine.ui_engine.ui.tooltip.Tooltip;

public final class APINotification {
    private final API api;
    private final UIEngineState uiEngineState;
    private final MediaManager mediaManager;
    private final UIConfig uiConfig;

    public final APITopNotification top = new APITopNotification();
    public final APITooltipNotification tooltip = new APITooltipNotification();

    APINotification(API api, UIEngineState uiEngineState, MediaManager mediaManager) {
        this.api = api;
        this.uiEngineState = uiEngineState;
        this.mediaManager = mediaManager;
        this.uiConfig = uiEngineState.config;
    }

    public final class APITopNotification{
        public final TopNotificationAction DEFAULT_NOTIFICATION_ACTION = new TopNotificationAction() {};

        public TopNotification create(String text) {
            return create(text, DEFAULT_NOTIFICATION_ACTION, false, uiConfig.notification_top_defaultDisplayTime);
        }

        public TopNotification create(String text, TopNotificationAction topNotificationAction) {
            return create(text, topNotificationAction, false, uiConfig.notification_top_defaultDisplayTime);
        }

        public TopNotification create(String text, TopNotificationAction topNotificationAction, boolean clickAble) {
            return create(text, topNotificationAction,clickAble, uiConfig.notification_top_defaultDisplayTime);
        }

        public TopNotification create(String text, TopNotificationAction topNotificationAction, boolean clickAble, int displayTime) {
            TopNotification notification = new TopNotification();
            notification.text = Tools.Text.validString(text);
            notification.clickAble = clickAble;
            notification.displayTime = Math.max(displayTime,0);
            notification.color = new Color(uiConfig.notification_top_defaultColor);
            notification.fontColor = uiConfig.ui_font_defaultColor.cpy();
            notification.topNotificationAction = topNotificationAction != null ? topNotificationAction : DEFAULT_NOTIFICATION_ACTION;
            notification.displayTimer = 0;
            int textWidth = mediaManager.fontTextWidth(uiConfig.ui_font, notification.text);
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

        public void setName(TopNotification notification, String name) {
            if (notification == null) return;
            notification.name = Tools.Text.validString(name);
        }

        public void setData(TopNotification notification, Object data) {
            if (notification == null) return;
            notification.data = data;
        }

        public void setNotificationAction(TopNotification notification, TopNotificationAction topNotificationAction) {
            if (notification == null) return;
            notification.topNotificationAction = topNotificationAction;
        }

        public void setDisplayTime(TopNotification notification, int displayTime) {
            if (notification == null) return;
            notification.displayTime = Math.max(displayTime, 0);
        }

        public void setColor(TopNotification notification, Color color) {
            if (notification == null || color == null) return;
            notification.color.set(color);
        }

        public void setFontColor(TopNotification notification, Color color) {
            if (notification == null) return;
            notification.fontColor.set(color);
        }

        public void setText(TopNotification topNotification, String text) {
            if (topNotification == null) return;
            topNotification.text = Tools.Text.validString(text);
        }
    }

    public final class APITooltipNotification {

        public TooltipNotification create(int x, int y, Tooltip tooltip){
            return create(x,y,tooltip,uiConfig.notification_tooltip_defaultDisplayTime);
        }

        public TooltipNotification create(int x, int y, Tooltip tooltip, int displayTime){
            TooltipNotification tooltipNotification= new TooltipNotification();
            tooltipNotification.x = x;
            tooltipNotification.y = y;
            tooltipNotification.tooltip = tooltip;
            tooltipNotification.displayTime = displayTime;
            tooltipNotification.displayTimer = 0;
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



    }






}
