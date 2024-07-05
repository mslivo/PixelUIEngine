package net.mslivo.core.engine.tools.particles.immediate;

import net.mslivo.core.engine.ui_engine.rendering.PrimitiveRenderer;

public interface PrimitiveParticleRenderHook<T> {

    default void beforeRenderParticle(PrimitiveRenderer renderer, PrimitiveParticle<T> particle){return;}

    default void afterRenderParticle(PrimitiveRenderer renderer, PrimitiveParticle<T> particle){return;}
}
