package net.mslivo.core.engine.tools.appengine;

public interface AppEngineOutputQueue {
    void addOutput(AppEngineIO appEngineIO);
    AppEngineIO newIO(int type);
}
