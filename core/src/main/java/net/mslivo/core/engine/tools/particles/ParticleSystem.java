package net.mslivo.core.engine.tools.particles;

import net.mslivo.core.engine.media_manager.CMediaFont;
import net.mslivo.core.engine.media_manager.CMediaSprite;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.function.Consumer;

abstract class ParticleSystem<T>{

    final ArrayList<Particle> particles;
    final ArrayDeque<Particle> deleteQueue;
    final int particleLimit;
    final ArrayDeque<Particle<T>> particlePool;
    final ParticleDataProvider<T> particleDataProvider;

    public interface ParticleConsumer<T,O> {
        default void accept(Particle<T> particle){};
        default void accept(Particle<T> particle, O data){};
    }

    ParticleSystem(int particleLimit, ParticleDataProvider<T> particleDataProvider){
        this.particles = new ArrayList<>();
        this.deleteQueue = new ArrayDeque<>();
        this.particleLimit = Math.max(particleLimit, 0);
        this.particleDataProvider = particleDataProvider;
        this.particlePool = new ArrayDeque<>(particleLimit);
    }

    Particle particleNew(ParticleType type, float x, float y, float r, float g, float b, float a, float rotation, float scaleX, float scaleY, int array_index, float origin_x, float origin_y, CMediaSprite appearance, CMediaFont font, String text, float animation_offset, boolean visible) {
        if (!canAddParticle()) return null;
        Particle<T> particle = particlePool.size() > 0 ? particlePool.pop() : new Particle<>();
        particle.type = type;
        particle.x = x;
        particle.y = y;
        particle.r = r;
        particle.g = g;
        particle.b = b;
        particle.a = a;
        particle.rotation = rotation;
        particle.scaleX = scaleX;
        particle.scaleY = scaleY;
        particle.array_index = array_index;
        particle.origin_x = origin_x;
        particle.origin_y = origin_y;
        particle.appearance = appearance;
        particle.font = font;
        particle.text = text;
        particle.animation_offset = animation_offset;
        particle.visible = visible;
        if (this.particleDataProvider != null) {
            if (particle.data == null) particle.data = particleDataProvider.provideNewInstance();
        } else {
            particle.data = null;
        }
        return particle;
    }

    void deleteQueuedParticles() {
        Particle<T> deleteParticle;
        while ((deleteParticle = deleteQueue.poll()) != null) {
            removeParticleFromSystem(deleteParticle);
        }
    }

    void addParticleToSystem(Particle<T> particle) {
        if (particle == null) return;
        onParticleCreate(particle);
        particles.add(particle);
        return;
    }

    void removeParticleFromSystem(Particle<T> particle) {
        if (particle == null) return;
        onParticleDestroy(particle);
        particles.remove(particle);
        // add back to pool
        particlePool.add(particle);
    }

    public boolean canAddParticle() {
        return this.particles.size() < particleLimit;
    }

    public void removeAllParticles() {
        deleteQueue.addAll(particles);
        deleteQueuedParticles();
    }

    public void forEveryParticle(ParticleConsumer<T,?> consumer) {
        forEveryParticle(consumer, null);
    }

    public <O> void forEveryParticle(ParticleConsumer<T,O> consumer, O data) {
        if(data != null) {
            for (int i = 0; i < particles.size(); i++) consumer.accept(particles.get(i), data);
        }else{
            for (int i = 0; i < particles.size(); i++) consumer.accept(particles.get(i));
        }
    }

    public int getParticleCount() {
        return this.particles.size();
    }

    public int canAddParticleAmount() {
        return particleLimit - particles.size();
    }

    public void shutdown() {
        removeAllParticles();
        particlePool.clear();
    }

    public void update() {
        if (particles.size() == 0) return;
        for (int i = 0; i < particles.size(); i++) {
            Particle<T> particle = particles.get(i);
            if (!updateParticle(particle, i)) {
                deleteQueue.add(particle);
            }
        }
        deleteQueuedParticles();
    }

    protected void onParticleCreate(Particle<T> particle){};

    protected void onParticleDestroy(Particle<T> particle){};

    protected abstract boolean updateParticle(Particle<T> particle, int index);

}
