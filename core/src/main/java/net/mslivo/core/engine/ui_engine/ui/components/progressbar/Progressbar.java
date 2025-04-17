package net.mslivo.core.engine.ui_engine.ui.components.progressbar;

import com.badlogic.gdx.graphics.Color;
import net.mslivo.core.engine.media_manager.CMediaFont;
import net.mslivo.core.engine.ui_engine.ui.actions.ProgressBarAction;
import net.mslivo.core.engine.ui_engine.ui.components.Component;

public class Progressbar extends Component {
    public float progress;
    public boolean progressText;
    public boolean progressText2Decimal;
    public Color fontColor;
    public ProgressBarAction progressBarAction;
}
