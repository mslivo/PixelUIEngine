package org.mslivo.core.engine.tools.transitions;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;

public class FallOutTransition implements Transition {
    private float yTo;
    private float ySpeed;
    private int screenHeight;
    @Override
    public void init(int screenWidth, int screenHeight) {
        this.yTo = 0;
        this.screenHeight = screenHeight;
        this.ySpeed = MathUtils.round(this.screenHeight /80f);
    }

    @Override
    public boolean update() {
        ySpeed -= screenHeight /1400f;
        if(ySpeed <= -24) ySpeed = -24;
        yTo += ySpeed;
        return yTo <= -screenHeight;
    }

    @Override
    public void render(SpriteBatch batch, TextureRegion texture_from, TextureRegion texture_to) {
        batch.begin();
        batch.setColor(Color.WHITE);
        batch.draw(texture_to, 0, 0);
        batch.draw(texture_from, 0, MathUtils.round(yTo));
        batch.end();
    }

}
