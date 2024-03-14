package net.mslivo.core.engine.tools.rendering.transitions.transitions;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import net.mslivo.core.engine.tools.rendering.transitions.TRANSITION_RENDER_MODE;
import net.mslivo.core.engine.tools.rendering.transitions.Transition;

public class ZoomInTransition implements Transition {
    private float zoom, zoomAcc;
    private int screenWidth;
    private int screenHeight;
    @Override
    public TRANSITION_RENDER_MODE init(int screenWidth, int screenHeight) {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        this.zoom = 0f;
        this.zoomAcc = 0.02f;
        return TRANSITION_RENDER_MODE.TO_FIRST;
    }

    @Override
    public boolean update() {
        this.zoom += zoomAcc;
        zoomAcc += 0.001f;
        return this.zoom > 1f;
    }

    @Override
    public void renderFrom(SpriteBatch batch, TextureRegion texture_from) {
        batch.setColor(1f,1f,1f,1f-zoom);
        batch.draw(texture_from, -screenWidth*(zoom/2f),-screenHeight*(zoom/2f),screenWidth*(zoom+1), screenHeight*(zoom+1));
        batch.setColor(Color.WHITE);
    }

    @Override
    public void renderTo(SpriteBatch batch, TextureRegion texture_to) {
        batch.draw(texture_to, 0, 0);
    }

}
