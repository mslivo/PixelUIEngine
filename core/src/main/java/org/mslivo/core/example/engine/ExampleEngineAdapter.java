package org.mslivo.core.example.engine;

import org.mslivo.core.engine.game_engine.EngineInput;
import org.mslivo.core.engine.game_engine.EngineOutput;
import org.mslivo.core.engine.game_engine.GameEngineAdapter;
import org.mslivo.core.engine.tools.lthreadpool.LThreadPoolUpdater;
import org.mslivo.core.example.data.ExampleData;

import java.util.ArrayDeque;

public class ExampleEngineAdapter implements GameEngineAdapter<ExampleData>, LThreadPoolUpdater {

    private ExampleData data;

    private ArrayDeque<EngineOutput> outputs;

    public void init(ExampleData data, ArrayDeque<EngineOutput> outputs) {
        this.data = data;
        this.outputs = outputs;
    }

    @Override
    public void processInput(EngineInput engineInput) {
        switch (engineInput.type()) {

        }
    }

    @Override
    public void update() {
        /* Main Loop: modify data, produce outputs */
    }

    @Override
    public void shutdown() {

    }

    @Override
    public void updateFromThread(Object object, int index) {

    }
}
