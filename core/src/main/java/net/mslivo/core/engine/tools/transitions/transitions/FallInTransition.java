package net.mslivo.core.engine.tools.transitions.transitions;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import net.mslivo.core.engine.tools.transitions.TRANSITION_RENDER_MODE;
import net.mslivo.core.engine.tools.transitions.TRANSITION_SPEED;
import net.mslivo.core.engine.tools.transitions.Transition;
import net.mslivo.core.engine.ui_engine.rendering.renderer.SpriteBasicColorTweakRenderer;

public class FallInTransition extends Transition {
    private float yTo;
    private float ySpeed;
    private int bounce;
    private int screenHeight;
    private Runnable bounceAction;

    public FallInTransition(){
        this(null, null);
    }

    public FallInTransition(TRANSITION_SPEED transitionSpeed){
        this(null, transitionSpeed);
    }
    public FallInTransition(Runnable bounceAction, TRANSITION_SPEED transitionSpeed) {
        super(transitionSpeed);
        this.bounceAction = bounceAction;
    }

    @Override
    public TRANSITION_RENDER_MODE getRenderMode() {
        return TRANSITION_RENDER_MODE.FROM_FIRST;
    }


    @Override
    public void init(SpriteBasicColorTweakRenderer spriteRenderer, int screenWidth, int screenHeight) {
        this.yTo = screenHeight;
        this.ySpeed = 0f;
        this.screenHeight = screenHeight;
        this.bounce = 5;
    }

    @Override
    public boolean update() {

        ySpeed -= screenHeight/1400f;
        yTo += ySpeed;
        if (yTo <= 0) {
            yTo = 0;
            ySpeed = -ySpeed / 2f;
            yTo += ySpeed;
            bounce--;
            if(bounceAction != null) bounceAction.run();
        }
        return bounce <= 0;
    }

    @Override
    public void renderFrom(SpriteBasicColorTweakRenderer spriteRenderer, TextureRegion texture_from) {
        spriteRenderer.setColor(Color.GRAY);
        spriteRenderer.draw(texture_from, 0, 0);
    }

    @Override
    public void renderTo(SpriteBasicColorTweakRenderer spriteRenderer, TextureRegion texture_to) {
        spriteRenderer.setColor(Color.GRAY);
        spriteRenderer.draw(texture_to, 0, MathUtils.round(yTo));
    }

    @Override
    public void finished(SpriteBasicColorTweakRenderer spriteRenderer) {

    }


}
