package net.mslivo.core.engine.tools.particle_new.particles;

public abstract sealed class Particle<D> permits PrimitiveParticle, SpriteParticle, TextParticle {
    public boolean visible;
    public D data;
}
