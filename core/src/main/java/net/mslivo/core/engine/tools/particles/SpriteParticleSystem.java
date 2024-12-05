package net.mslivo.core.engine.tools.particles;

import com.badlogic.gdx.graphics.Color;
import net.mslivo.core.engine.media_manager.*;
import net.mslivo.core.engine.tools.particles.particles.*;
import net.mslivo.core.engine.ui_engine.rendering.SpriteRenderer;

public final class SpriteParticleSystem<T> extends ParticleSystem<T> {

    private final Color spriteRendererBackupColor;

    public SpriteParticleSystem(Class<T> dataClass, ParticleUpdater<T> particleUpdater) {
        this(dataClass, particleUpdater, Integer.MAX_VALUE);
    }

    public SpriteParticleSystem(Class<T> dataClass, ParticleUpdater<T> particleUpdater, int maxParticles) {
        super(dataClass, particleUpdater, maxParticles);
        this.spriteRendererBackupColor = new Color(Color.CLEAR);
    }

    public void render(MediaManager mediaManager, SpriteRenderer spriteRenderer) {
        render(mediaManager, spriteRenderer, 0);
    }

    public void render(MediaManager mediaManager, SpriteRenderer spriteRenderer, float animation_timer) {
        if (super.numParticles == 0) return;
        this.spriteRendererBackupColor.set(spriteRenderer.getColor());
        for (int i = 0; i < particles.size(); i++) {
            SpriteParticle<T> particle = (SpriteParticle) particles.get(i);
            if (!particle.visible) continue;
            switch (particle) {
                case ImageParticle<T> imageParticle -> {
                    spriteRenderer.setColor(imageParticle.r, imageParticle.g, imageParticle.b, imageParticle.a);
                    spriteRenderer.drawCMediaImage(imageParticle.sprite, imageParticle.x, imageParticle.y, imageParticle.origin_x, imageParticle.origin_y,
                            mediaManager.imageWidth(imageParticle.sprite), mediaManager.imageHeight(imageParticle.sprite),
                            imageParticle.scaleX, imageParticle.scaleY, imageParticle.rotation);
                }
                case ArrayParticle<T> arrayParticle -> {
                    spriteRenderer.setColor(arrayParticle.r, arrayParticle.g, arrayParticle.b, arrayParticle.a);
                    spriteRenderer.drawCMediaArray(arrayParticle.sprite, arrayParticle.arrayIndex, arrayParticle.x, arrayParticle.y, arrayParticle.origin_x, arrayParticle.origin_y,
                            mediaManager.arrayWidth(arrayParticle.sprite), mediaManager.arrayHeight(arrayParticle.sprite),
                            arrayParticle.scaleX, arrayParticle.scaleY, arrayParticle.rotation);
                }
                case AnimationParticle<T> animationParticle -> {
                    spriteRenderer.setColor(animationParticle.r, animationParticle.g, animationParticle.b, animationParticle.a);
                    spriteRenderer.drawCMediaAnimation(animationParticle.sprite, (animation_timer + animationParticle.animationOffset), animationParticle.x, animationParticle.y, animationParticle.origin_x, animationParticle.origin_y,
                            mediaManager.animationWidth(animationParticle.sprite), mediaManager.animationHeight(animationParticle.sprite),
                            animationParticle.scaleX, animationParticle.scaleY, animationParticle.rotation);
                }
                case TextParticle<T> textParticle -> {
                    spriteRenderer.setColor(textParticle.r, textParticle.g, textParticle.b, textParticle.a);
                    spriteRenderer.drawCMediaFont(textParticle.font, textParticle.x, textParticle.y, textParticle.text, textParticle.centerX, textParticle.centerY);
                }
            }
        }

        spriteRenderer.setColor(spriteRendererBackupColor);
    }

    public ImageParticle<T> addImageParticle(CMediaImage cMediaImage, float x, float y) {
        return addImageParticle(cMediaImage, x, y, 0.5f, 0.5f, 0.5f, 1f, 0f, 0f, 1f, 1f, 0f, true);
    }

    public ImageParticle<T> addImageParticle(CMediaImage cMediaImage, float x, float y, float r, float g, float b, float a) {
        return addImageParticle(cMediaImage, x, y, r, g, b, a, 0f, 0f, 1f, 1f, 0f, true);
    }

    public ImageParticle<T> addImageParticle(CMediaImage cMediaImage, float x, float y, float r, float g, float b, float a, float origin_x, float origin_y) {
        return addImageParticle(cMediaImage, x, y, r, g, b, a, origin_x, origin_y, 1f, 1f, 0f, true);
    }

    public ImageParticle<T> addImageParticle(CMediaImage cMediaImage, float x, float y, float r, float g, float b, float a, float origin_x, float origin_y, float scaleX, float scaleY) {
        return addImageParticle(cMediaImage, x, y, r, g, b, a, origin_x, origin_y, scaleX, scaleY, 0f, true);
    }

    public ImageParticle<T> addImageParticle(CMediaImage cMediaImage, float x, float y, float r, float g, float b, float a, float origin_x, float origin_y, float scaleX, float scaleY, float rotation) {
        return addImageParticle(cMediaImage, x, y, r, g, b, a, origin_x, origin_y, scaleX, scaleY, rotation, true);
    }

    public ImageParticle<T> addImageParticle(CMediaImage cMediaImage, float x, float y, float r, float g, float b, float a, float origin_x, float origin_y, float scaleX, float scaleY, float rotation, boolean visible) {
        if (!canAddParticle())
            return null;
        ImageParticle<T> particle = getNextImageParticle(cMediaImage, x, y, r, g, b, a, origin_x, origin_y, scaleX, scaleY, rotation, visible);
        addParticleToSystem(particle);
        return particle;
    }

    public ArrayParticle<T> addArrayParticle(CMediaArray cMediaArray, int arrayIndex, float x, float y) {
        return addArrayParticle(cMediaArray, arrayIndex, x, y, 0.5f, 0.5f, 0.5f, 1.0f, 0f, 0f, 1f, 1f, 0f, true);
    }

    public ArrayParticle<T> addArrayParticle(CMediaArray cMediaArray, int arrayIndex, float x, float y, float r, float g, float b, float a) {
        return addArrayParticle(cMediaArray, arrayIndex, x, y, r, g, b, a, 0f, 0f, 1f, 1f, 0f, true);
    }

    public ArrayParticle<T> addArrayParticle(CMediaArray cMediaArray, int arrayIndex, float x, float y, float r, float g, float b, float a, float origin_x, float origin_y) {
        return addArrayParticle(cMediaArray, arrayIndex, x, y, r, g, b, a, origin_x, origin_y, 1f, 1f, 0f, true);
    }

    public ArrayParticle<T> addArrayParticle(CMediaArray cMediaArray, int arrayIndex, float x, float y, float r, float g, float b, float a, float origin_x, float origin_y, float scaleX, float scaleY) {
        return addArrayParticle(cMediaArray, arrayIndex, x, y, r, g, b, a, origin_x, origin_y, scaleX, scaleY, 0f, true);
    }

    public ArrayParticle<T> addArrayParticle(CMediaArray cMediaArray, int arrayIndex, float x, float y, float r, float g, float b, float a, float origin_x, float origin_y, float scaleX, float scaleY, float rotation) {
        return addArrayParticle(cMediaArray, arrayIndex, x, y, r, g, b, a, origin_x, origin_y, scaleX, scaleY, rotation, true);
    }

    public ArrayParticle<T> addArrayParticle(CMediaArray cMediaArray, int arrayIndex, float x, float y, float r, float g, float b, float a, float origin_x, float origin_y, float scaleX, float scaleY, float rotation, boolean visible) {
        if (!canAddParticle())
            return null;
        ArrayParticle<T> particle = getNextArrayParticle(cMediaArray, arrayIndex, x, y, r, g, b, a, origin_x, origin_y, scaleX, scaleY, rotation, visible);
        addParticleToSystem(particle);
        return particle;
    }

    public AnimationParticle<T> addAnimationParticle(CMediaAnimation cMediaAnimation, float animationOffset, float x, float y) {
        return addAnimationParticle(cMediaAnimation, animationOffset, x, y, 0.5f, 0.5f, 0.5f, 1f, 0f, 0f, 1f, 1f, 0f, true);
    }

    public AnimationParticle<T> addAnimationParticle(CMediaAnimation cMediaAnimation, float animationOffset, float x, float y, float r, float g, float b, float a) {
        return addAnimationParticle(cMediaAnimation, animationOffset, x, y, r, g, b, a, 0f, 0f, 1f, 1f, 0f, true);
    }

    public AnimationParticle<T> addAnimationParticle(CMediaAnimation cMediaAnimation, float animationOffset, float x, float y, float r, float g, float b, float a, float origin_x, float origin_y) {
        return addAnimationParticle(cMediaAnimation, animationOffset, x, y, r, g, b, a, origin_x, origin_y, 1f, 1f, 0f, true);
    }

    public AnimationParticle<T> addAnimationParticle(CMediaAnimation cMediaAnimation, float animationOffset, float x, float y, float r, float g, float b, float a, float origin_x, float origin_y, float scaleX, float scaleY) {
        return addAnimationParticle(cMediaAnimation, animationOffset, x, y, r, g, b, a, origin_x, origin_y, scaleX, scaleY, 0f, true);
    }

    public AnimationParticle<T> addAnimationParticle(CMediaAnimation cMediaAnimation, float animationOffset, float x, float y, float r, float g, float b, float a, float origin_x, float origin_y, float scaleX, float scaleY, float rotation) {
        return addAnimationParticle(cMediaAnimation, animationOffset, x, y, r, g, b, a, origin_x, origin_y, scaleX, scaleY, rotation, true);
    }

    public AnimationParticle<T> addAnimationParticle(CMediaAnimation cMediaAnimation, float animationOffset, float x, float y, float r, float g, float b, float a, float origin_x, float origin_y, float scaleX, float scaleY, float rotation, boolean visible) {
        if (!canAddParticle())
            return null;
        AnimationParticle<T> particle = getNextAnimationParticle(cMediaAnimation, animationOffset, x, y, r, g, b, a, origin_x, origin_y, scaleX, scaleY, rotation, visible);
        addParticleToSystem(particle);
        return particle;
    }

    public TextParticle<T> addTextParticle(CMediaFont cMediaFont, String text, float x, float y) {
        return addTextParticle(cMediaFont, text, x, y, 0.5f, 0.5f, 0.5f, 1f, false, false, true);
    }

    public TextParticle<T> addTextParticle(CMediaFont cMediaFont, String text, float x, float y, float r, float g, float b, float a) {
        return addTextParticle(cMediaFont, text, x, y, r, g, b, a, false, false, true);
    }

    public TextParticle<T> addTextParticle(CMediaFont cMediaFont, String text, float x, float y, float r, float g, float b, float a, boolean centerX, boolean centerY) {
        return addTextParticle(cMediaFont, text, x, y, r, g, b, a, centerX, centerY, true);
    }

    public TextParticle<T> addTextParticle(CMediaFont cMediaFont, String text, float x, float y, float r, float g, float b, float a, boolean centerX, boolean centerY, boolean visible) {
        if (!canAddParticle())
            return null;
        TextParticle<T> particle = getNextTextParticle(cMediaFont, text, x, y, r, g, b, a, centerX, centerY, visible);
        addParticleToSystem(particle);
        return particle;
    }

    private ImageParticle<T> getNextImageParticle(CMediaImage sprite, float x, float y, float r, float g, float b, float a, float origin_x, float origin_y, float scaleX, float scaleY, float rotation, boolean visible) {

        ImageParticle<T> particle = (ImageParticle<T>) getParticleFromPool(ImageParticle.class);
        if (particle == null)
            particle = new ImageParticle<>();

        super.particleSetParticleData(particle, x, y, r, g, b, a, visible);
        particleSetTextureBasedParticleData(particle, origin_x, origin_y, scaleX, scaleY, rotation);
        particle.sprite = sprite;
        return particle;
    }

    private ArrayParticle<T> getNextArrayParticle(CMediaArray sprite, int arrayIndex, float x, float y, float r, float g, float b, float a, float origin_x, float origin_y, float scaleX, float scaleY, float rotation, boolean visible) {
        ArrayParticle<T> particle = (ArrayParticle<T>) getParticleFromPool(ArrayParticle.class);
        if (particle == null)
            particle = new ArrayParticle<>();
        super.particleSetParticleData(particle, x, y, r, g, b, a, visible);
        particleSetTextureBasedParticleData(particle, origin_x, origin_y, scaleX, scaleY, rotation);
        particle.sprite = sprite;
        particle.arrayIndex = arrayIndex;
        return particle;
    }

    private AnimationParticle<T> getNextAnimationParticle(CMediaAnimation sprite, float animationOffset, float x, float y, float r, float g, float b, float a, float origin_x, float origin_y, float scaleX, float scaleY, float rotation, boolean visible) {
        AnimationParticle<T> particle = (AnimationParticle<T>) getParticleFromPool(AnimationParticle.class);
        if (particle == null)
            particle = new AnimationParticle<>();
        super.particleSetParticleData(particle, x, y, r, g, b, a, visible);
        particleSetTextureBasedParticleData(particle, origin_x, origin_y, scaleX, scaleY, rotation);
        particle.sprite = sprite;
        particle.animationOffset = animationOffset;
        return particle;
    }

    private TextParticle<T> getNextTextParticle(CMediaFont font, String text, float x, float y, float r, float g, float b, float a, boolean centerX, boolean centerY, boolean visible) {
        TextParticle<T> particle = (TextParticle<T>) getParticleFromPool(TextParticle.class);
        if (particle == null)
            particle = new TextParticle<>();
        super.particleSetParticleData(particle, x, y, r, g, b, a, visible);
        particle.font = font;
        particle.text = text;
        particle.centerX = centerX;
        particle.centerY = centerY;
        return particle;
    }

    private void particleSetTextureBasedParticleData(TextureBasedParticle<T> textureBasedParticle, float origin_x, float origin_y, float scaleX, float scaleY, float rotation) {
        textureBasedParticle.origin_x = origin_x;
        textureBasedParticle.origin_y = origin_y;
        textureBasedParticle.scaleX = scaleX;
        textureBasedParticle.scaleY = scaleY;
        textureBasedParticle.rotation = rotation;
    }
}
