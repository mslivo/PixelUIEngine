package org.vnna.core.engine.ui_engine.misc;

import java.util.HashMap;

public class GraphInfo {

    public final long highest_value;

    public final long lowest_value;

    public final HashMap<Integer, Long> value_at_position;

    public final HashMap<Integer, Integer> index_at_position;

    public GraphInfo(long highest_value, long lowest_value, int steps, HashMap<Integer, Long> value_at_position,HashMap<Integer, Integer>  index_at_position) {
        this.highest_value = highest_value;
        this.lowest_value = lowest_value;
        this.value_at_position = value_at_position;
        this.index_at_position = index_at_position;
    }
}
