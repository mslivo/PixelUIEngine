package net.mslivo.pixelui.utils.particles.particles;

public abstract sealed class Particle<D> permits EmptyParticle, PrimitiveParticle, SpriteParticle {
    public float x, y;
    public float r, g, b, a;
    public boolean visible;
    public D data;
}
