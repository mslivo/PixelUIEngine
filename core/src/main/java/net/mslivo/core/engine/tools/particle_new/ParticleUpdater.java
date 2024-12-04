package net.mslivo.core.engine.tools.particle_new;

import net.mslivo.core.engine.tools.particle_new.particles.Particle;

import java.io.StringReader;

public interface ParticleUpdater<T> {
    default boolean updateParticle(Particle<T> particle){
        return true;
    };

    default void resetParticleData(T particleData){
        return;
    };
}
