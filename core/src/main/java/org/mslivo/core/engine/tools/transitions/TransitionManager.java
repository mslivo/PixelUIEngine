package org.mslivo.core.engine.tools.transitions;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import org.mslivo.core.engine.tools.Tools;
import org.mslivo.core.engine.ui_engine.UIEngine;
import org.mslivo.core.engine.ui_engine.misc.NestedFrameBuffer;
import org.mslivo.core.engine.ui_engine.misc.PixelPerfectViewport;

public class TransitionManager {
    private NestedFrameBuffer frameBuffer_from;
    private TextureRegion texture_from;
    private NestedFrameBuffer frameBuffer_to;
    private TextureRegion texture_to;
    private SpriteBatch batch_screen;
    private Viewport viewport_screen;
    private OrthographicCamera camera_screen;
    private Transition transition;
    private int transitionSpeed;
    private boolean initialized;
    private boolean finished;
    private int screenWidth, screenHeight;

    public TransitionManager() {
        this.finished = true;
        this.initialized = false;
    }

    public void init(UIEngine from, UIEngine to, Transition transition) {
        this.init(from, to, transition, 1);
    }

    public void init(UIEngine from, UIEngine to, Transition transition, int transitionSpeed) {
        int screenWidth = Tools.Calc.lowerBounds(Gdx.graphics.getWidth(),1);
        int screenHeight = Tools.Calc.lowerBounds(Gdx.graphics.getHeight(),1);
        this.transition = transition == null ? new FadeTransition() : transition;
        this.transitionSpeed = Tools.Calc.inBounds(transitionSpeed, 1, 10);

        boolean createNew = this.screenWidth != screenWidth || this.screenHeight != screenHeight;
        if (createNew) {
            if (frameBuffer_from != null) frameBuffer_from.dispose();
            frameBuffer_from = new NestedFrameBuffer(Pixmap.Format.RGBA8888, screenWidth, screenHeight, false);
            frameBuffer_from.getColorBufferTexture().setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
            texture_from = new TextureRegion(frameBuffer_from.getColorBufferTexture());
            texture_from.flip(false, true);

            if (frameBuffer_to != null) frameBuffer_to.dispose();
            frameBuffer_to = new NestedFrameBuffer(Pixmap.Format.RGBA8888, screenWidth, screenHeight, false);
            frameBuffer_to.getColorBufferTexture().setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
            texture_to = new TextureRegion(frameBuffer_to.getColorBufferTexture());
            texture_to.flip(false, true);

            batch_screen = batch_screen != null ? batch_screen : new SpriteBatch(8191);
            camera_screen = new OrthographicCamera(screenWidth, screenHeight);
            camera_screen.setToOrtho(false);
            camera_screen.update();
            camera_screen.position.set(screenWidth / 2, screenHeight / 2, 1f);

            viewport_screen = new ScreenViewport(camera_screen);
            viewport_screen.update(screenWidth, screenHeight, true);
            this.screenWidth = screenWidth;
            this.screenHeight = screenHeight;
        }

        // Capture Buffers
        if (from != null) {
            frameBuffer_from.begin();
            from.render();
            frameBuffer_from.end();
        }
        if (to != null) {
            frameBuffer_to.begin();
            to.render();
            frameBuffer_to.end();
        }
        this.transition.init(screenWidth, screenHeight);
        this.initialized = true;
        this.finished = false;
    }

    public boolean update() {
        if (!initialized) return true;
        if (this.finished) return true;
        for (int i = 0; i < this.transitionSpeed; i++) {
            if(this.transition.update()){
                this.finished = true;
                return true;
            }
        }
        return false;
    }

    public void render() {
        if (!initialized) return;
        Gdx.gl.glClearColor(0, 0, 0, 0);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        // Render Transition
        {
            viewport_screen.apply();
            batch_screen.setProjectionMatrix(camera_screen.combined);
            this.transition.render(batch_screen, texture_from, texture_to);
        }
    }

    public void shutdown() {
        if (batch_screen != null) batch_screen.dispose();
        this.batch_screen = null;
        if (frameBuffer_to != null) frameBuffer_from.getColorBufferTexture().dispose();
        this.frameBuffer_to = null;
        this.texture_to = null;
        if (frameBuffer_from != null) frameBuffer_from.getColorBufferTexture().dispose();
        this.frameBuffer_from = null;
        this.texture_from = null;
        this.camera_screen = null;
        this.viewport_screen = null;
        this.initialized = false;
        this.finished = true;
    }

    public boolean isFinished() {
        return finished;
    }

    public boolean isInitialized() {
        return initialized;
    }
}
