package org.mslivo.core.engine.tools.rendering.particles;

import com.badlogic.gdx.graphics.Color;
import org.mslivo.core.engine.ui_engine.render.ShaderBatch;

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

    public void render(ShaderBatch shaderBatch) {
        if (particles.size() == 0) return;
        backup.r = shaderBatch.getColor().r;
        backup.g = shaderBatch.getColor().g;
        backup.b = shaderBatch.getColor().b;
        backup.a = shaderBatch.getColor().a;

        for (int i = 0; i < particles.size(); i++) {
            Particle<T> particle = particles.get(i);
            if (!particle.visible) continue;
            shaderBatch.setColor(particle.r, particle.g, particle.b, particle.a);
            switch (particle.type) {
                case SHADER_PIXEL -> {
                    shaderBatch.drawPoint(particle.x, particle.y);
                }
                default -> {
                    throw new RuntimeException("Particle Type " + particle.type.name() + " not supported by " + this.getClass().getSimpleName());
                }
            }
        }
        shaderBatch.setColor(backup);
    }


    /* ------- PIXEL ------- */
    protected Particle<T> addParticle(float x, float y, float r, float g, float b) {
        Particle<T> particle = particleNew(ParticleType.SHADER_PIXEL, x, y, r, g, b, 1f, 0, 0, 0, 0, 0, 0, null, null, null, 0, true);
        addParticleToSystem(particle);
        return particle;
    }

    protected Particle<T> addParticle(float x, float y, float r, float g, float b, float a) {
        Particle<T> particle = particleNew(ParticleType.SHADER_PIXEL, x, y, r, g, b, a, 0, 0, 0, 0, 0, 0, null, null, null, 0, true);
        addParticleToSystem(particle);
        return particle;
    }
}
