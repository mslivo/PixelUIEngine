package net.mslivo.core.engine.ui_engine;

import net.mslivo.core.engine.media_manager.MediaManager;
import net.mslivo.core.engine.media_manager.media.CMediaCursor;
import net.mslivo.core.engine.tools.Tools;
import net.mslivo.core.engine.ui_engine.state.UIConfig;
import net.mslivo.core.engine.ui_engine.state.UIEngineState;
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
        this.uiConfig = uiEngineState.uiEngineConfig;
        this.mediaManager = mediaManager;
    }


    public MouseTool create(String name, Object data, CMediaCursor cursor) {
        return create(name, data, cursor, cursor, null);
    }

    public MouseTool create(String name, Object data, CMediaCursor cursor, CMediaCursor cursorDown) {
        return create(name, data, cursor, cursorDown, null);
    }

    public MouseTool create(String name, Object data, CMediaCursor cursor, MouseToolAction mouseToolAction) {
        return create(name, data, cursor, cursor, mouseToolAction);
    }

    public MouseTool create(String name, Object data, CMediaCursor cursor, CMediaCursor cursorDown, MouseToolAction mouseToolAction) {
        MouseTool mouseTool = new MouseTool();
        mouseTool.name = name;
        mouseTool.data = data;
        mouseTool.cursor = cursor;
        mouseTool.cursorDown = cursorDown;
        mouseTool.mouseToolAction = mouseToolAction;
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

    public void setCursor(MouseTool mouseTool, CMediaCursor cursor) {
        if (mouseTool == null) return;
        mouseTool.cursor = cursor;
    }

    public void setCursorDown(MouseTool mouseTool, CMediaCursor cursorDown) {
        if (mouseTool == null) return;
        mouseTool.cursorDown = cursorDown;
    }

    public void setMouseToolAction(MouseTool mouseTool, MouseToolAction mouseToolAction) {
        if (mouseTool == null) return;
        mouseTool.mouseToolAction = mouseToolAction;
    }

}


