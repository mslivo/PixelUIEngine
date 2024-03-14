package net.mslivo.core.engine.tools.rendering.transitions.transitions;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import net.mslivo.core.engine.tools.rendering.transitions.TRANSITION_RENDER_MODE;
import net.mslivo.core.engine.tools.rendering.transitions.Transition;

public class FallInTransition implements Transition {
    private float yTo;
    private float ySpeed;
    private int bounce;
    private int screenHeight;
    private Runnable bounceAction;

    public FallInTransition(){
        this(null);
    }
    public FallInTransition(Runnable bounceAction) {
        this.bounceAction = bounceAction;
    }

    @Override
    public TRANSITION_RENDER_MODE init(int screenWidth, int screenHeight) {
        this.yTo = screenHeight;
        this.ySpeed = 0f;
        this.screenHeight = screenHeight;
        this.bounce = 5;
        return TRANSITION_RENDER_MODE.FROM_FIRST;
    }

    @Override
    public boolean update() {

        ySpeed -= screenHeight/1400f;
        yTo += ySpeed;
        if (yTo <= 0) {
            yTo = 0;
            ySpeed = -ySpeed / 2f;
            yTo += ySpeed;
            bounce--;
            if(bounceAction != null) bounceAction.run();
        }
        return bounce <= 0;
    }

    @Override
    public void renderFrom(SpriteBatch batch, TextureRegion texture_from) {
        batch.setColor(Color.WHITE);
        batch.draw(texture_from, 0, 0);
    }

    @Override
    public void renderTo(SpriteBatch batch, TextureRegion texture_to) {
        batch.setColor(Color.WHITE);
        batch.draw(texture_to, 0, MathUtils.round(yTo));
    }



}
