package net.mslivo.pixelui.engine;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.IntSet;
import net.mslivo.pixelui.engine.actions.TextFieldAction;

public class Textfield extends Component {
    public String content;
    public Color fontColor;
    public Color markerColor;
    public TextFieldAction textFieldAction;
    public int contentMaxLength;
    public int offset;
    public IntSet allowedCharacters;
    public int caretPosition;
    public int markedContentBegin;
    public int markedContentEnd;
    public boolean contentValid;

    Textfield() {
    }
}
