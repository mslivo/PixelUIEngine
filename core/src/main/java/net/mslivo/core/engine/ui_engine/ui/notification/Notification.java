package net.mslivo.core.engine.ui_engine.ui.notification;

public abstract sealed class Notification permits TopNotification, TooltipNotification {
    public long displayTimer;
    public int displayTime;
    public String name;
    public Object data;
    public boolean addedToScreen;
}
