package net.mslivo.example;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import net.mslivo.example.ui.ExampleUIEngineAdapter;
import net.mslivo.example.ui.media.ExampleBaseMedia;
import net.mslivo.core.engine.media_manager.MediaManager;
import net.mslivo.core.engine.tools.Tools;
import net.mslivo.core.engine.tools.engine.AppEngine;
import net.mslivo.core.engine.tools.rendering.transitions.transitions.FadeTransition;
import net.mslivo.core.engine.tools.rendering.transitions.TransitionManager;
import net.mslivo.core.engine.ui_engine.UIEngine;
import net.mslivo.example.data.ExampleData;
import net.mslivo.example.data.ExampleDataGenerator;
import net.mslivo.example.engine.ExampleEngineAdapter;

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
    private AppEngine<ExampleEngineAdapter, ExampleData> appEngine;
    private UIEngine<ExampleUIEngineAdapter> uiEngine;
    private UIEngine<ExampleUIEngineAdapter> uiEngine_transition;

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
        Tools.App.setTargetUpdates(ExampleMainConstants.UPDATE_RATE);
        this.transitionManager = new TransitionManager();
        // Load Assets
        Tools.Log.inProgress("Loading Assets");
        this.mediaManager = new MediaManager();
        this.mediaManager.prepareUICMedia();
        this.mediaManager.prepareCMedia(ExampleBaseMedia.ALL);
        this.mediaManager.loadAssets();
        Tools.Log.done();

        // Engine
        Tools.Log.inProgress("Starting Engine");
        this.data = ExampleDataGenerator.create_exampleData();
        this.engineAdapter = new ExampleEngineAdapter();
        this.appEngine = new AppEngine<>(this.engineAdapter, this.data);
        Tools.Log.done();

        // Input/Render
        Tools.Log.inProgress("Starting UI");
        this.uiEngine = new UIEngine<>(
                new ExampleUIEngineAdapter(this.appEngine),
                this.mediaManager,ExampleMainConstants.INTERNAL_RESOLUTION_WIDTH, ExampleMainConstants.INTERNAL_RESOLUTION_HEIGHT,
                        ExampleMainConstants.viewportMode);
        Tools.Log.done();

        this.state = STATE.RUN;
    }

    @Override
    public void render() {
        switch (state) {
            case RUN -> {
                if (Tools.App.isRunUpdate()) {
                    // 1. Update UI Engine -> Gather Input & Process Output
                    profile_time_gui = System.currentTimeMillis();
                    this.uiEngine.update();
                    profile_time_gui = System.currentTimeMillis() - profile_time_gui;
                    // 2. Update AppEngine -> Process Input & Create Output
                    profile_time_engine = System.currentTimeMillis();
                    this.appEngine.update();
                    profile_time_engine = System.currentTimeMillis() - profile_time_engine;
                }

                // 3. Render Everything
                profile_time_render = System.currentTimeMillis();
                this.uiEngine.render();
                profile_time_render = System.currentTimeMillis() - profile_time_render;


                // Check for transition + Reset
                if (this.uiEngine.getAdapter().isResetPressed()) {
                    this.uiEngine_transition = new UIEngine<>(
                            new ExampleUIEngineAdapter(this.appEngine),
                            this.mediaManager,ExampleMainConstants.INTERNAL_RESOLUTION_WIDTH, ExampleMainConstants.INTERNAL_RESOLUTION_HEIGHT,
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
        this.appEngine.shutdown();
        this.mediaManager.shutdown();
        this.transitionManager.shutdown();
    }


}
