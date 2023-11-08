package org.mslivo.core.engine.tools.transitions;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import org.mslivo.core.engine.tools.Tools;

public class FadeTransition implements Transition {
    private float fadeOut;
    private float fadeIn;

    @Override
    public void init(int screenWidth, int screenHeight) {
        this.fadeOut = 0f;
        this.fadeIn = 0f;
    }

    @Override
    public boolean update() {
        if(this.fadeOut < 1f){
            this.fadeOut = Tools.Calc.upperBounds(fadeOut+0.06f,1f);
            return false;
        }else if(this.fadeIn < 1f){
            this.fadeIn = Tools.Calc.upperBounds(fadeIn+0.06f,1f);
            return false;
        }else{
            return true;
        }
    }

    @Override
    public void render(SpriteBatch batch, TextureRegion texture_from, TextureRegion texture_to) {
        batch.begin();
        if(this.fadeOut < 1f){
            float color = Tools.Calc.inBounds(1f-fadeOut,0f,1f);
            batch.setColor(color,color,color,1f);
            batch.draw(texture_from, 0, 0);
        }else if(this.fadeIn <= 1f){
            float color = Tools.Calc.inBounds(fadeIn,0f,1f);
            batch.setColor(color,color,color,1f);
            batch.draw(texture_to, 0, 0);
        }
        batch.end();
    }


}
