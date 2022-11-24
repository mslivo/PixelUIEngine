package org.vnna.core.engine.ui_engine.gui.components.scrollbar;

import org.vnna.core.engine.ui_engine.gui.actions.ScrollBarAction;
import org.vnna.core.engine.ui_engine.gui.components.Component;

import java.util.ArrayList;

public abstract class ScrollBar extends Component {

    public float scrolled;

    public boolean buttonPressed;

    public ScrollBarAction scrollBarAction;
}
