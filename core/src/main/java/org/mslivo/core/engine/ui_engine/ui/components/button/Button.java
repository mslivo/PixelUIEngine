package org.mslivo.core.engine.ui_engine.ui.components.button;

import org.mslivo.core.engine.ui_engine.ui.actions.ButtonAction;
import org.mslivo.core.engine.ui_engine.ui.components.Component;

public abstract class Button extends Component {
    public ButtonAction buttonAction;
    public boolean pressed;
    public ButtonMode mode;
    public int offset_content_x;
    public int offset_content_y;
}
