package org.mslivo.core.example.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import org.mslivo.core.engine.game_engine.EngineInput;
import org.mslivo.core.engine.game_engine.EngineOutput;
import org.mslivo.core.engine.game_engine.GameEngine;
import org.mslivo.core.engine.media_manager.MediaManager;
import org.mslivo.core.engine.ui_engine.API;
import org.mslivo.core.engine.ui_engine.UIAdapter;
import org.mslivo.core.engine.ui_engine.gui.actions.ButtonAction;
import org.mslivo.core.engine.ui_engine.gui.actions.HotKeyAction;
import org.mslivo.core.engine.ui_engine.gui.components.button.TextButton;
import org.mslivo.core.engine.ui_engine.media.GUIBaseMedia;
import org.mslivo.core.example.data.ExampleData;
import org.mslivo.core.example.engine.ExampleEngineAdapter;
import org.mslivo.core.example.ui.media.ExampleBaseMedia;
import org.mslivo.core.example.ui.windows.ExampleWindowGenerator;

public class ExampleUIAdapter implements UIAdapter {

    private API api;

    private MediaManager mediaManager;

    private final GameEngine<ExampleEngineAdapter, ExampleData> gameEngine;

    private final ExampleData data;

    private float animation_timer;

    private int start_x, start_y, end_x, end_y;

    public ExampleUIAdapter(GameEngine<ExampleEngineAdapter, ExampleData> gameEngine) {
        this.gameEngine = gameEngine;
        this.data = gameEngine.getData();
    }

    @Override
    public void init(API api, MediaManager mediaManager) {
        this.api = api;
        this.mediaManager = mediaManager;
        this.animation_timer = 0;
        this.start_x = -(api.resolutionWidth() / 2);
        this.start_y = -(api.resolutionHeight() / 2);
        this.end_x = (api.resolutionWidth() / 2);
        this.end_y = (api.resolutionHeight() / 2);

        // Init GUI
        TextButton createExampleWindowButton = api.components.button.textButton.create(0, 0, 10, 2, "Example Wnd", new ButtonAction() {
            @Override
            public void onRelease() {
                api.addWindow(api.windows.createFromGenerator(ExampleWindowGenerator.class, "Example Window", gameEngine, mediaManager));
            }
        }, null, false, false);

        api.components.button.centerContent(createExampleWindowButton);
        api.addScreenComponent(createExampleWindowButton);

        createExampleWindowButton.buttonAction.onPress();
        createExampleWindowButton.buttonAction.onRelease();

        api.addHotKey(api.hotkey.create(new int[]{Input.Keys.ESCAPE}, new HotKeyAction() {
            @Override
            public void onPress() {
                api.closeAllWindows();
            }
        }));
        api.setMouseTool(api.mouseTool.create("Pointer", null, GUIBaseMedia.GUI_CURSOR_ARROW));

    }

    @Override
    public void update() {
        // Process Outputs
        while (gameEngine.outputAvailable()){
            EngineOutput engineOutput = gameEngine.processOutput();
        }

        // Create Inputs
        if (api.input.keyDown()) {
            gameEngine.input(new EngineInput(0,"TestInput"));
        }
    }


    @Override
    public void render(SpriteBatch batch, boolean mainViewPort) {
        animation_timer += Gdx.graphics.getDeltaTime();
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        // Draw game based on data
        batch.begin();
        for (int x = start_x; x < end_x; x += 32) {
            for (int y = start_y; y < end_y; y += 32) {
                mediaManager.drawCMediaAnimation(batch, ExampleBaseMedia.GUI_BACKGROUND,
                        x, y, animation_timer);
            }
        }

        batch.end();

    }

    @Override
    public void shutdown() {
        mediaManager.shutdown();
    }


}
