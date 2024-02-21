package org.mslivo.core.engine.ui_engine;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import org.mslivo.core.engine.media_manager.MediaManager;
import org.mslivo.core.engine.ui_engine.ui.components.viewport.GameViewPort;
import org.mslivo.core.engine.ui_engine.render.ImmediateRenderer;

public interface UIAdapter {

    void init(API api, MediaManager mediaManager);

    void update();

    void render(SpriteBatch batch, ImmediateRenderer imRenderer, GameViewPort gameViewPort);

    void shutdown();

    default void renderBeforeUI(SpriteBatch batch, ImmediateRenderer imRenderer) {
    }

    default void renderAfterUI(SpriteBatch batch, ImmediateRenderer imRenderer) {
    }

    default void renderFinalScreen(SpriteBatch spriteBatch_screen, TextureRegion texture_game, TextureRegion texture_gui,
                                   int internalResolutionWidth, int internalResolutionHeight) {
        spriteBatch_screen.begin();
        spriteBatch_screen.draw(texture_game, 0, 0, internalResolutionWidth, internalResolutionHeight);
        spriteBatch_screen.draw(texture_gui, 0, 0, internalResolutionWidth, internalResolutionHeight);
        spriteBatch_screen.end();
    }

}
