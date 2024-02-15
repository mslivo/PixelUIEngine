package org.mslivo.core.engine.ui_engine.misc;

public final class GraphInfo {
    public final long lowestValue;
    public final long highestValue;
    public final int[] indexAtPosition;
    public final long[] valueAtPosition;

    public GraphInfo(long lowestValue, long highestValue, int[] indexAtPosition, long[] valueAtPosition) {
        this.lowestValue = lowestValue;
        this.highestValue = highestValue;
        this.indexAtPosition = indexAtPosition;
        this.valueAtPosition = valueAtPosition;
    }

}