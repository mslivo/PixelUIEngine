package org.mslivo.core.engine.media_manager;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.TextureData;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.utils.Align;
import org.mslivo.core.engine.media_manager.media.*;
import org.mslivo.core.engine.tools.Tools;
import org.mslivo.core.engine.ui_engine.media.GUIBaseMedia;
import org.mslivo.core.engine.ui_engine.misc.FColor;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
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

    private final HashMap<CMediaSound, Sound> medias_sounds = new HashMap<>();

    private final HashMap<CMediaMusic, Music> medias_music = new HashMap<>();

    private final HashMap<CMediaImage, TextureRegion> medias_images = new HashMap<>();

    private final HashMap<CMediaCursor, TextureRegion> medias_cursors = new HashMap<>();

    private final HashMap<CMediaFont, BitmapFont> medias_fonts = new HashMap<>();

    private final HashMap<CMediaArray, TextureRegion[]> medias_arrays = new HashMap<>();

    private final HashMap<CMediaAnimation, Animation> medias_animations = new HashMap<>();

    private final ArrayDeque<CMedia> loadMediaList = new ArrayDeque<>();

    public final HashSet<CMedia> loadedMediaList = new HashSet<>();

    private TextureAtlas textureAtlas;

    public MediaManager() {
        unloadAndReset();
    }


    /* ----- Prepare ----- */
    public boolean prepareCMedia(CMedia cMedia) {
        if (loaded) return false;
        loadMediaList.add(cMedia);
        return true;
    }

    public boolean prepareCMedia(CMedia[] cMedias) {
        if (loaded) return false;
        for (int i = 0; i < cMedias.length; i++) {
            loadMediaList.add(cMedias[i]);
        }
        return true;
    }

    public boolean prepareGUICMedia() {
        if (loaded) return false;
        prepareCMedia(GUIBaseMedia.ALL);
        return true;
    }

    /* ----- Load ---- */

    public boolean loadAssets() {
        return loadAssets(DEFAULT_PAGE_WIDTH, DEFAULT_PAGE_HEIGHT, null, Texture.TextureFilter.Nearest);
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
        ArrayList<CMedia> imageData = new ArrayList<>();
        ArrayList<CMedia> soundData = new ArrayList<>();
        int step = 0;
        int stepsMax = 0;
        boolean anyImageData = false;

        // Split into Image and Sound Data
        CMedia loadMedia;
        while ((loadMedia = loadMediaList.poll()) != null) {
            if (!loadedMediaList.contains(loadMedia)) {
                if (loadMedia instanceof CMediaGFX || loadMedia.getClass() == CMediaFont.class) {
                    imageData.add(loadMedia);
                    loadedMediaList.add(loadMedia);
                    stepsMax += 2;
                    anyImageData = true;
                } else if (loadMedia.getClass() == CMediaSound.class || loadMedia.getClass() == CMediaMusic.class) {
                    soundData.add(loadMedia);
                    loadedMediaList.add(loadMedia);
                    stepsMax += 1;
                } else {
                    throw new RuntimeException("Unknown CMedia Format. File \"" + loadMedia.file + "\", Format: \"" + loadMedia.getClass().getSimpleName() + "\"");
                }
            }
        }

        // 1. Load Image Data Into Pixmap Packer
        for (int i = 0; i < imageData.size(); i++) {
            CMedia imageMedia = imageData.get(i);
            String textureFileName = imageMedia.getClass() == CMediaFont.class ? imageMedia.file.replace(".fnt", ".png") : imageMedia.file;
            Texture texture = new Texture(Tools.File.findResource(DIR_GRAPHICS + textureFileName), false);
            TextureData textureData = texture.getTextureData();
            textureData.prepare();
            pixmapPacker.pack(imageMedia.file, textureData.consumePixmap());
            textureData.disposePixmap();
            texture.dispose();
            step++;
            if (loadProgress != null) loadProgress.onLoadStep(imageMedia.file, step, stepsMax);
        }


        // 2. Create Image Texture Atlas
        if (anyImageData) {
            this.textureAtlas = new TextureAtlas();
            pixmapPacker.updateTextureAtlas(textureAtlas, textureFilter, textureFilter, false);
        }

        // 3. Prepare image Data
        for (int i = 0; i < imageData.size(); i++) {
            CMedia imageMedia = imageData.get(i);
            if (imageMedia.getClass() == CMediaImage.class) {
                CMediaImage cMediaImage = (CMediaImage) imageMedia;
                medias_images.put(cMediaImage, textureAtlas.findRegion(imageMedia.file));
            } else if (imageMedia.getClass() == CMediaCursor.class) {
                CMediaCursor cMediaCursor = (CMediaCursor) imageMedia;
                medias_cursors.put(cMediaCursor, textureAtlas.findRegion(imageMedia.file));
            } else if (imageMedia.getClass() == CMediaArray.class) {
                CMediaArray cMediaArray = (CMediaArray) imageMedia;
                TextureRegion[] result = splitFrames(cMediaArray.file, cMediaArray.tile_width, cMediaArray.tile_height,
                        cMediaArray.frameOffset, cMediaArray.frameLength);
                medias_arrays.put(cMediaArray, result);
            } else if (imageMedia.getClass() == CMediaAnimation.class) {
                CMediaAnimation cMediaAnimation = (CMediaAnimation) imageMedia;
                TextureRegion[] result = splitFrames(cMediaAnimation.file, cMediaAnimation.tile_width, cMediaAnimation.tile_height,
                        cMediaAnimation.frameOffset, cMediaAnimation.frameLength);
                Animation<TextureRegion> animation = new Animation<>(cMediaAnimation.animation_speed, result);
                medias_animations.put(cMediaAnimation, animation);
            } else if (imageMedia.getClass() == CMediaFont.class) {
                CMediaFont cMediaFont = (CMediaFont) imageMedia;
                BitmapFont bitmapFont = new BitmapFont(Tools.File.findResource(DIR_GRAPHICS + cMediaFont.file), textureAtlas.findRegion(cMediaFont.file));
                medias_fonts.put(cMediaFont, bitmapFont);
            }


            step++;
            if (loadProgress != null) loadProgress.onLoadStep(imageMedia.file, step, stepsMax);
        }

        // 4. Load Sound Data
        for (int i = 0; i < soundData.size(); i++) {
            CMedia soundMedia = soundData.get(i);
            if (soundMedia.getClass() == CMediaSound.class) {
                if (!loadedMediaList.contains(soundMedia.file)) {
                    CMediaSound cMediaSound = (CMediaSound) soundMedia;
                    Sound sound = Gdx.audio.newSound(Tools.File.findResource(DIR_SOUND_FX + cMediaSound.file));
                    medias_sounds.put(cMediaSound, sound);
                }
            } else if (soundMedia.getClass() == CMediaMusic.class) {
                if (!loadedMediaList.contains(soundMedia.file)) {
                    CMediaMusic cMediaMusic = (CMediaMusic) soundMedia;
                    Music music = Gdx.audio.newMusic(Tools.File.findResource(DIR_MUSIC + soundMedia.file));
                    medias_music.put(cMediaMusic, music);
                }
            }


            step++;
            if (loadProgress != null) loadProgress.onLoadStep(soundMedia.file, step, stepsMax);
        }

        imageData.clear();
        soundData.clear();
        pixmapPacker.dispose();
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
        // Images
        if (textureAtlas != null) {
            this.textureAtlas.dispose();
        }
        // Sounds
        for (CMediaSound cMediaSound : medias_sounds.keySet()) {
            medias_sounds.get(cMediaSound).dispose();
        }
        this.medias_sounds.clear();
        for (CMediaMusic cMediaMusic : medias_music.keySet()) {
            medias_music.get(cMediaMusic).dispose();
        }
        this.medias_music.clear();
        // Fonts
        for (CMediaFont cMediaFont : medias_fonts.keySet()) {
            medias_fonts.get(cMediaFont).dispose();
        }

        this.medias_fonts.clear();
        this.medias_cursors.clear();
        this.medias_images.clear();
        this.medias_arrays.clear();
        this.medias_animations.clear();
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
    public void drawCMediaGFX(SpriteBatch batch, CMediaGFX cMedia, float x, float y) {
        drawCMediaGFX(batch, cMedia, x, y, 0, 0);
    }

    public void drawCMediaGFX(SpriteBatch batch, CMediaGFX cMedia, float x, float y, int arrayIndex, float animationTimer) {
        if (cMedia == null) return;
        if (cMedia.getClass() == CMediaImage.class) {
            drawCMediaImage(batch, (CMediaImage) cMedia, x, y);
        } else if (cMedia.getClass() == CMediaAnimation.class) {
            drawCMediaAnimation(batch, (CMediaAnimation) cMedia, x, y, animationTimer);
        } else if (cMedia.getClass() == CMediaArray.class) {
            drawCMediaArray(batch, (CMediaArray) cMedia, x, y, arrayIndex);
        } else if (cMedia.getClass() == CMediaCursor.class) {
            drawCMediaCursor(batch, (CMediaCursor) cMedia, x, y);
        }
    }

    public void drawCMediaGFX(SpriteBatch batch, CMediaGFX cMedia, float x, float y, float origin_x, float origin_y) {
        drawCMediaGFX(batch, cMedia, x, y, origin_x, origin_y, 0, 0);
    }

    public void drawCMediaGFX(SpriteBatch batch, CMediaGFX cMedia, float x, float y, float origin_x, float origin_y, int arrayIndex, float animationTimer) {
        if (cMedia == null) return;
        if (cMedia.getClass() == CMediaImage.class) {
            drawCMediaImage(batch, (CMediaImage) cMedia, x, y, origin_x, origin_y);
        } else if (cMedia.getClass() == CMediaAnimation.class) {
            drawCMediaAnimation(batch, (CMediaAnimation) cMedia, x, y, animationTimer, origin_x, origin_y);
        } else if (cMedia.getClass() == CMediaArray.class) {
            drawCMediaArray(batch, (CMediaArray) cMedia, x, y, arrayIndex, origin_x, origin_y);
        }
    }

    public void drawCMediaGFX(SpriteBatch batch, CMediaGFX cMedia, float x, float y, float origin_x, float origin_y, float width, float height) {
        drawCMediaGFX(batch, cMedia, x, y, origin_x, origin_y, width, height, 0, 0);
    }

    public void drawCMediaGFX(SpriteBatch batch, CMediaGFX cMedia, float x, float y, float origin_x, float origin_y, float width, float height, int arrayIndex, float animationTimer) {
        if (cMedia == null) return;
        if (cMedia.getClass() == CMediaImage.class) {
            drawCMediaImage(batch, (CMediaImage) cMedia, x, y, origin_x, origin_y, width, height);
        } else if (cMedia.getClass() == CMediaAnimation.class) {
            drawCMediaAnimation(batch, (CMediaAnimation) cMedia, x, y, animationTimer, origin_x, origin_y, width, height);
        } else if (cMedia.getClass() == CMediaArray.class) {
            drawCMediaArray(batch, (CMediaArray) cMedia, x, y, arrayIndex, origin_x, origin_y, width, height);
        }
    }

    public void drawCMediaGFX(SpriteBatch batch, CMediaGFX cMedia, float x, float y, float origin_x, float origin_y, float width, float height, float rotation) {
        drawCMediaGFX(batch, cMedia, x, y, origin_x, origin_y, width, height, rotation, 0, 0);
    }

    public void drawCMediaGFX(SpriteBatch batch, CMediaGFX cMedia, float x, float y, float origin_x, float origin_y, float width, float height, float rotation, float animationTimer, int arrayIndex) {
        if (cMedia == null) return;
        if (cMedia.getClass() == CMediaImage.class) {
            drawCMediaImage(batch, (CMediaImage) cMedia, x, y, origin_x, origin_y, width, height, rotation);
        } else if (cMedia.getClass() == CMediaAnimation.class) {
            drawCMediaAnimation(batch, (CMediaAnimation) cMedia, x, y, animationTimer, origin_x, origin_y, width, height, rotation);
        } else if (cMedia.getClass() == CMediaArray.class) {
            drawCMediaArray(batch, (CMediaArray) cMedia, x, y, arrayIndex, origin_x, origin_y, width, height, rotation);
        }
    }

    public void drawCMediaGFXCut(SpriteBatch batch, CMediaGFX cMedia, float x, float y, int widthCut, int heightCut) {
        drawCMediaGFXCut(batch, cMedia, x, y, widthCut, heightCut, 0, 0);
    }

    public void drawCMediaGFXCut(SpriteBatch batch, CMediaGFX cMedia, float x, float y, int widthCut, int heightCut, float animationTimer, int arrayIndex) {
        drawCMediaGFXCut(batch, cMedia, x, y, 0, 0, widthCut, heightCut, animationTimer, arrayIndex);
    }

    public void drawCMediaGFXCut(SpriteBatch batch, CMediaGFX cMedia, float x, float y, int srcX, int srcY, int widthCut, int heightCut) {
        drawCMediaGFXCut(batch, cMedia, x, y, srcX, srcY, widthCut, heightCut, 0, 0);
    }

    public void drawCMediaGFXCut(SpriteBatch batch, CMediaGFX cMedia, float x, float y, int srcX, int srcY, int widthCut, int heightCut, float animationTimer, int arrayIndex) {
        if (cMedia == null) return;
        if (cMedia.getClass() == CMediaImage.class) {
            drawCMediaImageCut(batch, (CMediaImage) cMedia, x, y, srcX, srcY, widthCut, heightCut);
        } else if (cMedia.getClass() == CMediaAnimation.class) {
            drawCMediaAnimationCut(batch, (CMediaAnimation) cMedia, x, y, animationTimer, srcX, srcY, widthCut, heightCut);
        } else if (cMedia.getClass() == CMediaArray.class) {
            drawCMediaArrayCut(batch, (CMediaArray) cMedia, x, y, arrayIndex, srcX, srcY, widthCut, heightCut);
        }
    }

    public void drawCMediaGFXScale(SpriteBatch batch, CMediaGFX cMedia, float x, float y, float origin_x, float origin_y, float scaleX, float scaleY) {
        drawCMediaGFX(batch, cMedia, x, y, origin_x, origin_y, scaleX, scaleY, 0, 0, 0);
    }

    public void drawCMediaGFXScale(SpriteBatch batch, CMediaGFX cMedia, float x, float y, float origin_x, float origin_y, float scaleX, float scaleY, float animationTimer, int arrayIndex) {
        drawCMediaGFX(batch, cMedia, x, y, origin_x, origin_y, scaleX, scaleY, 0, animationTimer, arrayIndex);
    }

    public void drawCMediaGFXScale(SpriteBatch batch, CMediaGFX cMedia, float x, float y, float origin_x, float origin_y, float scaleX, float scaleY, float rotation) {
        drawCMediaGFX(batch, cMedia, x, y, origin_x, origin_y, scaleX, scaleY, rotation, 0, 0);
    }

    public void drawCMediaGFXScale(SpriteBatch batch, CMediaGFX cMedia, float x, float y, float origin_x, float origin_y, float scaleX, float scaleY, float rotation, float animationTimer, int arrayIndex) {
        if (cMedia == null) return;
        if (cMedia.getClass() == CMediaImage.class) {
            drawCMediaImageScale(batch, (CMediaImage) cMedia, x, y, origin_x, origin_y, scaleX, scaleY, rotation);
        } else if (cMedia.getClass() == CMediaAnimation.class) {
            drawCMediaAnimationScale(batch, (CMediaAnimation) cMedia, x, y, animationTimer, origin_x, origin_y, scaleX, scaleY, rotation);
        } else if (cMedia.getClass() == CMediaArray.class) {
            drawCMediaArrayScale(batch, (CMediaArray) cMedia, x, y, arrayIndex, origin_x, origin_y, scaleX, scaleY, rotation);
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

    public void drawCMediaCursor(SpriteBatch batch, CMediaCursor cMedia, float x, float y) {
        TextureRegion texture = getCMediaCursor(cMedia);
        batch.draw(texture, x - cMedia.hotspot_x, y - cMedia.hotspot_y, 0, 0, texture.getRegionWidth(), texture.getRegionHeight(), 1, 1, 0);
    }

    public TextureRegion getCMediaCursor(CMediaCursor cMedia) {
        return medias_cursors.get(cMedia);
    }


    /* ----- CMediaImage ----- */

    public void drawCMediaImage(SpriteBatch batch, CMediaImage cMedia, float x, float y) {
        TextureRegion texture = getCMediaImage(cMedia);
        batch.draw(texture, x, y, 0, 0, texture.getRegionWidth(), texture.getRegionHeight(), 1, 1, 0);
    }

    public void drawCMediaImage(SpriteBatch batch, CMediaImage cMedia, float x, float y, float origin_x, float origin_y) {
        TextureRegion texture = getCMediaImage(cMedia);
        batch.draw(texture, x, y, origin_x, origin_y, texture.getRegionWidth(), texture.getRegionHeight(), 1, 1, 0);
    }

    public void drawCMediaImage(SpriteBatch batch, CMediaImage cMedia, float x, float y, float origin_x, float origin_y, float width, float height) {
        TextureRegion texture = getCMediaImage(cMedia);
        batch.draw(texture, x, y, origin_x, origin_y, width, height, 1, 1, 0);
    }

    public void drawCMediaImage(SpriteBatch batch, CMediaImage cMedia, float x, float y, float origin_x, float origin_y, float width, float height, float rotation) {
        TextureRegion texture = getCMediaImage(cMedia);
        batch.draw(texture, x, y, origin_x, origin_y, width, height, 1, 1, rotation);
    }

    public void drawCMediaImageCut(SpriteBatch batch, CMediaImage cMedia, float x, float y, int widthCut, int heightCut) {
        TextureRegion texture = getCMediaImage(cMedia);
        batch.draw(texture.getTexture(), x, y, texture.getRegionX(), texture.getRegionY(), widthCut, heightCut);
    }

    public void drawCMediaImageCut(SpriteBatch batch, CMediaImage cMedia, float x, float y, int srcX, int srcY, int widthCut, int heightCut) {
        TextureRegion texture = getCMediaImage(cMedia);
        batch.draw(texture.getTexture(), x, y, texture.getRegionX() + srcX, texture.getRegionY() + srcY, widthCut, heightCut);
    }

    public void drawCMediaImageScale(SpriteBatch batch, CMediaImage cMedia, float x, float y, float origin_x, float origin_y, float scaleX, float scaleY) {
        TextureRegion texture = getCMediaImage(cMedia);
        batch.draw(texture, x, y, origin_x, origin_y, texture.getRegionWidth(), texture.getRegionHeight(), scaleX, scaleY, 0);
    }

    public void drawCMediaImageScale(SpriteBatch batch, CMediaImage cMedia, float x, float y, float origin_x, float origin_y, float scaleX, float scaleY, float rotation) {
        TextureRegion texture = getCMediaImage(cMedia);
        batch.draw(texture, x, y, origin_x, origin_y, texture.getRegionWidth(), texture.getRegionHeight(), scaleX, scaleY, rotation);
    }

    public TextureRegion getCMediaImage(CMediaImage cMedia) {
        return medias_images.get(cMedia);
    }

    public TextureRegion getCMediaAnimation(CMediaAnimation cMedia, float animationTimer) {
        return (TextureRegion) medias_animations.get(cMedia).getKeyFrame(animationTimer, true);
    }

    /* --- CMediaAnimation  --- */

    public void drawCMediaAnimation(SpriteBatch batch, CMediaAnimation cMedia, float x, float y, float animationTimer) {
        TextureRegion textureRegion = getCMediaAnimation(cMedia, animationTimer);
        batch.draw(textureRegion, x, y, 0, 0, textureRegion.getRegionWidth(), textureRegion.getRegionHeight(), 1, 1, 0);
    }

    public void drawCMediaAnimation(SpriteBatch batch, CMediaAnimation cMedia, float x, float y, float animationTimer, float origin_x, float origin_y) {
        TextureRegion textureRegion = getCMediaAnimation(cMedia, animationTimer);
        batch.draw(textureRegion, x, y, origin_x, origin_y, textureRegion.getRegionWidth(), textureRegion.getRegionHeight(), 1, 1, 0);
    }

    public void drawCMediaAnimation(SpriteBatch batch, CMediaAnimation cMedia, float x, float y, float animationTimer, float origin_x, float origin_y, float width, float height) {
        TextureRegion textureRegion = getCMediaAnimation(cMedia, animationTimer);
        batch.draw(textureRegion, x, y, origin_x, origin_y, width, height, 1, 1, 0);
    }

    public void drawCMediaAnimation(SpriteBatch batch, CMediaAnimation cMedia, float x, float y, float animationTimer, float origin_x, float origin_y, float width, float height, float rotation) {
        TextureRegion textureRegion = getCMediaAnimation(cMedia, animationTimer);
        batch.draw(textureRegion, x, y, origin_x, origin_y, width, height, 1, 1, rotation);
    }

    public void drawCMediaAnimationCut(SpriteBatch batch, CMediaAnimation cMedia, float x, float y, float animationTimer, int widthCut, int heightCut) {
        drawCMediaAnimationCut(batch, cMedia, x, y, animationTimer, 0, 0, widthCut, heightCut);
    }

    public void drawCMediaAnimationCut(SpriteBatch batch, CMediaAnimation cMedia, float x, float y, float animationTimer, int srcX, int srcY, int widthCut, int heightCut) {
        TextureRegion textureRegion = getCMediaAnimation(cMedia, animationTimer);
        batch.draw(textureRegion.getTexture(), x, y, textureRegion.getRegionX() + srcX, textureRegion.getRegionY() + srcY, widthCut, heightCut);
    }

    public void drawCMediaAnimationScale(SpriteBatch batch, CMediaAnimation cMedia, float x, float y, float animationTimer, float origin_x, float origin_y, float scaleX, float scaleY) {
        TextureRegion textureRegion = getCMediaAnimation(cMedia, animationTimer);
        batch.draw(textureRegion, x, y, origin_x, origin_y, textureRegion.getRegionWidth(), textureRegion.getRegionHeight(), scaleX, scaleY, 0);
    }

    public void drawCMediaAnimationScale(SpriteBatch batch, CMediaAnimation cMedia, float x, float y, float animationTimer, float origin_x, float origin_y, float scaleX, float scaleY, float rotation) {
        TextureRegion textureRegion = getCMediaAnimation(cMedia, animationTimer);
        batch.draw(textureRegion, x, y, origin_x, origin_y, textureRegion.getRegionWidth(), textureRegion.getRegionHeight(), scaleX, scaleY, rotation);
    }

    public boolean isCMediaAnimationFinished(CMediaAnimation cMedia, float animationTimer) {
        return medias_animations.get(cMedia).isAnimationFinished(animationTimer);
    }

    public int getCMediaAnimationKeyFrameIndex(CMediaAnimation cMedia, float animationTimer) {
        return medias_animations.get(cMedia).getKeyFrameIndex(animationTimer);
    }

    /* --- CMediaArray  --- */

    public void drawCMediaArray(SpriteBatch batch, CMediaArray cMedia, float x, float y, int arrayIndex) {
        TextureRegion textureRegion = getCMediaArray(cMedia, arrayIndex);
        batch.draw(textureRegion, x, y, 0, 0, textureRegion.getRegionWidth(), textureRegion.getRegionHeight(), 1, 1, 0);
    }

    public void drawCMediaArray(SpriteBatch batch, CMediaArray cMedia, float x, float y, int arrayIndex, float origin_x, float origin_y) {
        TextureRegion textureRegion = getCMediaArray(cMedia, arrayIndex);
        batch.draw(textureRegion, x, y, origin_x, origin_y, textureRegion.getRegionWidth(), textureRegion.getRegionHeight(), 1, 1, 0);
    }

    public void drawCMediaArray(SpriteBatch batch, CMediaArray cMedia, float x, float y, int arrayIndex, float origin_x, float origin_y, float width, float height) {
        TextureRegion textureRegion = getCMediaArray(cMedia, arrayIndex);
        batch.draw(textureRegion, x, y, origin_x, origin_y, width, height, 1, 1, 0);
    }

    public void drawCMediaArray(SpriteBatch batch, CMediaArray cMedia, float x, float y, int arrayIndex, float origin_x, float origin_y, float width, float height, float rotation) {
        TextureRegion textureRegion = getCMediaArray(cMedia, arrayIndex);
        batch.draw(textureRegion, x, y, origin_x, origin_y, width, height, 1, 1, rotation);
    }

    public void drawCMediaArrayCut(SpriteBatch batch, CMediaArray cMedia, float x, float y, int arrayIndex, int widthCut, int heightCut) {
        drawCMediaArrayCut(batch, cMedia, x, y, arrayIndex, 0, 0, widthCut, heightCut);
    }

    public void drawCMediaArrayCut(SpriteBatch batch, CMediaArray cMedia, float x, float y, int arrayIndex, int srcX, int srcY, int widthCut, int heightCut) {
        TextureRegion textureRegion = getCMediaArray(cMedia, arrayIndex);
        batch.draw(textureRegion.getTexture(), x, y, textureRegion.getRegionX() + srcX, textureRegion.getRegionY() + srcY, widthCut, heightCut);
    }

    public void drawCMediaArrayScale(SpriteBatch batch, CMediaArray cMedia, float x, float y, int arrayIndex, float origin_x, float origin_y, float scaleX, float scaleY) {
        TextureRegion textureRegion = getCMediaArray(cMedia, arrayIndex);
        batch.draw(textureRegion, x, y, origin_x, origin_y, textureRegion.getRegionWidth(), textureRegion.getRegionHeight(), scaleX, scaleY, 0);
    }

    public void drawCMediaArrayScale(SpriteBatch batch, CMediaArray cMedia, float x, float y, int arrayIndex, float origin_x, float origin_y, float scaleX, float scaleY, float rotation) {
        TextureRegion textureRegion = getCMediaArray(cMedia, arrayIndex);
        batch.draw(textureRegion, x, y, origin_x, origin_y, textureRegion.getRegionWidth(), textureRegion.getRegionHeight(), scaleX, scaleY, rotation);
    }

    public int getCMediaArraySize(CMediaArray cMedia) {
        return medias_arrays.get(cMedia).length;
    }

    public TextureRegion getCMediaArray(CMediaArray cMedia, int arrayIndex) {
        return medias_arrays.get(cMedia)[arrayIndex];
    }
    /* --- CMediaFont  --- */

    public void drawCMediaFont(SpriteBatch batch, CMediaFont cMedia, float x, float y, String text) {
        BitmapFont bitmapFont = getCMediaFont(cMedia);
        bitmapFont.draw(batch, text, (x + cMedia.offset_x), (y + cMedia.offset_y));
    }

    public void drawCMediaFont(SpriteBatch batch, CMediaFont cMedia, float x, float y, String text, int maxWidth) {
        BitmapFont bitmapFont = getCMediaFont(cMedia);
        bitmapFont.draw(batch, text, (x + cMedia.offset_x), (y + cMedia.offset_y), 0, text.length(), maxWidth, Align.left, false, "");
    }

    public void setCMediaFontColorWhite(CMediaFont font) {
        setCMediaFontColor(font, 1, 1, 1, 1);
    }

    public void setCMediaFontColor(CMediaFont font, FColor fColor) {
        setCMediaFontColor(font, fColor.r, fColor.g, fColor.b, fColor.a);
    }

    public void setCMediaFontColor(CMediaFont font, float r, float g, float b, float a) {
        getCMediaFont(font).setColor(r, g, b, a);
    }

    public BitmapFont getCMediaFont(CMediaFont cMedia) {
        return medias_fonts.get(cMedia);
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

    public long playCMediaSound(CMediaSound cMedia, float volume, float pan, float pitch) {
        return getCMediaSound(cMedia).play(volume, pitch, pan);
    }

    public Sound getCMediaSound(CMediaSound cMedia) {
        return medias_sounds.get(cMedia);
    }

    public Music getCMediaMusic(CMediaMusic cMedia) {
        return medias_music.get(cMedia);
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
