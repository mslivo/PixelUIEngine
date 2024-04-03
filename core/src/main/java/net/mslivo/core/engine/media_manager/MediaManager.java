package net.mslivo.core.engine.media_manager;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.loaders.ModelLoader;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.TextureData;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.loader.G3dModelLoader;
import com.badlogic.gdx.graphics.g3d.loader.ObjLoader;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.JsonReader;
import net.mslivo.core.engine.media_manager.media.*;
import net.mslivo.core.engine.tools.Tools;
import net.mslivo.core.engine.ui_engine.UIBaseMedia;
import net.mslivo.core.engine.ui_engine.render.SpriteRenderer;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;

/**
 * Created by Admin on 07.02.2019.
 */
public class MediaManager {
    public static final String DIR_MUSIC = "music/", DIR_GRAPHICS = "sprites/", DIR_SOUND = "sound/",  DIR_MODELS = "models/";
    private static final String ERROR_FILE_MISSING = "file missing";
    private static final String ERROR_ALREADY_LOADED_OTHER = "CMedia File \"%s\": Already loaded in another MediaManager";
    private static final String ERROR_DUPLICATE = "CMedia File \"%s\": Duplicate file detected";
    private static final String ERROR_UNKNOWN_FORMAT = "CMedia File \"%s\": class \"%s\" not supported";
    private static final String ERROR_UNKNOWN_3D_FORMAT = "CMedia File \"%s\": 3D model format not supported";
    private static final GlyphLayout glyphLayout = new GlyphLayout();
    private static final int DEFAULT_PAGE_WIDTH = 4096;
    private static final int DEFAULT_PAGE_HEIGHT = 4096;
    private boolean loaded = false;
    private Sound[] medias_sounds = null;
    private Music[] medias_music = null;
    private TextureRegion[] medias_images = null;
    private TextureRegion[] medias_cursors = null;
    private BitmapFont[] medias_fonts = null;
    private TextureRegion[][] medias_arrays = null;
    private Animation[] medias_animations = null;
    private final ArrayDeque<CMedia> loadMediaList = new ArrayDeque<>();
    private ArrayList<CMedia> loadedMediaList = new ArrayList<>();
    private TextureAtlas textureAtlas = null;
    private ObjLoader objLoader = null;
    private G3dModelLoader g3dLoader = null;
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
        int imagesIdx = 0, cursorIdx = 0, arraysIdx = 0, animationsIdx = 0, fontsIdx = 0, soundIdx = 0, musicIdx = 0;
        while ((loadMedia = loadMediaList.poll()) != null) {
            if (duplicateCheck.contains(loadMedia))
                throw new RuntimeException(String.format(ERROR_DUPLICATE, loadMedia.file));
            if (loadMedia.mediaManagerIndex != CMedia.MEDIAMANGER_INDEX_NONE)
                throw new RuntimeException(String.format(ERROR_ALREADY_LOADED_OTHER, loadMedia.file));
            if (loadMedia instanceof CMediaSprite || loadMedia.getClass() == CMediaFont.class) {
                imageCMediaLoadStack.add(loadMedia);
            } else if (loadMedia.getClass() == CMediaSound.class || loadMedia.getClass() == CMediaMusic.class) {
                soundCMediaLoadStack.add(loadMedia);
            } else {
                throw new RuntimeException(String.format(ERROR_UNKNOWN_FORMAT, loadMedia.file, loadMedia.getClass().getSimpleName()));
            }
            switch (loadMedia) {
                case CMediaImage cMediaImage -> imagesMax++;
                case CMediaCursor cMediaCursor -> cursorMax++;
                case CMediaArray cMediaArray -> arraysMax++;
                case CMediaAnimation cMediaAnimation -> animationsMax++;
                case CMediaFont cMediaFont -> fontsMax++;
                case CMediaSound cMediaSound -> soundMax++;
                case CMediaMusic cMediaMusic -> musicMax++;
                default -> {
                }
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

        // 5. Fill CMedia Arrays with TextureAtlas Data
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

        // 6. Fill CMedia Arrays with Sound Data
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

        // 7. Finished
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

    public static CMediaImage create_CMediaImage(String file) {
        if (file == null || file.trim().length() == 0) throw new RuntimeException(ERROR_FILE_MISSING);
        return new CMediaImage(file);
    }

    public static CMediaCursor create_CMediaCursor(String file, int hotspot_x, int hotspot_y) {
        if (file == null || file.trim().length() == 0) throw new RuntimeException(ERROR_FILE_MISSING);
        CMediaCursor cMediaCursor = new CMediaCursor(file);
        cMediaCursor.hotspot_x = hotspot_x;
        cMediaCursor.hotspot_y = hotspot_y;
        return cMediaCursor;
    }

    public static CMediaAnimation create_CMediaAnimation(String file, int tileWidth, int tileHeight, float animation_speed) {
        return create_CMediaAnimation(file, tileWidth, tileHeight, animation_speed, 0, Integer.MAX_VALUE);
    }

    public static CMediaAnimation create_CMediaAnimation(String file, int tileWidth, int tileHeight, float animation_speed, int frameOffset, int frameLength) {
        if (file == null || file.trim().length() == 0) throw new RuntimeException(ERROR_FILE_MISSING);
        CMediaAnimation cMediaAnimation = new CMediaAnimation(file);
        cMediaAnimation.tile_width = Tools.Calc.lowerBounds(tileWidth, 1);
        cMediaAnimation.tile_height = Tools.Calc.lowerBounds(tileHeight, 1);
        cMediaAnimation.animation_speed = animation_speed;
        cMediaAnimation.frameOffset = Tools.Calc.lowerBounds(frameOffset, 0);
        cMediaAnimation.frameLength = frameLength;
        return cMediaAnimation;
    }

    public static CMediaFont create_CMediaFont(String file, int offset_x, int offset_y) {
        if (file == null || file.trim().length() == 0) throw new RuntimeException(ERROR_FILE_MISSING);
        CMediaFont cMediaFont = new CMediaFont(file);
        cMediaFont.offset_x = offset_x;
        cMediaFont.offset_y = offset_y;
        return cMediaFont;
    }

    public static CMediaMusic create_CMediaMusic(String file) {
        if (file == null || file.trim().length() == 0) throw new RuntimeException(ERROR_FILE_MISSING);
        return new CMediaMusic(file);
    }

    public static CMediaSound create_CMediaSound(String file) {
        if (file == null || file.trim().length() == 0) throw new RuntimeException(ERROR_FILE_MISSING);
        return new CMediaSound(file);
    }

    public static CMediaArray create_CMediaArray(String file, int tileWidth, int tileHeight) {
        return create_CMediaArray(file, tileWidth, tileHeight, 0, Integer.MAX_VALUE);
    }

    public static CMediaArray create_CMediaArray(String file, int tileWidth, int tileHeight, int frameOffset, int frameLength) {
        if (file == null || file.trim().length() == 0) throw new RuntimeException(ERROR_FILE_MISSING);
        CMediaArray cMediaArray = new CMediaArray(file);
        cMediaArray.tile_width = Tools.Calc.lowerBounds(tileWidth, 1);
        cMediaArray.tile_height = Tools.Calc.lowerBounds(tileHeight, 1);
        cMediaArray.frameOffset = Tools.Calc.lowerBounds(frameOffset, 0);
        cMediaArray.frameLength = frameLength;
        return cMediaArray;
    }

    public TextureRegion getCMediaCursor(CMediaCursor cMedia) {
        return medias_cursors[cMedia.mediaManagerIndex];
    }

    public TextureRegion getCMediaImage(CMediaImage cMedia) {
        return medias_images[cMedia.mediaManagerIndex];
    }

    public TextureRegion getCMediaAnimation(CMediaAnimation cMedia, float animationTimer) {
        return (TextureRegion) medias_animations[cMedia.mediaManagerIndex].getKeyFrame(animationTimer, true);
    }

    public TextureRegion getCMediaArray(CMediaArray cMedia, int arrayIndex) {
        return medias_arrays[cMedia.mediaManagerIndex][arrayIndex];
    }

    public Sound getCMediaSound(CMediaSound cMediaSound) {
        return medias_sounds[cMediaSound.mediaManagerIndex];
    }

    public Music getCMediaMusic(CMediaMusic cMediaMusic) {
        return medias_music[cMediaMusic.mediaManagerIndex];
    }


    public boolean isCMediaAnimationFinished(CMediaAnimation cMedia, float animationTimer) {
        return medias_animations[cMedia.mediaManagerIndex].isAnimationFinished(animationTimer);
    }

    public int getCMediaAnimationKeyFrameIndex(CMediaAnimation cMedia, float animationTimer) {
        return medias_animations[cMedia.mediaManagerIndex].getKeyFrameIndex(animationTimer);
    }

    public int imageWidth(CMediaSprite cMedia) {
        return imageWidth(cMedia, true);
    }

    public int imageWidth(CMediaSprite cMedia, boolean tileWidth) {
        if (tileWidth) {
            if (cMedia.getClass() == CMediaArray.class) {
                return ((CMediaArray) cMedia).tile_width;
            } else if (cMedia.getClass() == CMediaAnimation.class) {
                return ((CMediaAnimation) cMedia).tile_width;
            }
        }
        return textureAtlas.findRegion(cMedia.file).getRegionWidth();
    }

    public int imageHeight(CMediaSprite cMedia) {
        return imageHeight(cMedia, true);
    }

    public int imageHeight(CMediaSprite cMedia, boolean tileHeight) {
        if (tileHeight) {
            if (cMedia.getClass() == CMediaArray.class) {
                return ((CMediaArray) cMedia).tile_height;
            } else if (cMedia.getClass() == CMediaAnimation.class) {
                return ((CMediaAnimation) cMedia).tile_height;
            }
        }
        return textureAtlas.findRegion(cMedia.file).getRegionHeight();
    }

    public int getCMediaArraySize(CMediaArray cMedia) {
        return medias_arrays[cMedia.mediaManagerIndex].length;
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

    /* ---- Shutdown ---- */
    public void shutdown() {
        this.unloadAndReset();
        return;
    }

    public boolean isLoaded() {
        return loaded;
    }
}
