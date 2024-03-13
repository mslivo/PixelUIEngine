package org.mslivo.core.engine.ui_engine.ui.components.button;

import org.mslivo.core.engine.ui_engine.ui.actions.ButtonAction;
import org.mslivo.core.engine.ui_engine.ui.components.Component;

public abstract class Button extends Component {
    public ButtonAction buttonAction;
    public boolean pressed;
    public ButtonMode mode;
    public boolean toggleDisabled;
    public int contentOffset_x;
    public int contentOffset_y;
}
