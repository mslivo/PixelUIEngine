package net.mslivo.core.engine.ui_engine.ui.actions;

import com.badlogic.gdx.graphics.Color;
import net.mslivo.core.engine.media_manager.CMediaSprite;

public interface HasItemIcons<T> {

    default CMediaSprite icon(T item){
        return null;
    };

    default int iconIndex(T item) {
        return 0;
    }

    default Color iconColor(T item){
        return Color.GRAY;
    }

    default boolean iconFlipX(){return false;}

    default boolean iconFlipY(){return false;}

}
