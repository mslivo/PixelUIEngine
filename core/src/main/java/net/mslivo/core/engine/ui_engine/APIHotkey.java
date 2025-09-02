package net.mslivo.core.engine.ui_engine;

import net.mslivo.core.engine.media_manager.MediaManager;
import net.mslivo.core.engine.tools.Tools;
import net.mslivo.core.engine.ui_engine.state.UIEngineState;
import net.mslivo.core.engine.ui_engine.state.config.UIConfig;
import net.mslivo.core.engine.ui_engine.ui.actions.HotKeyAction;
import net.mslivo.core.engine.ui_engine.ui.hotkeys.HotKey;

import java.util.Arrays;

public final class APIHotkey {
    private final API api;
    private final UIEngineState uiEngineState;
    private final UIConfig uiConfig;
    private final MediaManager mediaManager;

    APIHotkey(API api, UIEngineState uiEngineState, MediaManager mediaManager) {
        this.api = api;
        this.uiEngineState = uiEngineState;
        this.uiConfig = uiEngineState.config;
        this.mediaManager = mediaManager;
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


