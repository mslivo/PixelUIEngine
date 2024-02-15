package org.mslivo.core.engine.ui_engine.gui.components.scrollbar;

import org.mslivo.core.engine.ui_engine.gui.actions.ScrollBarAction;
import org.mslivo.core.engine.ui_engine.gui.components.Component;

public abstract class ScrollBar extends Component {
    public float scrolled;
    public boolean buttonPressed;
    public ScrollBarAction scrollBarAction;
}
