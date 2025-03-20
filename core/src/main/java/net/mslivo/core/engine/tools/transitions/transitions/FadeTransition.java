package net.mslivo.core.engine.tools.transitions.transitions;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import net.mslivo.core.engine.tools.transitions.TRANSITION_RENDER_MODE;
import net.mslivo.core.engine.tools.transitions.TRANSITION_SPEED;
import net.mslivo.core.engine.tools.transitions.Transition;
import net.mslivo.core.engine.ui_engine.rendering.renderer.SpriteBasicColorTweakRenderer;

public class FadeTransition extends Transition {
    private float fadeOut;
    private float fadeIn;

    public FadeTransition(){
        super();
    }

    public FadeTransition(TRANSITION_SPEED transitionSpeed){
        super(transitionSpeed);
    }

    @Override
    public TRANSITION_RENDER_MODE getRenderMode() {
        return TRANSITION_RENDER_MODE.FROM_FIRST;
    }

    @Override
    public void init(SpriteBasicColorTweakRenderer spriteRenderer, int screenWidth, int screenHeight) {
        this.fadeOut = 0f;
        this.fadeIn = 0f;
    }

    @Override
    public boolean update() {
        if(this.fadeOut < 1f){
            this.fadeOut = Math.min(fadeOut+0.05f,1f);
            return false;
        }else if(this.fadeIn < 1f){
            this.fadeIn = Math.min(fadeIn+0.05f,1f);
            return false;
        }else{
            return true;
        }
    }

    @Override
    public void renderFrom(SpriteBasicColorTweakRenderer spriteRenderer, TextureRegion texture_from) {
        if(this.fadeOut < 1f){
            float color = Math.clamp(0.5f-(fadeOut*0.5f),0f,1f);
            spriteRenderer.setTweak(color,0.5f,0.5f,0.0f);
            spriteRenderer.setColor(color,color,color,1f);
            spriteRenderer.draw(texture_from, 0, 0);
            spriteRenderer.setAllReset();
        }
    }

    @Override
    public void renderTo(SpriteBasicColorTweakRenderer spriteRenderer, TextureRegion texture_to) {
        if(this.fadeOut >= 1 && this.fadeIn <= 1f){
            float color = Math.clamp(fadeIn*0.5f,0f,1f);
            spriteRenderer.setTweak(color,0.5f,0.5f,0.0f);
            spriteRenderer.setColor(color,color,color,1f);
            spriteRenderer.draw(texture_to, 0, 0);
            spriteRenderer.setAllReset();
        }

    }


    @Override
    public void finished(SpriteBasicColorTweakRenderer spriteRenderer) {

    }

}
