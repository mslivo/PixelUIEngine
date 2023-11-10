package org.mslivo.core.engine.tools.transitions;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import org.mslivo.core.engine.tools.Tools;
import org.mslivo.core.engine.ui_engine.UIEngine;
import org.mslivo.core.engine.ui_engine.misc.NestedFrameBuffer;

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
    private UIEngine from, to;
    private TRANSITION_MODE transitionMode;
    private static final Texture pixel = new Texture("sprites/gui/pixel.png");

    public TransitionManager() {
        this.finished = true;
        this.initialized = false;
    }

    public void init(UIEngine from, UIEngine to, Transition transition) {
        this.init(from, to, transition, 1);
    }

    public void init(UIEngine from, UIEngine to, Transition transition, int transitionSpeed) {
        int screenWidth = Tools.Calc.lowerBounds(Gdx.graphics.getWidth(), 1);
        int screenHeight = Tools.Calc.lowerBounds(Gdx.graphics.getHeight(), 1);
        this.from = from;
        this.to = to;
        this.transition = transition == null ? new FadeTransition() : transition;
        this.transitionSpeed = Tools.Calc.inBounds(transitionSpeed, 1, 10);

        boolean createNew = this.screenWidth != screenWidth || this.screenHeight != screenHeight;
        if (createNew) {
            this.screenWidth = screenWidth;
            this.screenHeight = screenHeight;
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
            viewport_screen = new ScreenViewport(camera_screen);
            viewport_screen.update(screenWidth, screenHeight, true);
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
        transitionMode = this.transition.init(screenWidth, screenHeight);
        if (transitionMode == null) transitionMode = TRANSITION_MODE.FROM_FIRST;
        this.initialized = true;
        this.finished = false;
    }

    public boolean update() {
        if (!initialized) return true;
        if (this.finished) return true;
        for (int i = 0; i < this.transitionSpeed; i++) {
            if (this.transition.update()) {
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

            batch_screen.begin();
            switch (transitionMode) {
                case FROM_FIRST -> {
                    this.transition.renderFrom(batch_screen, texture_from);
                    drawViewportBlackBars(batch_screen, from.getViewPortScreenX(), from.getViewPortScreenY(), from.getViewPortScreenWidth(), from.getViewPortScreenHeight());
                    this.transition.renderTo(batch_screen, texture_to);
                    drawViewportBlackBars(batch_screen, to.getViewPortScreenX(), to.getViewPortScreenY(), to.getViewPortScreenWidth(), to.getViewPortScreenHeight());
                }
                case TO_FIRST -> {
                    this.transition.renderTo(batch_screen, texture_to);
                    drawViewportBlackBars(batch_screen, to.getViewPortScreenX(), to.getViewPortScreenY(), to.getViewPortScreenWidth(), to.getViewPortScreenHeight());
                    this.transition.renderFrom(batch_screen, texture_from);
                    drawViewportBlackBars(batch_screen, from.getViewPortScreenX(), from.getViewPortScreenY(), from.getViewPortScreenWidth(), from.getViewPortScreenHeight());
                }
            }
            batch_screen.end();
        }

    }

    private void drawViewportBlackBars(SpriteBatch batch, int viewPortScreenX, int viewPortScreenY, int viewPortScreenWidth, int viewPortScreenHeight) {
        batch_screen.setColor(Color.BLACK);
        int ysum = viewPortScreenY + viewPortScreenHeight;
        int xsum = viewPortScreenX + viewPortScreenWidth;
        batch.draw(pixel, 0, 0, viewPortScreenX, screenHeight);
        batch.draw(pixel, 0, 0, screenWidth, viewPortScreenY);
        batch.draw(pixel, 0, ysum, screenWidth, (screenHeight - ysum));
        batch.draw(pixel, xsum, 0, (screenWidth - xsum), screenHeight);
        batch_screen.setColor(Color.WHITE);
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
        this.from = null;
        this.to = null;
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
