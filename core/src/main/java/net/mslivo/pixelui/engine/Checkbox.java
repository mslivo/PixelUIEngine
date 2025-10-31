package net.mslivo.pixelui.engine;

import com.badlogic.gdx.graphics.Color;
import net.mslivo.pixelui.engine.constants.CHECKBOX_STYLE;
import net.mslivo.pixelui.engine.actions.CheckboxAction;

public final class Checkbox extends Component {
    public CHECKBOX_STYLE checkBoxStyle;
    public String text;
    public boolean checked;
    public Color fontColor;
    public CheckboxAction checkBoxAction;

    Checkbox() {
    }
}
