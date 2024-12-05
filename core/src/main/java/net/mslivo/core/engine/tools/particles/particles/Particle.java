package net.mslivo.core.engine.tools.particles.particles;

public abstract sealed class Particle<D> permits PrimitiveParticle, SpriteParticle, TextParticle {
    public float x, y;
    public float r, g, b, a;
    public boolean visible;
    public D data;
}
