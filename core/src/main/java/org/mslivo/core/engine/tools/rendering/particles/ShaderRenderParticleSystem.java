package org.mslivo.core.engine.tools.rendering.particles;

import com.badlogic.gdx.graphics.Color;
import org.mslivo.core.engine.ui_engine.render.ShaderRenderer;

/*
 * Particle System must be extended and implemented
 */
public abstract class ShaderRenderParticleSystem<T> extends ParticleSystem<T> {
    private Color backup;

    public ShaderRenderParticleSystem(int particleLimit) {
        this(particleLimit, null);
    }

    public ShaderRenderParticleSystem(int particleLimit, ParticleDataProvider<T> particleDataProvider) {
        super(particleLimit, particleDataProvider);
        backup = new Color();
    }

    public void render(ShaderRenderer shaderRenderer) {
        if (particles.size() == 0) return;
        backup.r = shaderRenderer.getColor().r;
        backup.g = shaderRenderer.getColor().g;
        backup.b = shaderRenderer.getColor().b;
        backup.a = shaderRenderer.getColor().a;

        for (int i = 0; i < particles.size(); i++) {
            Particle<T> particle = particles.get(i);
            if (!particle.visible) continue;
            shaderRenderer.setColor(particle.r, particle.g, particle.b, particle.a);
            switch (particle.type) {
                case IMMEDIATE_PIXEL -> {
                    shaderRenderer.drawPixel(particle.x, particle.y);
                }
                default -> {
                    throw new RuntimeException("Particle Type " + particle.type.name() + " not supported by " + this.getClass().getSimpleName());
                }
            }
        }
        shaderRenderer.setColor(backup);
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
