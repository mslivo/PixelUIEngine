package org.vnna.core.example.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import org.vnna.core.engine.game_engine.GameEngine;
import org.vnna.core.engine.media_manager.MediaManager;
import org.vnna.core.engine.ui_engine.API;
import org.vnna.core.engine.ui_engine.UIAdapter;
import org.vnna.core.engine.ui_engine.gui.actions.ButtonAction;
import org.vnna.core.engine.ui_engine.gui.actions.HotKeyAction;
import org.vnna.core.engine.ui_engine.gui.components.button.TextButton;
import org.vnna.core.engine.ui_engine.gui.tool.PointerMouseTool;
import org.vnna.core.example.ui.media.ExampleBaseMedia;
import org.vnna.core.example.ui.windows.ExampleWindowGenerator;

public class ExampleUIAdapter implements UIAdapter {

    private API api;

    private GameEngine gameEngine;

    public float animation_timer;

    public MediaManager mediaManager;


    public ExampleUIAdapter(GameEngine gameEngine) {
        this.gameEngine = gameEngine;
    }

    @Override
    public void init(API api, MediaManager mediaManager) {
        this.api = api;
        this.mediaManager = mediaManager;
        this.animation_timer = 0;

        // Init GUI
        TextButton createExampleWindowButton = api.components.button.textButton.create(0, 0, 10, 2, "Example Wnd", new ButtonAction() {
            @Override
            public void onRelease() {
                api.addWindow(api.windows.createFromGenerator(ExampleWindowGenerator.class, "Example Window", gameEngine, mediaManager));
            }
        }, null, false, false);

        api.components.button.centerContent(createExampleWindowButton);
        api.addScreenComponent( createExampleWindowButton);

        createExampleWindowButton.buttonAction.onPress();
        createExampleWindowButton.buttonAction.onRelease();

        api.addHotKey(new int[]{Input.Keys.ESCAPE}, new HotKeyAction() {
            @Override
            public void onPress() {
                api.closeAllWindows();
            }
        });

        api.setMouseTool(new PointerMouseTool());

    }

    @Override
    public void update() {
        // Keys
        if(api.input.keyDown()){
            for(int key : api.input.keyCodesDown()){

            }

        }

        //Mouse
        if(api.input.mouseMoved()){
            api.input.mouseX();
            api.input.mouseY();


        }


    }

    @Override
    public void render(SpriteBatch batch, boolean mainViewPort) {
        animation_timer += Gdx.graphics.getDeltaTime();
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        // Draw Game Here

        int startX = -(api.resolutionWidth() / 2);
        int startY = -(api.resolutionHeight() / 2);
        int endX = +(api.resolutionWidth() / 2);
        int endY = +(api.resolutionHeight() / 2);

        batch.begin();
        for(int x=startX;x<endX;x+=32){
            for(int y=startY;y<endY;y+=32){
                mediaManager.drawCMediaAnimation(batch, ExampleBaseMedia.GUI_BACKGROUND,
                        x,y, animation_timer);
            }
        }
        batch.end();

    }

    @Override
    public void shutdown() {


    }


}
