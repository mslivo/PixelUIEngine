package net.mslivo.pixelui.utils.transitions;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import net.mslivo.pixelui.engine.UIEngine;
import net.mslivo.pixelui.engine.constants.VIEWPORT_MODE;
import net.mslivo.pixelui.rendering.NestedFrameBuffer;
import net.mslivo.pixelui.rendering.PixelPerfectViewport;
import net.mslivo.pixelui.rendering.SpriteRenderer;

public class TransitionManager implements Disposable {
    private static final String ERROR_FROM_TO_NULL = "\"from\" and \"to\" are both null";

    private NestedFrameBuffer frameBuffer_from;
    private TextureRegion texture_from;
    private NestedFrameBuffer frameBuffer_to;
    private TextureRegion texture_to;
    private SpriteRenderer spriteRenderer_screen;
    private Viewport viewport_screen;
    private OrthographicCamera camera_screen;
    private Transition transition;
    private float nextUpdate;
    private boolean finished;
    private int resolutionWidth, resolutionHeight;
    private VIEWPORT_MODE viewportMode;
    private TRANSITION_RENDER_MODE transitionRenderMode;
    private UIEngine from;
    private UIEngine to;

    public TransitionManager(UIEngine from, UIEngine to, Transition transition) {
        if (from == null && to == null)
            throw new RuntimeException(ERROR_FROM_TO_NULL);

        if (transition == null || transition.transitionSpeed == TRANSITION_SPEED.IMMEDIATE) {
            this.finished = true;
            return;
        }

        this.from = from;
        this.to = to;
        this.nextUpdate = 0f;
        this.resolutionWidth = transitionResolutionWidth(from, to);
        this.resolutionHeight = transitionResolutionHeight(from, to);
        this.viewportMode = transitionViewPortMode(from, to);
        this.transition = transition;

        this.frameBuffer_from = new NestedFrameBuffer(Pixmap.Format.RGBA8888, this.resolutionWidth, this.resolutionHeight, false);
        this.frameBuffer_from.getColorBufferTexture().setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        this.texture_from = new TextureRegion(this.frameBuffer_from.getColorBufferTexture());
        this.texture_from.flip(false, true);

        this.frameBuffer_to = new NestedFrameBuffer(Pixmap.Format.RGBA8888, this.resolutionWidth, this.resolutionHeight, false);
        this.frameBuffer_to.getColorBufferTexture().setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        this.texture_to = new TextureRegion(this.frameBuffer_to.getColorBufferTexture());
        this.texture_to.flip(false, true);

        this.camera_screen = new OrthographicCamera();
        this.camera_screen.setToOrtho(false,this.resolutionWidth, this.resolutionHeight);
        this.viewport_screen = createViewport(this.viewportMode, this.camera_screen, this.resolutionWidth, this.resolutionHeight);

        this.spriteRenderer_screen = new SpriteRenderer();
        this.spriteRenderer_screen.setColor(Color.GRAY);
        this.viewport_screen.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);


        this.captureUIEngineFrameBuffer(this.from, this.frameBuffer_from);
        this.captureUIEngineFrameBuffer(this.to, this.frameBuffer_to);

        this.transitionRenderMode = this.transition.getRenderMode();
        if (this.transitionRenderMode == null)
            this.transitionRenderMode = TRANSITION_RENDER_MODE.FROM_FIRST;
        this.transition.init(this.spriteRenderer_screen, this.resolutionWidth, this.resolutionHeight);
        this.finished = false;


    }

    private int transitionResolutionWidth(UIEngine from, UIEngine to) {
        if (from != null && to == null) {
            return from.getResolutionWidth();
        } else if (from == null && to != null) {
            return to.getResolutionWidth();
        } else if (from != null && to != null) {
            return Math.max(from.getResolutionWidth(), to.getResolutionWidth());
        }
        return 0;
    }

    private int transitionResolutionHeight(UIEngine from, UIEngine to) {
        if (from != null && to == null) {
            return from.getResolutionHeight();
        } else if (from == null && to != null) {
            return to.getResolutionHeight();
        } else if (from != null && to != null) {
            return Math.max(from.getResolutionHeight(), to.getResolutionHeight());
        }
        return 0;
    }

    private VIEWPORT_MODE transitionViewPortMode(UIEngine from, UIEngine to) {
        if (from != null && to == null) {
            return from.getViewportMode();
        } else if (from == null && to != null) {
            return to.getViewportMode();
        }
        return to.getViewportMode();
    }

    private void captureUIEngineFrameBuffer(UIEngine uiEngine, NestedFrameBuffer frameBuffer) {
        if (uiEngine != null) {
            uiEngine.render(true);
        }
        frameBuffer.begin();
        this.spriteRenderer_screen.setProjectionMatrix(this.camera_screen.combined);
        this.spriteRenderer_screen.begin();
        if (uiEngine != null) {
            this.spriteRenderer_screen.draw(uiEngine.getFrameBufferComposite().getFlippedTextureRegion(), 0, 0, this.resolutionWidth, this.resolutionHeight);
        } else {
            Gdx.gl.glClearColor(0f, 0f, 0f, 1);
            Gdx.gl.glClear(GL32.GL_COLOR_BUFFER_BIT);
        }
        this.spriteRenderer_screen.end();
        frameBuffer.end();
    }

    public boolean update() {
        if (this.finished) return true;

        this.nextUpdate += this.transition.transitionSpeed.value;
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
        Gdx.gl.glClear(GL32.GL_COLOR_BUFFER_BIT);
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

    private void finish() {
        if (this.finished) return;

        this.transition.finished(spriteRenderer_screen);
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

    @Override
    public void dispose() {
        if (this.frameBuffer_from != null)
            this.frameBuffer_from.dispose();
        if (this.frameBuffer_to != null)
            this.frameBuffer_to.dispose();
        if (this.texture_from != null)
            this.texture_from.getTexture().dispose();
        if (this.texture_to != null)
            this.texture_to.getTexture().dispose();
        if (this.spriteRenderer_screen != null)
            this.spriteRenderer_screen.dispose();
    }
}
