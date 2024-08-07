package net.mslivo.core.engine.tools.transitions;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import net.mslivo.core.engine.tools.transitions.transitions.FadeTransition;
import net.mslivo.core.engine.ui_engine.constants.VIEWPORT_MODE;
import net.mslivo.core.engine.ui_engine.UIEngine;
import net.mslivo.core.engine.ui_engine.rendering.NestedFrameBuffer;
import net.mslivo.core.engine.ui_engine.rendering.PixelPerfectViewport;

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
    private int resolutionWidth, resolutionHeight;
    private VIEWPORT_MODE VIEWPORTMODE;
    private TRANSITION_RENDER_MODE transitionMode;
    private UIEngine from;
    private UIEngine to;

    public TransitionManager() {
        this.finished = true;
        this.initialized = false;
    }

    public void init(UIEngine from, UIEngine to) {
        this.init(from, to, null, 1, false);
    }

    public void init(UIEngine from, UIEngine to, Transition transition) {
        this.init(from, to, transition, 1, false);
    }

    public void init(UIEngine from, UIEngine to, Transition transition, int transitionSpeed) {
        this.init(from, to, transition, transitionSpeed, false);
    }

    public void init(UIEngine from, UIEngine to, Transition transition, int transitionSpeed, boolean updateUIEngine) {
        if(from == null) throw new RuntimeException("UIEngine from is null");
        if(to == null) throw new RuntimeException("UIEngine to is null");
        if (from.getResolutionWidth() != to.getResolutionWidth())
            throw new RuntimeException("UIEngine internalResolutionWidth does not match");
        if (from.getResolutionHeight() != to.getResolutionHeight())
            throw new RuntimeException("UIEngine internalResolutionHeight does not match");
        if (from.getViewportMode() != to.getViewportMode()) throw new RuntimeException("viewportMode does not match");
        this.from = from;
        this.to = to;
        int resolutionWidth = from.getResolutionWidth();
        int resolutionHeight = from.getResolutionHeight();
        VIEWPORT_MODE VIEWPORTMODE = from.getViewportMode();
        this.transition = transition == null ? new FadeTransition() : transition;
        this.transitionSpeed = Math.clamp(transitionSpeed, 1, 10);
        boolean createNew = this.resolutionWidth != resolutionWidth || this.resolutionHeight != resolutionHeight || this.VIEWPORTMODE != VIEWPORTMODE;
        if (createNew) {
            this.resolutionWidth = resolutionWidth;
            this.resolutionHeight = resolutionHeight;
            this.VIEWPORTMODE = VIEWPORTMODE;

            if (frameBuffer_from != null) frameBuffer_from.dispose();
            frameBuffer_from = new NestedFrameBuffer(Pixmap.Format.RGBA8888, resolutionWidth, resolutionHeight, false);
            frameBuffer_from.getColorBufferTexture().setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
            texture_from = new TextureRegion(frameBuffer_from.getColorBufferTexture());
            texture_from.flip(false, true);

            if (frameBuffer_to != null) frameBuffer_to.dispose();
            frameBuffer_to = new NestedFrameBuffer(Pixmap.Format.RGBA8888, resolutionWidth, resolutionHeight, false);
            frameBuffer_to.getColorBufferTexture().setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
            texture_to = new TextureRegion(frameBuffer_to.getColorBufferTexture());
            texture_to.flip(false, true);

            camera_screen = new OrthographicCamera(resolutionWidth, resolutionHeight);
            camera_screen.setToOrtho(false);
            viewport_screen = createViewport(VIEWPORTMODE, camera_screen, resolutionWidth, resolutionHeight);
        }

        // PixmapIO.writePNG(new FileHandle("E:\\from.png"),Pixmap.createFromFrameBuffer(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight()));


        // Capture Buffers
        batch_screen = batch_screen != null ? batch_screen : new SpriteBatch(8191);
        batch_screen.setColor(Color.WHITE);
        viewport_screen.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);

        { // Capture From Framebuffer
            if (updateUIEngine) from.update();
            from.render(true);
            frameBuffer_from.begin();
            batch_screen.setProjectionMatrix(camera_screen.combined);
            batch_screen.begin();
            batch_screen.draw(from.getFrameBufferScreen().getFlippedTextureRegion(), 0, 0, resolutionWidth, resolutionHeight);
            batch_screen.end();
            frameBuffer_from.end();
        }
        { // Capture To Framebuffer
            if (updateUIEngine) to.update();
            to.render(true);
            frameBuffer_to.begin();
            batch_screen.setProjectionMatrix(camera_screen.combined);
            batch_screen.begin();
            batch_screen.draw(to.getFrameBufferScreen().getFlippedTextureRegion(), 0, 0, resolutionWidth, resolutionHeight);
            batch_screen.end();
            frameBuffer_to.end();
        }
        transitionMode = this.transition.init(resolutionWidth, resolutionHeight);
        if (transitionMode == null) transitionMode = TRANSITION_RENDER_MODE.FROM_FIRST;
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
                    this.transition.renderTo(batch_screen, texture_to);
                }
                case TO_FIRST -> {
                    this.transition.renderTo(batch_screen, texture_to);
                    this.transition.renderFrom(batch_screen, texture_from);
                }
            }
            batch_screen.end();
        }

    }

    public void shutdown() {
        if (batch_screen != null) batch_screen.dispose();
        this.batch_screen = null;
        if (frameBuffer_to != null) {
            frameBuffer_to.dispose();
            frameBuffer_to = null;
            texture_to = null;
        }
        if (frameBuffer_from != null) {
            frameBuffer_from.dispose();
            frameBuffer_from = null;
            texture_from = null;
        }
        this.camera_screen = null;
        this.viewport_screen = null;
        this.resolutionWidth = -1;
        this.resolutionHeight = -1;
        this.VIEWPORTMODE = null;
        this.initialized = false;
        this.finished = true;
        this.from = null;
        this.to = null;
    }

    public boolean isFinished() {
        return finished;
    }

    public boolean isInitialized() {
        return initialized;
    }

    private Viewport createViewport(VIEWPORT_MODE VIEWPORTMODE, OrthographicCamera camera_screen, int internalResolutionWidth, int internalResolutionHeight) {
        return switch (VIEWPORTMODE) {
            case FIT -> new FitViewport(internalResolutionWidth, internalResolutionHeight, camera_screen);
            case PIXEL_PERFECT ->
                    new PixelPerfectViewport(internalResolutionWidth, internalResolutionHeight, camera_screen, 1);
            case STRETCH -> new StretchViewport(internalResolutionWidth, internalResolutionHeight, camera_screen);
        };
    }

    public UIEngine getFrom() {
        return from;
    }

    public UIEngine getTo() {
        return to;
    }

}
