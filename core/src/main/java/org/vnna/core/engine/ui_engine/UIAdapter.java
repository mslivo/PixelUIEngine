package org.vnna.core.engine.ui_engine;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import org.vnna.core.engine.media_manager.MediaManager;

public interface UIAdapter {

    void init(API api, MediaManager mediaManager);

    void update();

    void render(SpriteBatch spriteBatch, boolean mainCamera);

    void shutdown();

    default void renderBeforeGUI(SpriteBatch spriteBatch_gui){return;};

    default void renderAfterGUI(SpriteBatch spriteBatch_gui){return;};

}
