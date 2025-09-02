package net.mslivo.pixelui.engine;

import net.mslivo.pixelui.engine.constants.BUTTON_MODE;
import net.mslivo.pixelui.engine.actions.ButtonAction;

public abstract sealed class Button extends Component permits ImageButton, TextButton {
    public ButtonAction buttonAction;
    public boolean pressed;
    public BUTTON_MODE mode;
    public boolean toggleDisabled;
    public int contentOffset_x;
    public int contentOffset_y;

    Button() {
    }
}
