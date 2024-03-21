package net.mslivo.core.engine.tools.engine;

public interface AppEngineAdapter<D> {
    void init(D data, Output outputs);
    default void beforeInputs() {}
    void processInput(int type, Object[] params);
    void update();
    void shutdown();
}
