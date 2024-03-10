package org.mslivo.example.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import org.mslivo.core.engine.media_manager.MediaManager;
import org.mslivo.core.engine.tools.engine.AppEngine;
import org.mslivo.core.engine.ui_engine.API;
import org.mslivo.core.engine.ui_engine.UIAdapter;
import org.mslivo.core.engine.ui_engine.UIBaseMedia;
import org.mslivo.core.engine.ui_engine.input.KeyCode;
import org.mslivo.core.engine.ui_engine.render.SpriteRenderer;
import org.mslivo.core.engine.ui_engine.render.ImmediateRenderer;
import org.mslivo.core.engine.ui_engine.ui.actions.ButtonAction;
import org.mslivo.core.engine.ui_engine.ui.actions.HotKeyAction;
import org.mslivo.core.engine.ui_engine.ui.components.button.ButtonMode;
import org.mslivo.core.engine.ui_engine.ui.components.button.TextButton;
import org.mslivo.core.engine.ui_engine.ui.components.viewport.AppViewPort;
import org.mslivo.example.data.ExampleData;
import org.mslivo.example.engine.ExampleEngineAdapter;
import org.mslivo.example.ui.media.ExampleBaseMedia;
import org.mslivo.example.ui.windows.ExampleWindowGenerator;

public class ExampleUIAdapter implements UIAdapter {

    private API api;

    private MediaManager mediaManager;

    private final AppEngine<ExampleEngineAdapter, ExampleData> appEngine;

    private final ExampleData data;

    private float animation_timer;
    private boolean resetPressed;

    public ExampleUIAdapter(AppEngine<ExampleEngineAdapter, ExampleData> appEngine) {
        this.appEngine = appEngine;
        this.data = appEngine.getData();
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
                api.addWindow(api.window.createFromGenerator(new ExampleWindowGenerator(api), "Example Window", appEngine, mediaManager));
            }
        }, null, 0, ButtonMode.DEFAULT);

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
        api.setMouseTool(api.mouseTool.create("Pointer", null, UIBaseMedia.UI_CURSOR_ARROW));


        api.config.input.setHardwareMouseEnabled(true);

        api.config.input.setKeyboardMouseEnabled(true);
        api.config.input.setKeyboardMouseButtonsUp(new int[]{KeyCode.Key.UP});
        api.config.input.setKeyboardMouseButtonsDown(new int[]{KeyCode.Key.DOWN});
        api.config.input.setKeyboardMouseButtonsLeft(new int[]{KeyCode.Key.LEFT});
        api.config.input.setKeyboardMouseButtonsRight(new int[]{KeyCode.Key.RIGHT});
        api.config.input.setKeyboardMouseButtonsMouse1(new int[]{KeyCode.Key.CONTROL_LEFT});

        api.config.input.setGamePadMouseEnabled(true);
        api.config.input.setGamePadMouseStickLeftEnabled(true);
        api.config.input.setGamePadMouseStickRightEnabled(true);
        api.config.input.setGamePadMouseButtonsMouse1(new int[]{KeyCode.GamePad.A});
        api.config.input.setGamePadMouseButtonsMouse2(new int[]{KeyCode.GamePad.B});
    }

    @Override
    public void update() {
        // Process Outputs
        while (appEngine.nextOutput()) {
            int type = appEngine.getOutputType();
            Object[] params = appEngine.getOutputParams();
        }


        // Create Inputs
        // appEngine.input(new EngineInput(0,"TestInput"));

        API._Input._KeyBoard keyBoard = api.input.keyboard;
        while (keyBoard.event.keyDownHasNext()) {
            int key = keyBoard.event.keyDownNext();
            switch (key) {
                case KeyCode.Key.Q -> {
                    api.input.mouse.emulated.setPosition(10, 10);
                }
            }
        }
    }


    @Override
    public void render(SpriteRenderer batch, ImmediateRenderer immediateRenderer, AppViewPort appViewPort) {
        animation_timer += Gdx.graphics.getDeltaTime();
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Draw app based on data

        batch.begin();
        for (int x = 0; x < api.resolutionWidth(); x += 16) {
            for (int y = 0; y < api.resolutionHeight(); y += 16) {
                mediaManager.drawCMediaAnimation(batch, ExampleBaseMedia.GUI_BACKGROUND,
                        x, y, animation_timer);
            }
        }
        batch.end();

        immediateRenderer.begin();
        for (int ix = 0; ix < 10; ix++) {
            for (int iy = 0; iy < 10; iy++) {
                immediateRenderer.setColor(ix / 10f, iy / 10f, 1f, 0.5f);
                immediateRenderer.vertex(100 + ix, 100 + iy);
            }
        }
        immediateRenderer.end();

        immediateRenderer.begin(GL20.GL_LINES);
        for (int ix = 0; ix < 10; ix++) {
            for (int iy = 0; iy < 10; iy++) {
                immediateRenderer.setColor(ix / 10f, iy / 10f, 1f, 0.5f);
                immediateRenderer.vertex(0, 0, 100 + ix, 100 + iy);
            }
        }
        immediateRenderer.end();

        immediateRenderer.begin(GL20.GL_TRIANGLES);
        immediateRenderer.vertex(0, 0, 100, 100, 200, 0);
        immediateRenderer.end();

    }

    @Override
    public void shutdown() {

    }


}
