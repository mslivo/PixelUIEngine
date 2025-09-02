package net.mslivo.pixelui.engine;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.IntSet;
import net.mslivo.pixelui.engine.actions.TextFieldAction;

public class Textfield extends Component {
    public String content;
    public Color fontColor;
    public TextFieldAction textFieldAction;
    public int contentMaxLength;
    public int offset;
    public IntSet allowedCharacters;
    public int markerPosition;
    public boolean contentValid;

    Textfield() {
    }
}
