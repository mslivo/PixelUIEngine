package net.mslivo.pixelui.engine;

import com.badlogic.gdx.graphics.Color;
import net.mslivo.pixelui.engine.actions.ProgressBarAction;

public class Progressbar extends Component {
    public float progress;
    public boolean progressText;
    public boolean progressText2Decimal;
    public Color fontColor;
    public ProgressBarAction progressBarAction;

    Progressbar() {
    }
}
