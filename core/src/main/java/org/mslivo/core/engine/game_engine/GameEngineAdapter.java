package org.mslivo.core.engine.game_engine;

import org.mslivo.core.engine.game_engine.inout.EngineInput;
import org.mslivo.core.engine.game_engine.inout.EngineOutput;

import java.util.ArrayList;

public interface GameEngineAdapter {

    void init(Object data, ArrayList<EngineOutput> outputs);

    default void beforeInputs() {
    }

    void update();

    void shutdown();

    void processInput(EngineInput engineInput);

}
