package net.mslivo.core.engine.tools.particles.particles;

public abstract sealed class SpriteParticle<D> extends Particle<D> permits TextParticle, TextureBasedParticle {

}
