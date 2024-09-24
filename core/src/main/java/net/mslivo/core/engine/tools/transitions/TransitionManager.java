package net.mslivo.core.engine.tools.transitions;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import net.mslivo.core.engine.ui_engine.UIEngine;
import net.mslivo.core.engine.ui_engine.constants.VIEWPORT_MODE;
import net.mslivo.core.engine.ui_engine.rendering.NestedFrameBuffer;
import net.mslivo.core.engine.ui_engine.rendering.PixelPerfectViewport;
import net.mslivo.core.engine.ui_engine.rendering.SpriteRenderer;

public class TransitionManager {
    private static final String ERROR_RESOLUTION_WIDTH = "\"from\" and \"to\" resolution width does not match";
    private static final String ERROR_RESOLUTION_HEIGHT = "\"from\" and \"to\" resolution height does not match";

    private NestedFrameBuffer frameBuffer_from;
    private TextureRegion texture_from;
    private NestedFrameBuffer frameBuffer_to;
    private TextureRegion texture_to;
    private SpriteRenderer spriteRenderer_screen;
    private Viewport viewport_screen;
    private OrthographicCamera camera_screen;
    private Transition transition;
    private TRANSITION_SPEED transitionSpeed;
    private float nextUpdate;
    private boolean finished;
    private int resolutionWidth, resolutionHeight;
    private VIEWPORT_MODE viewportMode;
    private TRANSITION_RENDER_MODE transitionRenderMode;
    private UIEngine from;
    private UIEngine to;

    public TransitionManager(UIEngine from, UIEngine to, Transition transition) {
        this(from, to, transition, TRANSITION_SPEED.X1, false);
    }

    public TransitionManager(UIEngine from, UIEngine to, Transition transition, TRANSITION_SPEED transitionSpeed) {
        this(from, to, transition, transitionSpeed, false);
    }

    public TransitionManager(UIEngine from, UIEngine to, Transition transition, TRANSITION_SPEED transitionSpeed, boolean updateUIEngine) {
        if (transitionSpeed == null)
            transitionSpeed = TRANSITION_SPEED.X1;
        if (to == null || from == null || transition == null)
            transitionSpeed = TRANSITION_SPEED.IMMEDIATE;

        if (transitionSpeed == TRANSITION_SPEED.IMMEDIATE) {
            this.finished = true;
        } else {
            if (from.getResolutionWidth() != to.getResolutionWidth())
                throw new RuntimeException(ERROR_RESOLUTION_WIDTH);
            if (from.getResolutionHeight() != to.getResolutionHeight())
                throw new RuntimeException(ERROR_RESOLUTION_HEIGHT);
            this.from = from;
            this.to = to;
            this.nextUpdate = 0f;
            this.resolutionWidth = from.getResolutionWidth();
            this.resolutionHeight = from.getResolutionHeight();
            this.viewportMode = from.getViewportMode();
            this.transition = transition;
            this.transitionSpeed = transitionSpeed;

            this.frameBuffer_from = new NestedFrameBuffer(Pixmap.Format.RGBA8888, this.resolutionWidth, this.resolutionHeight, false);
            this.frameBuffer_from.getColorBufferTexture().setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
            this.texture_from = new TextureRegion(this.frameBuffer_from.getColorBufferTexture());
            this.texture_from.flip(false, true);

            this.frameBuffer_to = new NestedFrameBuffer(Pixmap.Format.RGBA8888, this.resolutionWidth, this.resolutionHeight, false);
            this.frameBuffer_to.getColorBufferTexture().setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
            this.texture_to = new TextureRegion(this.frameBuffer_to.getColorBufferTexture());
            this.texture_to.flip(false, true);

            this.camera_screen = new OrthographicCamera(this.resolutionWidth, this.resolutionHeight);
            this.camera_screen.setToOrtho(false);
            this.viewport_screen = createViewport(this.viewportMode, this.camera_screen, this.resolutionWidth, this.resolutionHeight);

            this.spriteRenderer_screen = new SpriteRenderer();
            this.spriteRenderer_screen.setColor(Color.GRAY);
            this.viewport_screen.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);

            { // Capture From Framebuffer
                if (updateUIEngine) from.update();
                from.render(true);
                this.frameBuffer_from.begin();
                this.spriteRenderer_screen.setProjectionMatrix(this.camera_screen.combined);
                this.spriteRenderer_screen.begin();
                this.spriteRenderer_screen.draw(from.getFrameBufferScreen().getFlippedTextureRegion(), 0, 0, this.resolutionWidth, this.resolutionHeight);
                this.spriteRenderer_screen.end();
                this.frameBuffer_from.end();
            }
            { // Capture To Framebuffer
                if (updateUIEngine) to.update();
                to.render(true);
                this.frameBuffer_to.begin();
                this.spriteRenderer_screen.setProjectionMatrix(this.camera_screen.combined);
                this.spriteRenderer_screen.begin();
                this.spriteRenderer_screen.draw(to.getFrameBufferScreen().getFlippedTextureRegion(), 0, 0, this.resolutionWidth, this.resolutionHeight);
                this.spriteRenderer_screen.end();
                this.frameBuffer_to.end();
            }

            this.transitionRenderMode = this.transition.getRenderMode();
            if (this.transitionRenderMode == null)
                this.transitionRenderMode = TRANSITION_RENDER_MODE.FROM_FIRST;
            this.transition.init(this.resolutionWidth, this.resolutionHeight);
            this.finished = false;
        }


    }

    public boolean update() {
        if (this.finished) return true;

        this.nextUpdate += transitionSpeed.value;
        while (nextUpdate >= 1f) {
            if (this.transition.update()) {
                finish();
                return true;
            }
            nextUpdate -= 1f;
        }
        return false;
    }


    public void render() {
        if (this.finished) return;
        Gdx.gl.glClearColor(0, 0, 0, 0);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        // Render Transition
        {
            viewport_screen.apply();
            spriteRenderer_screen.setProjectionMatrix(camera_screen.combined);
            spriteRenderer_screen.begin();
            switch (transitionRenderMode) {
                case FROM_FIRST -> {
                    this.transition.renderFrom(spriteRenderer_screen, texture_from);
                    this.transition.renderTo(spriteRenderer_screen, texture_to);
                }
                case TO_FIRST -> {
                    this.transition.renderTo(spriteRenderer_screen, texture_to);
                    this.transition.renderFrom(spriteRenderer_screen, texture_from);
                }
            }
            spriteRenderer_screen.end();
        }
    }

    public boolean isFinished() {
        return finished;
    }

    public UIEngine getFrom() {
        return from;
    }

    public UIEngine getTo() {
        return to;
    }

    public Transition getTransition() {
        return transition;
    }

    public TRANSITION_SPEED getTransitionSpeed() {
        return transitionSpeed;
    }

    private void finish() {
        if (this.finished) return;
        this.frameBuffer_from.dispose();
        this.frameBuffer_to.dispose();
        this.transition.shutdown();
        this.transition = null;
        this.camera_screen = null;
        this.finished = true;
    }

    private Viewport createViewport(VIEWPORT_MODE VIEWPORTMODE, OrthographicCamera camera_screen, int internalResolutionWidth, int internalResolutionHeight) {
        return switch (VIEWPORTMODE) {
            case FIT -> new FitViewport(internalResolutionWidth, internalResolutionHeight, camera_screen);
            case PIXEL_PERFECT ->
                    new PixelPerfectViewport(internalResolutionWidth, internalResolutionHeight, camera_screen, 1);
            case STRETCH -> new StretchViewport(internalResolutionWidth, internalResolutionHeight, camera_screen);
        };
    }

}
