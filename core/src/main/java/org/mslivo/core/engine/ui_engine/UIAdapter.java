package org.mslivo.core.engine.ui_engine;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import org.mslivo.core.engine.media_manager.MediaManager;

public interface UIAdapter {

    void init(API api, MediaManager mediaManager);

    void update();

    void render(SpriteBatch spriteBatch, boolean mainCamera);

    void shutdown();

    default void renderBeforeGUI(SpriteBatch spriteBatch_gui){return;};

    default void renderAfterGUI(SpriteBatch spriteBatch_gui){return;};

}
