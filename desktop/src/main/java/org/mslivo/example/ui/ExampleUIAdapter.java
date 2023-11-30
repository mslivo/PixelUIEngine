package org.mslivo.example.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import org.mslivo.core.engine.media_manager.MediaManager;
import org.mslivo.core.engine.tools.engine.GameEngine;
import org.mslivo.core.engine.ui_engine.API;
import org.mslivo.core.engine.ui_engine.UIAdapter;
import org.mslivo.core.engine.ui_engine.gui.actions.ButtonAction;
import org.mslivo.core.engine.ui_engine.gui.actions.HotKeyAction;
import org.mslivo.core.engine.ui_engine.gui.components.button.ButtonMode;
import org.mslivo.core.engine.ui_engine.gui.components.button.TextButton;
import org.mslivo.core.engine.ui_engine.media.GUIBaseMedia;
import org.mslivo.example.data.ExampleData;
import org.mslivo.example.engine.ExampleEngineAdapter;
import org.mslivo.example.ui.media.ExampleBaseMedia;
import org.mslivo.example.ui.windows.ExampleWindowGenerator;

public class ExampleUIAdapter implements UIAdapter {

    private API api;

    private MediaManager mediaManager;

    private final GameEngine<ExampleEngineAdapter, ExampleData> gameEngine;

    private final ExampleData data;

    private float animation_timer;
    private boolean resetPressed;

    public ExampleUIAdapter(GameEngine<ExampleEngineAdapter, ExampleData> gameEngine) {
        this.gameEngine = gameEngine;
        this.data = gameEngine.getData();
    }

    public void setResetPressed(boolean resetPressed) {
        this.resetPressed = resetPressed;
    }
    public boolean isResetPressed() {
        return resetPressed;
    }


    @Override
    public void init(API api, MediaManager mediaManager) {
        this.api = api;
        this.mediaManager = mediaManager;
        this.animation_timer = 0;

        // Example Wnd Button
        TextButton createExampleWindowButton = api.components.button.textButton.create(0, 0, 10, 2, "Example Wnd", new ButtonAction() {
            @Override
            public void onRelease() {
                api.addWindow(api.windows.createFromGenerator(new ExampleWindowGenerator(api), "Example Window", gameEngine, mediaManager));
            }
        }, null, ButtonMode.DEFAULT);

        api.components.button.centerContent(createExampleWindowButton);
        api.addScreenComponent(createExampleWindowButton);

        createExampleWindowButton.buttonAction.onPress();
        createExampleWindowButton.buttonAction.onRelease();

        // Transition Btn
        TextButton transitionBtn = api.components.button.textButton.create(0, 2, 10, 2, "Reset", new ButtonAction() {
            @Override
            public void onRelease() {
                resetPressed = true;
            }
        });
        api.components.button.centerContent(transitionBtn);
        api.addScreenComponent(transitionBtn);


        // HotKey
        api.addHotKey(api.hotkey.create(new int[]{Input.Keys.ESCAPE}, new HotKeyAction() {
            @Override
            public void onPress() {
                api.closeAllWindows();
            }
        }));
        api.camera.moveAbs(api.resolutionWidth() / 2, api.resolutionHeight() / 2);
        api.setMouseTool(api.mouseTool.create("Pointer", null, GUIBaseMedia.GUI_CURSOR_ARROW));

        api.config.setHardwareMouseEnabled(true);
        api.config.setKeyboardMouseEnabled(true);
        api.config.setGamePadMouseEnabled(true);
        api.config.setGamePadMouseStickRightEnabled(true);
    }

    @Override
    public void update() {
        // Process Outputs
        while (gameEngine.nextOutput()) {
            int type = gameEngine.getOutputType();
            Object[] params = gameEngine.getOutputParams();
        }

        // Create Inputs
        // gameEngine.input(new EngineInput(0,"TestInput"));
    }


    @Override
    public void render(SpriteBatch batch, boolean mainViewPort) {
        animation_timer += Gdx.graphics.getDeltaTime();
        Gdx.gl.glClear(GL30.GL_COLOR_BUFFER_BIT);

        // Draw game based on data
        batch.begin();
        for (int x = 0; x < api.resolutionWidth(); x += 16) {
            for (int y = 0; y < api.resolutionHeight(); y += 16) {
                mediaManager.drawCMediaAnimation(batch, ExampleBaseMedia.GUI_BACKGROUND,
                        x, y, animation_timer);
            }
        }

        batch.end();

    }

    @Override
    public void shutdown() {

    }


}
