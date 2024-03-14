package net.mslivo.core.engine.ui_engine.ui.components.button;

import net.mslivo.core.engine.ui_engine.ui.actions.ButtonAction;
import net.mslivo.core.engine.ui_engine.ui.components.Component;

public abstract class Button extends Component {
    public ButtonAction buttonAction;
    public boolean pressed;
    public ButtonMode mode;
    public boolean toggleDisabled;
    public int contentOffset_x;
    public int contentOffset_y;
}
