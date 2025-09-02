package net.mslivo.pixelui.engine;

import net.mslivo.pixelui.media_manager.MediaManager;
import net.mslivo.pixelui.utils.Tools;
import net.mslivo.pixelui.engine.actions.HotKeyAction;

import java.util.Arrays;

public final class APIHotkey {
    private final API api;
    private final UIEngineState uiEngineState;
    private final UICommonUtils uiCommonUtils;
    private final MediaManager mediaManager;
    private final UIEngineConfig uiEngineConfig;

    APIHotkey(API api, UIEngineState uiEngineState, UICommonUtils uiCommonUtils, MediaManager mediaManager) {
        this.api = api;
        this.uiEngineState = uiEngineState;
        this.uiCommonUtils = uiCommonUtils;
        this.mediaManager = mediaManager;
        this.uiEngineConfig = uiEngineState.config;

    }

    public final HotKeyAction DEFAULT_HOTKEY_ACTION = new HotKeyAction() {
    };

    public HotKey create(int[] keyCodes, HotKeyAction hotKeyAction) {
        HotKey hotKey = new HotKey();
        hotKey.pressed = false;
        hotKey.keyCodes = keyCodes != null ? Arrays.copyOf(keyCodes, keyCodes.length) : new int[]{};
        hotKey.hotKeyAction = hotKeyAction != null ? hotKeyAction : DEFAULT_HOTKEY_ACTION;
        hotKey.name = "";
        hotKey.data = null;
        return hotKey;
    }

    public void setKeyCodes(HotKey hotKey, int[] keyCodes) {
        if (hotKey == null) return;
        hotKey.keyCodes = Arrays.copyOf(keyCodes, keyCodes.length);
    }

    public void setHotKeyAction(HotKey hotKey, HotKeyAction hotKeyAction) {
        if (hotKey == null) return;
        hotKey.hotKeyAction = hotKeyAction != null ? hotKeyAction : DEFAULT_HOTKEY_ACTION;
    }

    public void setName(HotKey hotKey, String name) {
        if (hotKey == null) return;
        hotKey.name = Tools.Text.validString(name);
    }

    public void setData(HotKey hotKey, Object data) {
        if (hotKey == null) return;
        hotKey.data = data;
    }

    public String keysAsText(HotKey hotKey) {
        String[] names = new String[hotKey.keyCodes.length];
        for (int i = 0; i < hotKey.keyCodes.length; i++) {
            names[i] = com.badlogic.gdx.Input.Keys.toString(hotKey.keyCodes[i]);
        }
        return String.join("+", names);
    }
}


