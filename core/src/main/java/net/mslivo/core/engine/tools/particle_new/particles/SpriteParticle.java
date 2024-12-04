package net.mslivo.core.engine.tools.particle_new.particles;

public abstract sealed class SpriteParticle<D> extends Particle<D> permits AnimationParticle, ArrayParticle, ImageParticle {
    public float x, y;
    public float r, g, b, a;
    public float rotation, scaleX, scaleY;
    public float origin_x, origin_y;
}
