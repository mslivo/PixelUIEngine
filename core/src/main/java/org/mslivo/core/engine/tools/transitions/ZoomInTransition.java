package org.mslivo.core.engine.tools.transitions;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import org.mslivo.core.engine.tools.Tools;

public class ZoomInTransition implements Transition {
    private float zoom;
    private int screenWidth;
    private int screenHeight;
    @Override
    public void init(int screenWidth, int screenHeight) {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        this.zoom = 0f;
    }

    @Override
    public boolean update() {
        this.zoom += 0.05f;
        return this.zoom > 1f;
    }

    @Override
    public void render(SpriteBatch batch, TextureRegion texture_from, TextureRegion texture_to) {
        batch.begin();

        batch.setColor(Color.WHITE);
        batch.draw(texture_to, 0, 0);

        batch.setColor(1f,1f,1f,1f-zoom);
        batch.draw(texture_from, -screenWidth*(zoom/2f),-screenHeight*(zoom/2f),screenWidth*(zoom+1), screenHeight*(zoom+1));
        /*
        int x = -MathUtils.round(screenWidth*zoom);
        int y = -MathUtils.round(screenHeight*zoom);
        batch.draw(texture_from, x, y,screenWidth*zoom*2,screenHeight*zoom*2);
*/


        batch.end();
    }


}
