package net.mslivo.core.engine.tools.transitions;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public interface Transition {

    TRANSITION_RENDER_MODE init(int screenWidth, int screenHeight);

    boolean update();

    void renderFrom(SpriteBatch batch, TextureRegion texture_from);
    void renderTo(SpriteBatch batch, TextureRegion texture_to);

}
