package net.mslivo.core.engine.tools.transitions.transitions;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import net.mslivo.core.engine.tools.transitions.TRANSITION_RENDER_MODE;
import net.mslivo.core.engine.tools.transitions.TRANSITION_SPEED;
import net.mslivo.core.engine.tools.transitions.Transition;
import net.mslivo.core.engine.ui_engine.rendering.renderer.SpriteRenderer;

public class ImmediateTransition extends Transition {

    public ImmediateTransition(){
        super(TRANSITION_SPEED.IMMEDIATE);
    }

    @Override
    public TRANSITION_RENDER_MODE getRenderMode() {
        return TRANSITION_RENDER_MODE.FROM_FIRST;
    }

    @Override
    public void init(SpriteRenderer spriteRenderer, int screenWidth, int screenHeight) {

    }

    @Override
    public boolean update() {
        return true;
    }

    @Override
    public void renderFrom(SpriteRenderer spriteRenderer, TextureRegion texture_from) {
    }

    @Override
    public void renderTo(SpriteRenderer spriteRenderer, TextureRegion texture_to) {
    }

    @Override
    public void finished(SpriteRenderer spriteRenderer) {

    }

}
