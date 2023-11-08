package org.mslivo.core.engine.tools.transitions;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class FallOutTransition implements Transition {
    private float yTo;
    private float ySpeed;
    private int height;
    @Override
    public void init(int screenWidth, int screenHeight) {
        this.yTo = 0;
        this.height = screenHeight;
        this.ySpeed = 12;
    }

    @Override
    public boolean update() {

        ySpeed -= 0.5f;
        yTo += ySpeed;
        if(yTo < -16) ySpeed = -16;
        return yTo <= -height;
    }

    @Override
    public void render(SpriteBatch batch, TextureRegion texture_from, TextureRegion texture_to) {
        batch.begin();
        batch.draw(texture_to, 0, 0);
        batch.draw(texture_from, 0, yTo);
        batch.end();
    }

}
