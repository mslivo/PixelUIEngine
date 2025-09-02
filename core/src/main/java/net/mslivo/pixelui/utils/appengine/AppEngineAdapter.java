package net.mslivo.pixelui.utils.appengine;

public interface AppEngineAdapter<D> {
    void init(D data, AppEngineOutputQueue outputQueue);
    default void beforeInputs() {}
    void processInput(AppEngineIO engineIO);
    void update();
    void shutdown();
}
