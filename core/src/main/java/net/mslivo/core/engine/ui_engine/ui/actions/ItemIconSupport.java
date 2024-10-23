package net.mslivo.core.engine.ui_engine.ui.actions;

import com.badlogic.gdx.graphics.Color;
import net.mslivo.core.engine.media_manager.CMediaSprite;

public interface ItemIconSupport<T> {

    default CMediaSprite icon(T item){
        return null;
    };

    default int iconIndex(T item) {
        return 0;
    }

    default Color iconColor(T item){
        return Color.GRAY;
    }

}
