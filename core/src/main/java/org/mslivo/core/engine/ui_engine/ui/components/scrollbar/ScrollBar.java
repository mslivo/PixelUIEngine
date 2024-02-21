package org.mslivo.core.engine.ui_engine.ui.components.scrollbar;

import org.mslivo.core.engine.ui_engine.ui.actions.ScrollBarAction;
import org.mslivo.core.engine.ui_engine.ui.components.Component;

public abstract class ScrollBar extends Component {
    public float scrolled;
    public boolean buttonPressed;
    public ScrollBarAction scrollBarAction;
}
