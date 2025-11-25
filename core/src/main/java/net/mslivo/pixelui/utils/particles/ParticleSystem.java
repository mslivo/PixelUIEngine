package net.mslivo.pixelui.utils.particles;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.Queue;
import net.mslivo.pixelui.utils.Tools;
import net.mslivo.pixelui.utils.particles.particles.EmptyParticle;
import net.mslivo.pixelui.utils.particles.particles.Particle;

import java.lang.reflect.Modifier;
import java.util.function.Consumer;

public sealed abstract class ParticleSystem<T> implements Disposable permits PrimitiveParticleSystem, SpriteParticleSystem {
    protected Class<T> dataClass;
    protected int maxParticles;
    protected int numParticles;
    protected final ObjectMap<Class, Queue<Particle<T>>> particlePools;
    protected final Queue<Particle<T>> deleteQueue;
    protected final Consumer<Particle<T>> parallelConsumer;
    protected final ParticleUpdater<T> particleUpdater;
    protected final Array<Particle<T>> particles;

    protected ParticleSystem(Class<T> dataClass, ParticleUpdater<T> particleUpdater, int maxParticles) {
        this.dataClass = dataClass;
        this.numParticles = 0;
        this.maxParticles = Math.max(maxParticles, 0);
        this.deleteQueue = new Queue<>();
        this.particlePools = new ObjectMap<>();
        this.particles = new Array<>();
        this.particleUpdater = particleUpdater != null ? particleUpdater : new ParticleUpdater<T>() {
        };
        this.parallelConsumer = particle -> {
            boolean remove = !particleUpdater.updateParticle(particle);
            if (particle != null && remove) {
                synchronized (deleteQueue) {
                    deleteQueue.addLast(particle);
                }
            }
        };
    }

    public void update() {
        if (this.numParticles == 0) return;
        // Update
        for (int i = 0; i < this.particles.size; i++) {
            Particle<T> particle = particles.get(i);
            if (!particleUpdater.updateParticle(particle))
                deleteQueue.addLast(particle);
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

    public boolean hasAnyParticles() {
        return numParticles > 0;
    }


    public void removeAllParticles() {
        for (int i = 0; i < this.particles.size; i++)
            this.deleteQueue.addLast(this.particles.get(i));
        deleteQueuedParticles();
    }

    public int getMaxParticles() {
        return maxParticles;
    }

    public void setMaxParticles(int maxParticles) {
        this.maxParticles = Math.max(maxParticles, 0);
    }

    @Override
    public void dispose() {
        removeAllParticles();
        particlePools.values().forEach(particlePool -> particlePool.clear());
        particlePools.clear();
    }

    public void forEachParticle(Consumer<Particle<T>> particleConsumer) {
        for (int i = 0; i < particles.size; i++) particleConsumer.accept(particles.get(i));
    }

    private void addParticleToPool(Class particleClass, Particle<T> particle) {
        if (!this.particlePools.containsKey(particleClass))
            this.particlePools.put(particleClass, new Queue<>());
        this.particlePools.get(particleClass).addLast(particle);
    }

    protected void removeParticleFromSystem(Particle<T> particle) {
        if (particle == null) return;
        this.particles.removeValue(particle, true);
        addParticleToPool(particle.getClass(), particle);
        this.numParticles--;
    }

    protected void addParticleToSystem(Particle<T> particle) {
        if (particle == null) return;
        particles.add(particle);
        this.numParticles++;
    }

    public void moveParticleToPosition(Particle<T> particle, int newIndex) {
        final int index = particles.indexOf(particle, true);
        if (index < 0 || index == newIndex) return;

        particles.removeIndex(index);
        if (index < newIndex) newIndex--;

        particles.insert(newIndex, particle);
    }

    public void moveParticleToFront(Particle<T> particle) {
        moveParticleToPosition(particle, 0);
    }

    public void moveParticleToEnd(Particle<T> particle) {
        moveParticleToPosition(particle, particles.size - 1);
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
            this.particlePools.put(particleClass, new Queue<>());
        final Queue<Particle<T>> queue = this.particlePools.get(particleClass);
        return queue.isEmpty() ? null : queue.removeFirst();
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

    public Array<Particle<T>> getParticles() {
        return particles;
    }

    public EmptyParticle<T> addEmptyParticle(float x, float y) {
        return addEmptyParticle(x, y, 0.5f, 0.5f, 0.5f, 1f, true);
    }

    public EmptyParticle<T> addEmptyParticle(float x, float y, float r, float g, float b, float a) {
        return addEmptyParticle(x, y, r, g, b, a, true);
    }

    public EmptyParticle<T> addEmptyParticle(float x, float y, float r, float g, float b, float a, boolean visible) {
        if (!canAddParticle())
            return null;
        EmptyParticle<T> particle = getNextEmptyParticle(x, y, r, g, b, a, visible);
        addParticleToSystem(particle);
        return particle;
    }

    private EmptyParticle<T> getNextEmptyParticle(float x, float y, float r, float g, float b, float a, boolean visible) {
        EmptyParticle<T> particle = (EmptyParticle<T>) getParticleFromPool(EmptyParticle.class);
        if (particle == null)
            particle = new EmptyParticle<>();
        this.particleSetParticleData(particle, x, y, r, g, b, a, visible);
        return particle;
    }

    private void deleteQueuedParticles() {
        while (!deleteQueue.isEmpty()) {
            final Particle<T> deleteParticle = deleteQueue.removeFirst();
            removeParticleFromSystem(deleteParticle);
        }
    }

}
