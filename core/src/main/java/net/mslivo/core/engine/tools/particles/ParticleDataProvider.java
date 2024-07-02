package net.mslivo.core.engine.tools.particles;

public interface ParticleDataProvider<D> {

    default D provideNewInstance(){return null;};

}
