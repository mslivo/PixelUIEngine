package net.mslivo.core.engine.ui_engine.ui.components;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.IntSet;
import net.mslivo.core.engine.ui_engine.ui.actions.TextFieldAction;

public class Textfield extends Component {
    public String content;
    public Color fontColor;
    public TextFieldAction textFieldAction;
    public int contentMaxLength;
    public int offset;
    public IntSet allowedCharacters;
    public int markerPosition;
    public boolean contentValid;
}
