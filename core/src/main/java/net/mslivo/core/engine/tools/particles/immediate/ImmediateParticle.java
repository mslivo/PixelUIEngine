package net.mslivo.core.engine.tools.particles.immediate;

import com.badlogic.gdx.utils.FloatArray;

public class ImmediateParticle<D> {
    public ImmediateParticleType type;
    public FloatArray x = new FloatArray();
    public FloatArray y = new FloatArray();
    public FloatArray color_r = new FloatArray();
    public FloatArray color_g = new FloatArray();
    public FloatArray color_b = new FloatArray();
    public FloatArray color_a = new FloatArray();
    public boolean visible;
    public D data;
}
