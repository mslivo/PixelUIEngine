package net.mslivo.core.engine.tools.particles.sprite;

import net.mslivo.core.engine.ui_engine.rendering.SpriteRenderer;

public interface SpriteParticleRenderHook<T> {

    default void beforeRenderParticle(SpriteRenderer batch, SpriteParticle<T> particle){return;}

    default void afterRenderParticle(SpriteRenderer batch, SpriteParticle<T> particle){return;}

    default boolean renderSpriteParticle(SpriteParticle spriteParticle) {
        return true;
    }
}
