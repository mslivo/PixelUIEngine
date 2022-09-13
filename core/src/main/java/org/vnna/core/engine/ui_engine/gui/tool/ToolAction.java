package org.vnna.core.engine.ui_engine.gui.tool;

public interface ToolAction {


    default void onClick(int button,int x, int y){return;};

    default void onDoubleClick(int button,int x, int y){return;};

    default void onRelease(int button,int x, int y){return;};

    default void onDrag(int x, int y){return;};


}
