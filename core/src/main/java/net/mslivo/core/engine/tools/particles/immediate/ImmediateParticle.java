package net.mslivo.core.engine.tools.particles.immediate;

import com.badlogic.gdx.utils.FloatArray;

public class ImmediateParticle<D> {
    public ImmediateParticleType type;
    public FloatArray vxX = new FloatArray();
    public FloatArray vxY = new FloatArray();
    public FloatArray vxColorR= new FloatArray();
    public FloatArray vxColorG= new FloatArray();
    public FloatArray vxColorB= new FloatArray();
    public FloatArray vxColorA= new FloatArray();
    public boolean visible;
    public D data;
}
