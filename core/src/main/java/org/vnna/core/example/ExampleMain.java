package org.vnna.core.example;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import org.vnna.core.engine.game_engine.GameEngine;
import org.vnna.core.engine.ui_engine.media.GUIBaseMedia;
import org.vnna.core.engine.media_manager.MediaManager;
import org.vnna.core.engine.ui_engine.UIEngine;
import org.vnna.core.engine.ui_engine.UIAdapter;
import org.vnna.core.engine.tools.Tools;

import org.vnna.core.example.data.ExampleData;
import org.vnna.core.example.engine.ExampleEngineAdapter;
import org.vnna.core.example.ui.ExampleUIAdapter;
import org.vnna.core.example.ui.media.ExampleBaseMedia;

import java.nio.file.Path;

public class ExampleMain extends ApplicationAdapter {

    /* Subsystems */

    private MediaManager mediaManager;

    private ExampleData exampleData;

    private GameEngine<ExampleEngineAdapter> gameEngine;

    private UIEngine<UIAdapter> uiEngine;

    public ExampleStartParameters exampleStartParameters;

    public ExampleEngineAdapter exampleEngineAdapter;

    public UIAdapter UIAdapter;

    /* */

    private long profile_time_gui, profile_time_engine, profile_time_render;

    private long timer_debug_info;

    public ExampleMain(ExampleStartParameters exampleStartParameters) {
        this.exampleStartParameters = exampleStartParameters;
    }

    @Override
    public void resize(int width, int height) {
        if(this.uiEngine != null) this.uiEngine.resize(width,height);
    }

    @Override
    public void create() {

        this.bootEngine();

    }

    private void bootEngine() {
        Tools.logInProgress("Loading DataFile");
        try {
            this.exampleData = (ExampleData) Tools.File.readObjectFromFile(Path.of(exampleStartParameters.dataFile));
        } catch (Exception e) {
            Tools.logError("Error while loading Datafile: " + e.getLocalizedMessage());
            e.printStackTrace();
            System.exit(0);
        }
        Tools.logDone();
        Tools.logInProgress("Loading Assets");
        this.mediaManager = new MediaManager();

        this.mediaManager.prepareFromStaticClass(GUIBaseMedia.class);
        this.mediaManager.prepareFromStaticClass(ExampleBaseMedia.class);
        this.mediaManager.loadAssets();

        Tools.logDone();
        // Engine
        Tools.logInProgress("Starting Engine Subsystem");
        this.exampleEngineAdapter = new ExampleEngineAdapter();
        this.gameEngine = new GameEngine<>(this.exampleEngineAdapter, exampleData);

        Tools.logDone();
        // Input/Render
        Tools.logInProgress("Starting Input/Render Subsystem");
        this.UIAdapter = new ExampleUIAdapter(this.gameEngine);
        this.uiEngine = new UIEngine<>(this.UIAdapter,
                mediaManager,
                exampleStartParameters.internalResolutionWidth, exampleStartParameters.internalResolutionHeight,
                exampleStartParameters.viewportMode, exampleStartParameters.stretchModeUpSampling);
        Tools.logDone();
    }


    @Override
    public void render() {

        // Update Input
        profile_time_gui = System.currentTimeMillis();
        this.uiEngine.update();
        this.gameEngine.clearOutputs();
        profile_time_gui = System.currentTimeMillis() - profile_time_gui;

        // Run engine?
        if (Tools.Calc.shouldRun(10, gameEngine.getLastUpdateTime())) { // engine is called 10/Second
            profile_time_engine = System.currentTimeMillis();
            gameEngine.update();
            profile_time_engine = System.currentTimeMillis() - profile_time_engine;
        }

        // Render
        profile_time_render = System.currentTimeMillis();
        this.uiEngine.render();
        profile_time_render = System.currentTimeMillis() - profile_time_render;


        // Debug Out
        if (System.currentTimeMillis() - timer_debug_info > 5000) {
            Tools.logBenchmark("Render: "+profile_time_render+"ms");
            timer_debug_info = System.currentTimeMillis();
        }
    }

    @Override
    public void dispose() {
        Tools.logInProgress("Shutting down...");
        this.shutdownEngine();
        Tools.logDone();
        Gdx.app.exit();
        System.exit(0);
    }

    private void shutdownEngine() {
        this.uiEngine.shutdown();
        this.gameEngine.shutdown();
        this.mediaManager.shutdown();
    }


}
