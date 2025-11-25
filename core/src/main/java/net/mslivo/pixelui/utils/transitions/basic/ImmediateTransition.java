package net.mslivo.pixelui.utils.transitions.basic;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import net.mslivo.pixelui.rendering.WgSpriteRenderer;
import net.mslivo.pixelui.utils.transitions.TRANSITION_RENDER_MODE;
import net.mslivo.pixelui.utils.transitions.TRANSITION_SPEED;
import net.mslivo.pixelui.utils.transitions.Transition;

public class ImmediateTransition extends Transition {

    public ImmediateTransition(){
        super(TRANSITION_SPEED.IMMEDIATE);
    }

    @Override
    public TRANSITION_RENDER_MODE getRenderMode() {
        return TRANSITION_RENDER_MODE.FROM_FIRST;
    }

    @Override
    public void init(WgSpriteRenderer spriteRenderer, int screenWidth, int screenHeight) {

    }

    @Override
    public boolean update() {
        return true;
    }

    @Override
    public void renderFrom(WgSpriteRenderer spriteRenderer, TextureRegion texture_from) {
    }

    @Override
    public void renderTo(WgSpriteRenderer spriteRenderer, TextureRegion texture_to) {
    }

    @Override
    public void finished(WgSpriteRenderer spriteRenderer) {

    }

}
