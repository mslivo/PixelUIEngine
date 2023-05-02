package org.mslivo.core.engine.game_engine.inout;


public record EngineOutput(int type, Object... p) {

    public Object p(int index) {
        return (index >= 0 && index < p.length) ? p[index] : null;
    }

    public int pCount(){
        return p.length;
    }
}
