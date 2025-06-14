package net.mslivo.core.engine.tools.particles;

import com.badlogic.gdx.math.MathUtils;
import net.mslivo.core.engine.media_manager.*;
import net.mslivo.core.engine.tools.particles.particles.*;
import net.mslivo.core.engine.ui_engine.rendering.renderer.SpriteRenderer;

public final class SpriteParticleSystem<T> extends ParticleSystem<T> {

    public interface RenderHook<T> {
        void renderBeforeParticle(Particle<T> particle, SpriteRenderer spriteRenderer);

        void renderAfterParticle(Particle<T> particle, SpriteRenderer spriteRenderer);
    }

    private RenderHook renderHook;

    public SpriteParticleSystem(Class<T> dataClass, ParticleUpdater<T> particleUpdater) {
        this(dataClass, particleUpdater, Integer.MAX_VALUE);
    }

    public SpriteParticleSystem(Class<T> dataClass, ParticleUpdater<T> particleUpdater, int maxParticles) {
        this(dataClass, particleUpdater, maxParticles, null);
    }

    public SpriteParticleSystem(Class<T> dataClass, ParticleUpdater<T> particleUpdater, int maxParticles, RenderHook<T> renderHook) {
        super(dataClass, particleUpdater, maxParticles);
        this.renderHook = renderHook;
    }


    public void render(MediaManager mediaManager, SpriteRenderer spriteRenderer) {
        render(mediaManager, spriteRenderer, 0);
    }

    public void render(MediaManager mediaManager, SpriteRenderer spriteRenderer, float animation_timer) {
        if (super.numParticles == 0) return;
        spriteRenderer.saveState();
        for (int i = 0; i < particles.size(); i++) {
            Particle<T> particle = particles.get(i);
            if (!particle.visible) continue;
            if (renderHook != null)
                renderHook.renderBeforeParticle(particle, spriteRenderer);

            final int x = MathUtils.round(particle.x);
            final int y = MathUtils.round(particle.y);

            switch (particle) {
                case ImageParticle<T> imageParticle -> {
                    CMediaImage cMediaImage = (CMediaImage) imageParticle.sprite;
                    spriteRenderer.setColor(imageParticle.r, imageParticle.g, imageParticle.b, imageParticle.a);
                    spriteRenderer.drawCMediaImage(cMediaImage, x, y, imageParticle.origin_x, imageParticle.origin_y,
                            mediaManager.imageWidth(cMediaImage), mediaManager.imageHeight(cMediaImage),
                            imageParticle.scaleX, imageParticle.scaleY, imageParticle.rotation);
                }
                case ArrayParticle<T> arrayParticle -> {
                    CMediaArray cMediaArray = (CMediaArray) arrayParticle.sprite;
                    spriteRenderer.setColor(arrayParticle.r, arrayParticle.g, arrayParticle.b, arrayParticle.a);
                    spriteRenderer.drawCMediaArray(cMediaArray, arrayParticle.arrayIndex,  x, y, arrayParticle.origin_x, arrayParticle.origin_y,
                            mediaManager.arrayWidth(cMediaArray), mediaManager.arrayHeight(cMediaArray),
                            arrayParticle.scaleX, arrayParticle.scaleY, arrayParticle.rotation);
                }
                case AnimationParticle<T> animationParticle -> {
                    CMediaAnimation cMediaAnimation = (CMediaAnimation) animationParticle.sprite;
                    spriteRenderer.setColor(animationParticle.r, animationParticle.g, animationParticle.b, animationParticle.a);
                    spriteRenderer.drawCMediaAnimation(cMediaAnimation, (animation_timer + animationParticle.animationOffset),  x, y, animationParticle.origin_x, animationParticle.origin_y,
                            mediaManager.animationWidth(cMediaAnimation), mediaManager.animationHeight(cMediaAnimation),
                            animationParticle.scaleX, animationParticle.scaleY, animationParticle.rotation);
                }
                case TextParticle<T> textParticle -> {
                    spriteRenderer.setColor(textParticle.r, textParticle.g, textParticle.b, textParticle.a);
                    spriteRenderer.drawCMediaFont(textParticle.font,  x, y, textParticle.text, 0,textParticle.text.length(),textParticle.centerX, textParticle.centerY);
                }
                case EmptyParticle _ -> {
                }
                default -> throw new IllegalStateException("Invalid particle type: "+particle.getClass().getSimpleName());
            }
            if (renderHook != null)
                renderHook.renderAfterParticle(particle, spriteRenderer);
        }

        spriteRenderer.loadState();
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

    public RenderHook getRenderHook() {
        return renderHook;
    }

    public void setRenderHook(RenderHook renderHook) {
        this.renderHook = renderHook;
    }

}
