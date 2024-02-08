package org.mslivo.core.engine.tools.rendering.particles;

import com.badlogic.gdx.graphics.Color;
import org.mslivo.core.engine.ui_engine.misc.render.ImmediateRenderer;

/*
 * Particle System must be extended and implemented
 */
public abstract class ImmediateRenderParticleSystem<T> extends ParticleSystem<T> {
    private Color backup;

    public ImmediateRenderParticleSystem(int particleLimit) {
        this(particleLimit, null);
    }

    public ImmediateRenderParticleSystem(int particleLimit, ParticleDataProvider<T> particleDataProvider) {
        super(particleLimit, particleDataProvider);
        backup = new Color();
    }

    public void render(ImmediateRenderer immediateRenderer) {
        if (particles.size() == 0) return;
        backup.r = immediateRenderer.getColor().r;
        backup.g = immediateRenderer.getColor().g;
        backup.b = immediateRenderer.getColor().b;
        backup.a = immediateRenderer.getColor().a;

        for (int i = 0; i < particles.size(); i++) {
            Particle<T> particle = particles.get(i);
            if (!particle.visible) continue;
            immediateRenderer.setColor(particle.r, particle.g, particle.b, particle.a);
            switch (particle.type) {
                case IMMEDIATE_PIXEL -> {
                    immediateRenderer.drawPixel(particle.x, particle.y);
                }
                default -> {
                    throw new RuntimeException("Particle Type " + particle.type.name() + " not supported by " + this.getClass().getSimpleName());
                }
            }
        }
        immediateRenderer.setColor(backup);
    }


    /* ------- PIXEL ------- */
    protected Particle<T> addParticle(float x, float y, float r, float g, float b) {
        Particle<T> particle = particleNew(ParticleType.IMMEDIATE_PIXEL, x, y, r, g, b, 1f, 0, 0, 0, 0, 0, 0, null, null, null, 0, true);
        addParticleToSystem(particle);
        return particle;
    }

    protected Particle<T> addParticle(float x, float y, float r, float g, float b, float a) {
        Particle<T> particle = particleNew(ParticleType.IMMEDIATE_PIXEL, x, y, r, g, b, a, 0, 0, 0, 0, 0, 0, null, null, null, 0, true);
        addParticleToSystem(particle);
        return particle;
    }
}
