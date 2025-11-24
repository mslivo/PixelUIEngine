package net.mslivo.pixelui.engine;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Disposable;
import net.mslivo.pixelui.rendering.SpriteRenderer;
import net.mslivo.pixelui.media.MediaManager;

public interface UIEngineAdapter extends Disposable {
    void init(API api, MediaManager mediaManager);
    void update();
    void render(OrthographicCamera camera, AppViewport appViewPort);

    default void renderComposite(OrthographicCamera camera, SpriteRenderer spriteRenderer, TextureRegion texture_game, TextureRegion texture_uiComponent, TextureRegion texture_uiModal,
                                 int resolutionWidth, int resolutionHeight, boolean modalActive) {
        spriteRenderer.setProjectionMatrix(camera.combined);
        spriteRenderer.setBlendFunctionComposite();
        spriteRenderer.begin();

        if(modalActive){
            spriteRenderer.setTweak(0.5f,0f,0.45f,0.0f);
            spriteRenderer.draw(texture_game, 0, 0, resolutionWidth, resolutionHeight);
            spriteRenderer.draw(texture_uiComponent, 0, 0, resolutionWidth, resolutionHeight);
            spriteRenderer.reset();
        }else{
            spriteRenderer.draw(texture_game, 0, 0, resolutionWidth, resolutionHeight);
            spriteRenderer.draw(texture_uiComponent, 0, 0, resolutionWidth, resolutionHeight);
        }
        spriteRenderer.draw(texture_uiModal, 0, 0, resolutionWidth, resolutionHeight);

        spriteRenderer.end();
        spriteRenderer.reset();
    }


}
