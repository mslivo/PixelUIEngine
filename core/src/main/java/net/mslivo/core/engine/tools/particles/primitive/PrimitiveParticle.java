package net.mslivo.core.engine.tools.particles.primitive;

import com.badlogic.gdx.utils.FloatArray;

public class PrimitiveParticle<D> {
    public int primitiveType;
    public float[] x,y;
    public float[] r,g,b,a;
    public int vertexes;
    public boolean visible;
    public D data;
}
