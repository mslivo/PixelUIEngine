package net.mslivo.core.engine.ui_engine;

import net.mslivo.core.engine.media_manager.MediaManager;
import net.mslivo.core.engine.media_manager.media.CMediaSprite;
import net.mslivo.core.engine.tools.Tools;
import net.mslivo.core.engine.ui_engine.state.UIEngineState;
import net.mslivo.core.engine.ui_engine.state.config.UIConfig;
import net.mslivo.core.engine.ui_engine.ui.actions.MouseToolAction;
import net.mslivo.core.engine.ui_engine.ui.mousetool.MouseTool;

public final class APIMouseTool {
    private API api;
    private UIEngineState uiEngineState;
    private UIConfig uiConfig;
    private MediaManager mediaManager;

    APIMouseTool(API api, UIEngineState uiEngineState, MediaManager mediaManager) {
        this.api = api;
        this.uiEngineState = uiEngineState;
        this.uiConfig = uiEngineState.config;
        this.mediaManager = mediaManager;
    }

    public MouseTool create(String name, Object data, CMediaSprite cursor) {
        return create(name, data, cursor, cursor, null);
    }

    public MouseTool create(String name, Object data, CMediaSprite cursor, CMediaSprite cursorDown) {
        return create(name, data, cursor, cursorDown, null);
    }

    public MouseTool create(String name, Object data, CMediaSprite cursor, MouseToolAction mouseToolAction) {
        return create(name, data, cursor, cursor, mouseToolAction);
    }

    public MouseTool create(String name, Object data, CMediaSprite cursor, CMediaSprite cursorDown, MouseToolAction mouseToolAction) {
        MouseTool mouseTool = new MouseTool();
        mouseTool.name = name;
        mouseTool.data = data;
        mouseTool.cursor = cursor;
        mouseTool.cursorDown = cursorDown;
        mouseTool.mouseToolAction = mouseToolAction;
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

    public void setMouseToolAction(MouseTool mouseTool, MouseToolAction mouseToolAction) {
        if (mouseTool == null) return;
        mouseTool.mouseToolAction = mouseToolAction;
    }

    public void setCursorArrayIndex(MouseTool mouseTool, int cursorArrayIndex) {
        if (mouseTool == null) return;
        mouseTool.cursorArrayIndex = Math.max(0,cursorArrayIndex);
    }


}


