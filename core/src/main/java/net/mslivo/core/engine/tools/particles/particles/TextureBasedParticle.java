package net.mslivo.core.engine.tools.particles.particles;

public abstract sealed class TextureBasedParticle<D> extends SpriteParticle<D> permits AnimationParticle, ArrayParticle, ImageParticle {
    public float rotation, scaleX, scaleY;
    public float origin_x, origin_y;
}
