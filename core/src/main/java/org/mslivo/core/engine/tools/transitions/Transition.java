package org.mslivo.core.engine.tools.transitions;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

interface Transition {

    void init(int screenWidth, int screenHeight);

    boolean update();

    void render(SpriteBatch batch, TextureRegion texture_from, TextureRegion texture_to);
}
