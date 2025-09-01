package net.mslivo.core.engine.tools.transitions.transitions;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import net.mslivo.core.engine.tools.Tools;
import net.mslivo.core.engine.tools.transitions.TRANSITION_RENDER_MODE;
import net.mslivo.core.engine.tools.transitions.TRANSITION_SPEED;
import net.mslivo.core.engine.tools.transitions.Transition;
import net.mslivo.core.engine.ui_engine.rendering.ShaderParser;
import net.mslivo.core.engine.ui_engine.rendering.renderer.SpriteRenderer;

public class PixelateTransition extends Transition {
    private float fadeOut;
    private float fadeIn;

    public PixelateTransition() {
        super();
    }

    public PixelateTransition(TRANSITION_SPEED transitionSpeed) {
        super(transitionSpeed);
    }

    @Override
    public TRANSITION_RENDER_MODE getRenderMode() {
        return TRANSITION_RENDER_MODE.FROM_FIRST;
    }

    private ShaderProgram pixelationShader = ShaderParser.parse(Tools.File.findResource("shaders/pixelui/pixelation.sprite.glsl"));

    @Override
    public void init(SpriteRenderer spriteRenderer, int screenWidth, int screenHeight) {
        this.fadeOut = 0f;
        this.fadeIn = 0f;
        spriteRenderer.setShader(pixelationShader);
    }

    @Override
    public boolean update() {
        if (this.fadeOut < 1f) {
            this.fadeOut = Math.min(fadeOut + 0.02f, 1f);
            return false;
        } else if (this.fadeIn < 1f) {
            this.fadeIn = Math.min(fadeIn + 0.02f, 1f);
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void renderFrom(SpriteRenderer spriteRenderer, TextureRegion texture_from) {
        if (this.fadeOut < 1f) {
            spriteRenderer.setTweak(fadeOut,0f,0f,0f);
            spriteRenderer.draw(texture_from, 0, 0);
            spriteRenderer.setAllReset();
        }
    }

    @Override
    public void renderTo(SpriteRenderer spriteRenderer, TextureRegion texture_to) {
        if (this.fadeOut >= 1 && this.fadeIn <= 1f) {
            float color = Math.clamp(fadeIn * 0.5f, 0f, 1f);
            spriteRenderer.setTweak(1f-fadeIn,0f,0f,0f);
            spriteRenderer.draw(texture_to, 0, 0);
            spriteRenderer.setAllReset();
        }

    }

    @Override
    public void finished(SpriteRenderer spriteRenderer) {
        spriteRenderer.setShader(null);
    }

}
