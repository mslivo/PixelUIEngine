package net.mslivo.core.engine.ui_engine.ui.components.scrollbar;

import net.mslivo.core.engine.ui_engine.ui.actions.ScrollBarAction;
import net.mslivo.core.engine.ui_engine.ui.components.Component;

public abstract class Scrollbar extends Component {
    public float scrolled;
    public boolean buttonPressed;
    public ScrollBarAction scrollBarAction;
}
