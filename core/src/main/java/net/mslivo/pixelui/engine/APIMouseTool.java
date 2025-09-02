package net.mslivo.pixelui.engine;

import net.mslivo.pixelui.media_manager.MediaManager;
import net.mslivo.pixelui.media_manager.CMediaSprite;
import net.mslivo.pixelui.utils.Tools;

public final class APIMouseTool {
    private final API api;
    private final UIEngineState uiEngineState;
    private final UICommonUtils uiCommonUtils;
    private final MediaManager mediaManager;
    private final UIEngineConfig uiEngineConfig;


    APIMouseTool(API api, UIEngineState uiEngineState, UICommonUtils uiCommonUtils, MediaManager mediaManager) {
        this.api = api;
        this.uiEngineState = uiEngineState;
        this.uiCommonUtils = uiCommonUtils;
        this.mediaManager = mediaManager;
        this.uiEngineConfig = uiEngineState.config;
    }

    public MouseTool create(String name, Object data, CMediaSprite cursor) {
        return create(name, data, cursor, cursor);
    }

    public MouseTool create(String name, Object data, CMediaSprite cursor, CMediaSprite cursorDown) {
        MouseTool mouseTool = new MouseTool();
        mouseTool.name = name;
        mouseTool.data = data;
        mouseTool.cursor = cursor;
        mouseTool.cursorDown = cursorDown;
        mouseTool.cursorArrayIndex = 0;
        return mouseTool;
    }

    public void setName(MouseTool mouseTool, String name) {
        if (mouseTool == null) return;
        mouseTool.name = Tools.Text.validString(name);
    }

    public void setData(MouseTool mouseTool, Object data) {
        if (mouseTool == null) return;
        mouseTool.data = data;
    }

    public void setCursor(MouseTool mouseTool, CMediaSprite cursor) {
        if (mouseTool == null) return;
        mouseTool.cursor = cursor;
    }

    public void setCursorDown(MouseTool mouseTool, CMediaSprite cursorDown) {
        if (mouseTool == null) return;
        mouseTool.cursorDown = cursorDown;
    }

    public void setCursorArrayIndex(MouseTool mouseTool, int cursorArrayIndex) {
        if (mouseTool == null) return;
        mouseTool.cursorArrayIndex = Math.max(0, cursorArrayIndex);
    }

}


