package net.mslivo.core.engine.tools.transitions.transitions;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import net.mslivo.core.engine.tools.transitions.TRANSITION_RENDER_MODE;
import net.mslivo.core.engine.tools.transitions.Transition;

public class FadeTransition implements Transition {
    private float fadeOut;
    private float fadeIn;

    @Override
    public TRANSITION_RENDER_MODE init(int screenWidth, int screenHeight) {
        this.fadeOut = 0f;
        this.fadeIn = 0f;
        return TRANSITION_RENDER_MODE.FROM_FIRST;
    }

    @Override
    public boolean update() {
        if(this.fadeOut < 1f){
            this.fadeOut = Math.min(fadeOut+0.06f,1f);
            return false;
        }else if(this.fadeIn < 1f){
            this.fadeIn = Math.min(fadeIn+0.06f,1f);
            return false;
        }else{
            return true;
        }
    }

    @Override
    public void renderFrom(SpriteBatch batch, TextureRegion texture_from) {
        if(this.fadeOut < 1f){
            float color = Math.clamp(1f-fadeOut,0f,1f);
            batch.setColor(color,color,color,1f);
            batch.draw(texture_from, 0, 0);
        }else {
            batch.setColor(Color.WHITE);
        }
    }

    @Override
    public void renderTo(SpriteBatch batch, TextureRegion texture_to) {
        if(this.fadeOut >= 1 && this.fadeIn <= 1f){
            float color = Math.clamp(fadeIn,0f,1f);
            batch.setColor(color,color,color,1f);
            batch.draw(texture_to, 0, 0);
        }else{
            batch.setColor(Color.WHITE);
        }

    }

}
