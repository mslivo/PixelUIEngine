package net.mslivo.pixelui.utils.appengine;

import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.Queue;

/**
 * Modifies Data Structure 1 update step at a time.
 * Sends input to adapter & gathers outputs using object pooling.
 */
public class AppEngine<A extends AppEngineAdapter<D>, D extends Object> implements Disposable {

    private static final Object[] RESET_OBJECT = new Object[AppEngineIO.PARAMETERS_MAX];
    private static final int[] RESET_INT = new int[AppEngineIO.PARAMETERS_MAX];
    private static final long[] RESET_LONG = new long[AppEngineIO.PARAMETERS_MAX];
    private static final float[] RESET_FLOAT = new float[AppEngineIO.PARAMETERS_MAX];
    private static final double[] RESET_DOUBLE = new double[AppEngineIO.PARAMETERS_MAX];
    private static final boolean[] RESET_BOOLEAN = new boolean[AppEngineIO.PARAMETERS_MAX];

    static {
        for (int i = 0; i < AppEngineIO.PARAMETERS_MAX; i++) {
            RESET_OBJECT[i] = null;
            RESET_INT[i] = 0;
            RESET_LONG[i] = 0l;
            RESET_FLOAT[i] = 0;
            RESET_DOUBLE[i] = 0;
            RESET_BOOLEAN[i] = false;
        }
    }

    private long ticks;
    private AppEngineIO lastOutput;

    private final A adapter;
    private final D data;
    private final Queue<AppEngineIO> inputs;
    private final Queue<AppEngineIO> outputs;
    private final Queue<AppEngineIO> engineIOPool;
    private final AppEngineOutputQueue appEngineOutputQueue = new AppEngineOutputQueue() {
        @Override
        public synchronized void addOutput(AppEngineIO output) {
            output.locked = true;
            outputs.addLast(output);
        }

        public synchronized AppEngineIO newIO(int type) {
            AppEngineIO appEngineIO = getUnlockedUIFromPool(type);
            return appEngineIO;
        }

    };

    public AppEngine(A adapter, D data) {
        final String errorMessageNull = "Cannot initialize AppEngine: %s is null";
        if (data == null) throw new RuntimeException(String.format(errorMessageNull, "data"));
        if (adapter == null) throw new RuntimeException(String.format(errorMessageNull, "adapter"));

        this.lastOutput = null;
        this.ticks = 0;

        this.data = data;
        this.inputs = new Queue<>();
        this.outputs = new Queue<>();
        this.engineIOPool = new Queue<>();
        this.adapter = adapter;

        this.adapter.init(this.data, this.appEngineOutputQueue);
    }

    public long getTicks() {
        return ticks;
    }

    private AppEngineIO getUnlockedUIFromPool(int type) {
        AppEngineIO appEngineIO = engineIOPool.isEmpty() ? new AppEngineIO() : engineIOPool.removeFirst();
        appEngineIO.locked = false;
        appEngineIO.type = type;
        appEngineIO.readIndex = 0;
        appEngineIO.writeIndex = 0;
        System.arraycopy(RESET_OBJECT, 0, appEngineIO.objectStack, 0, AppEngineIO.PARAMETERS_MAX);
        System.arraycopy(RESET_INT, 0, appEngineIO.intStack, 0, AppEngineIO.PARAMETERS_MAX);
        System.arraycopy(RESET_FLOAT, 0, appEngineIO.floatStack, 0, AppEngineIO.PARAMETERS_MAX);
        return appEngineIO;
    }

    public boolean outputAvailable() {
        return !this.outputs.isEmpty();
    }

    public AppEngineIO processOutput() {
        if (lastOutput != null) engineIOPool.addLast(lastOutput);
        if (outputAvailable()) {
            lastOutput = outputs.removeFirst();
            return lastOutput;
        } else {
            lastOutput = null;
            return null;
        }
    }

    public void clearOutputs() {
        for(int i=0;i<this.outputs.size;i++)
            engineIOPool.addLast(this.outputs.get(i));
        this.outputs.clear();
        lastOutput = null;
    }

    public A getAdapter() {
        return adapter;
    }

    public D getData() {
        return data;
    }

    public void update() {
        adapter.beforeInputs();
        // Process Inputs
        while (!this.inputs.isEmpty()) {
            final AppEngineIO engineIO = this.inputs.removeFirst();
            adapter.processInput(engineIO);
            engineIOPool.addLast(engineIO);
        }
        // Update Engine
        adapter.update();
        this.ticks++;
    }

    @Override
    public void dispose() {
        inputs.clear();
        outputs.clear();
        adapter.shutdown();
    }

    public synchronized AppEngineIO newIO(int type) {
        AppEngineIO appEngineIO = getUnlockedUIFromPool(type);
        return appEngineIO;
    }

    public synchronized void addInput(AppEngineIO input) {
        input.locked = true;
        inputs.addLast(input);
    }

}
