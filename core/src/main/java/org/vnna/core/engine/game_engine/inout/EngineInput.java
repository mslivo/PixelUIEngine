package org.vnna.core.engine.game_engine.inout;


public class EngineInput {

    public final int type;

    public final Object[] p;

    public EngineInput(int type, Object... p){
        this.type = type;
        this.p = p;
    }

    public Object getParameter(int index){
        return (index >= 0 && index < p.length) ? p[index] : null;
    }

}
