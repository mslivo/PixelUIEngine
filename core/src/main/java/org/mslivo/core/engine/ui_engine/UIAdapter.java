package org.mslivo.core.engine.ui_engine;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import org.mslivo.core.engine.media_manager.MediaManager;
import org.mslivo.core.engine.ui_engine.render.ImmediateRenderer;
import org.mslivo.core.engine.ui_engine.render.SpriteRenderer;
import org.mslivo.core.engine.ui_engine.ui.components.viewport.GameViewPort;

public interface UIAdapter {

    void init(API api, MediaManager mediaManager);

    void update();

    void render(SpriteRenderer batch, ImmediateRenderer immediateRenderer, GameViewPort gameViewPort);

    void shutdown();

    default void renderBeforeUI(SpriteRenderer spriteRenderer, ImmediateRenderer immediateRenderer) {
    }

    default void renderAfterUI(SpriteRenderer spriteRenderer, ImmediateRenderer immediateRenderer) {
    }

    default void renderFinalScreen(SpriteRenderer spriteRenderer, TextureRegion texture_game, TextureRegion texture_ui,
                                   int internalResolutionWidth, int internalResolutionHeight, boolean gameGrayScale) {
        spriteRenderer.begin();
        // Draw App Framebuffer
        if(gameGrayScale) spriteRenderer.setSaturation(0f);
        spriteRenderer.draw(texture_game, 0, 0, internalResolutionWidth, internalResolutionHeight);
        if(gameGrayScale) spriteRenderer.setSaturation(1f);
        // Draw UI Framebuffer
        spriteRenderer.draw(texture_ui, 0, 0, internalResolutionWidth, internalResolutionHeight);
        spriteRenderer.end();
    }

}
