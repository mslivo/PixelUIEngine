package net.mslivo.pixelui.engine;

import net.mslivo.pixelui.media.CMediaSprite;
import net.mslivo.pixelui.engine.actions.ImageAction;

public class Image extends Component {
    public CMediaSprite image;
    public int arrayIndex;
    public ImageAction imageAction;
    public boolean flipX, flipY;

    Image() {
    }
}
