package org.vnna.core.engine.ui_engine.gui.tool;

import org.vnna.core.engine.media_manager.media.CMediaCursor;

public abstract class MouseTool {

    public ToolAction toolAction;

    public CMediaCursor cursor;

    public CMediaCursor cursor_down;

    private CMediaCursor override_cursor;

    private boolean overrideCursor;

    public CMediaCursor getOverrideCursor() {
        return override_cursor;
    }

    public void overrideCursor(CMediaCursor cursor_override){
        this.override_cursor = cursor_override;
        overrideCursor = true;
        return;
    }

    public void setToolAction(ToolAction toolAction){
        this.toolAction = toolAction;
    }

    public boolean isCursorOverride(){
        return overrideCursor;
    }

    public void resetCursorOverride(){
        overrideCursor = false;
        return;
    }

    public MouseTool(CMediaCursor cursor, ToolAction toolAction){
        this.cursor = cursor;
        this.cursor_down = cursor;
        this.toolAction = toolAction;
        this.override_cursor = null;
        this.overrideCursor = false;
    }

    public MouseTool(CMediaCursor cursor, CMediaCursor cursor_down, ToolAction toolAction){
        this.cursor = cursor;
        this.cursor_down = cursor_down;
        this.toolAction = toolAction;
        this.override_cursor = null;
        this.overrideCursor = false;
    }

}
