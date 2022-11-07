package org.vnna.core.engine.ui_engine.gui.actions;

import org.vnna.core.engine.media_manager.media.CMediaGFX;

public interface ComboBoxAction<T extends Object> {

    default CMediaGFX icon(T listItem){
        return null;
    };

    default int iconArrayIndex(T listItem){ // if CMedia is CMediaArray
        return 0;
    };

    default String text(T listItem){
        return listItem.toString();
    };

    default void onItemSelected(T selectedItem){
        return;
    };

    default void onOpen(){return;}

    default void onClose(){return;}

    default void onMouseClick(int button){
        return;
    }

    default void onMouseDoubleClick(int button){
        return;
    }

    default void onKeyDown(int keyCodeUp){
        return;
    }

    default void onKeyUp(int keyCodeDown){
        return;
    }

    default void onMouseScroll(float scrolled){return;}
}
