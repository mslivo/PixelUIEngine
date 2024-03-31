package net.mslivo.core.engine.ui_engine;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import net.mslivo.core.engine.ui_engine.render.SpriteRenderer;
import net.mslivo.core.engine.media_manager.MediaManager;
import net.mslivo.core.engine.ui_engine.ui.components.viewport.AppViewPort;

public interface UIEngineAdapter {
    void init(API api, MediaManager mediaManager);
    void update();
    void render(OrthographicCamera camera, AppViewPort appViewPort);
    default void renderUIBefore(OrthographicCamera camera) {
    }
    default void renderUIAfter(OrthographicCamera camera) {
    }
    default void renderComposite(OrthographicCamera camera, SpriteRenderer spriteRenderer, TextureRegion texture_game, TextureRegion texture_ui,
                                 int resolutionWidth, int resolutionHeight, boolean appGrayScale) {
        spriteRenderer.setProjectionMatrix(camera.combined);
        spriteRenderer.begin();
        // Draw App Framebuffer
        if(appGrayScale) spriteRenderer.setSaturation(0f);
        spriteRenderer.draw(texture_game, 0, 0, resolutionWidth, resolutionHeight);
        if(appGrayScale) spriteRenderer.setSaturation(0.5f);
        // Draw UI Framebuffer
        spriteRenderer.draw(texture_ui, 0, 0, resolutionWidth, resolutionHeight);
        spriteRenderer.end();
    }
    void shutdown();
}
