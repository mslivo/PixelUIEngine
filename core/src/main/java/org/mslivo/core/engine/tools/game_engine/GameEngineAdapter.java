package org.mslivo.core.engine.tools.game_engine;

public interface GameEngineAdapter<D> {

    void init(D data, Output outputs);

    default void beforeInputs() {}

    void update();

    void shutdown();

    void processInput(int type, Object[] params);

}
