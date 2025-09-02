package net.mslivo.core.engine.ui_engine.ui.components;

import com.badlogic.gdx.graphics.Color;
import net.mslivo.core.engine.ui_engine.constants.CHECKBOX_STYLE;
import net.mslivo.core.engine.ui_engine.ui.actions.CheckboxAction;

public class Checkbox extends Component {
    public CHECKBOX_STYLE checkBoxStyle;
    public String text;
    public boolean checked;
    public Color fontColor;
    public CheckboxAction checkBoxAction;
}
