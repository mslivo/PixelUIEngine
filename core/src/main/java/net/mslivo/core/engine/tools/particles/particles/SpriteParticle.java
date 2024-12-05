package net.mslivo.core.engine.tools.particles.particles;

public abstract sealed class SpriteParticle<D> extends Particle<D> permits AnimationParticle, ArrayParticle, ImageParticle {
    public float rotation, scaleX, scaleY;
    public float origin_x, origin_y;
}
