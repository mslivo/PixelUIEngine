package net.mslivo.core.engine.tools.transitions;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import net.mslivo.core.engine.ui_engine.rendering.SpriteRenderer;

public interface Transition {
    default TRANSITION_RENDER_MODE getRenderMode(){
        return TRANSITION_RENDER_MODE.FROM_FIRST;
    };
    void init(int screenWidth, int screenHeight);
    boolean update();
    void renderFrom(SpriteRenderer spriteRenderer, TextureRegion texture_from);
    void renderTo(SpriteRenderer spriteRenderer, TextureRegion texture_to);
    void shutdown();
}
