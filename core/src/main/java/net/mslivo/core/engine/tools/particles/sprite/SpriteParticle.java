package net.mslivo.core.engine.tools.particles.sprite;

import net.mslivo.core.engine.media_manager.CMediaFont;
import net.mslivo.core.engine.media_manager.CMediaSprite;

public class SpriteParticle<D> {
    public SpriteParticleType type;
    public float x, y;
    public float r, g, b, a;
    public float rotation, scaleX, scaleY;
    public int array_index;
    public float origin_x, origin_y;
    public CMediaSprite appearance;
    public CMediaFont font;
    public String text;
    public float animation_offset;
    public boolean visible;
    public D data;
}
