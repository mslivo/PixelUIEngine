package org.mslivo.core.engine.ui_engine.ui.components.map;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import org.mslivo.core.engine.ui_engine.ui.actions.CanvasAction;
import org.mslivo.core.engine.ui_engine.ui.components.Component;

import java.util.ArrayList;

public class Canvas extends Component {
    public Pixmap pixmap;
    public Texture texture;
    public CanvasAction canvasAction;
    public ArrayList<CanvasImage> canvasImages;
}
