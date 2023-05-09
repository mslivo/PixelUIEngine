package org.mslivo.core.engine.game_engine;

public interface GameEngineAdapter<D> {

    void init(D data, Output outputs);

    default void beforeInputs() {}

    void update();

    void shutdown();

    void processInput(EngineInput engineInput);

}
