package net.mslivo.core.engine.tools.particles.particles;

import net.mslivo.core.engine.media_manager.CMediaAnimation;
import net.mslivo.core.engine.media_manager.CMediaSprite;

public abstract sealed class TextureBasedParticle<D> extends SpriteParticle<D> permits AnimationParticle, ArrayParticle, ImageParticle {
    public CMediaSprite sprite;
    public float rotation, scaleX, scaleY;
    public float origin_x, origin_y;
}
