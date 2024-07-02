package net.mslivo.core.engine.tools.particles;

import net.mslivo.core.engine.ui_engine.rendering.ImmediateRenderer;
import net.mslivo.core.engine.ui_engine.rendering.SpriteRenderer;

public interface ParticleRenderHook<T> {

    default void beforeRenderParticle(SpriteRenderer batch, Particle<T> particle){return;}

    default void afterRenderParticle(SpriteRenderer batch, Particle<T> particle){return;}

    default void beforeRenderParticle(ImmediateRenderer renderer, Particle<T> particle){return;}

    default void afterRenderParticle(ImmediateRenderer renderer, Particle<T> particle){return;}
}
