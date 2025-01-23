package net.mslivo.example;

import com.badlogic.gdx.ApplicationAdapter;
import net.mslivo.core.engine.media_manager.MediaManager;
import net.mslivo.core.engine.tools.Tools;
import net.mslivo.core.engine.tools.transitions.TransitionManager;
import net.mslivo.core.engine.tools.transitions.transitions.FadeTransition;
import net.mslivo.core.engine.tools.transitions.transitions.PixelateTransition;
import net.mslivo.core.engine.ui_engine.UIEngine;
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
        this.transitionManager = null;
        // Load Assets
        System.out.println("Loading Assets");
        this.mediaManager = new MediaManager();
        this.mediaManager.prepareUICMedia();
        this.mediaManager.prepareCMedia(ExampleBaseMedia.ALL);
        this.mediaManager.loadAssets();
        System.out.println("Done.");

        // Input/Render
        System.out.println("Starting UI");
        this.uiEngine = new UIEngine<>(
                new ExampleUIEngineAdapter(),
                this.mediaManager, ExampleMainConstants.INTERNAL_RESOLUTION_WIDTH, ExampleMainConstants.INTERNAL_RESOLUTION_HEIGHT,
                ExampleMainConstants.viewportMode, true);
        System.out.println("Done.");

        this.state = STATE.RUN;
    }

    @Override
    public void render() {
        switch (state) {
            case RUN -> {
                if (Tools.App.runUpdate()) {
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
                    this.transitionManager = new TransitionManager(this.uiEngine, this.uiEngine_transition, new PixelateTransition(), true);
                    this.transitionManager.render();
                    state = STATE.TRANSITION;
                    return;
                }

            }
            case TRANSITION -> {
                if (Tools.App.runUpdate()) {
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
        if (System.currentTimeMillis() - timer_debug_info > 1000) {
            System.out.println(Tools.Text.benchmark());
            timer_debug_info = System.currentTimeMillis();
        }
    }

    @Override
    public void dispose() {
        System.out.println("Shutting down...");
        this.shutdownEngine();
        System.out.println("Done.");
    }

    private void shutdownEngine() {
        this.uiEngine.shutdown();
        this.mediaManager.shutdown();
    }


}
