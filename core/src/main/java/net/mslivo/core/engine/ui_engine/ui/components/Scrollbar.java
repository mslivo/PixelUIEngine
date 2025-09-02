package net.mslivo.core.engine.ui_engine.ui.components;

import net.mslivo.core.engine.ui_engine.ui.actions.ScrollBarAction;

public abstract sealed class Scrollbar extends Component permits ScrollbarHorizontal, ScrollbarVertical {
    public float scrolled;
    public boolean buttonPressed;
    public ScrollBarAction scrollBarAction;
}
