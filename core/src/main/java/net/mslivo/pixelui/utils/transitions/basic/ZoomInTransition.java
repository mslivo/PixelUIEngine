package net.mslivo.pixelui.utils.transitions.basic;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import net.mslivo.pixelui.utils.transitions.TRANSITION_RENDER_MODE;
import net.mslivo.pixelui.utils.transitions.TRANSITION_SPEED;
import net.mslivo.pixelui.utils.transitions.Transition;
import net.mslivo.pixelui.rendering.SpriteRenderer;

public class ZoomInTransition extends Transition {
    private float zoom, zoomAcc;
    private int screenWidth;
    private int screenHeight;

    public ZoomInTransition() {
        super();
    }

    public ZoomInTransition(TRANSITION_SPEED transitionSpeed) {
        super(transitionSpeed);
    }

    @Override
    public TRANSITION_RENDER_MODE getRenderMode() {
        return TRANSITION_RENDER_MODE.TO_FIRST;
    }

    @Override
    public void init(SpriteRenderer spriteRenderer, int screenWidth, int screenHeight) {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        this.zoom = 0f;
        this.zoomAcc = 0.02f;
    }

    @Override
    public boolean update() {
        this.zoom += zoomAcc;
        return this.zoom > 1f;
    }

    @Override
    public void renderFrom(SpriteRenderer spriteRenderer, TextureRegion texture_from) {
        spriteRenderer.setColor(0.5f,0.5f,0.5f,1f-zoom);
        spriteRenderer.draw(texture_from, -screenWidth*(zoom/2f),-screenHeight*(zoom/2f),screenWidth*(zoom+1), screenHeight*(zoom+1));
        spriteRenderer.setColor(Color.GRAY);
    }

    @Override
    public void renderTo(SpriteRenderer spriteRenderer, TextureRegion texture_to) {
        spriteRenderer.draw(texture_to, 0, 0);
    }

    @Override
    public void finished(SpriteRenderer spriteRenderer) {

    }

}
