package net.mslivo.core.engine.ui_engine.ui.components;

import net.mslivo.core.engine.ui_engine.constants.BUTTON_MODE;
import net.mslivo.core.engine.ui_engine.ui.actions.ButtonAction;

public abstract sealed class Button extends Component permits ImageButton, TextButton {
    public ButtonAction buttonAction;
    public boolean pressed;
    public BUTTON_MODE mode;
    public boolean toggleDisabled;
    public int contentOffset_x;
    public int contentOffset_y;
}
