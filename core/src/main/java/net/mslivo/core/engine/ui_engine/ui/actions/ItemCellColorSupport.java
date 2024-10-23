package net.mslivo.core.engine.ui_engine.ui.actions;

import com.badlogic.gdx.graphics.Color;

public interface ItemCellColorSupport<T> {

    default Color cellColor(T item){
        return Color.WHITE;
    }
}
