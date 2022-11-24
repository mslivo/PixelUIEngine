package org.vnna.core.engine.ui_engine.gui.actions;

import org.vnna.core.engine.media_manager.media.CMediaGFX;

public abstract class ComboBoxAction<T extends Object> {

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

    public void onMouseClick(int button) {
    }

    public void onMouseDoubleClick(int button) {
    }

    public void onMouseScroll(float scrolled) {
    }
}
