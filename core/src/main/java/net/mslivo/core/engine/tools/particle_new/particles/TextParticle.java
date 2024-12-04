package net.mslivo.core.engine.tools.particle_new.particles;

import net.mslivo.core.engine.media_manager.CMediaFont;

public final class TextParticle<D> extends Particle<D> {
    public float x, y;
    public float r, g, b, a;
    public boolean centerX, centerY;
    public CMediaFont font;
    public String text;
}
