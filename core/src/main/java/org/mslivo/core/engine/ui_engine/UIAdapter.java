package org.mslivo.core.engine.ui_engine;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import org.mslivo.core.engine.media_manager.MediaManager;
import org.mslivo.core.engine.ui_engine.render.UIImmediateBatch;
import org.mslivo.core.engine.ui_engine.render.UISpriteBatch;
import org.mslivo.core.engine.ui_engine.ui.components.viewport.GameViewPort;

public interface UIAdapter {

    void init(API api, MediaManager mediaManager);

    void update();

    void render(UISpriteBatch batch, UIImmediateBatch uIImmediateBatch, GameViewPort gameViewPort);

    void shutdown();

    default void renderBeforeUI(UISpriteBatch batch, UIImmediateBatch uIImmediateBatch) {
    }

    default void renderAfterUI(UISpriteBatch batch, UIImmediateBatch uIImmediateBatch) {
    }

    default void renderFinalScreen(UISpriteBatch spriteBatch_screen, TextureRegion texture_game, TextureRegion texture_ui,
                                   int internalResolutionWidth, int internalResolutionHeight) {
        spriteBatch_screen.begin();
        spriteBatch_screen.draw(texture_game, 0, 0, internalResolutionWidth, internalResolutionHeight);
        spriteBatch_screen.draw(texture_ui, 0, 0, internalResolutionWidth, internalResolutionHeight);
        spriteBatch_screen.end();
    }

}
