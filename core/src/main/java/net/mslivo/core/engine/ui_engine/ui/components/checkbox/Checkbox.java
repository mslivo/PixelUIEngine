package net.mslivo.core.engine.ui_engine.ui.components.checkbox;

import net.mslivo.core.engine.media_manager.media.CMediaFont;
import net.mslivo.core.engine.ui_engine.constants.CHECKBOX_STYLE;
import net.mslivo.core.engine.ui_engine.ui.actions.CheckboxAction;
import net.mslivo.core.engine.ui_engine.ui.components.Component;

public class Checkbox extends Component {
    public CHECKBOX_STYLE checkBoxStyle;
    public String text;
    public boolean checked;
    public CMediaFont font;
    public CheckboxAction checkBoxAction;
}
