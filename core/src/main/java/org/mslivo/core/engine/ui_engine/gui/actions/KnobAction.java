package org.mslivo.core.engine.ui_engine.gui.actions;

public abstract class KnobAction {

    public void onTurned(float turned, float amount){
    }

    public void onPress(){
    }

    public void onRelease(){
    }

    public void onMouseClick(int button){
    }

    public void onMouseDoubleClick(int button){
    }

    public void onMouseScroll(float scrolled){}
}
