package org.vnna.core.engine.ui_engine.gui.components.button;

import org.vnna.core.engine.ui_engine.gui.actions.ButtonAction;
import org.vnna.core.engine.ui_engine.gui.components.Component;

public abstract class Button extends Component {

    public ButtonAction buttonAction;

    public boolean pressed;

    public boolean canHold;

    public boolean toggleMode;

    public int offset_content_x;

    public int offset_content_y;

}
