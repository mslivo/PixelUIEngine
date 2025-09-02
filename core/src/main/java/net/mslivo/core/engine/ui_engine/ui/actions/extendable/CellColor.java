package net.mslivo.core.engine.ui_engine.ui.actions.extendable;


import com.badlogic.gdx.graphics.Color;

public interface CellColor<T> {

    default Color cellColor(){
        return Color.WHITE;
    }
}
