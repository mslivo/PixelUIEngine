package net.mslivo.example;

import com.badlogic.gdx.ApplicationAdapter;
import net.mslivo.pixelui.media.MediaManager;
import net.mslivo.pixelui.theme.Base8x8Theme;
import net.mslivo.pixelui.utils.Tools;
import net.mslivo.pixelui.utils.misc.UpdateTimer;
import net.mslivo.pixelui.utils.misc.ValueWatcher;
import net.mslivo.pixelui.utils.transitions.TransitionManager;
import net.mslivo.pixelui.utils.transitions.basic.PixelateTransition;
import net.mslivo.pixelui.engine.UIEngine;
import net.mslivo.example.ui.ExampleUIEngineAdapter;
import net.mslivo.example.ui.media.ExampleBaseMedia;

public class ExampleMain extends ApplicationAdapter {
    enum STATE {
        RUN, TRANSITION
    }

    private STATE state;
    private TransitionManager transitionManager;
    private MediaManager mediaManager;
    private UIEngine<ExampleUIEngineAdapter> uiEngine;
    private UIEngine<ExampleUIEngineAdapter> uiEngine_transition;
    private long timer_debug_info;
    private UpdateTimer updateTimer;
    private Base8x8Theme theme8x8 = new Base8x8Theme();

    public ExampleMain() {
    }

    @Override
    public void resize(int width, int height) {
        if (this.uiEngine != null) this.uiEngine.resize(width, height);
    }


    @Override
    public void create() {
        new ValueWatcher.Int();

        this.updateTimer = new UpdateTimer(ExampleMainConstants.UPDATE_RATE);
        this.transitionManager = null;
        // Load Assets
        System.out.println("Loading Assets");


        this.mediaManager = new MediaManager();
        this.mediaManager.prepareCMedia(theme8x8.cMedia());
        this.mediaManager.prepareCMedia(ExampleBaseMedia.ALL);
        this.mediaManager.loadAssets();
        System.out.println("Done.");

        // Input/Render
        System.out.println("Starting UI");

        this.uiEngine = new UIEngine<>(
                new ExampleUIEngineAdapter(),
                this.mediaManager, theme8x8, ExampleMainConstants.INTERNAL_RESOLUTION_WIDTH, ExampleMainConstants.INTERNAL_RESOLUTION_HEIGHT,
                ExampleMainConstants.viewportMode,true);
        System.out.println("Done.");

        this.state = STATE.RUN;

    }


    @Override
    public void render() {
        switch (state) {

            case RUN -> {
                if (updateTimer.shouldUpdate()) {
                    this.uiEngine.update();
                }

                this.uiEngine.render();
                // Check for transition + Reset
                if (this.uiEngine.getAdapter().isResetPressed()) {
                    this.uiEngine_transition = new UIEngine<>(
                            new ExampleUIEngineAdapter(),
                            this.mediaManager, theme8x8, ExampleMainConstants.INTERNAL_RESOLUTION_WIDTH, ExampleMainConstants.INTERNAL_RESOLUTION_HEIGHT,
                            ExampleMainConstants.viewportMode);
                    this.uiEngine_transition.update();
                    this.transitionManager = new TransitionManager(this.uiEngine, this.uiEngine_transition, new PixelateTransition());
                    this.transitionManager.render();
                    state = STATE.TRANSITION;
                    return;
                }

            }
            case TRANSITION -> {
                if (updateTimer.shouldUpdate()) {
                    boolean finished = this.transitionManager.update();
                    if (finished) {
                        // Replace with new UIEngine after Reset
                        this.uiEngine.dispose();
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
        if (System.currentTimeMillis() - timer_debug_info > 1000) {
            System.out.println(Tools.Text.benchmark());
            timer_debug_info = System.currentTimeMillis();
        }


    }

    @Override
    public void dispose() {
        System.out.println("Shutting down...");
        this.shutdownEngine();
        System.out.println("Done.");
    }

    private void shutdownEngine() {
        this.uiEngine.dispose();
        this.mediaManager.dispose();
    }


}
