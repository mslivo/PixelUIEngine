package net.mslivo.core.engine.ui_engine.ui.mousetool;

import net.mslivo.core.engine.media_manager.media.CMediaSprite;
import net.mslivo.core.engine.ui_engine.ui.actions.MouseToolAction;

public class MouseTool {
    public String name;
    public Object data;
    public MouseToolAction mouseToolAction;
    public CMediaSprite cursor;
    public int cursorArrayIndex;
    public CMediaSprite cursorDown;
}
