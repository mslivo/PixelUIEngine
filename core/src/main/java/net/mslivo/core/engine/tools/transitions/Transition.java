package net.mslivo.core.engine.tools.transitions;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import net.mslivo.core.engine.ui_engine.rendering.SpriteRenderer;

public abstract class Transition {

    public final TRANSITION_SPEED transitionSpeed;

    public Transition() {
        this.transitionSpeed = TRANSITION_SPEED.X1;
    }

    public Transition(TRANSITION_SPEED transitionSpeed) {
        this.transitionSpeed = transitionSpeed;
    }

    public abstract TRANSITION_RENDER_MODE getRenderMode();

    public abstract void init(int screenWidth, int screenHeight);

    public abstract boolean update();

    public abstract void renderFrom(SpriteRenderer spriteRenderer, TextureRegion texture_from);

    public abstract void renderTo(SpriteRenderer spriteRenderer, TextureRegion texture_to);

    public abstract void shutdown();

}
