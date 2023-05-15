package org.mslivo.core.engine.game_engine;


public record EngineOutput(int type, Object... p) {

    public Object p(int index) {
        if(p == null || p.length == 0) return null;
        return (index >= 0 && index < p.length) ? p[index] : null;
    }

    public int pCount(){
        if(p == null) return 0;
        return p.length;
    }
}
