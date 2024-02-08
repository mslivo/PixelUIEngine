package org.mslivo.core.engine.tools.rendering.particles;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import org.mslivo.core.engine.media_manager.MediaManager;
import org.mslivo.core.engine.media_manager.media.*;

/*
 * Particle System must be extended and implemented
 */
public abstract class SpriteParticleSystem<T> extends ParticleSystem<T> {
    private final MediaManager mediaManager;
    private Color backup;
    private Color backup_font;

    public SpriteParticleSystem(MediaManager mediaManager, int particleLimit) {
        this(mediaManager, particleLimit, null);
    }

    public SpriteParticleSystem(MediaManager mediaManager, int particleLimit, ParticleDataProvider<T> particleDataProvider) {
        super(particleLimit, particleDataProvider);
        this.mediaManager = mediaManager;
        backup = new Color();
        backup_font = new Color();
    }

    public void render(SpriteBatch batch) {
        render(batch, 0);
    }

    public void render(SpriteBatch batch, float animation_timer) {
        if (particles.size() == 0) return;
        backup.r = batch.getColor().r;
        backup.g = batch.getColor().g;
        backup.b = batch.getColor().b;
        backup.a = batch.getColor().a;

        for (int i = 0; i < particles.size(); i++) {
            Particle<T> particle = particles.get(i);
            if (!particle.visible) continue;
            batch.setColor(particle.r, particle.g, particle.b, particle.a);
            switch (particle.type) {
                case SPRITE_FONT -> {
                    if (particle.text != null && particle.font != null) {
                        BitmapFont font = mediaManager.getCMediaFont(particle.font);
                        backup_font.r = font.getColor().r;
                        backup_font.g = font.getColor().g;
                        backup_font.b = font.getColor().b;
                        backup_font.a = font.getColor().a;
                        // performance: dont use mediamanager
                        font.setColor(particle.r, particle.g, particle.b, particle.a);
                        font.draw(batch, particle.text, (particle.x + particle.font.offset_x), (particle.y + particle.font.offset_y));
                        font.setColor(backup_font);
                    }
                }
                case SPRITE_IMAGE -> {
                    mediaManager.drawCMediaImageScale(batch, (CMediaImage) particle.appearance, particle.x, particle.y, particle.origin_x, particle.origin_y, particle.scaleX, particle.scaleY, particle.rotation);
                }
                case SPRITE_ARRAY -> {
                    mediaManager.drawCMediaArrayScale(batch, (CMediaArray) particle.appearance, particle.x, particle.y, particle.array_index, particle.origin_x, particle.origin_y, particle.scaleX, particle.scaleY, particle.rotation);
                }
                case SPRITE_ANIMATION -> {
                    mediaManager.drawCMediaAnimationScale(batch, (CMediaAnimation) particle.appearance, particle.x, particle.y, (animation_timer + particle.animation_offset), particle.origin_x, particle.origin_y, particle.scaleX, particle.scaleY);
                }
                case SPRITE_CURSOR -> {
                    mediaManager.drawCMediaCursor(batch, (CMediaCursor) particle.appearance, particle.x, particle.y);
                }
                default -> {
                    throw new RuntimeException("Particle Type " + particle.type.name() + " not supported by " + this.getClass().getSimpleName());
                }
            }
        }
        batch.setColor(backup);
    }


    /* ------- Cursor ------- */
    protected Particle<T> addParticle(CMediaCursor cMediaCursor, float x, float y) {
        Particle<T> particle = particleNew(ParticleType.SPRITE_CURSOR, x, y, 1f, 1f, 1f, 1f, 0f, 1f, 1f, 0, 0f, 0f, cMediaCursor, null, null, 0f, true);
        addParticleToSystem(particle);
        return particle;
    }

    protected Particle<T> addParticle(CMediaCursor cMediaCursor, float x, float y, float r, float g, float b, float a) {
        Particle<T> particle = particleNew(ParticleType.SPRITE_CURSOR, x, y, r, g, b, a, 0f, 1f, 1f, 0, 0f, 0f, cMediaCursor, null, null, 0f, true);
        addParticleToSystem(particle);
        return particle;
    }

    protected Particle<T> addParticle(CMediaCursor cMediaCursor, float x, float y, float r, float g, float b, float a, float origin_x, float origin_y) {
        Particle<T> particle = particleNew(ParticleType.SPRITE_CURSOR, x, y, r, g, b, a, 0f, 1f, 1f, 0, origin_x, origin_y, cMediaCursor, null, null, 0f, true);
        addParticleToSystem(particle);
        return particle;
    }

    /* ------- Font ------- */

    protected Particle<T> addParticle(CMediaFont cMediaFont, String text, float x, float y) {
        Particle<T> particle = particleNew(ParticleType.SPRITE_FONT, x, y, 1f, 1f, 1f, 1f, 0f, 1f, 1f, 0, 0f, 0f, null, cMediaFont, text, 0f, true);
        addParticleToSystem(particle);
        return particle;
    }

    protected Particle<T> addParticle(CMediaFont cMediaFont, String text, float x, float y, float r, float g, float b, float a) {
        Particle<T> particle = particleNew(ParticleType.SPRITE_FONT, x, y, r, g, b, a, 0f, 1f, 1f, 0, 0f, 0f, null, cMediaFont, text, 0f, true);
        addParticleToSystem(particle);
        return particle;
    }

    protected Particle<T> addParticle(CMediaFont cMediaFont, String text, float x, float y, float r, float g, float b, float a, float origin_x, float origin_y) {
        Particle<T> particle = particleNew(ParticleType.SPRITE_FONT, x, y, r, g, b, a, 0f, 1f, 1f, 0, origin_x, origin_y, null, cMediaFont, text, 0f, true);
        addParticleToSystem(particle);
        return particle;
    }

    /* ------- Image ------- */

    protected Particle<T> addParticle(CMediaImage cMediaImage, float x, float y) {
        Particle<T> particle = particleNew(ParticleType.SPRITE_IMAGE, x, y, 1f, 1f, 1f, 1f, 0f, 1f, 1f, 0, 0f, 0f, cMediaImage, null, null, 0f, true);
        addParticleToSystem(particle);
        return particle;
    }

    protected Particle<T> addParticle(CMediaImage cMediaImage, float x, float y, float r, float g, float b, float a) {
        Particle<T> particle = particleNew(ParticleType.SPRITE_IMAGE, x, y, r, g, b, a, 0f, 1f, 1f, 0, 0, 0, cMediaImage, null, null, 0, true);
        addParticleToSystem(particle);
        return particle;
    }

    protected Particle<T> addParticle(CMediaImage cMediaImage, float x, float y, float r, float g, float b, float a, float origin_x, float origin_y, float rotation, float scaleX, float scaleY) {
        Particle<T> particle = particleNew(ParticleType.SPRITE_IMAGE, x, y, r, g, b, a, rotation, scaleX, scaleY, 0, origin_x, origin_y, cMediaImage, null, null, 0, true);
        addParticleToSystem(particle);
        return particle;
    }

    /* ------- Animation ------- */

    protected Particle<T> addParticle(CMediaAnimation cMediaAnimation, float animation_offset, float x, float y) {
        Particle<T> particle = particleNew(ParticleType.SPRITE_ARRAY, x, y, 1f, 1f, 1f, 1f, 0f, 1f, 1f, 0, 0f, 0f, cMediaAnimation, null, null, animation_offset, true);
        addParticleToSystem(particle);
        return particle;
    }

    protected Particle<T> addParticle(CMediaAnimation cMediaAnimation, float animation_offset, float x, float y, float r, float g, float b, float a) {
        Particle<T> particle = particleNew(ParticleType.SPRITE_ARRAY, x, y, r, g, b, a, 0f, 1f, 1f, 0, 0f, 0f, cMediaAnimation, null, null, animation_offset, true);
        addParticleToSystem(particle);
        return particle;
    }

    protected Particle<T> addParticle(CMediaAnimation cMediaAnimation, float animation_offset, float x, float y, float r, float g, float b, float a, float origin_x, float origin_y, float rotation, float scaleX, float scaleY) {
        Particle<T> particle = particleNew(ParticleType.SPRITE_ARRAY, x, y, r, g, b, a, rotation, scaleX, scaleY, 0, origin_x, origin_y, cMediaAnimation, null, null, animation_offset, true);
        addParticleToSystem(particle);
        return particle;
    }

    /* ------- Array ------- */

    protected Particle<T> addParticle(CMediaArray cMediaArray, int array_index, float x, float y) {
        Particle<T> particle = particleNew(ParticleType.SPRITE_ARRAY, x, y, 1f, 1f, 1f, 1f, 0f, 1f, 1f, array_index, 0f, 0f, cMediaArray, null, null, 0, true);
        addParticleToSystem(particle);
        return particle;
    }

    protected Particle<T> addParticle(CMediaArray cMediaArray, int array_index, float x, float y, float r, float g, float b, float a) {
        Particle<T> particle = particleNew(ParticleType.SPRITE_ARRAY, x, y, r, g, b, a, 0f, 1f, 1f, array_index, 0f, 0f, cMediaArray, null, null, 0, true);
        addParticleToSystem(particle);
        return particle;
    }

    protected Particle<T> addParticle(CMediaArray cMediaArray, int array_index, float x, float y, float r, float g, float b, float a, float origin_x, float origin_y, float rotation, float scaleX, float scaleY) {
        Particle<T> particle = particleNew(ParticleType.SPRITE_ARRAY, x, y, r, g, b, a, rotation, scaleX, scaleY, array_index, origin_x, origin_y, cMediaArray, null, null, 0, true);
        addParticleToSystem(particle);
        return particle;
    }
}
