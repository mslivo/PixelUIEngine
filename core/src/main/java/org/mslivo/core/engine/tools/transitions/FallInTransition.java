package org.mslivo.core.engine.tools.transitions;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class FallInTransition implements Transition {
    private float yTo;
    private float ySpeed;
    private int bounce;

    @Override
    public void init(int screenWidth, int screenHeight) {
        this.yTo = screenHeight;
        this.ySpeed = 0f;
        this.bounce = 5;
    }

    @Override
    public boolean update() {

        ySpeed -= 0.5f;
        yTo += ySpeed;
        if (yTo <= 0) {
            yTo = 0;
            ySpeed = -ySpeed / 1.6f;
            yTo += ySpeed;
            bounce--;
        }
        return bounce <= 0;
    }

    @Override
    public void render(SpriteBatch batch, TextureRegion texture_from, TextureRegion texture_to) {
        batch.begin();
        batch.draw(texture_from, 0, 0);
        batch.draw(texture_to, 0, yTo);
        batch.end();
    }


}
