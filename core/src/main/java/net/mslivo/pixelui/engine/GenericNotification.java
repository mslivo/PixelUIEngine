package net.mslivo.pixelui.engine;

public abstract sealed class GenericNotification permits Notification, TooltipNotification {
    public long timer;
    public int displayTime;
    public String name;
    public Object data;
    public boolean addedToScreen;

    GenericNotification() {
    }
}
