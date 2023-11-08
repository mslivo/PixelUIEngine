package org.mslivo.core.engine.tools.transitions;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import org.mslivo.core.engine.ui_engine.UIEngine;

import java.io.File;

public class TransitionManager {

    /* ##### From ##### */
    private UIEngine from;
    private FrameBuffer frameBuffer_from;
    private TextureRegion texture_from;

    /* ##### To ##### */
    private UIEngine to;
    private FrameBuffer frameBuffer_to;
    private TextureRegion texture_to;

    /* ##### Screen ##### */
    private SpriteBatch batch_screen;
    private Viewport viewport_screen;
    private OrthographicCamera camera_screen;

    /* ##### Other ##### */
    private float transition;

    private boolean initialized;

    private int width, height;

    private Texture test = new Texture(new FileHandle("E:\\Code\\PixelUIEngine\\desktop\\assets_example\\sprites\\example\\example_icon_double.png"));

    public TransitionManager() {
        this.initialized = false;
    }

    public void init(UIEngine from, UIEngine to) {
        this.from = from;
        this.to = to;
        width = Gdx.graphics.getWidth();
        height = Gdx.graphics.getHeight();
        // From
        if (frameBuffer_from != null) frameBuffer_to.dispose();
        frameBuffer_from = new FrameBuffer(Pixmap.Format.RGBA8888, width, height, false);
        frameBuffer_from.getColorBufferTexture().setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        texture_from = new TextureRegion(frameBuffer_from.getColorBufferTexture());
        texture_from.flip(false, true);
        // To
        if (frameBuffer_to != null) frameBuffer_to.dispose();
        frameBuffer_to = new FrameBuffer(Pixmap.Format.RGBA8888, width, height, false);
        frameBuffer_to.getColorBufferTexture().setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        texture_to = new TextureRegion(frameBuffer_to.getColorBufferTexture());
        texture_to.flip(false, true);
        // Screen
        if (this.batch_screen != null) batch_screen.dispose();
        batch_screen = new SpriteBatch(8191);
        camera_screen = new OrthographicCamera(width, height);
        camera_screen.setToOrtho(false);
        //camera_screen.position.set(width /2, height / 2, 1f);
        camera_screen.update();
        viewport_screen = new StretchViewport(width, height, camera_screen);
        viewport_screen.update(width, height,true);


        // Capture Buffers
        frameBuffer_from.begin();
        from.render();
        Pixmap pixmap = Pixmap.createFromFrameBuffer(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        frameBuffer_from.end();


        //PixmapIO.writePNG(new FileHandle("E:\\fromTest.png"), pixmap);

        frameBuffer_to.begin();
        to.render();
        frameBuffer_to.end();


        this.transition = 0;
        this.initialized = true;
    }

    public boolean update() {
        if (!initialized) return true;
        if (transition >= 1f) return true;

        transition += 0.01f;


        return false;
    }

    public void render() {
        if (!initialized) return;
       // Gdx.gl.glClearColor(0, 0, 0, 0f);
        Gdx.gl.glClear(GL30.GL_COLOR_BUFFER_BIT);

        // Render Screen
        {
            batch_screen.setProjectionMatrix(camera_screen.combined);
            viewport_screen.apply();
            batch_screen.begin();
            batch_screen.setColor(1, 1, 1, 1f);


            // doesnt display anything
            for(int ix=-2000;ix<2000;ix+=50){
                for(int iy=-2000;iy<2000;iy+=50){
                    batch_screen.draw(texture_from, ix, iy);
                }
            }

            batch_screen.end();
        }
    }

    public void shutdown() {

    }

}
