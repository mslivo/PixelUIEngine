package org.mslivo.core.engine.media_manager;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.TextureData;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.utils.Align;
import org.mslivo.core.engine.media_manager.media.*;
import org.mslivo.core.engine.tools.Tools;
import org.mslivo.core.engine.ui_engine.UIBaseMedia;
import org.mslivo.core.engine.ui_engine.render.UISpriteBatch;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;

/**
 * Created by Admin on 07.02.2019.
 */
public class MediaManager {
    public static final String DIR_MUSIC = "music/", DIR_GRAPHICS = "sprites/", DIR_SOUND_FX = "sound/";
    private static final GlyphLayout glyphLayout = new GlyphLayout();
    private static final int DEFAULT_PAGE_WIDTH = 4096;
    private static final int DEFAULT_PAGE_HEIGHT = 4096;
    private boolean loaded;
    private Sound[] medias_sounds = null;
    private Music[] medias_music = null;
    private TextureRegion[] medias_images = null;
    private TextureRegion[] medias_cursors = null;
    private BitmapFont[] medias_fonts = null;
    private TextureRegion[][] medias_arrays = null;
    private Animation[] medias_animations = null;
    private final ArrayDeque<CMedia> loadMediaList = new ArrayDeque<>();
    private ArrayList<CMedia> loadedMediaList = new ArrayList<>();
    private TextureAtlas textureAtlas;

    public MediaManager() {
        unloadAndReset();
    }


    /* ----- Prepare ----- */
    public boolean prepareUICMedia() {
        return prepareCMedia(UIBaseMedia.ALL);
    }

    public boolean prepareCMedia(CMedia cMedia) {
        if (loaded) return false;
        loadMediaList.add(cMedia);
        return true;
    }

    public boolean prepareCMedia(CMedia[] cMedias) {
        if (loaded) return false;
        for (int i = 0; i < cMedias.length; i++) loadMediaList.add(cMedias[i]);
        return true;
    }

    /* ----- Load ---- */

    public boolean loadAssets() {
        return loadAssets(DEFAULT_PAGE_WIDTH, DEFAULT_PAGE_HEIGHT, null, Texture.TextureFilter.Nearest);
    }

    public boolean loadAssets(LoadProgress progress) {
        return loadAssets(DEFAULT_PAGE_WIDTH, DEFAULT_PAGE_HEIGHT, progress, Texture.TextureFilter.Nearest);
    }

    public boolean loadAssets(LoadProgress progress, Texture.TextureFilter textureFilter) {
        return loadAssets(DEFAULT_PAGE_WIDTH, DEFAULT_PAGE_HEIGHT, progress, textureFilter);
    }

    public boolean loadAssets(int pageWidth, int pageHeight) {
        return loadAssets(pageWidth, pageHeight, null, Texture.TextureFilter.Nearest);
    }

    private static final String ERROR_ALREADY_LOADED_OTHER = "CMedia File \"%s\": Already loaded in a MediaManager";
    private static final String ERROR_DUPLICATE = "CMedia File \"%s\": Duplicate file detected";
    private static final String ERROR_UNKNOWN_FORMAT = "CMedia File \"%s\": unknown format: %s";

    public boolean loadAssets(int pageWidth, int pageHeight, LoadProgress loadProgress, Texture.TextureFilter textureFilter) {
        if (loaded) return false;
        PixmapPacker pixmapPacker = new PixmapPacker(pageWidth, pageHeight, Pixmap.Format.RGBA8888, 2, true);
        ArrayList<CMedia> imageCMediaLoadStack = new ArrayList<>();
        ArrayList<CMedia> soundCMediaLoadStack = new ArrayList<>();
        HashSet<CMedia> duplicateCheck = new HashSet<>();
        int step = 0;
        int stepsMax = 0;

        // split into Image and Sound data, skip duplicates, check format and index
        CMedia loadMedia;
        int imagesMax = 0, cursorMax = 0, arraysMax = 0, animationsMax = 0, fontsMax = 0, soundMax = 0, musicMax = 0;
        int imagesIdx = 0, cursorIdx = 0, arraysIdx = 0, animationsIdx = 0, fontsIdx = 0, soundIdx = 0, musicIdx = 0;;
        while ((loadMedia = loadMediaList.poll()) != null) {
            if (duplicateCheck.contains(loadMedia))
                throw new RuntimeException(String.format(ERROR_DUPLICATE, loadMedia.file));
            if (loadMedia.mediaManagerIndex != CMedia.MEDIAMANGER_INDEX_NONE)
                throw new RuntimeException(String.format(ERROR_ALREADY_LOADED_OTHER, loadMedia.file));
            if (loadMedia instanceof CMediaGFX || loadMedia.getClass() == CMediaFont.class) {
                imageCMediaLoadStack.add(loadMedia);
            } else if (loadMedia.getClass() == CMediaSound.class || loadMedia.getClass() == CMediaMusic.class) {
                soundCMediaLoadStack.add(loadMedia);
            } else {
                throw new RuntimeException(String.format(ERROR_UNKNOWN_FORMAT, loadMedia.file, loadMedia.getClass().getSimpleName()));
            }
            switch (loadMedia){
                case CMediaImage cMediaImage -> imagesMax++;
                case CMediaCursor cMediaCursor -> cursorMax++;
                case CMediaArray cMediaArray -> arraysMax++;
                case CMediaAnimation cMediaAnimation -> animationsMax++;
                case CMediaFont cMediaFont -> fontsMax++;
                case CMediaSound cMediaSound -> soundMax++;
                case CMediaMusic cMediaMusic -> musicMax++;
                default -> {}
            }
            duplicateCheck.add(loadMedia);
            stepsMax++;
        }
        medias_images = new TextureRegion[imagesMax];
        medias_cursors = new TextureRegion[cursorMax];
        medias_arrays = new TextureRegion[arraysMax][];
        medias_animations = new Animation[animationsMax];
        medias_fonts = new BitmapFont[fontsMax];
        medias_sounds = new Sound[soundMax];
        medias_music = new Music[musicMax];
        duplicateCheck.clear();

        // 2. Load Image Data Into Pixmap Packer
        for (int i = 0; i < imageCMediaLoadStack.size(); i++) {
            CMedia imageMedia = imageCMediaLoadStack.get(i);
            String textureFileName = imageMedia.getClass() == CMediaFont.class ? imageMedia.file.replace(".fnt", ".png") : imageMedia.file;
            TextureData textureData = TextureData.Factory.loadFromFile(Tools.File.findResource(textureFileName), null, false);
            textureData.prepare();
            pixmapPacker.pack(imageMedia.file, textureData.consumePixmap());
            textureData.disposePixmap();
            step++;
            if (loadProgress != null) loadProgress.onLoadStep(imageMedia.file, step, stepsMax);
        }

        // 4. Create TextureAtlas
        this.textureAtlas = new TextureAtlas();
        pixmapPacker.updateTextureAtlas(textureAtlas, textureFilter, textureFilter, false);

        // 5. Fill arrays with TextureAtlas Data
        for (int i = 0; i < imageCMediaLoadStack.size(); i++) {
            CMedia imageMedia = imageCMediaLoadStack.get(i);
            switch (imageMedia) {
                case CMediaImage cMediaImage -> {
                    cMediaImage.mediaManagerIndex = imagesIdx;
                    medias_images[imagesIdx++] = textureAtlas.findRegion(cMediaImage.file);
                }
                case CMediaCursor cMediaCursor -> {
                    cMediaCursor.mediaManagerIndex = cursorIdx;
                    medias_cursors[cursorIdx++] = textureAtlas.findRegion(cMediaCursor.file);
                }
                case CMediaArray cMediaArray -> {
                    cMediaArray.mediaManagerIndex = arraysIdx;
                    medias_arrays[arraysIdx++] = splitFrames(cMediaArray.file, cMediaArray.tile_width, cMediaArray.tile_height,
                            cMediaArray.frameOffset, cMediaArray.frameLength);
                }
                case CMediaAnimation cMediaAnimation -> {
                    cMediaAnimation.mediaManagerIndex = animationsIdx;
                    medias_animations[animationsIdx++] = new Animation<>(cMediaAnimation.animation_speed,
                            splitFrames(cMediaAnimation.file, cMediaAnimation.tile_width, cMediaAnimation.tile_height, cMediaAnimation.frameOffset, cMediaAnimation.frameLength)
                    );
                }
                case CMediaFont cMediaFont -> {
                    cMediaFont.mediaManagerIndex = fontsIdx;
                    medias_fonts[fontsIdx++] = new BitmapFont(Tools.File.findResource(cMediaFont.file), textureAtlas.findRegion(cMediaFont.file));
                }
                default -> throw new IllegalStateException("Unexpected value: " + imageMedia);
            }
            loadedMediaList.add(imageMedia);
        }
        pixmapPacker.dispose();
        imageCMediaLoadStack.clear();

        // 6. Fill arrays with Sound Data
        for (int i = 0; i < soundCMediaLoadStack.size(); i++) {
            CMedia soundMedia = soundCMediaLoadStack.get(i);
            switch (soundMedia) {
                case CMediaSound cMediaSound -> {
                    cMediaSound.mediaManagerIndex = soundIdx;
                    medias_sounds[soundIdx++] = Gdx.audio.newSound(Tools.File.findResource(cMediaSound.file));
                }
                case CMediaMusic cMediaMusic -> {
                    cMediaMusic.mediaManagerIndex = musicIdx;
                    medias_music[musicIdx++] = Gdx.audio.newMusic(Tools.File.findResource(soundMedia.file));
                }
                default -> throw new IllegalStateException("Unexpected value: " + soundMedia);
            }
            loadedMediaList.add(soundMedia);
            step++;
            if (loadProgress != null) loadProgress.onLoadStep(soundMedia.file, step, stepsMax);
        }
        soundCMediaLoadStack.clear();

        // 5. Finished
        this.loaded = true;
        return true;
    }

    private TextureRegion[] splitFrames(String file, int tile_width, int tile_height, int frameOffset, int frameLength) {
        TextureRegion textureRegion = textureAtlas.findRegion(file);
        int width = (textureRegion.getRegionWidth() / tile_width);
        int height = (textureRegion.getRegionHeight() / tile_height);
        int maxFrames = Tools.Calc.upperBounds(width * height, frameLength);

        int frameCount = maxFrames - frameOffset;
        if (frameCount == 0) return new TextureRegion[]{};
        if (frameCount < 0)
            throw new RuntimeException("Error loading: \"" + file + "\": Negative frameCount = " + frameCount);


        TextureRegion[][] tmp = textureRegion.split(tile_width, tile_height);
        TextureRegion[] result = new TextureRegion[frameCount];
        int allCounter = 0;
        int indexCounter = 0;
        framesLoop:

        for (int ix = 0; ix < tmp.length; ix++) {
            for (int iy = 0; iy < tmp[0].length; iy++) {
                allCounter++;
                if (allCounter > frameOffset) {
                    result[indexCounter] = tmp[ix][iy];
                    indexCounter++;
                }
                if (allCounter >= frameLength) break framesLoop;
            }
        }
        return result;
    }

    private Pixmap extractPixmapFromTextureRegion(TextureRegion textureRegion) {
        TextureData textureData = textureRegion.getTexture().getTextureData();
        if (!textureData.isPrepared()) {
            textureData.prepare();
        }
        Pixmap pixmap = new Pixmap(textureRegion.getRegionWidth(), textureRegion.getRegionHeight(), textureData.getFormat());
        pixmap.drawPixmap(textureData.consumePixmap(), // The other Pixmap
                0, // The target x-coordinate (top left corner)
                0, // The target y-coordinate (top left corner)
                textureRegion.getRegionX(), // The source x-coordinate (top left corner)
                textureRegion.getRegionY(), // The source y-coordinate (top left corner)
                textureRegion.getRegionWidth(), // The width of the area from the other Pixmap in pixels
                textureRegion.getRegionHeight() // The height of the area from the other Pixmap in pixels
        );
        return pixmap;
    }

    /* --- Unload  ---- */
    public boolean unloadAndReset() {
        if (!loaded) return false;
        // Dispose Atlas
        if (textureAtlas != null) this.textureAtlas.dispose();
        textureAtlas = null;

        // Reset mediamanager index
        for (int i = 0; i < loadedMediaList.size(); i++)
            loadedMediaList.get(i).mediaManagerIndex = CMedia.MEDIAMANGER_INDEX_NONE;

        // Dispose and null
        for (int i = 0; i < medias_sounds.length; i++) medias_sounds[i].dispose();
        for (int i = 0; i < medias_music.length; i++) medias_music[i].dispose();
        for (int i = 0; i < medias_fonts.length; i++) medias_fonts[i].dispose();
        this.medias_cursors = null;
        this.medias_images = null;
        this.medias_arrays = null;
        this.medias_animations = null;
        this.medias_sounds = null;
        this.medias_music = null;
        this.medias_fonts = null;

        // Reset lists
        this.loadedMediaList.clear();
        this.loadMediaList.clear();
        this.loaded = false;
        return true;
    }

    /* ----- Statics ---- */

    public static CMediaImage create_CMediaImage(String file) {
        if (file == null || file.trim().length() == 0) throw new RuntimeException(" file missing");
        return new CMediaImage(file);
    }

    public static CMediaCursor create_CMediaCursor(String file, int hotspot_x, int hotspot_y) {
        if (file == null || file.trim().length() == 0) throw new RuntimeException("file missing");
        CMediaCursor cMediaCursor = new CMediaCursor(file);
        cMediaCursor.hotspot_x = hotspot_x;
        cMediaCursor.hotspot_y = hotspot_y;
        return cMediaCursor;
    }

    public static CMediaAnimation create_CMediaAnimation(String file, int tileWidth, int tileHeight, float animation_speed) {
        return create_CMediaAnimation(file, tileWidth, tileHeight, animation_speed, 0, Integer.MAX_VALUE);
    }

    public static CMediaAnimation create_CMediaAnimation(String file, int tileWidth, int tileHeight, float animation_speed, int frameOffset, int frameLength) {
        if (file == null || file.trim().length() == 0) throw new RuntimeException("file missing");
        CMediaAnimation cMediaAnimation = new CMediaAnimation(file);
        cMediaAnimation.tile_width = Tools.Calc.lowerBounds(tileWidth, 1);
        cMediaAnimation.tile_height = Tools.Calc.lowerBounds(tileHeight, 1);
        cMediaAnimation.animation_speed = animation_speed;
        cMediaAnimation.frameOffset = Tools.Calc.lowerBounds(frameOffset, 0);
        cMediaAnimation.frameLength = frameLength;
        return cMediaAnimation;
    }

    public static CMediaFont create_CMediaFont(String file, int offset_x, int offset_y) {
        if (file == null || file.trim().length() == 0) throw new RuntimeException("file missing");
        CMediaFont cMediaFont = new CMediaFont(file);
        cMediaFont.offset_x = offset_x;
        cMediaFont.offset_y = offset_y;
        return cMediaFont;
    }

    public static CMediaMusic create_CMediaMusic(String file) {
        if (file == null || file.trim().length() == 0) throw new RuntimeException("file missing");
        return new CMediaMusic(file);
    }

    public static CMediaSound create_CMediaSound(String file) {
        if (file == null || file.trim().length() == 0) throw new RuntimeException("file missing");
        return new CMediaSound(file);
    }

    public static CMediaArray create_CMediaArray(String file, int tileWidth, int tileHeight) {
        return create_CMediaArray(file, tileWidth, tileHeight, 0, Integer.MAX_VALUE);
    }

    public static CMediaArray create_CMediaArray(String file, int tileWidth, int tileHeight, int frameOffset, int frameLength) {
        if (file == null || file.trim().length() == 0) throw new RuntimeException("file missing");
        CMediaArray cMediaArray = new CMediaArray(file);
        cMediaArray.tile_width = Tools.Calc.lowerBounds(tileWidth, 1);
        cMediaArray.tile_height = Tools.Calc.lowerBounds(tileHeight, 1);
        cMediaArray.frameOffset = Tools.Calc.lowerBounds(frameOffset, 0);
        cMediaArray.frameLength = frameLength;
        return cMediaArray;
    }


    /* -----  CMediaGFX ----- */
    public void drawCMediaGFX(UISpriteBatch batch, CMediaGFX cMedia, float x, float y) {
        drawCMediaGFX(batch, cMedia, x, y, 0, 0);
    }

    public void drawCMediaGFX(UISpriteBatch batch, CMediaGFX cMedia, float x, float y, int arrayIndex, float animationTimer) {
        if (cMedia == null) return;
        switch (cMedia) {
            case CMediaImage cMediaImage -> drawCMediaImage(batch, cMediaImage, x, y);
            case CMediaAnimation cMediaAnimation -> drawCMediaAnimation(batch, cMediaAnimation, x, y, animationTimer);
            case CMediaArray cMediaArray -> drawCMediaArray(batch, cMediaArray, x, y, arrayIndex);
            case CMediaCursor cMediaCursor -> drawCMediaCursor(batch, cMediaCursor, x, y);
            default -> {
            }
        }
    }

    public void drawCMediaGFX(UISpriteBatch batch, CMediaGFX cMedia, float x, float y, float origin_x, float origin_y) {
        drawCMediaGFX(batch, cMedia, x, y, origin_x, origin_y, 0, 0);
    }

    public void drawCMediaGFX(UISpriteBatch batch, CMediaGFX cMedia, float x, float y, float origin_x, float origin_y, int arrayIndex, float animationTimer) {
        if (cMedia == null) return;
        switch (cMedia) {
            case CMediaImage cMediaImage -> drawCMediaImage(batch, cMediaImage, x, y, origin_x, origin_y);
            case CMediaAnimation cMediaAnimation ->
                    drawCMediaAnimation(batch, cMediaAnimation, x, y, animationTimer, origin_x, origin_y);
            case CMediaArray cMediaArray -> drawCMediaArray(batch, cMediaArray, x, y, arrayIndex, origin_x, origin_y);
            case CMediaCursor cMediaCursor -> drawCMediaCursor(batch, cMediaCursor, x, y);
            default -> {
            }
        }
    }

    public void drawCMediaGFX(UISpriteBatch batch, CMediaGFX cMedia, float x, float y, float origin_x, float origin_y, float width, float height) {
        drawCMediaGFX(batch, cMedia, x, y, origin_x, origin_y, width, height, 0, 0);
    }

    public void drawCMediaGFX(UISpriteBatch batch, CMediaGFX cMedia, float x, float y, float origin_x, float origin_y, float width, float height, int arrayIndex, float animationTimer) {
        if (cMedia == null) return;
        switch (cMedia) {
            case CMediaImage cMediaImage ->
                    drawCMediaImage(batch, cMediaImage, x, y, origin_x, origin_y, width, height);
            case CMediaAnimation cMediaAnimation ->
                    drawCMediaAnimation(batch, cMediaAnimation, x, y, animationTimer, origin_x, origin_y, width, height);
            case CMediaArray cMediaArray ->
                    drawCMediaArray(batch, cMediaArray, x, y, arrayIndex, origin_x, origin_y, width, height);
            case CMediaCursor cMediaCursor -> drawCMediaCursor(batch, cMediaCursor, x, y);
            default -> {
            }
        }
    }

    public void drawCMediaGFX(UISpriteBatch batch, CMediaGFX cMedia, float x, float y, float origin_x, float origin_y, float width, float height, float rotation) {
        drawCMediaGFX(batch, cMedia, x, y, origin_x, origin_y, width, height, rotation, 0, 0);
    }

    public void drawCMediaGFX(UISpriteBatch batch, CMediaGFX cMedia, float x, float y, float origin_x, float origin_y, float width, float height, float rotation, float animationTimer, int arrayIndex) {
        if (cMedia == null) return;
        switch (cMedia) {
            case CMediaImage cMediaImage ->
                    drawCMediaImage(batch, cMediaImage, x, y, origin_x, origin_y, width, height, rotation);
            case CMediaAnimation cMediaAnimation ->
                    drawCMediaAnimation(batch, cMediaAnimation, x, y, animationTimer, origin_x, origin_y, width, height, rotation);
            case CMediaArray cMediaArray ->
                    drawCMediaArray(batch, cMediaArray, x, y, arrayIndex, origin_x, origin_y, width, height, rotation);
            case CMediaCursor cMediaCursor -> drawCMediaCursor(batch, cMediaCursor, x, y);
            default -> {
            }
        }
    }

    public void drawCMediaGFXCut(UISpriteBatch batch, CMediaGFX cMedia, float x, float y, int widthCut, int heightCut) {
        drawCMediaGFXCut(batch, cMedia, x, y, widthCut, heightCut, 0, 0);
    }

    public void drawCMediaGFXCut(UISpriteBatch batch, CMediaGFX cMedia, float x, float y, int widthCut, int heightCut, float animationTimer, int arrayIndex) {
        drawCMediaGFXCut(batch, cMedia, x, y, 0, 0, widthCut, heightCut, animationTimer, arrayIndex);
    }

    public void drawCMediaGFXCut(UISpriteBatch batch, CMediaGFX cMedia, float x, float y, int srcX, int srcY, int widthCut, int heightCut) {
        drawCMediaGFXCut(batch, cMedia, x, y, srcX, srcY, widthCut, heightCut, 0, 0);
    }

    public void drawCMediaGFXCut(UISpriteBatch batch, CMediaGFX cMedia, float x, float y, int srcX, int srcY, int widthCut, int heightCut, float animationTimer, int arrayIndex) {
        if (cMedia == null) return;
        switch (cMedia) {
            case CMediaImage cMediaImage ->
                    drawCMediaImageCut(batch, cMediaImage, x, y, srcX, srcY, widthCut, heightCut);
            case CMediaAnimation cMediaAnimation ->
                    drawCMediaAnimationCut(batch, cMediaAnimation, x, y, animationTimer, srcX, srcY, widthCut, heightCut);
            case CMediaArray cMediaArray ->
                    drawCMediaArrayCut(batch, cMediaArray, x, y, arrayIndex, srcX, srcY, widthCut, heightCut);
            case CMediaCursor cMediaCursor -> drawCMediaCursor(batch, cMediaCursor, x, y);
            default -> {
            }
        }
    }

    public void drawCMediaGFXScale(UISpriteBatch batch, CMediaGFX cMedia, float x, float y, float origin_x, float origin_y, float scaleX, float scaleY) {
        drawCMediaGFX(batch, cMedia, x, y, origin_x, origin_y, scaleX, scaleY, 0, 0, 0);
    }

    public void drawCMediaGFXScale(UISpriteBatch batch, CMediaGFX cMedia, float x, float y, float origin_x, float origin_y, float scaleX, float scaleY, float animationTimer, int arrayIndex) {
        drawCMediaGFX(batch, cMedia, x, y, origin_x, origin_y, scaleX, scaleY, 0, animationTimer, arrayIndex);
    }

    public void drawCMediaGFXScale(UISpriteBatch batch, CMediaGFX cMedia, float x, float y, float origin_x, float origin_y, float scaleX, float scaleY, float rotation) {
        drawCMediaGFX(batch, cMedia, x, y, origin_x, origin_y, scaleX, scaleY, rotation, 0, 0);
    }

    public void drawCMediaGFXScale(UISpriteBatch batch, CMediaGFX cMedia, float x, float y, float origin_x, float origin_y, float scaleX, float scaleY, float rotation, float animationTimer, int arrayIndex) {
        if (cMedia == null) return;
        switch (cMedia) {
            case CMediaImage cMediaImage ->
                    drawCMediaImageScale(batch, cMediaImage, x, y, origin_x, origin_y, scaleX, scaleY, rotation);
            case CMediaAnimation cMediaAnimation ->
                    drawCMediaAnimationScale(batch, cMediaAnimation, x, y, animationTimer, origin_x, origin_y, scaleX, scaleY, rotation);
            case CMediaArray cMediaArray ->
                    drawCMediaArrayScale(batch, cMediaArray, x, y, arrayIndex, origin_x, origin_y, scaleX, scaleY, rotation);
            case CMediaCursor cMediaCursor -> drawCMediaCursor(batch, cMediaCursor, x, y);
            default -> {
            }
        }
    }

    public int imageWidth(CMediaGFX cMedia) {
        return imageWidth(cMedia, true);
    }

    public int imageWidth(CMediaGFX cMedia, boolean tileWidth) {
        if (tileWidth) {
            if (cMedia.getClass() == CMediaArray.class) {
                return ((CMediaArray) cMedia).tile_width;
            } else if (cMedia.getClass() == CMediaAnimation.class) {
                return ((CMediaAnimation) cMedia).tile_width;
            }
        }
        return textureAtlas.findRegion(cMedia.file).getRegionWidth();
    }

    public int imageHeight(CMediaGFX cMedia) {
        return imageHeight(cMedia, true);
    }


    public int imageHeight(CMediaGFX cMedia, boolean tileHeight) {
        if (tileHeight) {
            if (cMedia.getClass() == CMediaArray.class) {
                return ((CMediaArray) cMedia).tile_height;
            } else if (cMedia.getClass() == CMediaAnimation.class) {
                return ((CMediaAnimation) cMedia).tile_height;
            }
        }
        return textureAtlas.findRegion(cMedia.file).getRegionHeight();
    }

    /* ----- CMediaCursor ----- */

    public void drawCMediaCursor(UISpriteBatch batch, CMediaCursor cMedia, float x, float y) {
        TextureRegion texture = getCMediaCursor(cMedia);
        batch.draw(texture, x - cMedia.hotspot_x, y - cMedia.hotspot_y, 0, 0, texture.getRegionWidth(), texture.getRegionHeight(), 1, 1, 0);
    }

    public TextureRegion getCMediaCursor(CMediaCursor cMedia) {
        return medias_cursors[cMedia.mediaManagerIndex];
    }


    /* ----- CMediaImage ----- */

    public void drawCMediaImage(UISpriteBatch batch, CMediaImage cMedia, float x, float y) {
        TextureRegion texture = getCMediaImage(cMedia);
        batch.draw(texture, x, y, 0, 0, texture.getRegionWidth(), texture.getRegionHeight(), 1, 1, 0);
    }

    public void drawCMediaImage(UISpriteBatch batch, CMediaImage cMedia, float x, float y, float origin_x, float origin_y) {
        TextureRegion texture = getCMediaImage(cMedia);
        batch.draw(texture, x, y, origin_x, origin_y, texture.getRegionWidth(), texture.getRegionHeight(), 1, 1, 0);
    }

    public void drawCMediaImage(UISpriteBatch batch, CMediaImage cMedia, float x, float y, float origin_x, float origin_y, float width, float height) {
        TextureRegion texture = getCMediaImage(cMedia);
        batch.draw(texture, x, y, origin_x, origin_y, width, height, 1, 1, 0);
    }

    public void drawCMediaImage(UISpriteBatch batch, CMediaImage cMedia, float x, float y, float origin_x, float origin_y, float width, float height, float rotation) {
        TextureRegion texture = getCMediaImage(cMedia);
        batch.draw(texture, x, y, origin_x, origin_y, width, height, 1, 1, rotation);
    }

    public void drawCMediaImageCut(UISpriteBatch batch, CMediaImage cMedia, float x, float y, int widthCut, int heightCut) {
        TextureRegion texture = getCMediaImage(cMedia);
        batch.draw(texture.getTexture(), x, y, texture.getRegionX(), texture.getRegionY(), widthCut, heightCut);
    }

    public void drawCMediaImageCut(UISpriteBatch batch, CMediaImage cMedia, float x, float y, int srcX, int srcY, int widthCut, int heightCut) {
        TextureRegion texture = getCMediaImage(cMedia);
        batch.draw(texture.getTexture(), x, y, texture.getRegionX() + srcX, texture.getRegionY() + srcY, widthCut, heightCut);
    }

    public void drawCMediaImageScale(UISpriteBatch batch, CMediaImage cMedia, float x, float y, float origin_x, float origin_y, float scaleX, float scaleY) {
        TextureRegion texture = getCMediaImage(cMedia);
        batch.draw(texture, x, y, origin_x, origin_y, texture.getRegionWidth(), texture.getRegionHeight(), scaleX, scaleY, 0);
    }

    public void drawCMediaImageScale(UISpriteBatch batch, CMediaImage cMedia, float x, float y, float origin_x, float origin_y, float scaleX, float scaleY, float rotation) {
        TextureRegion texture = getCMediaImage(cMedia);
        batch.draw(texture, x, y, origin_x, origin_y, texture.getRegionWidth(), texture.getRegionHeight(), scaleX, scaleY, rotation);
    }

    public TextureRegion getCMediaImage(CMediaImage cMedia) {
        return medias_images[cMedia.mediaManagerIndex];
    }

    public TextureRegion getCMediaAnimation(CMediaAnimation cMedia, float animationTimer) {
        return (TextureRegion) medias_animations[cMedia.mediaManagerIndex].getKeyFrame(animationTimer, true);
    }

    /* --- CMediaAnimation  --- */

    public void drawCMediaAnimation(UISpriteBatch batch, CMediaAnimation cMedia, float x, float y, float animationTimer) {
        TextureRegion textureRegion = getCMediaAnimation(cMedia, animationTimer);
        batch.draw(textureRegion, x, y, 0, 0, textureRegion.getRegionWidth(), textureRegion.getRegionHeight(), 1, 1, 0);
    }

    public void drawCMediaAnimation(UISpriteBatch batch, CMediaAnimation cMedia, float x, float y, float animationTimer, float origin_x, float origin_y) {
        TextureRegion textureRegion = getCMediaAnimation(cMedia, animationTimer);
        batch.draw(textureRegion, x, y, origin_x, origin_y, textureRegion.getRegionWidth(), textureRegion.getRegionHeight(), 1, 1, 0);
    }

    public void drawCMediaAnimation(UISpriteBatch batch, CMediaAnimation cMedia, float x, float y, float animationTimer, float origin_x, float origin_y, float width, float height) {
        TextureRegion textureRegion = getCMediaAnimation(cMedia, animationTimer);
        batch.draw(textureRegion, x, y, origin_x, origin_y, width, height, 1, 1, 0);
    }

    public void drawCMediaAnimation(UISpriteBatch batch, CMediaAnimation cMedia, float x, float y, float animationTimer, float origin_x, float origin_y, float width, float height, float rotation) {
        TextureRegion textureRegion = getCMediaAnimation(cMedia, animationTimer);
        batch.draw(textureRegion, x, y, origin_x, origin_y, width, height, 1, 1, rotation);
    }

    public void drawCMediaAnimationCut(UISpriteBatch batch, CMediaAnimation cMedia, float x, float y, float animationTimer, int widthCut, int heightCut) {
        drawCMediaAnimationCut(batch, cMedia, x, y, animationTimer, 0, 0, widthCut, heightCut);
    }

    public void drawCMediaAnimationCut(UISpriteBatch batch, CMediaAnimation cMedia, float x, float y, float animationTimer, int srcX, int srcY, int widthCut, int heightCut) {
        TextureRegion textureRegion = getCMediaAnimation(cMedia, animationTimer);
        batch.draw(textureRegion.getTexture(), x, y, textureRegion.getRegionX() + srcX, textureRegion.getRegionY() + srcY, widthCut, heightCut);
    }

    public void drawCMediaAnimationScale(UISpriteBatch batch, CMediaAnimation cMedia, float x, float y, float animationTimer, float origin_x, float origin_y, float scaleX, float scaleY) {
        TextureRegion textureRegion = getCMediaAnimation(cMedia, animationTimer);
        batch.draw(textureRegion, x, y, origin_x, origin_y, textureRegion.getRegionWidth(), textureRegion.getRegionHeight(), scaleX, scaleY, 0);
    }

    public void drawCMediaAnimationScale(UISpriteBatch batch, CMediaAnimation cMedia, float x, float y, float animationTimer, float origin_x, float origin_y, float scaleX, float scaleY, float rotation) {
        TextureRegion textureRegion = getCMediaAnimation(cMedia, animationTimer);
        batch.draw(textureRegion, x, y, origin_x, origin_y, textureRegion.getRegionWidth(), textureRegion.getRegionHeight(), scaleX, scaleY, rotation);
    }

    public boolean isCMediaAnimationFinished(CMediaAnimation cMedia, float animationTimer) {
        return medias_animations[cMedia.mediaManagerIndex].isAnimationFinished(animationTimer);
    }

    public int getCMediaAnimationKeyFrameIndex(CMediaAnimation cMedia, float animationTimer) {
        return medias_animations[cMedia.mediaManagerIndex].getKeyFrameIndex(animationTimer);
    }

    /* --- CMediaArray  --- */

    public void drawCMediaArray(UISpriteBatch batch, CMediaArray cMedia, float x, float y, int arrayIndex) {
        TextureRegion textureRegion = getCMediaArray(cMedia, arrayIndex);
        batch.draw(textureRegion, x, y, 0, 0, textureRegion.getRegionWidth(), textureRegion.getRegionHeight(), 1, 1, 0);
    }

    public void drawCMediaArray(UISpriteBatch batch, CMediaArray cMedia, float x, float y, int arrayIndex, float origin_x, float origin_y) {
        TextureRegion textureRegion = getCMediaArray(cMedia, arrayIndex);
        batch.draw(textureRegion, x, y, origin_x, origin_y, textureRegion.getRegionWidth(), textureRegion.getRegionHeight(), 1, 1, 0);
    }

    public void drawCMediaArray(UISpriteBatch batch, CMediaArray cMedia, float x, float y, int arrayIndex, float origin_x, float origin_y, float width, float height) {
        TextureRegion textureRegion = getCMediaArray(cMedia, arrayIndex);
        batch.draw(textureRegion, x, y, origin_x, origin_y, width, height, 1, 1, 0);
    }

    public void drawCMediaArray(UISpriteBatch batch, CMediaArray cMedia, float x, float y, int arrayIndex, float origin_x, float origin_y, float width, float height, float rotation) {
        TextureRegion textureRegion = getCMediaArray(cMedia, arrayIndex);
        batch.draw(textureRegion, x, y, origin_x, origin_y, width, height, 1, 1, rotation);
    }

    public void drawCMediaArrayCut(UISpriteBatch batch, CMediaArray cMedia, float x, float y, int arrayIndex, int widthCut, int heightCut) {
        drawCMediaArrayCut(batch, cMedia, x, y, arrayIndex, 0, 0, widthCut, heightCut);
    }

    public void drawCMediaArrayCut(UISpriteBatch batch, CMediaArray cMedia, float x, float y, int arrayIndex, int srcX, int srcY, int widthCut, int heightCut) {
        TextureRegion textureRegion = getCMediaArray(cMedia, arrayIndex);
        batch.draw(textureRegion.getTexture(), x, y, textureRegion.getRegionX() + srcX, textureRegion.getRegionY() + srcY, widthCut, heightCut);
    }

    public void drawCMediaArrayScale(UISpriteBatch batch, CMediaArray cMedia, float x, float y, int arrayIndex, float origin_x, float origin_y, float scaleX, float scaleY) {
        TextureRegion textureRegion = getCMediaArray(cMedia, arrayIndex);
        batch.draw(textureRegion, x, y, origin_x, origin_y, textureRegion.getRegionWidth(), textureRegion.getRegionHeight(), scaleX, scaleY, 0);
    }

    public void drawCMediaArrayScale(UISpriteBatch batch, CMediaArray cMedia, float x, float y, int arrayIndex, float origin_x, float origin_y, float scaleX, float scaleY, float rotation) {
        TextureRegion textureRegion = getCMediaArray(cMedia, arrayIndex);
        batch.draw(textureRegion, x, y, origin_x, origin_y, textureRegion.getRegionWidth(), textureRegion.getRegionHeight(), scaleX, scaleY, rotation);
    }

    public int getCMediaArraySize(CMediaArray cMedia) {
        return medias_arrays[cMedia.mediaManagerIndex].length;
    }

    public TextureRegion getCMediaArray(CMediaArray cMedia, int arrayIndex) {
        return medias_arrays[cMedia.mediaManagerIndex][arrayIndex];
    }
    /* --- CMediaFont  --- */

    public void drawCMediaFont(UISpriteBatch batch, CMediaFont cMedia, float x, float y, String text) {
        BitmapFont bitmapFont = getCMediaFont(cMedia);
        bitmapFont.draw(batch, text, (x + cMedia.offset_x), (y + cMedia.offset_y));
    }

    public void drawCMediaFont(UISpriteBatch batch, CMediaFont cMedia, float x, float y, String text, int maxWidth) {
        BitmapFont bitmapFont = getCMediaFont(cMedia);
        bitmapFont.draw(batch, text, (x + cMedia.offset_x), (y + cMedia.offset_y), 0, text.length(), maxWidth, Align.left, false, "");
    }

    public void setCMediaFontColorWhite(CMediaFont font) {
        setCMediaFontColor(font, 1, 1, 1, 1);
    }

    public void setCMediaFontColor(CMediaFont font, Color color) {
        setCMediaFontColor(font, color.r, color.g, color.b, color.a);
    }

    public void setCMediaFontColor(CMediaFont font, float r, float g, float b, float a) {
        getCMediaFont(font).setColor(r, g, b, a);
    }

    public BitmapFont getCMediaFont(CMediaFont cMedia) {
        return medias_fonts[cMedia.mediaManagerIndex];
    }

    public int textWidth(CMediaFont font, String text) {
        glyphLayout.setText(getCMediaFont(font), text);
        return (int) glyphLayout.width;
    }

    public int textHeight(CMediaFont font, String text) {
        glyphLayout.setText(getCMediaFont(font), text);
        return (int) glyphLayout.height;
    }

    /* ----- CMediaSound ----- */

    public long playCMediaSound(CMediaSound cMedia) {
        return getCMediaSound(cMedia).play(1, 1, 0);
    }

    public long playCMediaSound(CMediaSound cMedia, float volume) {
        return getCMediaSound(cMedia).play(volume, 1, 0);
    }

    public long playCMediaSound(CMediaSound cMedia, float volume, float pan) {
        return getCMediaSound(cMedia).play(volume, 1, pan);
    }

    public long playCMediaSound(CMediaSound cMedia, float volume, float pitch, float pan) {
        return getCMediaSound(cMedia).play(volume, pitch, pan);
    }

    public long loopCMediaSound(CMediaSound cMediaSound) {
        return loopCMediaSound(cMediaSound, 1, 1, 0);
    }

    public long loopCMediaSound(CMediaSound cMediaSound, float volume) {
        return loopCMediaSound(cMediaSound, volume, 1, 0);
    }

    public long loopCMediaSound(CMediaSound cMediaSound, float volume, float pitch) {
        return loopCMediaSound(cMediaSound, volume, pitch, 0);
    }

    public long loopCMediaSound(CMediaSound cMediaSound, float volume, float pitch, float pan) {
        return getCMediaSound(cMediaSound).loop(volume, pitch, pan);
    }

    public Sound getCMediaSound(CMediaSound cMedia) {
        return medias_sounds[cMedia.mediaManagerIndex];
    }

    public Music getCMediaMusic(CMediaMusic cMedia) {
        return medias_music[cMedia.mediaManagerIndex];
    }


    /* ---- Shutdown ---- */
    public void shutdown() {
        this.unloadAndReset();
        return;
    }

    public boolean isLoaded() {
        return loaded;
    }
}
