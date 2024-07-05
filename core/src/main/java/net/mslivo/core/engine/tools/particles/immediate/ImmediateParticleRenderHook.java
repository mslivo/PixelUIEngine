package net.mslivo.core.engine.tools.particles.immediate;

import net.mslivo.core.engine.tools.particles.sprite.SpriteParticle;
import net.mslivo.core.engine.ui_engine.rendering.ImmediateRenderer;
import net.mslivo.core.engine.ui_engine.rendering.SpriteRenderer;

public interface ImmediateParticleRenderHook<T> {

    default void beforeRenderParticle(ImmediateRenderer renderer, ImmediateParticle<T> particle){return;}

    default void afterRenderParticle(ImmediateRenderer renderer, ImmediateParticle<T> particle){return;}
}
