package net.mslivo.pixelui.engine.actions.common;

import com.badlogic.gdx.graphics.Color;

public interface ItemCellColor<T> {

    default Color cellColor(T item){
        return Color.WHITE;
    }
}
