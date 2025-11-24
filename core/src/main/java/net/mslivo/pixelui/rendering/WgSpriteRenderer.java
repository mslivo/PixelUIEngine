package net.mslivo.pixelui.rendering;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL32;
import com.badlogic.gdx.graphics.g2d.BitmapFontCache;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Affine2;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.NumberUtils;
import com.monstrous.gdx.webgpu.graphics.WgShaderProgram;
import com.monstrous.gdx.webgpu.graphics.g2d.WgSpriteBatch;
import net.mslivo.pixelui.media.*;

public class WgSpriteRenderer extends WgSpriteBatch {

    private MediaManager mediaManager;
    private Color tempColor;

    public WgSpriteRenderer(MediaManager mediaManager) {
        super();
        this.mediaManager = mediaManager;
        this.tempColor = new Color();
    }

    public WgSpriteRenderer(MediaManager mediaManager,int maxSprites) {
        super(maxSprites);
        this.mediaManager = mediaManager;
        this.tempColor = new Color();
    }

    public WgSpriteRenderer(MediaManager mediaManager,int maxSprites, WgShaderProgram specificShader) {
        super(maxSprites, specificShader);
        this.mediaManager = mediaManager;
        this.tempColor = new Color();
    }

    public WgSpriteRenderer(MediaManager mediaManager,int maxSprites, WgShaderProgram specificShader, int maxFlushes) {
        super(maxSprites, specificShader, maxFlushes);
        this.mediaManager = mediaManager;
        this.tempColor = new Color();
    }

    // ----- CMediaSprite -----

    public void drawCMediaSprite(CMediaSprite cMediaSprite, final int index, final float animationTimer, final float x, final float y) {
        TextureRegion region = mediaManager.sprite(cMediaSprite, index, animationTimer);
        this.draw(region, x, y);
    }

    public void drawCMediaSprite(CMediaSprite cMediaSprite, final int index, final float animationTimer, final float x, final float y, final float width, final float height) {
        TextureRegion region = mediaManager.sprite(cMediaSprite, index, animationTimer);
        this.draw(region, x, y, width, height);
    }

    public void drawCMediaSprite(CMediaSprite cMediaSprite, final int index, final float animationTimer, final float x, final float y, final int srcX, final int srcY, final int srcWidth, final int srcHeight) {
        TextureRegion region = mediaManager.sprite(cMediaSprite, index, animationTimer);
        this.draw(region.getTexture(), x, y, region.getRegionX() + srcX, region.getRegionY() + srcY, srcWidth, srcHeight);
    }

    public void drawCMediaSprite(CMediaSprite cMediaSprite, final int index, final float animationTimer, final float x, final float y, final float originX, final float originY, final float width, final float height, final float scaleX, final float scaleY, final float rotation) {
        TextureRegion region = mediaManager.sprite(cMediaSprite, index, animationTimer);
        this.draw(region, x, y, originX, originY, width, height, scaleX, scaleY, rotation);
    }

    public void drawCMediaSprite(CMediaSprite cMediaSprite, final int index, final float animationTimer, final float x, final float y, final float originX, final float originY, final float width, final float height, final float scaleX, final float scaleY, final float rotation, final boolean clockwise) {
        TextureRegion region = mediaManager.sprite(cMediaSprite, index, animationTimer);
        this.draw(region, x, y, originX, originY, width, height, scaleX, scaleY, rotation, clockwise);
    }

    public void drawCMediaSprite(CMediaSprite cMediaSprite, final int index, final float animationTimer, final float x, final float y, final float originX, final float originY, final float width, final float height, final float scaleX, final float scaleY, final float rotation, final int srcX, final int srcY, final int srcWidth, final int srcHeight, final boolean flipX, final boolean flipY) {
        TextureRegion region = mediaManager.sprite(cMediaSprite, index, animationTimer);
        this.draw(region.getTexture(), x, y, originX, originY, width, height, scaleX, scaleY, rotation, region.getRegionX() + srcX, region.getRegionY() + srcY, srcWidth, srcHeight, flipX, flipY);
    }

    public void drawCMediaSprite(CMediaSprite cMediaSprite, final int index, final float animationTimer, final float x, final float y, final float width, final float height, final int srcX, final int srcY, final int srcWidth, final int srcHeight, final boolean flipX, final boolean flipY) {
        TextureRegion region = mediaManager.sprite(cMediaSprite, index, animationTimer);
        this.draw(region.getTexture(), x, y, width, height, region.getRegionX() + srcX, region.getRegionY() + srcY, srcWidth, srcHeight, flipX, flipY);
    }

    public void drawCMediaSprite(CMediaSprite cMediaSprite, final int index, final float animationTimer, final float x, final float y, final float width, final float height, final float u, final float v, final float u2, final float v2) {
        TextureRegion region = mediaManager.sprite(cMediaSprite, index, animationTimer);
        this.draw(region.getTexture(), x, y, width, height, u, v, u2, v2);
    }

    public void drawCMediaSprite(CMediaSprite cMediaSprite, final int index, final float animationTimer, final float[] spriteVertices, final int offset, final int count) {
        TextureRegion region = mediaManager.sprite(cMediaSprite, index, animationTimer);
        this.draw(region.getTexture(), spriteVertices, offset, count);
    }

    public void drawCMediaSprite(CMediaSprite cMediaSprite, final int index, final float animationTimer, final float width, final float height, final Affine2 transform) {
        TextureRegion region = mediaManager.sprite(cMediaSprite, index, animationTimer);
        this.draw(region, width, height, transform);
    }

    // ----- CMediaImage -----

    public void drawCMediaImage(final CMediaImage cMediaImage, final float x, final float y) {
        final TextureRegion region = mediaManager.image(cMediaImage);
        this.draw(region, x, y);
    }

    public void drawCMediaImage(final CMediaImage cMediaImage, final float x, final float y, final float width, final float height) {
        final TextureRegion region = mediaManager.image(cMediaImage);
        this.draw(region, x, y, width, height);
    }

    public void drawCMediaImage(final CMediaImage cMediaImage, final float x, final float y, final int srcX, final int srcY, final int srcWidth, final int srcHeight) {
        final TextureRegion region = mediaManager.image(cMediaImage);
        this.draw(region.getTexture(), x, y, region.getRegionX() + srcX, region.getRegionY() + srcY, srcWidth, srcHeight);
    }

    public void drawCMediaImage(final CMediaImage cMediaImage, final float x, final float y, final float originX, final float originY, final float width, final float height, final float scaleX, final float scaleY, final float rotation) {
        final TextureRegion region = mediaManager.image(cMediaImage);
        this.draw(region, x, y, originX, originY, width, height, scaleX, scaleY, rotation);
    }

    public void drawCMediaImage(final CMediaImage cMediaImage, final float x, final float y, final float originX, final float originY, final float width, final float height, final float scaleX, final float scaleY, final float rotation, final boolean clockwise) {
        final TextureRegion region = mediaManager.image(cMediaImage);
        this.draw(region, x, y, originX, originY, width, height, scaleX, scaleY, rotation, clockwise);
    }

    public void drawCMediaImage(final CMediaImage cMediaImage, final float x, final float y, final float originX, final float originY, final float width, final float height, final float scaleX, final float scaleY, final float rotation, final int srcX, final int srcY, final int srcWidth, final int srcHeight, final boolean flipX, final boolean flipY) {
        final TextureRegion region = mediaManager.image(cMediaImage);
        this.draw(region.getTexture(), x, y, originX, originY, width, height, scaleX, scaleY, rotation, region.getRegionX() + srcX, region.getRegionY() + srcY, srcWidth, srcHeight, flipX, flipY);
    }

    public void drawCMediaImage(final CMediaImage cMediaImage, final float x, final float y, final float width, final float height, final int srcX, final int srcY, final int srcWidth, final int srcHeight, boolean flipX, boolean flipY) {
        final TextureRegion region = mediaManager.image(cMediaImage);
        this.draw(region.getTexture(), x, y, width, height, region.getRegionX() + srcX, region.getRegionY() + srcY, srcWidth, srcHeight, flipX, flipY);
    }

    public void drawCMediaImage(final CMediaImage cMediaImage, final float x, final float y, final float width, final float height, final float u, final float v, final float u2, final float v2) {
        final TextureRegion region = mediaManager.image(cMediaImage);
        this.draw(region.getTexture(), x, y, width, height, u, v, u2, v2);
    }

    public void drawCMediaImage(final CMediaImage cMediaImage, final float[] spriteVertices, final int offset, final int count) {
        final TextureRegion region = mediaManager.image(cMediaImage);
        this.draw(region.getTexture(), spriteVertices, offset, count);
    }

    public void drawCMediaImage(final CMediaImage cMediaImage, final float width, final float height, final Affine2 transform) {
        final TextureRegion region = mediaManager.image(cMediaImage);
        this.draw(region, width, height, transform);
    }

    // ----- CMediaArray -----

    public void drawCMediaArray(final CMediaArray cMediaArray, final int index, final float x, final float y) {
        final TextureRegion region = mediaManager.array(cMediaArray, index);
        this.draw(region, x, y, region.getRegionWidth(), region.getRegionHeight());
    }

    public void drawCMediaArray(final CMediaArray cMediaArray, final int index, final float x, final float y, final float width, final float height) {
        final TextureRegion region = mediaManager.array(cMediaArray, index);
        this.draw(region, x, y, width, height);
    }

    public void drawCMediaArray(final CMediaArray cMediaArray, final int index, final float x, final float y, final int srcX, final int srcY, final int srcWidth, final int srcHeight) {
        final TextureRegion region = mediaManager.array(cMediaArray, index);
        this.draw(region.getTexture(), x, y, region.getRegionX() + srcX, region.getRegionY() + srcY, srcWidth, srcHeight);
    }

    public void drawCMediaArray(final CMediaArray cMediaArray, final int index, final float x, final float y, final float originX, final float originY, final float width, final float height, final float scaleX, final float scaleY, final float rotation) {
        final TextureRegion region = mediaManager.array(cMediaArray, index);
        this.draw(region, x, y, originX, originY, width, height, scaleX, scaleY, rotation);
    }

    public void drawCMediaArray(final CMediaArray cMediaArray, final int index, final float x, final float y, final float originX, final float originY, final float width, final float height, final float scaleX, final float scaleY, final float rotation, boolean clockwise) {
        final TextureRegion region = mediaManager.array(cMediaArray, index);
        this.draw(region, x, y, originX, originY, width, height, scaleX, scaleY, rotation, clockwise);
    }

    public void drawCMediaArray(final CMediaArray cMediaArray, final int index, final float x, final float y, final float originX, final float originY, final float width, final float height, final float scaleX, final float scaleY, final float rotation, final int srcX, final int srcY, final int srcWidth, final int srcHeight, final boolean flipX, final boolean flipY) {
        final TextureRegion region = mediaManager.array(cMediaArray, index);
        this.draw(region.getTexture(), x, y, originX, originY, width, height, scaleX, scaleY, rotation, region.getRegionX() + srcX, region.getRegionY() + srcY, srcWidth, srcHeight, flipX, flipY);
    }

    public void drawCMediaArray(final CMediaArray cMediaArray, final int index, final float x, final float y, final float width, final float height, final int srcX, final int srcY, final int srcWidth, final int srcHeight, final boolean flipX, final boolean flipY) {
        final TextureRegion region = mediaManager.array(cMediaArray, index);
        this.draw(region.getTexture(), x, y, width, height, region.getRegionX() + srcX, region.getRegionY() + srcY, srcWidth, srcHeight, flipX, flipY);
    }

    public void drawCMediaArray(final CMediaArray cMediaArray, final int index, final float x, final float y, final float width, final float height, final float u, final float v, final float u2, final float v2) {
        final TextureRegion region = mediaManager.array(cMediaArray, index);
        this.draw(region.getTexture(), x, y, width, height, u, v, u2, v2);
    }

    public void drawCMediaArray(final CMediaArray cMediaArray, final int index, final float[] spriteVertices, final int offset, final int count) {
        final TextureRegion region = mediaManager.array(cMediaArray, index);
        this.draw(region.getTexture(), spriteVertices, offset, count);
    }

    public void drawCMediaArray(final CMediaArray cMediaArray, final int index, final float width, final float height, final Affine2 transform) {
        final TextureRegion region = mediaManager.array(cMediaArray, index);
        this.draw(region, width, height, transform);
    }

    // ----- CMediaAnimation -----

    public void drawCMediaAnimation(final CMediaAnimation cMediaAnimation, final float animationTimer, final float x, final float y) {
        final TextureRegion region = mediaManager.animation(cMediaAnimation).getKeyFrame(animationTimer);
        this.draw(region, x, y);
    }

    public void drawCMediaAnimation(final CMediaAnimation cMediaAnimation, final float animationTimer, final float x, final float y, final float width, final float height) {
        final TextureRegion region = mediaManager.animation(cMediaAnimation).getKeyFrame(animationTimer);
        this.draw(region, x, y, width, height);
    }

    public void drawCMediaAnimation(final CMediaAnimation cMediaAnimation, final float animationTimer, final float x, final float y, final int srcX, final int srcY, final int srcWidth, final int srcHeight) {
        final TextureRegion region = mediaManager.animation(cMediaAnimation).getKeyFrame(animationTimer);
        this.draw(region.getTexture(), x, y, region.getRegionX() + srcX, region.getRegionY() + srcY, srcWidth, srcHeight);
    }

    public void drawCMediaAnimation(final CMediaAnimation cMediaAnimation, final float animationTimer, final float x, final float y, final float originX, final float originY, final float width, final float height, final float scaleX, final float scaleY, final float rotation) {
        final TextureRegion region = mediaManager.animation(cMediaAnimation).getKeyFrame(animationTimer);
        this.draw(region, x, y, originX, originY, width, height, scaleX, scaleY, rotation);
    }

    public void drawCMediaAnimation(final CMediaAnimation cMediaAnimation, final float animationTimer, final float x, final float y, final float originX, final float originY, final float width, final float height, final float scaleX, final float scaleY, final float rotation, final boolean clockwise) {
        final TextureRegion region = mediaManager.animation(cMediaAnimation).getKeyFrame(animationTimer);
        this.draw(region, x, y, originX, originY, width, height, scaleX, scaleY, rotation, clockwise);
    }

    public void drawCMediaAnimation(final CMediaAnimation cMediaAnimation, final float animationTimer, final float x, final float y, final float originX, final float originY, final float width, final float height, final float scaleX, final float scaleY, final float rotation, final int srcX, final int srcY, final int srcWidth, final int srcHeight, final boolean flipX, final boolean flipY) {
        final TextureRegion region = mediaManager.animation(cMediaAnimation).getKeyFrame(animationTimer);
        this.draw(region.getTexture(), x, y, originX, originY, width, height, scaleX, scaleY, rotation, region.getRegionX() + srcX, region.getRegionY() + srcY, srcWidth, srcHeight, flipX, flipY);
    }

    public void drawCMediaAnimation(final CMediaAnimation cMediaAnimation, final float animationTimer, final float x, final float y, final float width, final float height, final int srcX, final int srcY, final int srcWidth, final int srcHeight, boolean flipX, boolean flipY) {
        final TextureRegion region = mediaManager.animation(cMediaAnimation).getKeyFrame(animationTimer);
        this.draw(region.getTexture(), x, y, width, height, region.getRegionX() + srcX, region.getRegionY() + srcY, srcWidth, srcHeight, flipX, flipY);
    }

    public void drawCMediaAnimation(final CMediaAnimation cMediaAnimation, final float animationTimer, final float x, final float y, final float width, final float height, final float u, final float v, final float u2, final float v2) {
        final TextureRegion region = mediaManager.animation(cMediaAnimation).getKeyFrame(animationTimer);
        this.draw(region.getTexture(), x, y, width, height, u, v, u2, v2);
    }

    public void drawCMediaAnimation(final CMediaAnimation cMediaAnimation, final float animationTimer, final float[] spriteVertices, final int offset, final int count) {
        TextureRegion region = mediaManager.animation(cMediaAnimation).getKeyFrame(animationTimer);
        this.draw(region.getTexture(), spriteVertices, offset, count);
    }

    public void drawCMediaAnimation(final CMediaAnimation cMediaAnimation, final float animationTimer, final float width, final float height, final Affine2 transform) {
        TextureRegion region = mediaManager.animation(cMediaAnimation).getKeyFrame(animationTimer);
        this.draw(region, width, height, transform);
    }

    // ----- CMediaFont -----

    public void drawCMediaFont(final CMediaFont cMediaFont, final float x, final float y, CharSequence text) {
        this.drawCMediaFont(cMediaFont, x, y, text,0,text.length(), false, false, 0);
    }

    public void drawCMediaFont(final CMediaFont cMediaFont, final float x, final float y, CharSequence text, final int start, final int end) {
        this.drawCMediaFont(cMediaFont, x, y, text,start,end, false, false, 0);
    }

    public void drawCMediaFont(final CMediaFont cMediaFont, final float x, final float y, CharSequence text, final int start, final int end, final boolean centerX, final boolean centerY) {
        this.drawCMediaFont(cMediaFont, x, y, text,start,end, centerX, centerY, 0);
    }

    public void drawCMediaFont(final CMediaFont cMediaFont, final float x, final float y, CharSequence text, final int start, final int end, final boolean centerX, final boolean centerY, final int maxWidth) {
        if (cMediaFont == null) return;
        final float x_draw = centerX ? (x - MathUtils.round(mediaManager.fontTextWidth(cMediaFont, text) / 2f)) : x;
        final float y_draw = centerY ? (y - MathUtils.round(mediaManager.fontTextHeight(cMediaFont, text) / 2f)) : y;
        final BitmapFontCache fontCache = mediaManager.font(cMediaFont).getCache();
        final String truncate = maxWidth > 0 ? "" : null;

        fontCache.clear();
        fontCache.addText(text, x_draw, y_draw, Math.min(start,end), Math.min(end, text.length()), maxWidth, Align.left, false, truncate);

        // Multiply by Batch Color
        Color.abgr8888ToColor(this.tempColor, this.getPackedColor());
        final float batch_r = this.tempColor.r * 2f; // 0.5 = default -> x 2 = 1f for multiplication
        final float batch_g = this.tempColor.g * 2f;
        final float batch_b = this.tempColor.b * 2f;
        final float batch_a = this.tempColor.a;

        float[] fontVertices = fontCache.getVertices();
        for (int idx = 2; idx < fontVertices.length; idx += 5) {
            float fontColor = fontVertices[idx];
            Color.abgr8888ToColor(this.tempColor, fontColor);
            tempColor.mul(batch_r, batch_g, batch_b, batch_a);
            fontVertices[idx] = (colorPackedRGBA(tempColor.r, tempColor.g, tempColor.b, tempColor.a));
        }

        this.draw(fontCache.getFont().getRegion().getTexture(), fontVertices, 0, fontCache.getVertexCount(0));
    }

    private float colorPackedRGBA(final float red, final float green, final float blue, final float alpha) {
        return NumberUtils.intBitsToFloat(((int) (alpha * 255) << 24 & 0xFE000000) | ((int) (blue * 255) << 16 & 0xFF0000)
                | ((int) (green * 255) << 8 & 0xFF00) | ((int) (red * 255) & 0xFF));
    }


    public void setBlendFunctionLayer() {
        this.setBlendFunctionSeparate(GL32.GL_SRC_ALPHA, GL32.GL_ONE_MINUS_SRC_ALPHA, GL32.GL_ONE, GL32.GL_ONE_MINUS_SRC_ALPHA);
    }

    public void setBlendFunctionComposite() {
        this.setBlendFunction(GL32.GL_ONE, GL32.GL_ONE_MINUS_SRC_ALPHA);
    }


}
