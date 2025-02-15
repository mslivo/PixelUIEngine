package net.mslivo.core.engine.ui_engine;

import com.badlogic.gdx.graphics.GL32;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import net.mslivo.core.engine.ui_engine.rendering.SpriteRenderer;
import net.mslivo.core.engine.media_manager.MediaManager;
import net.mslivo.core.engine.ui_engine.ui.components.viewport.AppViewport;

public interface UIEngineAdapter {
    void init(API api, MediaManager mediaManager);
    void update();
    void render(OrthographicCamera camera, AppViewport appViewPort);

    default void renderComposite(OrthographicCamera camera, SpriteRenderer spriteRenderer, TextureRegion texture_game, TextureRegion texture_uiComponent,TextureRegion texture_uiModal,
                                 int resolutionWidth, int resolutionHeight, boolean grayScale) {
        spriteRenderer.setProjectionMatrix(camera.combined);
        spriteRenderer.setBlendFunction(GL32.GL_ONE,GL32.GL_ONE_MINUS_SRC_ALPHA);
        spriteRenderer.begin();

        if(grayScale){
            spriteRenderer.setTweak(0.5f,0f,0.45f,0.0f);
            spriteRenderer.draw(texture_game, 0, 0, resolutionWidth, resolutionHeight);
            spriteRenderer.draw(texture_uiComponent, 0, 0, resolutionWidth, resolutionHeight);
            spriteRenderer.setTweakAndColorReset();
        }else{

            spriteRenderer.setColor(0.5f,0.5f,0.5f,1f);
            spriteRenderer.setTweak(0.5f,0.5f,0.5f,0f);

            spriteRenderer.draw(texture_game, 0, 0, resolutionWidth, resolutionHeight);
            spriteRenderer.draw(texture_uiComponent, 0, 0, resolutionWidth, resolutionHeight);
        }
        spriteRenderer.draw(texture_uiModal, 0, 0, resolutionWidth, resolutionHeight);

        spriteRenderer.end();
        spriteRenderer.setAllReset();
    }

    void shutdown();

}
