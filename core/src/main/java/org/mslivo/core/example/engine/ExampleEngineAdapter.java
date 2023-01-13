package org.mslivo.core.example.engine;

import org.mslivo.core.engine.game_engine.GameEngineAdapter;
import org.mslivo.core.engine.game_engine.inout.EngineInput;
import org.mslivo.core.engine.game_engine.inout.EngineOutput;
import org.mslivo.core.engine.tools.listthreadpool.LThreadPoolUpdater;
import org.mslivo.core.example.data.ExampleData;

import java.util.ArrayList;

public class ExampleEngineAdapter implements GameEngineAdapter, LThreadPoolUpdater {

    private ExampleData exampleData;

    private ArrayList<EngineOutput> outputs;

    public void init(Object data, ArrayList<EngineOutput> outputs) {
        this.exampleData = (ExampleData) data;
        this.outputs = outputs;
    }

    @Override
    public void processInput(EngineInput engineInput) {
        switch (engineInput.type) {

        }
    }

    @Override
    public void update() {
        //modify data, add to outputs
    }

    @Override
    public void shutdown() {

    }

    @Override
    public void updateFromThread(Object object, int index) {

    }
}
