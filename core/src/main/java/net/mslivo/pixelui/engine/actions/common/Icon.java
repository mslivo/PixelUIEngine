package net.mslivo.pixelui.engine.actions.common;

import com.badlogic.gdx.graphics.Color;
import net.mslivo.pixelui.media_manager.CMediaSprite;

public interface Icon {

    default CMediaSprite icon(){
        return null;
    };

    default int iconIndex() {
        return 0;
    }

    default Color iconColor(){
        return Color.GRAY;
    }

    default boolean iconFlipX(){return false;}

    default boolean iconFlipY(){return false;}
}
