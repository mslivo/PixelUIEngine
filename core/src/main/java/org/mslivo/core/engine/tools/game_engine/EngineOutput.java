package org.mslivo.core.engine.tools.game_engine;


import java.util.Objects;

public final class EngineOutput {
    private final int type;
    private final Object[] p;

    public EngineOutput(int type, Object... p) {
        this.type = type;
        this.p = p;
    }

    public Object p(int index) {
        if (p == null || p.length == 0) return null;
        return (index >= 0 && index < p.length) ? p[index] : null;
    }

    public int pCount() {
        if (p == null) return 0;
        return p.length;
    }

    public int type() {
        return type;
    }

    public Object[] p() {
        return p;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (EngineOutput) obj;
        return this.type == that.type &&
                Objects.equals(this.p, that.p);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, p);
    }

    @Override
    public String toString() {
        return "EngineOutput[" +
                "type=" + type + ", " +
                "p=" + p + ']';
    }

}
