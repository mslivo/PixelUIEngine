package net.mslivo.example.engine;

import net.mslivo.core.engine.tools.engine.AppEngineAdapter;
import net.mslivo.core.engine.tools.engine.Output;
import net.mslivo.example.data.ExampleData;

public class ExampleEngineAdapter implements AppEngineAdapter<ExampleData> {

    private ExampleData data;

    private Output output;

    public void init(ExampleData data, Output output) {
        this.data = data;
        this.output = output;
    }


    @Override
    public void processInput(int type, Object[] params) {
        switch (type) {

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


}
