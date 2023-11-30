package org.mslivo.core.engine.tools.render.transitions;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;

public class ZoomOutTransition implements Transition {
    private float zoom, zoomAcc;
    private int screenWidth;
    private int screenHeight;

    @Override
    public TRANSITION_MODE init(int screenWidth, int screenHeight) {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        this.zoom = 1f;
        this.zoomAcc = 0.02f;
        return TRANSITION_MODE.TO_FIRST;
    }

    @Override
    public boolean update() {
        this.zoom -= zoomAcc;
        zoomAcc += 0.001f;
        return this.zoom < 0f;
    }

    @Override
    public void renderFrom(SpriteBatch batch, TextureRegion texture_from) {
        if(zoom > 0.1f) {
            batch.draw(texture_from,
                    MathUtils.round(screenWidth * (1f - zoom) * 0.5f),
                    MathUtils.round(screenHeight * (1f - zoom) * 0.5f),
                    MathUtils.round(screenWidth * zoom),
                    MathUtils.round(screenHeight * zoom));
        }
    }

    @Override
    public void renderTo(SpriteBatch batch, TextureRegion texture_to) {
        batch.setColor(Color.WHITE);
        batch.draw(texture_to, 0, 0);
    }

}
