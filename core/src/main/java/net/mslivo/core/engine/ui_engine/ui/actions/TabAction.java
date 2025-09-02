package net.mslivo.core.engine.ui_engine.ui.actions;

import net.mslivo.core.engine.ui_engine.ui.actions.extendable.Icon;

public interface TabAction extends Icon {

    default void onSelect(){
    }

}
