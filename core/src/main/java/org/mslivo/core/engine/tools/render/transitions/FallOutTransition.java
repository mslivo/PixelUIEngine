package org.mslivo.core.engine.tools.render.transitions;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;

public class FallOutTransition implements Transition {
    private float yTo;
    private float ySpeed;
    private int screenHeight;
    @Override
    public TRANSITION_MODE init(int screenWidth, int screenHeight) {
        this.yTo = 0;
        this.screenHeight = screenHeight;
        this.ySpeed = MathUtils.round(this.screenHeight /80f);
        return TRANSITION_MODE.TO_FIRST;
    }

    @Override
    public boolean update() {
        ySpeed -= screenHeight /1400f;
        yTo += ySpeed;
        return yTo <= -screenHeight;
    }

    @Override
    public void renderFrom(SpriteBatch batch, TextureRegion texture_from) {
        batch.setColor(Color.WHITE);
        batch.draw(texture_from, 0, MathUtils.round(yTo));
    }

    @Override
    public void renderTo(SpriteBatch batch, TextureRegion texture_to) {
        batch.setColor(Color.WHITE);
        batch.draw(texture_to, 0, 0);
    }

}
