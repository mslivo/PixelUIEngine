package net.mslivo.core.engine.tools.particle_new;

import com.badlogic.gdx.graphics.Color;
import net.mslivo.core.engine.media_manager.*;
import net.mslivo.core.engine.tools.Tools;
import net.mslivo.core.engine.tools.particle_new.particles.*;
import net.mslivo.core.engine.ui_engine.rendering.PrimitiveRenderer;
import net.mslivo.core.engine.ui_engine.rendering.SpriteRenderer;

import java.lang.reflect.Modifier;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Consumer;

public class ParticleSystem<T> {
    private static final int PRIMITIVE_VERTEXES_MAX = 3;
    private Class<T> dataClass;
    private MediaManager mediaManager;
    private SpriteRenderer spriteRenderer;
    private PrimitiveRenderer primitiveRenderer;
    private int maxParticles;
    private int numParticles;
    private final HashMap<Class, ArrayDeque<Particle<T>>> particlePools;
    private final ArrayDeque<Particle<T>> deleteQueue;
    private final Consumer<Particle<T>> parallelConsumer;
    private final ParticleUpdater<T> particleUpdater;
    private final ArrayList<Particle<T>> particles;
    private final Color spriteRendererBackupColor;
    private final Color primitiveRendererBackupColor;

    public ParticleSystem(MediaManager mediaManager, SpriteRenderer spriteRenderer, ParticleUpdater<T> particleUpdater, Class<T> dataClass) {
        this(mediaManager, spriteRenderer, null, dataClass, particleUpdater, Integer.MAX_VALUE);
    }

    public ParticleSystem(MediaManager mediaManager, SpriteRenderer spriteRenderer, Class<T> dataClass, ParticleUpdater<T> particleUpdater, int maxParticles) {
        this(mediaManager, spriteRenderer, null, dataClass, particleUpdater, maxParticles);
    }

    public ParticleSystem(PrimitiveRenderer primitiveRenderer, Class<T> dataClass, ParticleUpdater<T> particleUpdater) {
        this(null, null, primitiveRenderer, dataClass, particleUpdater, Integer.MAX_VALUE);
    }

    public ParticleSystem(PrimitiveRenderer primitiveRenderer, Class<T> dataClass, ParticleUpdater<T> particleUpdater, int maxParticles) {
        this(null, null, primitiveRenderer, dataClass, particleUpdater, maxParticles);
    }

    public ParticleSystem(MediaManager mediaManager, SpriteRenderer spriteRenderer, PrimitiveRenderer primitiveRenderer, Class<T> dataClass, ParticleUpdater<T> particleUpdater, int maxParticles) {
        this.mediaManager = mediaManager;
        this.spriteRenderer = spriteRenderer;
        this.primitiveRenderer = primitiveRenderer;
        this.dataClass = dataClass;
        this.numParticles = 0;
        this.maxParticles = Math.max(maxParticles, 0);
        this.deleteQueue = new ArrayDeque<>();
        this.particlePools = new HashMap<>();
        this.particles = new ArrayList<>();
        this.particleUpdater = particleUpdater;
        this.spriteRendererBackupColor = new Color(Color.CLEAR);
        this.primitiveRendererBackupColor = new Color(Color.CLEAR);
        this.parallelConsumer = new Consumer<>() {
            @Override
            public void accept(Particle<T> particle) {
                boolean remove = !updateParticle(particle);
                if (particle != null && remove) {
                    synchronized (deleteQueue) {
                        deleteQueue.add(particle);
                    }
                }
            }
        };
    }

    public void render() {
        render(0);
    }

    public void render(float animation_timer) {
        if (particles.size() == 0) return;
        if (this.spriteRenderer != null)
            this.spriteRendererBackupColor.set(spriteRenderer.getColor());
        if (this.primitiveRenderer != null)
            this.primitiveRendererBackupColor.set(primitiveRenderer.getVertexColor());

        for (int i = 0; i < particles.size(); i++) {
            Particle<T> particle = particles.get(i);
            if (!particle.visible) continue;

            switch (particle) {
                case ImageParticle imageParticle -> {
                    spriteRenderer.setColor(imageParticle.r, imageParticle.g, imageParticle.b, imageParticle.a);
                    spriteRenderer.drawCMediaImage(imageParticle.sprite, imageParticle.x, imageParticle.y, imageParticle.origin_x, imageParticle.origin_y,
                            mediaManager.imageWidth(imageParticle.sprite), mediaManager.imageHeight(imageParticle.sprite),
                            imageParticle.scaleX, imageParticle.scaleY, imageParticle.rotation);

                }
                case AnimationParticle animationParticle -> {
                    spriteRenderer.setColor(animationParticle.r, animationParticle.g, animationParticle.b, animationParticle.a);
                    spriteRenderer.drawCMediaAnimation(animationParticle.sprite, (animation_timer + animationParticle.animationOffset), animationParticle.x, animationParticle.y, animationParticle.origin_x, animationParticle.origin_y,
                            mediaManager.animationWidth(animationParticle.sprite), mediaManager.animationHeight(animationParticle.sprite),
                            animationParticle.scaleX, animationParticle.scaleY, animationParticle.rotation);
                }
                case ArrayParticle arrayParticle -> {
                    spriteRenderer.setColor(arrayParticle.r, arrayParticle.g, arrayParticle.b, arrayParticle.a);
                    spriteRenderer.drawCMediaArray(arrayParticle.sprite, arrayParticle.arrayIndex, arrayParticle.x, arrayParticle.y, arrayParticle.origin_x, arrayParticle.origin_y,
                            mediaManager.arrayWidth(arrayParticle.sprite), mediaManager.arrayHeight(arrayParticle.sprite),
                            arrayParticle.scaleX, arrayParticle.scaleY, arrayParticle.rotation);
                }
                case TextParticle textParticle -> {
                    spriteRenderer.setColor(textParticle.r, textParticle.g, textParticle.b, textParticle.a);
                    spriteRenderer.drawCMediaFont(textParticle.font, textParticle.x, textParticle.y, textParticle.text, textParticle.centerX, textParticle.centerY);
                }
                case PrimitiveParticle primitiveParticle -> {
                    if (primitiveRenderer.getPrimitiveType() != primitiveParticle.primitiveType) {
                        primitiveRenderer.end();
                        primitiveRenderer.begin(primitiveParticle.primitiveType);
                    }
                    for (int iv = 0; iv < primitiveParticle.numVertexes; iv++) {
                        primitiveRenderer.setVertexColor(primitiveParticle.r[iv], primitiveParticle.g[iv], primitiveParticle.b[iv], primitiveParticle.a[iv]);
                        primitiveRenderer.vertex(primitiveParticle.x[iv], primitiveParticle.y[iv]);
                    }
                }
            }
        }

        if (this.spriteRenderer != null)
            spriteRenderer.setColor(spriteRendererBackupColor);
        if (this.primitiveRenderer != null)
            primitiveRenderer.setVertexColor(primitiveRendererBackupColor);
    }

    public void update() {
        if (this.particles.size() == 0) return;
        // Update
        for (int i = 0; i < this.particles.size(); i++) {
            Particle<T> particle = particles.get(i);
            if (!updateParticle(particle)) {
                deleteQueue.add(particle);
            }
        }
        // Clear DeleteQueue
        deleteQueuedParticles();
    }

    public void updateParallel() {
        if (particles.size() == 0) return;
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

    private boolean updateParticle(Particle<T> particle) {
        if (particleUpdater == null)
            return false;
        return particleUpdater.updateParticle(particle);
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

    public void forEachParticle(Consumer<Particle<T>> particleConsumer) {
        for (int i = 0; i < particles.size(); i++) particleConsumer.accept(particles.get(i));
    }

    private void deleteQueuedParticles() {
        Particle<T> deleteParticle;
        while ((deleteParticle = deleteQueue.poll()) != null) {
            removeParticleFromSystem(deleteParticle);
        }
    }

    private void removeParticleFromSystem(Particle<T> particle) {
        if (particle == null) return;
        this.particles.remove(particle);
        addParticleToPool(particle.getClass(), particle);
        this.numParticles--;
    }

    private void addParticleToSystem(Particle<T> particle) {
        if (particle == null) return;
        particles.add(particle);
        this.numParticles++;
    }

    /* ------- Image ------- */

    public ImageParticle<T> addImageParticle(CMediaImage cMediaImage, float x, float y) {
        if (!canAddParticle())
            return null;
        ImageParticle<T> particle = getNextImageParticle(cMediaImage, x, y, 0.5f, 0.5f, 0.5f, 1f, 0f, 0f, 1f, 1f, 0f, true);
        addParticleToSystem(particle);
        return particle;
    }

    public ArrayParticle<T> addArrayParticle(CMediaArray cMediaArray, int arrayIndex, float x, float y) {
        if (!canAddParticle())
            return null;
        ArrayParticle<T> particle = getNextArrayParticle(cMediaArray, arrayIndex, x, y, 0.5f, 0.5f, 0.5f, 1f, 0f, 0f, 1f, 1f, 0f, true);
        addParticleToSystem(particle);
        return particle;
    }

    public AnimationParticle<T> addAnimationParticle(CMediaAnimation cMediaAnimation, float animationOffset, float x, float y) {
        if (!canAddParticle())
            return null;
        AnimationParticle<T> particle = getNextAnimationParticle(cMediaAnimation, animationOffset, x, y, 0.5f, 0.5f, 0.5f, 1f, 0f, 0f, 1f, 1f, 0f, true);
        addParticleToSystem(particle);
        return particle;
    }

    public TextParticle<T> addTextParticle(CMediaFont cMediaFont, String text, float x, float y) {
        if (!canAddParticle())
            return null;
        TextParticle<T> particle = getNextTextParticle(cMediaFont, text, x, y, 0.5f, 0.5f, 0.5f, 1f, false, false, true);
        addParticleToSystem(particle);
        return particle;
    }

    public PrimitiveParticle<T> addPrimitiveParticle(int particleType, float x1, float y1, float r1, float g1, float b1, float a1) {
        if (!canAddParticle())
            return null;
        PrimitiveParticle<T> particle = getNextPrimitiveParticle(particleType,
                x1, y1, r1, g1, b1, a1,
                true);
        addParticleToSystem(particle);
        return particle;
    }

    public PrimitiveParticle<T> addPrimitiveParticle(int particleType,
                                                     float x1, float y1, float r1, float g1, float b1, float a1,
                                                     float x2, float y2, float r2, float g2, float b2, float a2) {
        if (!canAddParticle())
            return null;
        PrimitiveParticle<T> particle = getNextPrimitiveParticle(particleType,
                x1, y1, r1, g1, b1, a1,
                x2, y2, r2, g2, b2, a2,
                true);
        addParticleToSystem(particle);
        return particle;
    }

    public PrimitiveParticle<T> addPrimitiveParticle(int particleType,
                                                     float x1, float y1, float r1, float g1, float b1, float a1,
                                                     float x2, float y2, float r2, float g2, float b2, float a2,
                                                     float x3, float y3, float r3, float g3, float b3, float a3) {
        if (!canAddParticle())
            return null;
        PrimitiveParticle<T> particle = getNextPrimitiveParticle(particleType,
                x1, y1, r1, g1, b1, a1,
                x2, y2, r2, g2, b2, a2,
                x3, y3, r3, g3, b3, a3,
                true);
        addParticleToSystem(particle);
        return particle;
    }


    private ImageParticle<T> getNextImageParticle(CMediaImage sprite, float x, float y, float r, float g, float b, float a, float origin_x, float origin_y, float scaleX, float scaleY, float rotation, boolean visible) {

        ImageParticle<T> particle = (ImageParticle<T>) getParticleFromPool(ImageParticle.class);
        if (particle == null)
            particle = new ImageParticle<>();

        particleSetParticleData(particle, visible);
        particleSetSpriteParticleData(particle, x, y, r, g, b, a, origin_x, origin_y, scaleX, scaleY, rotation);
        particle.sprite = sprite;
        return particle;
    }

    private ArrayParticle<T> getNextArrayParticle(CMediaArray sprite, int arrayIndex, float x, float y, float r, float g, float b, float a, float origin_x, float origin_y, float scaleX, float scaleY, float rotation, boolean visible) {
        ArrayParticle<T> particle = (ArrayParticle<T>) getParticleFromPool(ArrayParticle.class);
        if (particle == null)
            particle = new ArrayParticle<>();
        particleSetParticleData(particle, visible);
        particleSetSpriteParticleData(particle, x, y, r, g, b, a, origin_x, origin_y, scaleX, scaleY, rotation);
        particle.sprite = sprite;
        particle.arrayIndex = arrayIndex;
        return particle;
    }

    private AnimationParticle<T> getNextAnimationParticle(CMediaAnimation sprite, float animationOffset, float x, float y, float r, float g, float b, float a, float origin_x, float origin_y, float scaleX, float scaleY, float rotation, boolean visible) {
        AnimationParticle<T> particle = (AnimationParticle<T>) getParticleFromPool(AnimationParticle.class);
        if (particle == null)
            particle = new AnimationParticle<>();
        particleSetParticleData(particle, visible);
        particleSetSpriteParticleData(particle, x, y, r, g, b, a, origin_x, origin_y, scaleX, scaleY, rotation);
        particle.sprite = sprite;
        particle.animationOffset = animationOffset;
        return particle;
    }

    private TextParticle<T> getNextTextParticle(CMediaFont font, String text, float x, float y, float r, float g, float b, float a, boolean centerX, boolean centerY, boolean visible) {
        TextParticle<T> particle = (TextParticle<T>) getParticleFromPool(TextParticle.class);
        if (particle == null)
            particle = new TextParticle<>();
        particleSetParticleData(particle, visible);
        particle.font = font;
        particle.text = text;
        particle.x = x;
        particle.y = y;
        particle.r = r;
        particle.g = g;
        particle.b = b;
        particle.a = a;
        particle.centerX = centerX;
        particle.centerY = centerY;
        return particle;
    }

    private PrimitiveParticle<T> getNextPrimitiveParticle(int primitiveType,
                                                          float x1, float y1, float r1, float g1, float b1, float a1,
                                                          boolean visible) {
        PrimitiveParticle<T> particle = (PrimitiveParticle<T>) getParticleFromPool(PrimitiveParticle.class);
        if (particle == null) {
            particle = new PrimitiveParticle<>();
            primitiveParticleCreateArrays(particle);
        }
        particleSetParticleData(particle, visible);
        particle.primitiveType = primitiveType;
        particle.x[0] = x1;
        particle.y[0] = y1;
        particle.r[0] = r1;
        particle.g[0] = g1;
        particle.b[0] = b1;
        particle.a[0] = a1;
        particle.numVertexes = 1;
        return particle;
    }

    private PrimitiveParticle<T> getNextPrimitiveParticle(int primitiveType,
                                                          float x1, float y1, float r1, float g1, float b1, float a1,
                                                          float x2, float y2, float r2, float g2, float b2, float a2,
                                                          boolean visible) {
        PrimitiveParticle<T> particle = (PrimitiveParticle<T>) getParticleFromPool(PrimitiveParticle.class);
        if (particle == null) {
            particle = new PrimitiveParticle<>();
            primitiveParticleCreateArrays(particle);
        }
        particleSetParticleData(particle, visible);
        particle.primitiveType = primitiveType;
        particle.numVertexes = 2;

        particle.x[0] = x1;
        particle.y[0] = y1;
        particle.r[0] = r1;
        particle.g[0] = g1;
        particle.b[0] = b1;
        particle.a[0] = a1;

        particle.x[1] = x2;
        particle.y[1] = y2;
        particle.r[1] = r2;
        particle.g[1] = g2;
        particle.b[1] = b2;
        particle.a[1] = a2;

        return particle;
    }

    private PrimitiveParticle<T> getNextPrimitiveParticle(int primitiveType,
                                                          float x1, float y1, float r1, float g1, float b1, float a1,
                                                          float x2, float y2, float r2, float g2, float b2, float a2,
                                                          float x3, float y3, float r3, float g3, float b3, float a3,
                                                          boolean visible) {
        PrimitiveParticle<T> particle = (PrimitiveParticle<T>) getParticleFromPool(PrimitiveParticle.class);
        if (particle == null) {
            particle = new PrimitiveParticle<>();
            primitiveParticleCreateArrays(particle);
        }
        particleSetParticleData(particle, visible);
        particle.primitiveType = primitiveType;
        particle.numVertexes = 3;

        particle.x[0] = x1;
        particle.y[0] = y1;
        particle.r[0] = r1;
        particle.g[0] = g1;
        particle.b[0] = b1;
        particle.a[0] = a1;

        particle.x[1] = x2;
        particle.y[1] = y2;
        particle.r[1] = r2;
        particle.g[1] = g2;
        particle.b[1] = b2;
        particle.a[1] = a2;

        particle.x[2] = x3;
        particle.y[2] = y3;
        particle.r[2] = r3;
        particle.g[2] = g3;
        particle.b[2] = b3;
        particle.a[2] = a3;

        return particle;
    }

    private void particleSetParticleData(Particle<T> particle, boolean visible) {
        particle.visible = visible;
        if (particle.data == null)
            particle.data = createDataInstance();
        if (particle.data != null)
            particleUpdater.resetParticleData(particle.data);
    }

    private void particleSetSpriteParticleData(SpriteParticle<T> spriteParticle, float x, float y, float r, float g, float b, float a, float origin_x, float origin_y, float scaleX, float scaleY, float rotation) {
        spriteParticle.x = x;
        spriteParticle.y = y;
        spriteParticle.r = r;
        spriteParticle.g = g;
        spriteParticle.b = b;
        spriteParticle.a = a;
        spriteParticle.origin_x = origin_x;
        spriteParticle.origin_y = origin_y;
        spriteParticle.scaleX = scaleX;
        spriteParticle.scaleY = scaleY;
        spriteParticle.rotation = rotation;
    }

    private void primitiveParticleCreateArrays(PrimitiveParticle<T> primitiveParticle) {
        primitiveParticle.x = new float[PRIMITIVE_VERTEXES_MAX];
        primitiveParticle.y = new float[PRIMITIVE_VERTEXES_MAX];
        primitiveParticle.r = new float[PRIMITIVE_VERTEXES_MAX];
        primitiveParticle.g = new float[PRIMITIVE_VERTEXES_MAX];
        primitiveParticle.b = new float[PRIMITIVE_VERTEXES_MAX];
        primitiveParticle.a = new float[PRIMITIVE_VERTEXES_MAX];
    }

    private Particle<T> getParticleFromPool(Class particleClass) {
        if (!this.particlePools.containsKey(particleClass))
            this.particlePools.put(particleClass, new ArrayDeque<>());
        return this.particlePools.get(particleClass).poll();
    }

    private void addParticleToPool(Class particleClass, Particle<T> particle) {
        if (!this.particlePools.containsKey(particleClass))
            this.particlePools.put(particleClass, new ArrayDeque<>());
        this.particlePools.get(particleClass).add(particle);
    }

}
