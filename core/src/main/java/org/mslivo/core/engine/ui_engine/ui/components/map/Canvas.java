package org.mslivo.core.engine.ui_engine.ui.components.map;

import com.badlogic.gdx.graphics.Color;
import org.mslivo.core.engine.ui_engine.ui.actions.CanvasAction;
import org.mslivo.core.engine.ui_engine.ui.components.Component;

import java.util.ArrayList;

public class Canvas extends Component {
    public Color[][] map;
    public CanvasAction canvasAction;
    public ArrayList<CanvasImage> canvasImages;
}
