package org.mslivo.core.engine.ui_engine.misc;

import java.util.Objects;

public final class GraphInfo {
    private final long lowestValue;
    private final long highestValue;
    private final int[] indexAtPosition;
    private final long[] valueAtPosition;

    public GraphInfo(long lowestValue, long highestValue, int[] indexAtPosition, long[] valueAtPosition) {
        this.lowestValue = lowestValue;
        this.highestValue = highestValue;
        this.indexAtPosition = indexAtPosition;
        this.valueAtPosition = valueAtPosition;
    }

    public long lowestValue() {
        return lowestValue;
    }

    public long highestValue() {
        return highestValue;
    }

    public int[] indexAtPosition() {
        return indexAtPosition;
    }

    public long[] valueAtPosition() {
        return valueAtPosition;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (GraphInfo) obj;
        return this.lowestValue == that.lowestValue &&
                this.highestValue == that.highestValue &&
                Objects.equals(this.indexAtPosition, that.indexAtPosition) &&
                Objects.equals(this.valueAtPosition, that.valueAtPosition);
    }

    @Override
    public int hashCode() {
        return Objects.hash(lowestValue, highestValue, indexAtPosition, valueAtPosition);
    }

    @Override
    public String toString() {
        return "GraphInfo[" +
                "lowestValue=" + lowestValue + ", " +
                "highestValue=" + highestValue + ", " +
                "indexAtPosition=" + indexAtPosition + ", " +
                "valueAtPosition=" + valueAtPosition + ']';
    }


}