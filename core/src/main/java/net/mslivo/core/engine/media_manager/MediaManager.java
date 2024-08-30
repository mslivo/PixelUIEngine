package net.mslivo.core.engine.media_manager;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.TextureData;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.g3d.loader.G3dModelLoader;
import com.badlogic.gdx.graphics.g3d.loader.ObjLoader;
import com.badlogic.gdx.utils.Array;
import net.mslivo.core.engine.tools.Tools;
import net.mslivo.core.engine.ui_engine.media.UIEngineBaseMedia_8;
import net.mslivo.core.engine.ui_engine.rendering.ExtendedAnimation;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;

/**
 * Created by Admin on 07.02.2019.
 */
public final class MediaManager {
    public static final String DIR_MUSIC = "music/", DIR_GRAPHICS = "sprites/", DIR_SOUND = "sound/", DIR_MODELS = "models/";
    public static final int MEDIAMANGER_INDEX_NONE = -1;
    private static final String ERROR_NOT_LOADED = "CMedia File \"%s\": is not loaded into MediaManager";
    private static final String ERROR_ALREADY_LOADED_OTHER = "CMedia File \"%s\": Already loaded in another MediaManager";
    private static final String ERROR_DUPLICATE = "CMedia File \"%s\": Duplicate file detected";
    private static final String ERROR_FILE_NOT_FOUND = "CMedia File \"%s\": Does not exist";
    private static final String ERROR_UNKNOWN_FORMAT = "CMedia File \"%s\": class \"%s\" not supported";
    private static final GlyphLayout glyphLayout = new GlyphLayout();
    private static final int DEFAULT_PAGE_WIDTH = 4096;
    private static final int DEFAULT_PAGE_HEIGHT = 4096;
    private boolean loaded = false;
    private Sound[] medias_sounds = null;
    private Music[] medias_music = null;
    private TextureRegion[] medias_images = null;
    private BitmapFont[] medias_fonts = null;
    private TextureRegion[][] medias_arrays = null;
    private ExtendedAnimation[] medias_animations = null;
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
        return prepareCMedia(UIEngineBaseMedia_8.ALL);
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
        PixmapPacker pixmapPacker = new PixmapPacker(pageWidth, pageHeight, Pixmap.Format.RGBA8888, 4, true);
        ArrayList<CMedia> imageCMediaLoadStack = new ArrayList<>();
        ArrayList<CMedia> soundCMediaLoadStack = new ArrayList<>();
        HashSet<CMedia> duplicateCheck = new HashSet<>();
        int step = 0;
        int stepsMax = 0;

        // split into Image and Sound data, skip duplicates, check format and index
        CMedia loadMedia;
        int imagesMax = 0, arraysMax = 0, animationsMax = 0, fontsMax = 0, soundMax = 0, musicMax = 0;
        int imagesIdx = 0, arraysIdx = 0, animationsIdx = 0, fontsIdx = 0, soundIdx = 0, musicIdx = 0;
        while ((loadMedia = loadMediaList.poll()) != null) {
            if (!Tools.File.findResource(loadMedia.file).exists()) {
                throw new RuntimeException(String.format(ERROR_FILE_NOT_FOUND, loadMedia.file()));
            } else if (duplicateCheck.contains(loadMedia)) {
                throw new RuntimeException(String.format(ERROR_DUPLICATE, loadMedia.file()));
            } else if (loadMedia.mediaManagerIndex() != MEDIAMANGER_INDEX_NONE) {
                throw new RuntimeException(String.format(ERROR_ALREADY_LOADED_OTHER, loadMedia.file()));
            }

            if (loadMedia instanceof CMediaSprite || loadMedia.getClass() == CMediaFont.class) {
                imageCMediaLoadStack.add(loadMedia);
            } else if (loadMedia.getClass() == CMediaSound.class || loadMedia.getClass() == CMediaMusic.class) {
                soundCMediaLoadStack.add(loadMedia);
            } else {
                throw new RuntimeException(String.format(ERROR_UNKNOWN_FORMAT, loadMedia.file(), loadMedia.getClass().getSimpleName()));
            }
            switch (loadMedia) {
                case CMediaImage _ -> imagesMax++;
                case CMediaArray _ -> arraysMax++;
                case CMediaAnimation _ -> animationsMax++;
                case CMediaFont _ -> fontsMax++;
                case CMediaSound _ -> soundMax++;
                case CMediaMusic _ -> musicMax++;
                default -> {
                }
            }
            duplicateCheck.add(loadMedia);
            stepsMax++;
        }
        medias_images = new TextureRegion[imagesMax];
        medias_arrays = new TextureRegion[arraysMax][];
        medias_animations = new ExtendedAnimation[animationsMax];
        medias_fonts = new BitmapFont[fontsMax];
        medias_sounds = new Sound[soundMax];
        medias_music = new Music[musicMax];
        duplicateCheck.clear();

        // 2. Load Image Data Into Pixmap Packer
        for (int i = 0; i < imageCMediaLoadStack.size(); i++) {
            CMedia imageMedia = imageCMediaLoadStack.get(i);
            String textureFileName = imageMedia.getClass() == CMediaFont.class ? imageMedia.file().replace(".fnt", ".png") : imageMedia.file();
            TextureData textureData = TextureData.Factory.loadFromFile(Tools.File.findResource(textureFileName), null, false);
            textureData.prepare();
            pixmapPacker.pack(imageMedia.file(), textureData.consumePixmap());
            textureData.disposePixmap();
            step++;
            if (loadProgress != null) loadProgress.onLoadStep(imageMedia.file(), step, stepsMax);
        }

        // 4. Create TextureAtlas
        this.textureAtlas = new TextureAtlas();
        pixmapPacker.updateTextureAtlas(textureAtlas, textureFilter, textureFilter, false);

        // 5. Fill Arrays with TextureAtlas Data
        for (int i = 0; i < imageCMediaLoadStack.size(); i++) {
            CMedia imageMedia = imageCMediaLoadStack.get(i);
            switch (imageMedia) {
                case CMediaImage cMediaImage -> {
                    cMediaImage.setMediaManagerIndex(imagesIdx);
                    medias_images[imagesIdx++] = textureAtlas.findRegion(cMediaImage.file());
                }
                case CMediaArray cMediaArray -> {
                    cMediaArray.setMediaManagerIndex(arraysIdx);
                    medias_arrays[arraysIdx++] = splitFrames(cMediaArray.file(), cMediaArray.regionWidth, cMediaArray.regionHeight,
                            cMediaArray.frameOffset, cMediaArray.frameLength).toArray(TextureRegion.class);
                }
                case CMediaAnimation cMediaAnimation -> {
                    cMediaAnimation.setMediaManagerIndex(animationsIdx);
                    medias_animations[animationsIdx++] = new ExtendedAnimation(cMediaAnimation.animation_speed,
                            splitFrames(cMediaAnimation.file(), cMediaAnimation.regionWidth, cMediaAnimation.regionHeight, cMediaAnimation.frameOffset, cMediaAnimation.frameLength),
                            cMediaAnimation.playMode
                    );
                }
                case CMediaFont cMediaFont -> {
                    BitmapFont bitmapFont = new BitmapFont(Tools.File.findResource(cMediaFont.file()), textureAtlas.findRegion(cMediaFont.file()));
                    bitmapFont.setColor(Color.GRAY);
                    bitmapFont.getData().markupEnabled = cMediaFont.markupEnabled;
                    cMediaFont.setMediaManagerIndex(fontsIdx);
                    medias_fonts[fontsIdx++] = bitmapFont;
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
                    cMediaSound.setMediaManagerIndex(soundIdx);
                    medias_sounds[soundIdx++] = Gdx.audio.newSound(Tools.File.findResource(cMediaSound.file()));
                }
                case CMediaMusic cMediaMusic -> {
                    cMediaMusic.setMediaManagerIndex(musicIdx);
                    medias_music[musicIdx++] = Gdx.audio.newMusic(Tools.File.findResource(soundMedia.file()));
                }
                default -> throw new IllegalStateException("Unexpected value: " + soundMedia);
            }
            loadedMediaList.add(soundMedia);
            step++;
            if (loadProgress != null) loadProgress.onLoadStep(soundMedia.file(), step, stepsMax);
        }
        soundCMediaLoadStack.clear();

        // 7. Finished
        this.loaded = true;
        return true;
    }

    private Array<TextureRegion> splitFrames(String file, int tile_width, int tile_height, int frameOffset,
                                             int frameLength) {
        TextureRegion textureRegion = textureAtlas.findRegion(file);
        int width = (textureRegion.getRegionWidth() / tile_width);
        int height = (textureRegion.getRegionHeight() / tile_height);
        int maxFrames = Math.clamp(width * height, 0, frameLength);

        int frameCount = maxFrames - frameOffset;
        if (frameCount == 0) return new Array<>();
        if (frameCount < 0)
            throw new RuntimeException("Error loading: \"" + file + "\": Negative frameCount = " + frameCount);


        TextureRegion[][] tmp = textureRegion.split(tile_width, tile_height);
        Array<TextureRegion> result = new Array<>();
        int allCounter = 0;
        framesLoop:

        for (int ix = 0; ix < tmp.length; ix++) {
            for (int iy = 0; iy < tmp[0].length; iy++) {
                allCounter++;
                if (allCounter > frameOffset) {
                    result.add(tmp[ix][iy]);
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
            loadedMediaList.get(i).setMediaManagerIndex(MEDIAMANGER_INDEX_NONE);

        // Dispose and null
        for (int i = 0; i < medias_sounds.length; i++) medias_sounds[i].dispose();
        for (int i = 0; i < medias_music.length; i++) medias_music[i].dispose();
        for (int i = 0; i < medias_fonts.length; i++) medias_fonts[i].dispose();
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
        return new CMediaImage(file);
    }

    public static CMediaAnimation create_CMediaAnimation(String file, int tileWidth, int tileHeight,
                                                         float animation_speed) {
        return create_CMediaAnimation(file, tileWidth, tileHeight, animation_speed, 0, Integer.MAX_VALUE, ExtendedAnimation.PlayMode.LOOP);
    }

    public static CMediaAnimation create_CMediaAnimation(String file, int tileWidth, int tileHeight,
                                                         float animation_speed, int frameOffset, int frameLength) {
        return create_CMediaAnimation(file, tileWidth, tileHeight, animation_speed, frameOffset, frameLength, ExtendedAnimation.PlayMode.LOOP);
    }

    public static CMediaAnimation create_CMediaAnimation(String file, int tileWidth, int tileHeight,
                                                         float animation_speed, int frameOffset, int frameLength, ExtendedAnimation.PlayMode playMode) {
        CMediaAnimation cMediaAnimation = new CMediaAnimation(
                file,
                Math.max(tileWidth, 1),
                Math.max(tileHeight, 1),
                animation_speed,
                Math.max(frameOffset, 0),
                frameLength,
                playMode
        );
        return cMediaAnimation;
    }

    public static CMediaFont create_CMediaFont(String file, int offset_x, int offset_y) {
        return create_CMediaFont(file, offset_x, offset_y, false);
    }

    public static CMediaFont create_CMediaFont(String file, int offset_x, int offset_y, boolean markupEnabled) {
        CMediaFont cMediaFont = new CMediaFont(
                file, offset_x, offset_y, markupEnabled);
        return cMediaFont;
    }

    public static CMediaMusic create_CMediaMusic(String file) {
        return new CMediaMusic(file);
    }

    public static CMediaSound create_CMediaSound(String file) {
        return new CMediaSound(file);
    }

    public static CMediaArray create_CMediaArray(String file, int tileWidth, int tileHeight) {
        return create_CMediaArray(file, tileWidth, tileHeight, 0, Integer.MAX_VALUE);
    }

    public static CMediaArray create_CMediaArray(String file, int tileWidth, int tileHeight, int frameOffset,
                                                 int frameLength) {
        CMediaArray cMediaArray = new CMediaArray(
                file,
                Math.max(tileWidth, 1),
                Math.max(tileHeight, 1),
                Math.max(frameOffset, 0),
                Math.max(frameLength, 0)
        );
        return cMediaArray;
    }

    public Object getCMediaSprite(CMediaSprite cMediaSprite) {
        return switch (cMediaSprite) {
            case CMediaImage cMediaImage -> medias_images[cMediaImage.mediaManagerIndex()];
            case CMediaAnimation cMediaAnimation -> medias_animations[cMediaAnimation.mediaManagerIndex()];
            case CMediaArray cMediaArray -> medias_arrays[cMediaArray.mediaManagerIndex()];
            default -> throw new IllegalStateException("Unexpected value: " + cMediaSprite);
        };
    }


    public TextureRegion getCMediaImage(CMediaImage cMedia) {
        return medias_images[cMedia.mediaManagerIndex()];
    }

    public ExtendedAnimation getCMediaAnimation(CMediaAnimation cMedia) {
        return medias_animations[cMedia.mediaManagerIndex()];
    }

    public TextureRegion getCMediaArray(CMediaArray cMedia, int arrayIndex) {
        return medias_arrays[cMedia.mediaManagerIndex()][arrayIndex];
    }

    public Sound getCMediaSound(CMediaSound cMediaSound) {
        return medias_sounds[cMediaSound.mediaManagerIndex()];
    }

    public Music getCMediaMusic(CMediaMusic cMediaMusic) {
        return medias_music[cMediaMusic.mediaManagerIndex()];
    }

    public int getCMediaSpriteWidth(CMediaSprite cMedia) {
        return switch (cMedia) {
            case CMediaImage __ -> medias_images[cMedia.mediaManagerIndex()].getRegionWidth();
            case CMediaArray array -> array.regionWidth;
            case CMediaAnimation animation -> animation.regionWidth;
            default -> throw new IllegalStateException("Unexpected value: " + cMedia);
        };
    }

    public int getCMediaSpriteHeight(CMediaSprite cMedia) {
        return switch (cMedia) {
            case CMediaImage __ -> medias_images[cMedia.mediaManagerIndex()].getRegionHeight();
            case CMediaArray array -> array.regionHeight;
            case CMediaAnimation animation -> animation.regionHeight;
            default -> throw new IllegalStateException("Unexpected value: " + cMedia);
        };
    }

    public int getCMediaArraySize(CMediaArray cMedia) {
        return medias_arrays[cMedia.mediaManagerIndex()].length;
    }

    public BitmapFont getCMediaFont(CMediaFont cMedia) {
        return medias_fonts[cMedia.mediaManagerIndex()];
    }

    public int getCMediaFontTextWidth(CMediaFont font, String text) {
        glyphLayout.setText(getCMediaFont(font), text);
        return (int) glyphLayout.width;
    }

    public int getCMediaFontTextHeight(CMediaFont font, String text) {
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
