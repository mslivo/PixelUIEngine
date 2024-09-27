package net.mslivo.core.engine.tools.particles.sprite;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import net.mslivo.core.engine.media_manager.*;
import net.mslivo.core.engine.tools.particles.*;
import net.mslivo.core.engine.ui_engine.rendering.SpriteRenderer;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.function.Consumer;

/*
 * Particle System must be extended and implemented
 */
public abstract class SpriteParticleSystem<T>{

    private final MediaManager mediaManager;
    private final Color backupColor;
    private final Color backupColor_font;
    private final ArrayList<SpriteParticle> particles;
    private final ArrayDeque<SpriteParticle> deleteQueue;
    private final int particleLimit;
    private final ArrayDeque<SpriteParticle<T>> particlePool;
    private final ParticleDataProvider<T> particleDataProvider;
    private final SpriteParticleConsumer<Object> parallelConsumer;
    private SpriteParticleRenderHook<T> particleRenderHook;

    public interface SpriteParticleConsumer<O> extends Consumer<SpriteParticle> {
        default void accept(SpriteParticle particle) {
        }

        default void accept(SpriteParticle particle, O data) {
        }
    }

    public SpriteParticleSystem(MediaManager mediaManager, int particleLimit){
        this(mediaManager, particleLimit, null, null);
    }

    public SpriteParticleSystem(MediaManager mediaManager, int particleLimit, ParticleDataProvider<T> particleDataProvider){
        this(mediaManager, particleLimit, particleDataProvider, null);
    }

    public SpriteParticleSystem(MediaManager mediaManager, int particleLimit, ParticleDataProvider<T> particleDataProvider, SpriteParticleRenderHook<T> particleRenderHook){
        this.particles = new ArrayList<>();
        this.deleteQueue = new ArrayDeque<>();
        this.particleLimit = Math.max(particleLimit, 0);
        this.particleDataProvider = particleDataProvider != null ? particleDataProvider : new ParticleDataProvider<T>() {};
        this.particleRenderHook = particleRenderHook != null ? particleRenderHook : new SpriteParticleRenderHook<T>() {};
        this.parallelConsumer = new SpriteParticleConsumer<>() {
            @Override
            public void accept(SpriteParticle particle) {
                if (particle!= null && !SpriteParticleSystem.this.updateParticle(particle)) {
                    synchronized (deleteQueue) {
                        deleteQueue.add(particle);
                    }
                }
            }
        };
        this.particlePool = new ArrayDeque<>(particleLimit);
        this.mediaManager = mediaManager;
        this.backupColor = new Color();
        this.backupColor_font = new Color();
    }

    /* ------- Font ------- */

    protected SpriteParticle<T> addFontParticle(CMediaFont cMediaFont, String text, float x, float y) {
        SpriteParticle<T> particle = particleNew(SpriteParticleType.SPRITE_FONT, x, y, 0.5f, 0.5f, 0.5f, 1f, 0f, 1f, 1f, 0, 0f, 0f, null, cMediaFont, text, 0f, true);
        addParticleToSystem(particle);
        return particle;
    }

    protected SpriteParticle<T> addFontParticle(CMediaFont cMediaFont, String text, float x, float y, float r, float g, float b, float a) {
        SpriteParticle<T> particle = particleNew(SpriteParticleType.SPRITE_FONT, x, y, r, g, b, a, 0f, 1f, 1f, 0, 0f, 0f, null, cMediaFont, text, 0f, true);
        addParticleToSystem(particle);
        return particle;
    }

    protected SpriteParticle<T> addFontParticle(CMediaFont cMediaFont, String text, float x, float y, float r, float g, float b, float a, float origin_x, float origin_y) {
        SpriteParticle<T> particle = particleNew(SpriteParticleType.SPRITE_FONT, x, y, r, g, b, a, 0f, 1f, 1f, 0, origin_x, origin_y, null, cMediaFont, text, 0f, true);
        addParticleToSystem(particle);
        return particle;
    }

    /* ------- Image ------- */

    protected SpriteParticle<T> addImageParticle(CMediaImage cMediaImage, float x, float y) {
        SpriteParticle<T> particle = particleNew(SpriteParticleType.SPRITE_IMAGE, x, y, 0.5f, 0.5f, 0.5f, 1f, 0f, 1f, 1f, 0, 0f, 0f, cMediaImage, null, null, 0f, true);
        addParticleToSystem(particle);
        return particle;
    }

    protected SpriteParticle<T> addImageParticle(CMediaImage cMediaImage, float x, float y, float r, float g, float b, float a) {
        SpriteParticle<T> particle = particleNew(SpriteParticleType.SPRITE_IMAGE, x, y, r, g, b, a, 0f, 1f, 1f, 0, 0, 0, cMediaImage, null, null, 0, true);
        addParticleToSystem(particle);
        return particle;
    }

    protected SpriteParticle<T> addImageParticle(CMediaImage cMediaImage, float x, float y, float r, float g, float b, float a, float origin_x, float origin_y, float rotation, float scaleX, float scaleY) {
        SpriteParticle<T> particle = particleNew(SpriteParticleType.SPRITE_IMAGE, x, y, r, g, b, a, rotation, scaleX, scaleY, 0, origin_x, origin_y, cMediaImage, null, null, 0, true);
        addParticleToSystem(particle);
        return particle;
    }

    /* ------- Animation ------- */

    protected SpriteParticle<T> addAnimationParticle(CMediaAnimation cMediaAnimation, float animation_offset, float x, float y) {
        SpriteParticle<T> particle = particleNew(SpriteParticleType.SPRITE_ARRAY, x, y, 0.5f, 0.5f, 0.5f, 1f, 0f, 1f, 1f, 0, 0f, 0f, cMediaAnimation, null, null, animation_offset, true);
        addParticleToSystem(particle);
        return particle;
    }

    protected SpriteParticle<T> addAnimationParticle(CMediaAnimation cMediaAnimation, float animation_offset, float x, float y, float r, float g, float b, float a) {
        SpriteParticle<T> particle = particleNew(SpriteParticleType.SPRITE_ANIMATION, x, y, r, g, b, a, 0f, 1f, 1f, 0, 0f, 0f, cMediaAnimation, null, null, animation_offset, true);
        addParticleToSystem(particle);
        return particle;
    }

    protected SpriteParticle<T> addAnimationParticle(CMediaAnimation cMediaAnimation, float animation_offset, float x, float y, float r, float g, float b, float a, float origin_x, float origin_y, float rotation, float scaleX, float scaleY) {
        SpriteParticle<T> particle = particleNew(SpriteParticleType.SPRITE_ARRAY, x, y, r, g, b, a, rotation, scaleX, scaleY, 0, origin_x, origin_y, cMediaAnimation, null, null, animation_offset, true);
        addParticleToSystem(particle);
        return particle;
    }

    /* ------- Array ------- */

    protected SpriteParticle<T> addArrayParticle(CMediaArray cMediaArray, int array_index, float x, float y) {
        SpriteParticle<T> particle = particleNew(SpriteParticleType.SPRITE_ARRAY, x, y, 0.5f, 0.5f, 0.5f, 1f, 0f, 1f, 1f, array_index, 0f, 0f, cMediaArray, null, null, 0, true);
        addParticleToSystem(particle);
        return particle;
    }

    protected SpriteParticle<T> addArrayParticle(CMediaArray cMediaArray, int array_index, float x, float y, float r, float g, float b, float a) {
        SpriteParticle<T> particle = particleNew(SpriteParticleType.SPRITE_ARRAY, x, y, r, g, b, a, 0f, 1f, 1f, array_index, 0f, 0f, cMediaArray, null, null, 0, true);
        addParticleToSystem(particle);
        return particle;
    }

    protected SpriteParticle<T> addArrayParticle(CMediaArray cMediaArray, int array_index, float x, float y, float r, float g, float b, float a, float origin_x, float origin_y, float rotation, float scaleX, float scaleY) {
        SpriteParticle<T> particle = particleNew(SpriteParticleType.SPRITE_ARRAY, x, y, r, g, b, a, rotation, scaleX, scaleY, array_index, origin_x, origin_y, cMediaArray, null, null, 0, true);
        addParticleToSystem(particle);
        return particle;
    }

    /* ------- Public Methods ------- */

    public SpriteParticleRenderHook<T> getParticleRenderHook() {
        return particleRenderHook;
    }

    public void setParticleRenderHook(SpriteParticleRenderHook<T> particleRenderHook) {
        this.particleRenderHook = particleRenderHook;
    }

    public MediaManager getMediaManager() {
        return mediaManager;
    }

    public void updateParallel() {
        if (particles.size() == 0) return;
        particles.parallelStream().forEach(this.parallelConsumer);
        deleteQueuedParticles();
    }

    public void update() {
        if (particles.size() == 0) return;
        for (int i = 0; i < particles.size(); i++) {
            SpriteParticle<T> particle = particles.get(i);
            if (!updateParticle(particle)) {
                deleteQueue.add(particle);
            }
        }
        deleteQueuedParticles();
    }

    public void render(SpriteRenderer spriteRenderer) {
        render(spriteRenderer, 0);
    }

    public void render(SpriteRenderer spriteRenderer, float animation_timer) {
        if (particles.size() == 0) return;
        backupColor.set(spriteRenderer.getColor());

        configureSpriteRenderer(spriteRenderer);

        for (int i = 0; i < particles.size(); i++) {
            SpriteParticle<T> particle = particles.get(i);
            if (!particle.visible) continue;
            if(!particleRenderHook.renderSpriteParticle(particle)) continue;
            spriteRenderer.setColor(particle.r, particle.g, particle.b, particle.a);
            this.particleRenderHook.beforeRenderParticle(spriteRenderer, particle);
            switch (particle.type) {
                case SPRITE_FONT -> {
                    if (particle.text != null && particle.font != null) {
                        BitmapFont font = mediaManager.getCMediaFont(particle.font);
                        backupColor_font.r = font.getColor().r;
                        backupColor_font.g = font.getColor().g;
                        backupColor_font.b = font.getColor().b;
                        backupColor_font.a = font.getColor().a;
                        // performance: dont use mediamanager
                        font.setColor(particle.r, particle.g, particle.b, particle.a);
                        font.draw(spriteRenderer, particle.text, (particle.x + particle.font.offset_x), (particle.y + particle.font.offset_y));
                        font.setColor(backupColor_font);
                    }
                }
                case SPRITE_IMAGE -> {
                    spriteRenderer.drawCMediaImageScale((CMediaImage) particle.appearance, particle.x, particle.y, particle.origin_x, particle.origin_y, particle.scaleX, particle.scaleY, particle.rotation);
                }
                case SPRITE_ARRAY -> {
                    spriteRenderer.drawCMediaArrayScale((CMediaArray) particle.appearance, particle.x, particle.y, particle.array_index, particle.origin_x, particle.origin_y, particle.scaleX, particle.scaleY, particle.rotation);
                }
                case SPRITE_ANIMATION -> {
                    spriteRenderer.drawCMediaAnimationScale((CMediaAnimation) particle.appearance, particle.x, particle.y, (animation_timer + particle.animation_offset), particle.origin_x, particle.origin_y, particle.scaleX, particle.scaleY);
                }
                default -> {
                    throw new RuntimeException("Particle Type " + particle.type.name() + " not supported by " + this.getClass().getSimpleName());
                }
            }
            this.particleRenderHook.afterRenderParticle(spriteRenderer, particle);
        }

        // Set Back
        spriteRenderer.setColor(backupColor);
    }

    private void configureSpriteRenderer(SpriteRenderer spriteRenderer){
        if(!spriteRenderer.isDrawing()){
            spriteRenderer.begin();
        }
    }

    public boolean canAddParticle() {
        return this.particles.size() < particleLimit;
    }

    public int particleCount() {
        return this.particles.size();
    }

    private void addParticleToSystem(SpriteParticle<T> particle) {
        if (particle == null) return;
        onParticleCreate(particle);
        particles.add(particle);
    }

    public void shutdown() {
        removeAllParticles();
        particlePool.clear();
    }

    public void removeAllParticles() {
        deleteQueue.addAll(particles);
        deleteQueuedParticles();
    }


    public void forEachParticle(SpriteParticleConsumer consumer) {
        for (int i = 0; i < particles.size(); i++) consumer.accept(particles.get(i));
    }

    public <O> void forEachParticle(SpriteParticleConsumer consumer, O data) {
        for (int i = 0; i < particles.size(); i++) consumer.accept(particles.get(i), data);
    }

    /* ------- Private Methods ------- */

    private void deleteQueuedParticles() {
        SpriteParticle<T> deleteParticle;
        while ((deleteParticle = deleteQueue.poll()) != null) {
            removeParticleFromSystem(deleteParticle);
        }
    }

    private void removeParticleFromSystem(SpriteParticle<T> particle) {
        if (particle == null) return;
        onParticleDestroy(particle);
        this.particles.remove(particle);
        // add back to pool
        this.particlePool.add(particle);
    }

    private SpriteParticle particleNew(SpriteParticleType type, float x, float y, float r, float g, float b, float a, float rotation, float scaleX, float scaleY, int array_index, float origin_x, float origin_y, CMediaSprite appearance, CMediaFont font, String text, float animation_offset, boolean visible) {
        if (!canAddParticle()) return null;
        SpriteParticle<T> particle = particlePool.poll();
        if(particle == null) particle = new SpriteParticle<>();
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
        particle.data = particle.data == null ? particleDataProvider.provideNewInstance() : particle.data;
        if(particle.data != null) particleDataProvider.resetInstance(particle.data);
        return particle;
    }

    /* ------- Abstract Methods ------- */

    protected void onParticleCreate(SpriteParticle<T> particle){};

    protected void onParticleDestroy(SpriteParticle<T> particle){};

    protected abstract boolean updateParticle(SpriteParticle<T> particle);

}
