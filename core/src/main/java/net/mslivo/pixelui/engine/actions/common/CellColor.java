package net.mslivo.pixelui.engine.actions.common;


import com.badlogic.gdx.graphics.Color;

public interface CellColor<T> {

    default Color cellColor(){
        return Color.WHITE;
    }
}
