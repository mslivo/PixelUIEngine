package org.mslivo.core.engine.tools.rendering.particles;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import org.mslivo.core.engine.media_manager.MediaManager;
import org.mslivo.core.engine.media_manager.media.*;
import org.mslivo.core.engine.tools.Tools;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.function.Consumer;

/*
 * Particle System must be extended and implemented
 */
public abstract class ParticleSystem<T> {

    private final ArrayList<Particle> particles;

    private final ArrayDeque<Particle> deleteQueue;

    private final MediaManager mediaManager;

    private final int particleLimit;

    private final ArrayDeque<Particle<T>> particlePool;

    private final ParticleDataProvider<T> particleDataProvider;

    public ParticleSystem(MediaManager mediaManager, int particleLimit) {
        this(mediaManager, particleLimit, null);
    }

    public ParticleSystem(MediaManager mediaManager, int particleLimit, ParticleDataProvider<T> particleDataProvider) {
        this.mediaManager = mediaManager;
        this.particles = new ArrayList<>();
        this.deleteQueue = new ArrayDeque<>();
        this.particleLimit = Tools.Calc.lowerBounds(particleLimit, 0);
        this.particleDataProvider = particleDataProvider;
        particlePool = new ArrayDeque<>(particleLimit);
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

    private Particle<T> particleNew(ParticleType type, float x, float y, float r, float g, float b, float a, float rotation, float scaleX, float scaleY, int array_index, float origin_x, float origin_y, CMediaGFX appearance, CMediaFont font, String text, float animation_offset, boolean visible) {
        if (!canAddParticle()) return null;
        Particle<T> particle = particlePool.size() > 0 ? particlePool.pop() : new Particle<T>();
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

    private void deleteQueuedParticles() {
        Particle<T> deleteParticle;
        while ((deleteParticle = deleteQueue.poll()) != null) {
            removeParticleFromSystem(deleteParticle);
        }
    }

    private void addParticleToSystem(Particle<T> particle) {
        if (particle == null) return;
        onParticleCreate(particle);
        particles.add(particle);
        return;
    }

    private void removeParticleFromSystem(Particle<T> particle) {
        if (particle == null) return;
        onParticleDestroy(particle);
        particles.remove(particle);
        // add back to pool
        particlePool.add(particle);
    }

    public void forEveryParticle(Consumer<Particle> consumer) {
        for (int i = 0; i < particles.size(); i++) consumer.accept(particles.get(i));
    }

    public void removeAllParticles() {
        deleteQueue.addAll(particles);
        deleteQueuedParticles();
    }

    public int getParticleCount() {
        return this.particles.size();
    }

    public boolean canAddParticle() {
        return this.particles.size() < particleLimit;
    }

    public int canAddParticleAmount() {
        return particleLimit - particles.size();
    }

    public void render(SpriteBatch batch) {
        render(batch, 0);
    }

    public void render(SpriteBatch batch, float animation_timer) {
        if (particles.size() == 0) return;
        for (int i = 0; i < particles.size(); i++) {
            Particle<T> particle = particles.get(i);
            if (!particle.visible) continue;
            batch.setColor(particle.r, particle.g, particle.b, particle.a);
            switch (particle.type) {
                case FONT -> {
                    if (particle.text != null && particle.font != null) {
                        BitmapFont font = mediaManager.getCMediaFont(particle.font);
                        float bak_r = font.getColor().r;
                        float bak_g = font.getColor().g;
                        float bak_b = font.getColor().b;
                        float bak_a = font.getColor().a;
                        // performance: dont use mediamanager
                        font.setColor(particle.r, particle.g, particle.b, particle.a);
                        font.draw(batch, particle.text, (particle.x + particle.font.offset_x), (particle.y + particle.font.offset_y));
                        font.setColor(bak_r, bak_g, bak_b, bak_a);
                    }
                }
                case IMAGE -> {
                    mediaManager.drawCMediaImageScale(batch, (CMediaImage) particle.appearance, particle.x, particle.y, particle.origin_x, particle.origin_y, particle.scaleX, particle.scaleY, particle.rotation);
                }
                case ARRAY -> {
                    mediaManager.drawCMediaArrayScale(batch, (CMediaArray) particle.appearance, particle.x, particle.y, particle.array_index, particle.origin_x, particle.origin_y, particle.scaleX, particle.scaleY, particle.rotation);
                }
                case ANIMATION -> {
                    mediaManager.drawCMediaAnimationScale(batch, (CMediaAnimation) particle.appearance, particle.x, particle.y, (animation_timer + particle.animation_offset), particle.origin_x, particle.origin_y, particle.scaleX, particle.scaleY);
                }
                case CURSOR -> {
                    mediaManager.drawCMediaCursor(batch, (CMediaCursor) particle.appearance, particle.x, particle.y);
                }
            }
        }
        batch.setColor(Color.WHITE);
    }

    public void shutdown() {
        removeAllParticles();
    }

    protected abstract boolean updateParticle(Particle<T> particle, int index);

    protected abstract void onParticleCreate(Particle<T> particle);

    protected abstract void onParticleDestroy(Particle<T> particle);

    /* ------- Cursor ------- */
    protected Particle<T> addParticle(CMediaCursor cMediaCursor, float x, float y) {
        Particle<T> particle = particleNew(ParticleType.CURSOR, x, y, 1f, 1f, 1f, 1f, 0f, 1f, 1f, 0, 0f, 0f, cMediaCursor, null, null, 0f, true);
        addParticleToSystem(particle);
        return particle;
    }

    protected Particle<T> addParticle(CMediaCursor cMediaCursor, float x, float y, float r, float g, float b, float a) {
        Particle<T> particle = particleNew(ParticleType.CURSOR, x, y, r, g, b, a, 0f, 1f, 1f, 0, 0f, 0f, cMediaCursor, null, null, 0f, true);
        addParticleToSystem(particle);
        return particle;
    }

    protected Particle<T> addParticle(CMediaCursor cMediaCursor, float x, float y, float r, float g, float b, float a, float origin_x, float origin_y) {
        Particle<T> particle = particleNew(ParticleType.CURSOR, x, y, r, g, b, a, 0f, 1f, 1f, 0, origin_x, origin_y, cMediaCursor, null, null, 0f, true);
        addParticleToSystem(particle);
        return particle;
    }

    /* ------- Font ------- */

    protected Particle<T> addParticle(CMediaFont cMediaFont, String text, float x, float y) {
        Particle<T> particle = particleNew(ParticleType.FONT, x, y, 1f, 1f, 1f, 1f, 0f, 1f, 1f, 0, 0f, 0f, null, cMediaFont, text, 0f, true);
        addParticleToSystem(particle);
        return particle;
    }

    protected Particle<T> addParticle(CMediaFont cMediaFont, String text, float x, float y, float r, float g, float b, float a) {
        Particle<T> particle = particleNew(ParticleType.FONT, x, y, r, g, b, a, 0f, 1f, 1f, 0, 0f, 0f, null, cMediaFont, text, 0f, true);
        addParticleToSystem(particle);
        return particle;
    }

    protected Particle<T> addParticle(CMediaFont cMediaFont, String text, float x, float y, float r, float g, float b, float a, float origin_x, float origin_y) {
        Particle<T> particle = particleNew(ParticleType.FONT, x, y, r, g, b, a, 0f, 1f, 1f, 0, origin_x, origin_y, null, cMediaFont, text, 0f, true);
        addParticleToSystem(particle);
        return particle;
    }

    /* ------- Image ------- */

    protected Particle<T> addParticle(CMediaImage cMediaImage, float x, float y) {
        Particle<T> particle = particleNew(ParticleType.IMAGE, x, y, 1f, 1f, 1f, 1f, 0f, 1f, 1f, 0, 0f, 0f, cMediaImage, null, null, 0f, true);
        addParticleToSystem(particle);
        return particle;
    }

    protected Particle<T> addParticle(CMediaImage cMediaImage, float x, float y, float r, float g, float b, float a) {
        Particle<T> particle = particleNew(ParticleType.IMAGE, x, y, r, g, b, a, 0f, 1f, 1f, 0, 0, 0, cMediaImage, null, null, 0, true);
        addParticleToSystem(particle);
        return particle;
    }

    protected Particle<T> addParticle(CMediaImage cMediaImage, float x, float y, float r, float g, float b, float a, float origin_x, float origin_y, float rotation, float scaleX, float scaleY) {
        Particle<T> particle = particleNew(ParticleType.IMAGE, x, y, r, g, b, a, rotation, scaleX, scaleY, 0, origin_x, origin_y, cMediaImage, null, null, 0, true);
        addParticleToSystem(particle);
        return particle;
    }

    /* ------- Animation ------- */

    protected Particle<T> addParticle(CMediaAnimation cMediaAnimation, float animation_offset, float x, float y) {
        Particle<T> particle = particleNew(ParticleType.ARRAY, x, y, 1f, 1f, 1f, 1f, 0f, 1f, 1f, 0, 0f, 0f, cMediaAnimation, null, null, animation_offset, true);
        addParticleToSystem(particle);
        return particle;
    }

    protected Particle<T> addParticle(CMediaAnimation cMediaAnimation, float animation_offset, float x, float y, float r, float g, float b, float a) {
        Particle<T> particle = particleNew(ParticleType.ARRAY, x, y, r, g, b, a, 0f, 1f, 1f, 0, 0f, 0f, cMediaAnimation, null, null, animation_offset, true);
        addParticleToSystem(particle);
        return particle;
    }

    protected Particle<T> addParticle(CMediaAnimation cMediaAnimation, float animation_offset, float x, float y, float r, float g, float b, float a, float origin_x, float origin_y, float rotation, float scaleX, float scaleY) {
        Particle<T> particle = particleNew(ParticleType.ARRAY, x, y, r, g, b, a, rotation, scaleX, scaleY, 0, origin_x, origin_y, cMediaAnimation, null, null, animation_offset, true);
        addParticleToSystem(particle);
        return particle;
    }

    /* ------- Array ------- */

    protected Particle<T> addParticle(CMediaArray cMediaArray, int array_index, float x, float y) {
        Particle<T> particle = particleNew(ParticleType.ARRAY, x, y, 1f, 1f, 1f, 1f, 0f, 1f, 1f, array_index, 0f, 0f, cMediaArray, null, null, 0, true);
        addParticleToSystem(particle);
        return particle;
    }

    protected Particle<T> addParticle(CMediaArray cMediaArray, int array_index, float x, float y, float r, float g, float b, float a) {
        Particle<T> particle = particleNew(ParticleType.ARRAY, x, y, r, g, b, a, 0f, 1f, 1f, array_index, 0f, 0f, cMediaArray, null, null, 0, true);
        addParticleToSystem(particle);
        return particle;
    }

    protected Particle<T> addParticle(CMediaArray cMediaArray, int array_index, float x, float y, float r, float g, float b, float a, float origin_x, float origin_y, float rotation, float scaleX, float scaleY) {
        Particle<T> particle = particleNew(ParticleType.ARRAY, x, y, r, g, b, a, rotation, scaleX, scaleY, array_index, origin_x, origin_y, cMediaArray, null, null, 0, true);
        addParticleToSystem(particle);
        return particle;
    }
}
