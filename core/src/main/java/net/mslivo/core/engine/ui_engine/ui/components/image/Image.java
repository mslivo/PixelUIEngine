package net.mslivo.core.engine.ui_engine.ui.components.image;

import net.mslivo.core.engine.media_manager.media.CMediaSprite;
import net.mslivo.core.engine.ui_engine.ui.actions.ImageAction;
import net.mslivo.core.engine.ui_engine.ui.components.Component;

public class Image extends Component {
    public CMediaSprite image;
    public int arrayIndex;
    public float animationOffset;
    public ImageAction imageAction;
}
