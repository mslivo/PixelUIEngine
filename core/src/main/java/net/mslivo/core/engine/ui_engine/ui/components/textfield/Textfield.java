package net.mslivo.core.engine.ui_engine.ui.components.textfield;

import com.badlogic.gdx.utils.IntSet;
import net.mslivo.core.engine.media_manager.CMediaFont;
import net.mslivo.core.engine.ui_engine.ui.actions.TextFieldAction;
import net.mslivo.core.engine.ui_engine.ui.components.Component;

public class Textfield extends Component {
    public String content;
    public CMediaFont font;
    public TextFieldAction textFieldAction;
    public int contentMaxLength;
    public int offset;
    public IntSet allowedCharacters;
    public int markerPosition;
    public boolean contentValid;
}
