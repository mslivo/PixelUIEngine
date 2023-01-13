package org.mslivo.core.engine.ui_engine.gui.components.textfield;

import org.mslivo.core.engine.media_manager.media.CMediaFont;
import org.mslivo.core.engine.ui_engine.gui.actions.TextFieldAction;
import org.mslivo.core.engine.ui_engine.gui.components.Component;

import java.util.HashSet;

public class TextField extends Component {

    public String content;

    public CMediaFont font;

    public TextFieldAction textFieldAction;

    public boolean focused;

    public int contentMaxLength;

    public int offset;

    public HashSet<Character> allowedCharacters;

    public int markerPosition;

    public boolean contentValid;

}
