package net.mslivo.core.engine.tools.particles;

import net.mslivo.core.engine.tools.Tools;
import net.mslivo.core.engine.tools.particles.particles.Particle;

import java.lang.reflect.Modifier;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Consumer;

public sealed abstract class ParticleSystem<T> permits PrimitiveParticleSystem, SpriteParticleSystem {
    protected Class<T> dataClass;
    protected int maxParticles;
    protected int numParticles;
    protected final HashMap<Class, ArrayDeque<Particle<T>>> particlePools;
    protected final ArrayDeque<Particle<T>> deleteQueue;
    protected final Consumer<Particle<T>> parallelConsumer;
    protected final ParticleUpdater<T> particleUpdater;
    protected final ArrayList<Particle<T>> particles;

    protected ParticleSystem(Class<T> dataClass, ParticleUpdater<T> particleUpdater, int maxParticles) {
        this.dataClass = dataClass;
        this.numParticles = 0;
        this.maxParticles = Math.max(maxParticles, 0);
        this.deleteQueue = new ArrayDeque<>();
        this.particlePools = new HashMap<>();
        this.particles = new ArrayList<>();
        this.particleUpdater = particleUpdater != null ? particleUpdater : new ParticleUpdater<T>() {
        };
        this.parallelConsumer = new Consumer<>() {
            @Override
            public void accept(Particle<T> particle) {
                boolean remove = !particleUpdater.updateParticle(particle);
                if (particle != null && remove) {
                    synchronized (deleteQueue) {
                        deleteQueue.add(particle);
                    }
                }
            }
        };
    }

    public void update() {
        if (this.numParticles == 0) return;
        // Update
        for (int i = 0; i < this.particles.size(); i++) {
            Particle<T> particle = particles.get(i);
            if (!particleUpdater.updateParticle(particle))
                deleteQueue.add(particle);
        }
        // Clear DeleteQueue
        deleteQueuedParticles();
    }

    public void updateParallel() {
        if (this.numParticles == 0) return;
        Tools.App.runParallel(this.particles, this.parallelConsumer);
        deleteQueuedParticles();
    }

    public boolean canAddParticle() {
        return numParticles < maxParticles;
    }

    public int particleCount() {
        return numParticles;
    }

    public void removeAllParticles() {
        this.deleteQueue.addAll(this.particles);
        deleteQueuedParticles();
    }

    public void shutdown() {
        removeAllParticles();
        particlePools.values().forEach(particlePool -> particlePool.clear());
        particlePools.clear();
    }

    public void forEachParticle(Consumer<Particle<T>> particleConsumer) {
        for (int i = 0; i < particles.size(); i++) particleConsumer.accept(particles.get(i));
    }

    private void addParticleToPool(Class particleClass, Particle<T> particle) {
        if (!this.particlePools.containsKey(particleClass))
            this.particlePools.put(particleClass, new ArrayDeque<>());
        this.particlePools.get(particleClass).add(particle);
    }

    protected void removeParticleFromSystem(Particle<T> particle) {
        if (particle == null) return;
        this.particles.remove(particle);
        addParticleToPool(particle.getClass(), particle);
        this.numParticles--;
    }

    protected void addParticleToSystem(Particle<T> particle) {
        if (particle == null) return;
        particles.add(particle);
        this.numParticles++;
    }

    protected void particleSetParticleData(Particle<T> particle, float x, float y, float r, float g, float b, float a, boolean visible) {
        particle.x = x;
        particle.y = y;
        particle.r = r;
        particle.g = g;
        particle.b = b;
        particle.a = a;
        particle.visible = visible;
        if (particle.data == null)
            particle.data = createDataInstance();
        if (particle.data != null)
            particleUpdater.resetParticleData(particle.data);
    }

    protected Particle<T> getParticleFromPool(Class particleClass) {
        if (!this.particlePools.containsKey(particleClass))
            this.particlePools.put(particleClass, new ArrayDeque<>());
        return this.particlePools.get(particleClass).poll();
    }

    private T createDataInstance() {
        if (dataClass == null)
            return null;
        if (dataClass.getEnclosingClass() != null && !Modifier.isStatic(dataClass.getModifiers()))
            throw new RuntimeException("Nested Particle Data classes need to be static");
        try {
            return dataClass.getDeclaredConstructor().newInstance();
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Class is missing default Constructor: " + dataClass.getSimpleName(), e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void deleteQueuedParticles() {
        Particle<T> deleteParticle;
        while ((deleteParticle = deleteQueue.poll()) != null) {
            removeParticleFromSystem(deleteParticle);
        }
    }

}
