package org.mslivo.core.engine.ui_engine;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import org.mslivo.core.engine.media_manager.MediaManager;
import org.mslivo.core.engine.ui_engine.render.ImmediateRenderer;
import org.mslivo.core.engine.ui_engine.render.SpriteRenderer;
import org.mslivo.core.engine.ui_engine.ui.components.viewport.AppViewPort;

public interface UIAdapter {

    void init(API api, MediaManager mediaManager);

    void update();

    void render(SpriteRenderer batch, ImmediateRenderer immediateRenderer, AppViewPort appViewPort);

    void shutdown();

    default void renderBeforeUI(SpriteRenderer spriteRenderer, ImmediateRenderer immediateRenderer) {
    }

    default void renderAfterUI(SpriteRenderer spriteRenderer, ImmediateRenderer immediateRenderer) {
    }

    default void renderFinalScreen(SpriteRenderer spriteRenderer, TextureRegion texture_game, TextureRegion texture_ui,
                                   int internalResolutionWidth, int internalResolutionHeight, boolean appGrayScale) {

        spriteRenderer.begin();
        // Draw App Framebuffer
        if(appGrayScale) spriteRenderer.setSaturation(0f);
        spriteRenderer.draw(texture_game, 0, 0, internalResolutionWidth, internalResolutionHeight);
        if(appGrayScale) spriteRenderer.setSaturation(1f);
        // Draw UI Framebuffer
        spriteRenderer.draw(texture_ui, 0, 0, internalResolutionWidth, internalResolutionHeight);
        spriteRenderer.end();
    }

}
