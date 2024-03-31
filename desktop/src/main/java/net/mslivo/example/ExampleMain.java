package net.mslivo.example;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import net.mslivo.core.engine.media_manager.MediaManager;
import net.mslivo.core.engine.tools.Tools;
import net.mslivo.core.engine.tools.engine.AppEngine;
import net.mslivo.core.engine.tools.rendering.transitions.TransitionManager;
import net.mslivo.core.engine.tools.rendering.transitions.transitions.FadeTransition;
import net.mslivo.core.engine.ui_engine.UIEngine;
import net.mslivo.core.engine.ui_engine.UIEngineAdapter;
import net.mslivo.example.ui.ExampleUIEngineAdapter;
import net.mslivo.example.ui.media.ExampleBaseMedia;

public class ExampleMain extends ApplicationAdapter {
    enum STATE {
        RUN, TRANSITION
    }

    private STATE state;
    private TransitionManager transitionManager;
    private MediaManager mediaManager;
    private UIEngine<ExampleUIEngineAdapter> uiEngine;
    private UIEngine<ExampleUIEngineAdapter> uiEngine_transition;
    private long timer_debug_info;

    public ExampleMain() {
    }

    @Override
    public void resize(int width, int height) {
        if (this.uiEngine != null) this.uiEngine.resize(width, height);
    }

    @Override
    public void create() {
        Tools.App.setTargetUpdates(ExampleMainConstants.UPDATE_RATE);
        this.transitionManager = new TransitionManager();
        // Load Assets
        Tools.Log.inProgress("Loading Assets");
        this.mediaManager = new MediaManager();
        this.mediaManager.prepareUICMedia();
        this.mediaManager.prepareCMedia(ExampleBaseMedia.ALL);
        this.mediaManager.loadAssets();
        Tools.Log.done();

        // Input/Render
        Tools.Log.inProgress("Starting UI");
        this.uiEngine = new UIEngine<>(
                new ExampleUIEngineAdapter(),
                this.mediaManager, ExampleMainConstants.INTERNAL_RESOLUTION_WIDTH, ExampleMainConstants.INTERNAL_RESOLUTION_HEIGHT,
                ExampleMainConstants.viewportMode);
        Tools.Log.done();

        this.state = STATE.RUN;
    }

    @Override
    public void render() {
        switch (state) {
            case RUN -> {
                if (Tools.App.isRunUpdate()) {
                    this.uiEngine.update();
                }

                this.uiEngine.render();



                // Check for transition + Reset
                if (this.uiEngine.getAdapter().isResetPressed()) {
                    this.uiEngine_transition = new UIEngine<>(
                            new ExampleUIEngineAdapter(),
                            this.mediaManager, ExampleMainConstants.INTERNAL_RESOLUTION_WIDTH, ExampleMainConstants.INTERNAL_RESOLUTION_HEIGHT,
                            ExampleMainConstants.viewportMode);
                    this.uiEngine_transition.update();
                    this.transitionManager.init(this.uiEngine, this.uiEngine_transition, new FadeTransition());
                    this.transitionManager.render();
                    state = STATE.TRANSITION;
                    return;
                }

            }
            case TRANSITION -> {
                if (Tools.App.isRunUpdate()) {
                    boolean finished = this.transitionManager.update();
                    if (finished) {
                        // Replace with new UIEngine after Reset
                        this.uiEngine.shutdown();
                        this.uiEngine = this.uiEngine_transition;
                        this.uiEngine_transition = null;
                        this.state = STATE.RUN;
                        return;
                    }
                }
                transitionManager.render();
            }
        }

        // Debug Output
        if (System.currentTimeMillis() - timer_debug_info > 5000) {
            Tools.Log.benchmark();
            timer_debug_info = System.currentTimeMillis();
        }
    }

    @Override
    public void dispose() {
        Tools.Log.inProgress("Shutting down...");
        this.shutdownEngine();
        Tools.Log.done();
    }

    private void shutdownEngine() {
        this.uiEngine.shutdown();
        this.mediaManager.shutdown();
        this.transitionManager.shutdown();
    }


}
