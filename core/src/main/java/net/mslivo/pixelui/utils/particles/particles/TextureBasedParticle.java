package net.mslivo.pixelui.utils.particles.particles;

import net.mslivo.pixelui.media.CMediaSprite;

public abstract sealed class TextureBasedParticle<D> extends SpriteParticle<D> permits AnimationParticle, ArrayParticle, ImageParticle {
    public CMediaSprite sprite;
    public float rotation, scaleX, scaleY;
    public float origin_x, origin_y;
}
