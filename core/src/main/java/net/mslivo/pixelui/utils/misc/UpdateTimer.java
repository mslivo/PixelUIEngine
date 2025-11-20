package net.mslivo.pixelui.utils.misc;

import com.badlogic.gdx.Gdx;

public class UpdateTimer {

    private float accumulator;
    private float timeStep;
    private float maxAccumulated;
    private static final int DEFAULT_FRAME_ACCUMULATE = 2;

    public UpdateTimer(int maxUpdatesPerSecond) {
        setTargetUpdates(maxUpdatesPerSecond, DEFAULT_FRAME_ACCUMULATE);
    }

    public UpdateTimer(int maxUpdatesPerSecond, int maxAccumulatedSteps) {
        setTargetUpdates(maxUpdatesPerSecond,maxAccumulatedSteps);
    }

    public void setTargetUpdates(int maxUpdatesPerSecond) {
        setTargetUpdates(maxUpdatesPerSecond,DEFAULT_FRAME_ACCUMULATE);
    }

    public void setTargetUpdates(int maxUpdatesPerSecond, int maxAccumulatedSteps) {
        maxUpdatesPerSecond = Math.max(maxUpdatesPerSecond, 1);
        maxAccumulatedSteps = Math.max(maxAccumulatedSteps, 1);
        timeStep = 1f / maxUpdatesPerSecond;
        maxAccumulated = timeStep * maxAccumulatedSteps;
        accumulator = 0f;
    }

    public boolean shouldUpdate() {
        accumulator = Math.min(accumulator+Gdx.graphics.getDeltaTime(),maxAccumulated);

        if (accumulator >= timeStep) {
            accumulator -= timeStep;
            return true;
        }

        return false;
    }

    public float delta() {
        return timeStep; // fixed delta per update
    }
}
