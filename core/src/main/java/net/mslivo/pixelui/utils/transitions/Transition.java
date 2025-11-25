package net.mslivo.pixelui.utils.transitions;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import net.mslivo.pixelui.rendering.WgSpriteRenderer;

public abstract class Transition {

    public final TRANSITION_SPEED transitionSpeed;

    public Transition() {
        this.transitionSpeed = TRANSITION_SPEED.DEFAULT;
    }

    public Transition(TRANSITION_SPEED transitionSpeed) {
        if(transitionSpeed == null)
            transitionSpeed = TRANSITION_SPEED.DEFAULT;
        this.transitionSpeed = transitionSpeed;
    }

    public abstract TRANSITION_RENDER_MODE getRenderMode();

    public abstract void init(WgSpriteRenderer spriteRenderer, int screenWidth, int screenHeight);

    public abstract boolean update();

    public abstract void renderFrom(WgSpriteRenderer spriteRenderer, TextureRegion texture_from);

    public abstract void renderTo(WgSpriteRenderer spriteRenderer, TextureRegion texture_to);

    public abstract void finished(WgSpriteRenderer spriteRenderer);

}
