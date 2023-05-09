package org.mslivo.core.engine.game_engine;

import java.util.ArrayDeque;

public interface GameEngineAdapter<D> {

    void init(D data, ArrayDeque<EngineOutput> outputs);

    default void beforeInputs() {}

    void update();

    void shutdown();

    void processInput(EngineInput engineInput);

}
