package net.mslivo.core.engine.ui_engine.ui.actions;


import com.badlogic.gdx.graphics.Color;

public interface CellColorSupport<T> {

    default Color cellColor(){
        return Color.WHITE;
    }
}
