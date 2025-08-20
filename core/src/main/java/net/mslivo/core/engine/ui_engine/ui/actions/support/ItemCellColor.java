package net.mslivo.core.engine.ui_engine.ui.actions.support;

import com.badlogic.gdx.graphics.Color;

public interface ItemCellColor<T> {

    default Color cellColor(T item){
        return Color.WHITE;
    }
}
