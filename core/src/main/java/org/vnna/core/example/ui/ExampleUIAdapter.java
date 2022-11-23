package org.vnna.core.example.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import org.vnna.core.engine.game_engine.GameEngine;
import org.vnna.core.engine.media_manager.MediaManager;
import org.vnna.core.engine.tools.Tools;
import org.vnna.core.engine.tools.listthreadpool.ThreadPoolAlgorithm;
import org.vnna.core.engine.tools.particlesystem.ParticleSystem;
import org.vnna.core.engine.ui_engine.API;
import org.vnna.core.engine.ui_engine.UIAdapter;
import org.vnna.core.engine.ui_engine.gui.actions.ButtonAction;
import org.vnna.core.engine.ui_engine.gui.actions.HotKeyAction;
import org.vnna.core.engine.ui_engine.gui.components.button.TextButton;
import org.vnna.core.engine.ui_engine.gui.tool.PointerMouseTool;
import org.vnna.core.example.ui.media.ExampleBaseMedia;
import org.vnna.core.example.ui.particle.TestParticle;
import org.vnna.core.example.ui.windows.ExampleWindowGenerator;

public class ExampleUIAdapter implements UIAdapter {

    private int particlesTest = 0;

    private long particlesOutputTimer = 0;

    private API api;

    private GameEngine gameEngine;

    private float animation_timer;

    private MediaManager mediaManager;

    private ParticleSystem<TestParticle> particleSystem;

    private int start_x, start_y, end_x, end_y;

    public ExampleUIAdapter(GameEngine gameEngine) {
        this.gameEngine = gameEngine;
    }

    @Override
    public void init(API api, MediaManager mediaManager) {
        this.api = api;
        this.mediaManager = mediaManager;
        this.animation_timer = 0;
        this.start_x = -(api.resolutionWidth() / 2);
        this.start_y = -(api.resolutionHeight() / 2);
        this.end_x = +(api.resolutionWidth() / 2);
        this.end_y = +(api.resolutionHeight() / 2);

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

        api.addHotKey(new int[]{Input.Keys.ESCAPE}, new HotKeyAction() {
            @Override
            public void onPress() {
                api.closeAllWindows();
            }
        });

        api.setMouseTool(new PointerMouseTool());

        this.particleSystem = new ParticleSystem<>(mediaManager,Integer.MAX_VALUE);
    }

    @Override
    public void update() {
        if (api.input.keyDown()) {
            for (int key : api.input.keyCodesDown()) {
                if (key == Input.Keys.P) {
                    particlesTest = particlesTest == 4 ? 0 : particlesTest + 1;
                    if (particlesTest == 0) particleSystem.removeAllParticles();
                    Tools.log("Particle Performance Test [%d]", particlesTest);
                }
            }
        }

        updateParticlesPerformanceTest();
    }

    private void updateParticlesPerformanceTest() {
        if (particlesTest == 0) return;
        int amount = switch (particlesTest) {
            case 1 -> 1000;
            case 2 -> 2000;
            case 3 -> 4000;
            case 4 -> 8000;
            default -> 0;
        };
        for (int i2 = 0; i2 < amount; i2++) {
            this.particleSystem.addParticle(new TestParticle(
                    MathUtils.random(start_x, end_x),
                    MathUtils.random(start_y, end_y),
                    particlesTest
            ));
        }
        if (System.currentTimeMillis() - particlesOutputTimer > 1000) {
            Tools.log("Particles: " + particleSystem.getParticleCount());
            particlesOutputTimer = System.currentTimeMillis();
        }
        particleSystem.update();
    }

    @Override
    public void render(SpriteBatch batch, boolean mainViewPort) {
        animation_timer += Gdx.graphics.getDeltaTime();
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        // Draw Game Here

        batch.begin();
        for (int x = start_x; x < end_x; x += 32) {
            for (int y = start_y; y < end_y; y += 32) {
                mediaManager.drawCMediaAnimation(batch, ExampleBaseMedia.GUI_BACKGROUND,
                        x, y, animation_timer);
            }
        }

        if (particlesTest != 0) {
            particleSystem.render(batch);
        }
        batch.end();

    }

    @Override
    public void shutdown() {
        particleSystem.shutdown();
    }


}
