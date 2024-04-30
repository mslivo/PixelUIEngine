package net.mslivo.core.engine.tools.engine;

import java.util.ArrayDeque;

/**
 * Modifies Data Structure 1 update step at a time.
 * Sends input to adapter & gathers outputs using object pooling.
 */
public class AppEngine<A extends AppEngineAdapter<D>, D extends Object> {
    public static final int PARAMETERS_MAX = 5;
    private static final Object[] RESET_OBJECT = new Object[PARAMETERS_MAX];
    private static final int[] RESET_INT = new int[PARAMETERS_MAX];
    private static final float[] RESET_FLOAT = new float[PARAMETERS_MAX];
    static {
        for(int i=0;i<PARAMETERS_MAX;i++){
            RESET_OBJECT[i] = null;
            RESET_INT[i] = 0;
            RESET_FLOAT[i] = 0;
        }
    }

    private long lastUpdateTime;
    private long ticks;
    private AppEngineIO lastOutput;

    private final A adapter;
    private final D data;
    private final ArrayDeque<AppEngineIO> inputs;
    private final ArrayDeque<AppEngineIO> outputs;
    private final ArrayDeque<AppEngineIO> engineIOPool;
    private final AppEngineOutputQueue appEngineOutputQueue = new AppEngineOutputQueue() {
        @Override
        public AppEngineIO addOutput(int type) {
            AppEngineIO appEngineIO = getAndResetEngineIOFromPool(type);
            outputs.add(appEngineIO);
            return appEngineIO;
        }
    };

    public AppEngine(A adapter, D data) {
        final String errorMessageNull = "Cannot initialize AppEngine: %s is null";
        if(data == null) throw new RuntimeException(String.format(errorMessageNull, "data"));
        if(adapter == null) throw new RuntimeException(String.format(errorMessageNull, "adapter"));

        this.lastUpdateTime = 0;
        this.lastOutput = null;
        this.ticks = 0;

        this.data = data;
        this.inputs = new ArrayDeque<>();
        this.outputs = new ArrayDeque<>();
        this.engineIOPool = new ArrayDeque<>();
        this.adapter = adapter;

        this.adapter.init(this.data, this.appEngineOutputQueue);
    }

    public long getTicks() {
        return ticks;
    }

    public long getLastUpdateTime() {
        return lastUpdateTime;
    }

    private AppEngineIO getAndResetEngineIOFromPool(int type){
        AppEngineIO engineIO = engineIOPool.isEmpty() ?  new AppEngineIO() : engineIOPool.poll();
        engineIO.type = type;
        engineIO.readIndex = 0;
        engineIO.writeIndex = 0;
        System.arraycopy(RESET_OBJECT,0,engineIO.objectParams,0,PARAMETERS_MAX);
        System.arraycopy(RESET_INT,0,engineIO.intParams,0,PARAMETERS_MAX);
        System.arraycopy(RESET_FLOAT,0,engineIO.floatParams,0,PARAMETERS_MAX);
        return engineIO;
    }

    public boolean outputAvailable() {
        return !this.outputs.isEmpty();
    }

    public AppEngineIO processOutput(){
        if(lastOutput != null) engineIOPool.add(lastOutput);
        if(outputAvailable()){
            lastOutput = outputs.poll();
            return lastOutput;
        }else{
            lastOutput = null;
            return null;
        }
    }

    public void clearOutputs() {
        engineIOPool.addAll(outputs);
        outputs.clear();
        lastOutput = null;
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
        AppEngineIO engineIO;
        while ((engineIO = this.inputs.pollFirst()) != null) {
            adapter.processInput(engineIO);
            engineIOPool.add(engineIO);
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

    public AppEngineIO addInput(int type){
        AppEngineIO appEngineIO = getAndResetEngineIOFromPool(type);
        inputs.add(appEngineIO);
        return appEngineIO;
    }

}
