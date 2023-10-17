package org.mslivo.example.engine;

import org.mslivo.core.engine.game_engine.EngineInput;
import org.mslivo.core.engine.game_engine.GameEngineAdapter;
import org.mslivo.core.engine.game_engine.Output;
import org.mslivo.core.engine.tools.lthreadpool.LThreadPoolUpdater;
import org.mslivo.example.data.ExampleData;

public class ExampleEngineAdapter implements GameEngineAdapter<ExampleData>, LThreadPoolUpdater {

    private ExampleData data;

    private Output output;

    public void init(ExampleData data, Output output) {
        this.data = data;
        this.output = output;
    }

    @Override
    public void processInput(EngineInput engineInput) {
        switch (engineInput.type()) {

        }
    }

    @Override
    public void update() {
        // Main Loop: modify data & produce outputs
        // output.add(new EngineOutput(0,"TestOutput"));
    }

    @Override
    public void shutdown() {

    }

    @Override
    public void updateFromThread(Object object, int index) {

    }
}
