package org.mslivo.example;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import org.mslivo.core.engine.media_manager.MediaManager;
import org.mslivo.core.engine.tools.Tools;
import org.mslivo.core.engine.tools.game_engine.GameEngine;
import org.mslivo.core.engine.tools.transitions.*;
import org.mslivo.core.engine.ui_engine.UIEngine;
import org.mslivo.example.data.ExampleData;
import org.mslivo.example.data.ExampleDataGenerator;
import org.mslivo.example.engine.ExampleEngineAdapter;
import org.mslivo.example.ui.ExampleUIAdapter;
import org.mslivo.example.ui.media.ExampleBaseMedia;

public class ExampleMain extends ApplicationAdapter {

    enum STATE {
        RUN, TRANSITION
    }

    private STATE state;
    private TransitionManager transitionManager;
    /* Subsystems */

    private MediaManager mediaManager;
    private ExampleData data;
    public ExampleEngineAdapter engineAdapter;
    private GameEngine<ExampleEngineAdapter, ExampleData> gameEngine;
    private UIEngine<ExampleUIAdapter> uiEngine;
    private UIEngine<ExampleUIAdapter> uiEngine_transition;

    /* --- */

    private long profile_time_gui, profile_time_engine, profile_time_render;

    private long timer_debug_info;

    public ExampleMain() {
    }

    @Override
    public void resize(int width, int height) {
        if (this.uiEngine != null) this.uiEngine.resize(width, height);
    }

    @Override
    public void create() {
        this.transitionManager = new TransitionManager();
        // Load Assets
        Tools.Log.inProgress("Loading Assets");
        this.mediaManager = new MediaManager();
        this.mediaManager.prepareGUICMedia();
        this.mediaManager.prepareCMedia(ExampleBaseMedia.ALL);
        this.mediaManager.loadAssets();
        Tools.Log.done();

        // Engine
        Tools.Log.inProgress("Starting Engine");
        this.data = ExampleDataGenerator.create_exampleData();
        this.engineAdapter = new ExampleEngineAdapter();
        this.gameEngine = new GameEngine<>(this.engineAdapter, this.data);
        Tools.Log.done();

        // Input/Render
        Tools.Log.inProgress("Starting UI");
        this.uiEngine = new UIEngine<>(
                new ExampleUIAdapter(this.gameEngine),
                this.mediaManager,
                ExampleMainConstants.INTERNAL_RESOLUTION_WIDTH, ExampleMainConstants.INTERNAL_RESOLUTION_HEIGHT,
                ExampleMainConstants.VIEWPORT_MODE, true);
        Tools.Log.done();

        this.state = STATE.RUN;
    }

    @Override
    public void render() {
        switch (state) {
            case RUN -> {
                if (Tools.runStep(ExampleMainConstants.UPDATE_RATE)) {
                    // 1. Update UI Engine -> Gather Input & Process Output
                    profile_time_gui = System.currentTimeMillis();
                    this.uiEngine.update();
                    profile_time_gui = System.currentTimeMillis() - profile_time_gui;
                    // 2. Update Game Engine -> Process Input & Create Output
                    profile_time_engine = System.currentTimeMillis();
                    this.gameEngine.update();
                    profile_time_engine = System.currentTimeMillis() - profile_time_engine;
                }

                // 3. Render Everything
                profile_time_render = System.currentTimeMillis();
                this.uiEngine.render();
                profile_time_render = System.currentTimeMillis() - profile_time_render;


                // Check for transition + Reset
                if (this.uiEngine.getAdapter().isResetPressed()) {
                    this.uiEngine_transition = new UIEngine<>(
                            new ExampleUIAdapter(this.gameEngine),
                            this.mediaManager,
                            ExampleMainConstants.INTERNAL_RESOLUTION_WIDTH, ExampleMainConstants.INTERNAL_RESOLUTION_HEIGHT,
                            ExampleMainConstants.VIEWPORT_MODE, true);
                    this.uiEngine_transition.update();
                    this.transitionManager.init(this.uiEngine, this.uiEngine_transition, new FallOutTransition());
                    this.transitionManager.render();
                    state = STATE.TRANSITION;
                    return;
                }

            }
            case TRANSITION -> {
                if (Tools.runStep(ExampleMainConstants.UPDATE_RATE)) {
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
            Tools.Log.benchmark("UI: " + profile_time_gui + "ms", "Render: " + profile_time_render + "ms");
            timer_debug_info = System.currentTimeMillis();
        }
    }

    @Override
    public void dispose() {
        Tools.Log.inProgress("Shutting down...");
        this.shutdownEngine();
        Tools.Log.done();
        Gdx.app.exit();
        System.exit(0);
    }

    private void shutdownEngine() {
        this.uiEngine.shutdown();
        this.gameEngine.shutdown();
        this.mediaManager.shutdown();
        this.transitionManager.shutdown();
    }


}
