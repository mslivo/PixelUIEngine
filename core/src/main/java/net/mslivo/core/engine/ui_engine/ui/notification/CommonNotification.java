package net.mslivo.core.engine.ui_engine.ui.notification;

public abstract sealed class CommonNotification permits Notification, TooltipNotification {
    public long timer;
    public int displayTime;
    public String name;
    public Object data;
    public boolean addedToScreen;
}
