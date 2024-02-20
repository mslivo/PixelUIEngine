package org.mslivo.example.ui;

import com.badlogic.gdx.Gdx;
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
import org.mslivo.core.engine.ui_engine.gui.components.viewport.GameViewPort;
import org.mslivo.core.engine.ui_engine.input.KeyCode;
import org.mslivo.core.engine.ui_engine.media.GUIBaseMedia;
import org.mslivo.core.engine.ui_engine.misc.render.ImmediateRenderer;
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
        TextButton createExampleWindowButton = api.component.button.textButton.create(0, 0, 10, 2, "Example Wnd", new ButtonAction() {
            @Override
            public void onRelease() {
                api.addWindow(api.window.createFromGenerator(new ExampleWindowGenerator(api), "Example Window", gameEngine, mediaManager));
            }
        }, null, ButtonMode.DEFAULT);

        api.component.button.centerContent(createExampleWindowButton);
        api.addScreenComponent(createExampleWindowButton);

        createExampleWindowButton.buttonAction.onPress();
        createExampleWindowButton.buttonAction.onRelease();

        // Transition Btn
        TextButton transitionBtn = api.component.button.textButton.create(0, 2, 10, 2, "Reset", new ButtonAction() {
            @Override
            public void onRelease() {
                resetPressed = true;
            }
        });
        api.component.button.centerContent(transitionBtn);
        api.addScreenComponent(transitionBtn);


        // HotKey
        api.addHotKey(api.hotkey.create(new int[]{com.badlogic.gdx.Input.Keys.ESCAPE}, new HotKeyAction() {
            @Override
            public void onPress() {
                api.closeAllWindows();
            }
        }));
        api.camera.setPosition(api.resolutionWidth() / 2f, api.resolutionHeight() / 2f);
        api.setMouseTool(api.mouseTool.create("Pointer", null, GUIBaseMedia.GUI_CURSOR_ARROW));


        api.config.setInput_hardwareMouseEnabled(true);

        api.config.setInput_keyboardMouseEnabled(true);
        api.config.setInput_keyboardMouseButtonsUp(new int[]{KeyCode.Key.UP});
        api.config.setInput_keyboardMouseButtonsDown(new int[]{KeyCode.Key.DOWN});
        api.config.setInput_keyboardMouseButtonsLeft(new int[]{KeyCode.Key.LEFT});
        api.config.setInput_keyboardMouseButtonsRight(new int[]{KeyCode.Key.RIGHT});
        api.config.setInput_keyboardMouseButtonsMouse1(new int[]{KeyCode.Key.CONTROL_LEFT});

        api.config.setInput_gamePadMouseEnabled(true);
        api.config.setInput_gamePadMouseStickLeftEnabled(true);
        api.config.setInput_gamePadMouseStickRightEnabled(true);
        api.config.setInput_gamePadMouseButtonsMouse1(new int[]{KeyCode.GamePad.A});
        api.config.setInput_gamePadMouseButtonsMouse2(new int[]{KeyCode.GamePad.B});
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

        API._Input._KeyBoard keyBoard = api.input.keyboard;
        while (keyBoard.event.keyDownHasNext()){
            int key = keyBoard.event.keyDownNext();
            switch (key){
                case KeyCode.Key.Q -> {
                    api.input.mouse.setPosition(10,10);
                }
            }
        }
    }


    @Override
    public void render(SpriteBatch batch, ImmediateRenderer immediateRenderer, GameViewPort gameViewPort) {
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
