package org.mslivo.core.engine.game_engine;

import org.mslivo.core.engine.game_engine.inout.EngineInput;
import org.mslivo.core.engine.game_engine.inout.EngineOutput;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;

/**
 * Modifies Data Structure 1 step forward.
 * - Processes and deletes Inputs from inputlist
 * - Appends Outputs to output list
 */
public class GameEngine<T extends GameEngineAdapter> {

    private final Object data;

    private long lastUpdateTime;

    private long ticks;

    private final T adapter;

    private final Deque<EngineInput> inputs;

    private final ArrayList<EngineOutput> outputs;

    public long getTicks() {
        return ticks;
    }

    public long getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void input(EngineInput input) {
        inputs.add(input);
    }

    public boolean outputAvaiable() {
        return this.outputs.size() > 0;
    }

    public ArrayList<EngineOutput> getOutputs() {
        return this.outputs;
    }

    public void clearOutputs() {
        this.outputs.clear();
    }

    public T getAdapter() {
        return adapter;
    }

    public GameEngine(T adapter, Object data) {
        if (data == null || adapter == null) {
            throw new GameEngineException("Cannot initialize GameEngine: invalid parameters");
        }
        if (isInvalidDataObject(data.getClass())) {
            throw new GameEngineException("Cannot initialize data Object "+data.getClass().getSimpleName()+" invalid: contains non-public fields, methods or non-serializable classes");
        }

        this.data = data;
        this.inputs = new ArrayDeque<>();
        this.outputs = new ArrayList<>();
        this.lastUpdateTime = 0;
        // Start
        this.adapter = adapter;
        this.adapter.init(this.data, this.outputs);
    }

    private boolean isInvalidDataObject(Class checkClass) {
        if (Collection.class.isAssignableFrom(checkClass)) return false;
        if (!String.class.isAssignableFrom(checkClass)) return false;
        if (!Serializable.class.isAssignableFrom(checkClass)) return true;
        if (checkClass.getDeclaredMethods().length != 0) return true;
        for (Field field : checkClass.getDeclaredFields()) {
            if (!Modifier.isPublic(field.getModifiers())) {
                return true;
            } else {
                if (!field.getType().isPrimitive()) {
                    if (isInvalidDataObject(field.getType())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public void update() {
        adapter.beforeInputs();
        EngineInput engineInput = null;
        while ((engineInput = this.inputs.pollFirst()) != null) {
            adapter.processInput(engineInput);
        }
        inputs.clear();
        adapter.update();
        this.lastUpdateTime = System.currentTimeMillis();
        this.ticks = this.ticks + 1;
    }


    public void shutdown() {
        inputs.clear();
        outputs.clear();
        adapter.shutdown();
    }


}
