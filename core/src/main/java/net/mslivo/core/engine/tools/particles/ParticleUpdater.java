package net.mslivo.core.engine.tools.particles;

import net.mslivo.core.engine.tools.particles.particles.Particle;

public interface ParticleUpdater<T> {
    default boolean updateParticle(Particle<T> particle){
        return true;
    };

    default void resetParticleData(T particleData){
        return;
    };
}
