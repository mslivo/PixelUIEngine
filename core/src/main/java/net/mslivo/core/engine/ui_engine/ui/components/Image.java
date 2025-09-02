package net.mslivo.core.engine.ui_engine.ui.components;

import net.mslivo.core.engine.media_manager.CMediaSprite;
import net.mslivo.core.engine.ui_engine.ui.actions.ImageAction;

public class Image extends Component {
    public CMediaSprite image;
    public int arrayIndex;
    public ImageAction imageAction;
    public boolean flipX, flipY;
}
