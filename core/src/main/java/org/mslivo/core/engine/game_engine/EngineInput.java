package org.mslivo.core.engine.game_engine;


public record EngineInput(int type, Object... p) {

    public Object p(int index) {
        return (p.length > 0 && index >= 0 && index < p.length) ? p[index] : null;
    }

    public int pCount(){
        return p.length;
    }
}
