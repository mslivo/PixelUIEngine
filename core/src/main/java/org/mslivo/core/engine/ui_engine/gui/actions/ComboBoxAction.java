package org.mslivo.core.engine.ui_engine.gui.actions;

import org.mslivo.core.engine.media_manager.media.CMediaGFX;

public abstract class ComboBoxAction<T extends Object> extends CommonActions {

    public CMediaGFX icon(T listItem) {
        return null;
    }


    public int iconArrayIndex(T listItem) { // if CMedia is CMediaArray
        return 0;
    }

    public String text(T listItem) {
        return listItem.toString();
    }


    public void onItemSelected(T selectedItem) {
    }

    public void onOpen() {
    }

    public void onClose() {
    }

}
