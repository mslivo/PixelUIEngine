package net.mslivo.core.engine.tools.appengine;

import java.util.ArrayDeque;

/**
 * Modifies Data Structure 1 update step at a time.
 * Sends input to adapter & gathers outputs using object pooling.
 */
public class AppEngine<A extends AppEngineAdapter<D>, D extends Object> {

    private static final Object[] RESET_OBJECT = new Object[AppEngineIO.PARAMETERS_MAX];
    private static final int[] RESET_INT = new int[AppEngineIO.PARAMETERS_MAX];
    private static final long[] RESET_LONG = new long[AppEngineIO.PARAMETERS_MAX];
    private static final float[] RESET_FLOAT = new float[AppEngineIO.PARAMETERS_MAX];
    private static final double[] RESET_DOUBLE = new double[AppEngineIO.PARAMETERS_MAX];
    private static final boolean[] RESET_BOOLEAN = new boolean[AppEngineIO.PARAMETERS_MAX];
    static {
        for(int i=0;i<AppEngineIO.PARAMETERS_MAX;i++){
            RESET_OBJECT[i] = null;
            RESET_INT[i] = 0;
            RESET_LONG[i] = 0l;
            RESET_FLOAT[i] = 0;
            RESET_DOUBLE[i] = 0;
            RESET_BOOLEAN[i] = false;
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
        public synchronized AppEngineIO addOutput(int type) {
            AppEngineIO appEngineIO = getNextEngineIOFromPool(type);
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

    private AppEngineIO getNextEngineIOFromPool(int type){
        AppEngineIO engineIO = engineIOPool.isEmpty() ?  new AppEngineIO() : engineIOPool.poll();
        engineIO.type = type;
        engineIO.readIndex = 0;
        engineIO.writeIndex = 0;
        System.arraycopy(RESET_OBJECT,0,engineIO.objectStack,0,AppEngineIO.PARAMETERS_MAX);
        System.arraycopy(RESET_INT,0,engineIO.intStack,0,AppEngineIO.PARAMETERS_MAX);
        System.arraycopy(RESET_FLOAT,0,engineIO.floatStack,0,AppEngineIO.PARAMETERS_MAX);
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

    public synchronized AppEngineIO addInput(int type){
        AppEngineIO appEngineIO = getNextEngineIOFromPool(type);
        inputs.add(appEngineIO);
        return appEngineIO;
    }

}
