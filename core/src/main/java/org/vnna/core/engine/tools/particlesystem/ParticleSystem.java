package org.vnna.core.engine.tools.particlesystem;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import org.vnna.core.engine.media_manager.MediaManager;
import org.vnna.core.engine.media_manager.media.CMediaAnimation;
import org.vnna.core.engine.media_manager.media.CMediaArray;
import org.vnna.core.engine.media_manager.media.CMediaImage;
import org.vnna.core.engine.tools.particlesystem.particle.Particle;
import org.vnna.core.engine.tools.listthreadpool.LThreadPool;
import org.vnna.core.engine.tools.listthreadpool.LThreadPoolUpdater;
import org.vnna.core.engine.tools.listthreadpool.ThreadPoolAlgorithm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.function.Consumer;
import java.util.function.Function;

/*
 * Particle System for Managing Classes that Extend Particle
 */
public class ParticleSystem<T extends Particle> implements LThreadPoolUpdater<T> {

    private LThreadPool threadPool;

    private ArrayList<T> particles;

    private ArrayList<Integer> deleteQueue;

    private MediaManager mediaManager;

    private int particleLimit;

    public ParticleSystem(MediaManager mediaManager) {
        this(mediaManager, Integer.MAX_VALUE, Integer.MAX_VALUE, ThreadPoolAlgorithm.WORKSTEALING, 0);
    }

    public ParticleSystem(MediaManager mediaManager, int particleLimit) {
        this(mediaManager,  particleLimit, Integer.MAX_VALUE, ThreadPoolAlgorithm.WORKSTEALING, 0);
    }

    public ParticleSystem(MediaManager mediaManager, int particleLimit, int objectsPerThread) {
        this(mediaManager,  particleLimit, objectsPerThread, ThreadPoolAlgorithm.WORKSTEALING, 0);
    }

    public ParticleSystem(MediaManager mediaManager, int particleLimit, int objectsPerThread, ThreadPoolAlgorithm threadPoolAlgorithm) {
        this(mediaManager,  particleLimit, objectsPerThread, threadPoolAlgorithm, 5);
    }

    public ParticleSystem(MediaManager mediaManager, int particleLimit, int objectsPerThread, ThreadPoolAlgorithm threadPoolAlgorithm, int fixedThreadCount) {
        this.mediaManager = mediaManager;
        this.particles = new ArrayList<>();
        this.deleteQueue = new ArrayList<>();
        this.particleLimit = particleLimit;
        this.threadPool = new LThreadPool(particles, this, objectsPerThread, threadPoolAlgorithm, fixedThreadCount);
    }

    public void setParticleLimit(int particleLimit) {
        this.particleLimit = particleLimit;
    }

    public void update() {
        if (particles.size() == 0) return;

        this.threadPool.update();
        // remove sorted indexes in reverse order
        if (deleteQueue.size() > 0) {
            if (deleteQueue.size() == particles.size()) {
                particles.clear();
                deleteQueue.clear();
            } else {
                for (int i = deleteQueue.size() - 1; i >= 0; i--) {
                    particles.remove((int) deleteQueue.get(i));
                }
                deleteQueue.clear();
            }
        }
    }


    public void forEveryParticle(Consumer<T> consumer) {
        for (int i = 0; i < particles.size(); i++) {
            consumer.accept(particles.get(i));
        }
        return;
    }

    public void forEveryParticleMarkForDelete(Function<T, Boolean> forFunction) {
        for (int i = 0; i < particles.size(); i++) {
            if (forFunction.apply(particles.get(i))) markForDelete(i);
        }
        return;
    }


    private void markForDelete(int index) {
        synchronized (deleteQueue) {
            int insert = Collections.binarySearch(deleteQueue, index);
            if (insert < 0) {
                deleteQueue.add(-(insert + 1), index);
            }
        }
    }

    public int getParticleCount() {
        return this.particles.size();
    }

    public void addParticle(T particle) {
        if (canAddParticle()) {
            this.particles.add(particle);
        }
    }


    public boolean canAddParticle() {
        return this.particles.size() < particleLimit;
    }

    public int canAddParticleCount() {
        return particleLimit-this.particles.size();
    }

    public void render(SpriteBatch batch) {
        render(batch, 0);
    }

    public void render(SpriteBatch batch, float animation_timer) {
        if (particles.size() == 0) return;

        for (T particle : this.particles) {
            if(!particle.visible){
                continue;
            }
            batch.setColor(particle.r, particle.g, particle.b, particle.a);
            switch (particle.type) {
                case TEXT -> {
                    if (particle.text != null && particle.font != null) {
                        mediaManager.drawCMediaFont(batch,particle.font,particle.x , particle.y , particle.text);
                    }
                }
                case IMAGE -> {
                    mediaManager.drawCMediaImageScale(batch, (CMediaImage) particle.appearance, particle.x , particle.y ,particle.origin_x,particle.origin_y,particle.scaleX, particle.scaleY,particle.rotation);
                }
                case ARRAY -> {
                    mediaManager.drawCMediaArrayScale(batch, (CMediaArray) particle.appearance, particle.x, particle.y , particle.array_index,particle.origin_x,particle.origin_y,particle.scaleX, particle.scaleY, particle.rotation);
                }
                case ANIMATION -> {
                    mediaManager.drawCMediaAnimationScale(batch, (CMediaAnimation) particle.appearance, particle.x, particle.y, animation_timer,particle.origin_x,particle.origin_y,particle.scaleX, particle.scaleY);
                }
            }
        }
        batch.setColor(Color.WHITE);
    }

    @Override
    public void updateFromThread(T particle, int index) {
        if(!particle.update(this, particle, index)){
            markForDelete(index);
        }
    }
}
