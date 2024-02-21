package org.mslivo.core.engine.ui_engine.ui.components.map;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import org.mslivo.core.engine.ui_engine.ui.actions.MapAction;
import org.mslivo.core.engine.ui_engine.ui.components.Component;

import java.util.ArrayList;

public class Map extends Component {
    public Pixmap pMap;
    public Texture texture;
    public MapAction mapAction;
    public ArrayList<MapOverlay> mapOverlays;
}
