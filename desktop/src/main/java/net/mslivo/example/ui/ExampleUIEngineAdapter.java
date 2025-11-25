package net.mslivo.example.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL32;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.MathUtils;
import com.monstrous.gdx.webgpu.graphics.WgShaderProgram;
import net.mslivo.pixelui.media.MediaManager;
import net.mslivo.pixelui.rendering.WgSpriteRenderer;
import net.mslivo.pixelui.utils.Tools;
import net.mslivo.pixelui.utils.particles.ParticleUpdater;
import net.mslivo.pixelui.utils.particles.SpriteParticleSystem;
import net.mslivo.pixelui.utils.particles.particles.Particle;
import net.mslivo.pixelui.engine.API;
import net.mslivo.pixelui.engine.APIInput;
import net.mslivo.pixelui.engine.UIEngineAdapter;
import net.mslivo.pixelui.engine.constants.BUTTON_MODE;
import net.mslivo.pixelui.engine.constants.KeyCode;
import net.mslivo.pixelui.rendering.ShaderParser;
import net.mslivo.pixelui.engine.actions.ButtonAction;
import net.mslivo.pixelui.engine.actions.HotKeyAction;
import net.mslivo.pixelui.engine.TextButton;
import net.mslivo.pixelui.engine.AppViewport;
import net.mslivo.example.ui.media.ExampleBaseMedia;
import net.mslivo.example.ui.windows.ExampleWindowGeneratorP;


public class ExampleUIEngineAdapter implements UIEngineAdapter {
    private static final boolean IM_PERFORMANCE_TEST = false; // ~ 1000ms
    private static final boolean SPRITE_PERFORMANCE_TEST = false; // ~10ms
    private API api;
    private MediaManager mediaManager;
    private float animation_timer;
    private boolean resetPressed;
    //private WgSpriteRenderer spriteRenderer;
    //private WgSpriteRenderer spriteRendererDefault;
    //private PrimitiveRenderer primitiveRenderer; TODO
    private SpriteParticleSystem<ParticleDataInner> spriteParticleSystem;
    //private PrimitiveParticleSystem<ParticleDataInner> primitiveParticleSystem; TODO


    public static class ParticleDataInner {
        int randomData = 0;

        public ParticleDataInner() {
        }
    }


    public ExampleUIEngineAdapter() {
    }

    public boolean isResetPressed() {
        return resetPressed;
    }

    public void setResetPressed(boolean resetPressed) {
        this.resetPressed = resetPressed;
    }

    @Override
    public void init(API api, MediaManager mediaManager) {
        this.api = api;
        this.mediaManager = mediaManager;
        this.animation_timer = 0;
        //this.spriteRendererDefault = new WgSpriteRenderer(mediaManager);
        //this.spriteRenderer = new WgSpriteRenderer(mediaManager, 2000,new WgShaderProgram(Tools.File.findResource("shaders/pixelui/hsl.wgsl")));
        //this.spriteRenderer.setTweakReset(0.5f,0.5f,0.5f,0f);

        /*this.primitiveRenderer = new PrimitiveRenderer( ShaderParser.parse(Tools.File.findResource("shaders/pixelui/hsl.primitive.glsl")), PrimitiveRenderer.MAX_VERTEXES_DEFAULT);
        this.primitiveRenderer.setTweakReset(0.5f,0.5f,0.5f,0f);
        TODO
         */

        api.config.window.defaultEnforceScreenBounds = false;
        // Example Wnd Button
        TextButton createExampleWindowButton = api.component.button.textButton.create(0, 0, 10, 2, "Example Wnd", new ButtonAction() {
            @Override
            public void onRelease() {
                api.addWindow(api.window.createFromGenerator(new ExampleWindowGeneratorP(), mediaManager));
            }

        }, BUTTON_MODE.DEFAULT);

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
        api.setMouseTool(api.mouseTool.create("Pointer", null, api.theme().UI_CURSOR_ARROW));


        api.config.input.hardwareMouseEnabled = true;
        api.config.input.gamePadMouseEnabled = true;
        api.config.input.keyboardMouseEnabled = true;


        api.addHotKey(api.hotkey.create(new int[]{KeyCode.Key.F5}, new HotKeyAction() {
            @Override
            public void onRelease() {
                api.addWindowAsModal(api.widgets.modal.createMessageModal("test", new String[]{"test"}, null));
            }
        }));

        ParticleUpdater<ParticleDataInner> particleUpdater = new ParticleUpdater<>() {
            @Override
            public boolean updateParticle(Particle<ParticleDataInner> particle) {
                particle.x += MathUtils.random(-1, 1);
                particle.y--;
                return particle.y >= 0 ? true : false;
            }

            @Override
            public void resetParticleData(ParticleDataInner particleData) {
                return;
            }
        };

        //api.config.ui.font = UIEngineBaseMedia_8x8.UI_FONT_SMALL;

        this.spriteParticleSystem = new SpriteParticleSystem<>(ParticleDataInner.class, particleUpdater);
        //this.primitiveParticleSystem = new PrimitiveParticleSystem<>(ParticleDataInner.class, particleUpdater); TODO
    }

    @Override
    public void update() {
        APIInput.APIKeyboard keyBoard = api.input.keyboard;


        if (Tools.Calc.randomChance(30)) {
            spriteParticleSystem.addImageParticle(api.theme().UI_CURSOR_ARROW, MathUtils.random(0, api.resolutionWidth()), api.resolutionHeight());
            /*primitiveParticleSystem.addPrimitiveParticle(GL32.GL_LINES, MathUtils.random(0, api.resolutionWidth()), api.resolutionHeight(),
                    MathUtils.random(0f, 1f), MathUtils.random(0f, 1f), MathUtils.random(0f, 1f), 1f,
                    MathUtils.random(-5, 5), 10,
                    MathUtils.random(0f, 1f), MathUtils.random(0f, 1f), MathUtils.random(0f, 1f), 1f

            );
            TODO
             */
        }

        spriteParticleSystem.updateParallel();
        ///primitiveParticleSystem.updateParallel(); TODO
    }


    @Override
    public void render(OrthographicCamera camera, AppViewport appViewPort) {
        animation_timer += Gdx.graphics.getDeltaTime();
        Gdx.gl.glClear(GL32.GL_COLOR_BUFFER_BIT);

        /*
         TODO
        // Draw app based on data
        spriteRenderer.setProjectionMatrix(camera.combined);

        spriteRenderer.begin();

        spriteRenderer.setColor(0.5f, 0.5f, 0.5f, 1.0f);
        spriteRenderer.setTweak(0.5f, 0.5f, 0.5f, 0.0f);
        for (int x = 0; x < api.resolutionWidth(); x += 16) {
            for (int y = 0; y < api.resolutionHeight(); y += 16) {
                spriteRenderer.drawCMediaAnimation(ExampleBaseMedia.BACKGROUND, animation_timer,
                        x, y);
            }
        }

        spriteRenderer.end();




        if (IM_PERFORMANCE_TEST) {

            primitiveRenderer.saveState();

            primitiveRenderer.setProjectionMatrix(camera.combined);
            long time = System.nanoTime();
            final int VERTEXES = 250;

            primitiveRenderer.setBlendingEnabled(true);
            primitiveRenderer.begin();
            for (int i = 0; i < VERTEXES; i++) {
                for (int ix = 0; ix < 64; ix++) {
                    for (int iy = 0; iy < 64; iy++) {
                        primitiveRenderer.setVertexColor(ix / 10f, iy / 5f, 1f, 0.1f);
                        primitiveRenderer.vertex(100 + ix, 200 + iy);
                    }
                }
            }
            primitiveRenderer.end();
            System.out.println((VERTEXES*(64*64)) + " Vertexes: " + ((System.nanoTime() - time) / 1000) + "ns");
        }


        primitiveRenderer.setProjectionMatrix(camera.combined);


        // Primitive Drawing Test

        primitiveRenderer.begin(GL32.GL_POINTS);

        primitiveRenderer.setVertexColor(Color.BLUE);

        primitiveRenderer.vertex(50, 50);
        primitiveRenderer.vertex(51, 50);
        primitiveRenderer.vertex(52, 50);
        primitiveRenderer.vertex(53, 50);
        primitiveRenderer.vertex(54, 50);

        primitiveRenderer.end();




        primitiveRenderer.begin(GL32.GL_LINES);
        primitiveRenderer.setVertexColor(Color.RED);
        primitiveRenderer.vertex(50, 60);
        primitiveRenderer.vertex(55, 60);
        primitiveRenderer.end();


        primitiveRenderer.begin(GL32.GL_TRIANGLES);
        primitiveRenderer.setColor(Color.GRAY);
        primitiveRenderer.setVertexColor(Color.YELLOW);
        primitiveRenderer.vertex(50, 70);
        primitiveRenderer.setVertexColor(Color.BLUE);
        primitiveRenderer.vertex(55, 80);
        primitiveRenderer.setVertexColor(Color.GREEN);
        primitiveRenderer.vertex(60, 70);

        primitiveRenderer.setVertexColor(Color.MAGENTA);
        primitiveRenderer.vertex(50 + 15, 70);
        primitiveRenderer.vertex(55 + 15, 80);
        primitiveRenderer.vertex(60 + 15, 70);
        primitiveRenderer.end();


        primitiveRenderer.begin(GL32.GL_LINE_STRIP);
        primitiveRenderer.setVertexColor(Color.RED);

        primitiveRenderer.vertex(100, 140);
        primitiveRenderer.vertex(120, 140);
        primitiveRenderer.vertex(110, 160);
        primitiveRenderer.vertex(110, 180);

        primitiveRenderer.primitiveRestart();

        primitiveRenderer.setVertexColor(Color.BLUE);
        primitiveRenderer.setColor(0.5f, 0.5f, 0.5f, 1f);
        primitiveRenderer.vertex(100, 100);
        primitiveRenderer.vertex(130, 120);
        primitiveRenderer.vertex(150, 120);


        primitiveRenderer.end();




        spriteRenderer.begin();

        spriteParticleSystem.render(mediaManager, spriteRenderer);

        spriteRenderer.end();


        spriteRenderer.begin();

        spriteRenderer.setColor(0.5f, 0.5f, 0.5f, 1f);
        mediaManager.font(api.theme().UI_FONT).setColor(new Color(0.5f, 0.5f, 0.5f, 1f));
        spriteRenderer.drawCMediaFont(api.theme().UI_FONT, 4, 310, "SYMBOL: " + Tools.Text.fontSymbol(1));
        spriteRenderer.drawCMediaFont(api.theme().UI_FONT, 4, 300, "Text[#ff00ff]Text2");
        spriteRenderer.drawCMediaFont(api.theme().UI_FONT, 4, 290, "Text[#00ffff]Text2");
        spriteRenderer.drawCMediaFont(api.theme().UI_FONT, 4, 280, "Text[#ff00ff]Text2");


        spriteRenderer.loadState();
        spriteRenderer.end();


        if (SPRITE_PERFORMANCE_TEST) {

            long time = System.currentTimeMillis();
            spriteRenderer.begin();
            spriteRenderer.setColor(MathUtils.random(0f, 1f), MathUtils.random(0f, 1f), MathUtils.random(0f, 1f), 1f);

            int amount = 50;
            for (int i = 0; i < amount; i++) {
                for (int ix = 0; ix < 64; ix++) {
                    for (int iy = 0; iy < 64; iy++) {
                        spriteRenderer.drawCMediaImage(api.theme().UI_PIXEL, 200, 300);
                    }
                }
            }

            spriteRenderer.setColor(1f, 1f, 1f, 1f);

            spriteRenderer.drawCMediaImage(api.theme().UI_PIXEL, 400, 200, 10, 10);


            spriteRenderer.loadState();
            spriteRenderer.end();


            System.out.println("sprites:"+(amount*(64*64))+", time: "+((System.currentTimeMillis()-time)) + "ms");

        }


        spriteRenderer.begin();


        spriteRenderer.loadState();
        spriteRenderer.setShader(shaderProgram);
        spriteRenderer.setColor(0.5f, 0.5f, 0.5f, 1.0f);
        //spriteRenderer.setShader(shaderProgram);
        spriteRenderer.setTweak(0.5f,0.5f, 0.5f, 0f);

        spriteRenderer.drawCMediaImage(ExampleBaseMedia.EXAMPLE_TEST, 300, 100);
        spriteRenderer.setShader(null);


        spriteRenderer.end();
       */

    }

    private WgShaderProgram shaderProgram = new WgShaderProgram(Tools.File.findResource("shaders/pixelui/hsl.wgsl"));

    @Override
    public void dispose() {
        //spriteRenderer.dispose(); TODO
    }


}
