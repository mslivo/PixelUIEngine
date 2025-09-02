package net.mslivo.pixelui.engine;

import net.mslivo.pixelui.engine.actions.HotKeyAction;

public class HotKey {
    public int[] keyCodes;
    public boolean pressed;
    public HotKeyAction hotKeyAction;
    public String name;
    public Object data;

    HotKey() {
    }
}
