package org.mslivo.core.engine.ui_engine;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import org.mslivo.core.engine.media_manager.MediaManager;

public interface UIAdapter {

    void init(API api, MediaManager mediaManager);

    void update();

    void render(SpriteBatch spriteBatch, boolean mainCamera);

    void shutdown();

    default void renderUIBefore(SpriteBatch spriteBatch_gui) {
    }

    default void renderUIAfter(SpriteBatch spriteBatch_gui) {
    }

    default void renderFinalScreen(SpriteBatch spriteBatch_screen, TextureRegion texture_game, TextureRegion texture_gui,
                                   int internalResolutionWidth, int internalResolutionHeight) {
        spriteBatch_screen.begin();
        spriteBatch_screen.draw(texture_game, 0, 0, internalResolutionWidth, internalResolutionHeight);
        spriteBatch_screen.draw(texture_gui, 0, 0, internalResolutionWidth, internalResolutionHeight);
        spriteBatch_screen.end();
    }

}
