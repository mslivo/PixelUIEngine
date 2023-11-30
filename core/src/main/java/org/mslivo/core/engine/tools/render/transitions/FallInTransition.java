package org.mslivo.core.engine.tools.render.transitions;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;

public class FallInTransition implements Transition {
    private float yTo;
    private float ySpeed;
    private int bounce;

    private int screenHeight;

    @Override
    public TRANSITION_MODE init(int screenWidth, int screenHeight) {
        this.yTo = screenHeight;
        this.ySpeed = 0f;
        this.screenHeight = screenHeight;
        this.bounce = 5;
        return TRANSITION_MODE.FROM_FIRST;
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
