package org.vnna.core.engine.ui_engine.misc;

import java.util.HashMap;

public class GraphInfo {

    public final long highestValue;

    public final long lowestValue;

    public final int[] indexAtPosition;

    public final long[] valueAtPosition;

    public GraphInfo(long lowestValue, long highestValue, int[] indexAtPosition, long[] valueAtPosition) {
        this.lowestValue = lowestValue;
        this.highestValue = highestValue;
        this.indexAtPosition = indexAtPosition;
        this.valueAtPosition = valueAtPosition;
    }
}