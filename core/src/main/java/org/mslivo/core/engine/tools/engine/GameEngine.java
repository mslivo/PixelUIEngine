package org.mslivo.core.engine.tools.engine;

import java.util.ArrayDeque;

/**
 * Modifies Data Structure 1 update step at a time.
 * Sends input to adapter & gathers outputs using object pooling.
 */
public class GameEngine<A extends GameEngineAdapter<D>, D extends Object> {
    class EngineIO {
        private int type;
        private Object[] params;
    }

    private final A adapter;
    private final D data;
    private final ArrayDeque<EngineIO> inputs;
    private final ArrayDeque<EngineIO> inputPool;
    private final ArrayDeque<EngineIO> outputs;
    private final ArrayDeque<EngineIO> outputPool;
    private final String errorMessageNull = "Cannot initialize GameEngine: %s is null";
    private int outputType;
    private Object[] outputParams;
    private long lastUpdateTime;
    private long ticks;

    public GameEngine(A adapter, D data) {
        if(data == null) throw new RuntimeException(String.format(errorMessageNull, "data"));
        if(adapter == null) throw new RuntimeException(String.format(errorMessageNull, "adapter"));

        this.data = data;
        this.inputs = new ArrayDeque<>();
        this.inputPool = new ArrayDeque<>();
        this.outputs = new ArrayDeque<>();
        this.outputPool = new ArrayDeque<>();
        this.lastUpdateTime = 0;
        this.outputType = -1;
        this.outputParams = null;
        // Start
        this.adapter = adapter;

        Output output = new Output() {
            @Override
            public void add(int type, Object... params) {
                EngineIO engineIO = outputPool.isEmpty() ? new EngineIO() : outputPool.poll() ;
                engineIO.type = type;
                engineIO.params = params;
                outputs.add(engineIO);
            }
        };
        this.adapter.init(this.data, output);
    }

    public long getTicks() {
        return ticks;
    }

    public long getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void input(int type, Object... params) {
        EngineIO engineIO = inputPool.isEmpty() ?  new EngineIO() : inputPool.poll();
        engineIO.type = type;
        engineIO.params = params;
        inputs.add(engineIO);
    }

    public boolean outputAvailable() {
        return !this.outputs.isEmpty();
    }

    public boolean nextOutput(){
        if(outputAvailable()){
            EngineIO engineIO = outputs.poll();
            outputType = engineIO.type;
            outputParams = engineIO.params;
            outputPool.add(engineIO);
            return true;
        }else{
            this.outputType = -1;
            this.outputParams = null;
            return false;
        }
    }

    public void clearOutputs() {
        outputPool.addAll(this.outputs);
        this.outputs.clear();
        this.outputType = -1;
        this.outputParams = null;
    }

    public int getOutputType() {
        return outputType;
    }

    public Object[] getOutputParams() {
        return outputParams;
    }

    public int getOutputParamsSize() {
        return outputParams != null ? outputParams.length : 0;
    }

    public Object getOutputParam(int index) {
        return (outputParams != null && index < outputParams.length) ? outputParams[index] : null;
    }


    public A getAdapter() {
        return adapter;
    }

    public D getData(){
        return data;
    }

    public void update() {
        adapter.beforeInputs();
        // Process Inputs
        EngineIO engineIO;
        while ((engineIO = this.inputs.pollFirst()) != null) {
            adapter.processInput(engineIO.type, engineIO.params);
            inputPool.add(engineIO);
        }
        // Update Engine
        adapter.update();
        this.lastUpdateTime = System.currentTimeMillis();
        this.ticks++;
    }

    public void shutdown() {
        inputs.clear();
        outputs.clear();
        adapter.shutdown();
    }

}
