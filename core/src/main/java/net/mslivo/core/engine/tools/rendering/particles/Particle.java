package net.mslivo.core.engine.tools.rendering.particles;

import net.mslivo.core.engine.media_manager.media.CMediaFont;
import net.mslivo.core.engine.media_manager.media.CMediaGFX;

public class Particle<D> {
    public ParticleType type;
    public float x, y;
    public float r, g, b, a;
    public float rotation, scaleX, scaleY;
    public int array_index;
    public float origin_x, origin_y;
    public CMediaGFX appearance;
    public CMediaFont font;
    public String text;
    public float animation_offset;
    public boolean visible;
    public D data;
}
