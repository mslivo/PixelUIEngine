package org.mslivo.core.engine.ui_engine;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import org.mslivo.core.engine.media_manager.MediaManager;
import org.mslivo.core.engine.ui_engine.render.ImmediateBatch;
import org.mslivo.core.engine.ui_engine.ui.components.viewport.GameViewPort;

public interface UIAdapter {

    void init(API api, MediaManager mediaManager);

    void update();

    void render(SpriteBatch batch, ImmediateBatch immediateBatch, GameViewPort gameViewPort);

    void shutdown();

    default void renderBeforeUI(SpriteBatch batch, ImmediateBatch immediateBatch) {
    }

    default void renderAfterUI(SpriteBatch batch, ImmediateBatch immediateBatch) {
    }

    default void renderFinalScreen(SpriteBatch spriteBatch_screen, TextureRegion texture_game, TextureRegion texture_ui,
                                   int internalResolutionWidth, int internalResolutionHeight) {
        spriteBatch_screen.begin();
        spriteBatch_screen.draw(texture_game, 0, 0, internalResolutionWidth, internalResolutionHeight);
        spriteBatch_screen.draw(texture_ui, 0, 0, internalResolutionWidth, internalResolutionHeight);
        spriteBatch_screen.end();
    }

}
