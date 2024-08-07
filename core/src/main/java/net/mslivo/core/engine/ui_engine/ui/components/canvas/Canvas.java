package net.mslivo.core.engine.ui_engine.ui.components.canvas;

import net.mslivo.core.engine.ui_engine.rendering.ColorMap;
import net.mslivo.core.engine.ui_engine.ui.actions.CanvasAction;
import net.mslivo.core.engine.ui_engine.ui.components.Component;

import java.util.ArrayList;

public class Canvas extends Component {
    public ColorMap colorMap;
    public CanvasAction canvasAction;
    public ArrayList<CanvasImage> canvasImages;
}
