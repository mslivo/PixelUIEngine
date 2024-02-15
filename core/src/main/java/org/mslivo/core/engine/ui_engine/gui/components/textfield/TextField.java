package org.mslivo.core.engine.ui_engine.gui.components.textfield;

import com.badlogic.gdx.utils.IntSet;
import org.mslivo.core.engine.media_manager.media.CMediaFont;
import org.mslivo.core.engine.ui_engine.gui.actions.TextFieldAction;
import org.mslivo.core.engine.ui_engine.gui.components.Component;

public class TextField extends Component {
    public String content;
    public CMediaFont font;
    public TextFieldAction textFieldAction;
    public int contentMaxLength;
    public int offset;
    public IntSet allowedCharacters;
    public int markerPosition;
    public boolean contentValid;
}
