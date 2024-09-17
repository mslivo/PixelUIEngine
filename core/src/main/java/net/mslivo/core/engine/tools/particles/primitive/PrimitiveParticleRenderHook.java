package net.mslivo.core.engine.tools.particles.primitive;

import net.mslivo.core.engine.ui_engine.rendering.PrimitiveRenderer;

public interface PrimitiveParticleRenderHook<T> {

    default void beforeRenderParticle(PrimitiveRenderer renderer, PrimitiveParticle<T> particle){return;}

    default void afterRenderParticle(PrimitiveRenderer renderer, PrimitiveParticle<T> particle){return;}

    default boolean renderPrimitiveParticle(PrimitiveParticle<T> spriteParticle){
        return true;
    }
}
