package net.mslivo.core.engine.media_manager;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.TextureData;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.g3d.loader.G3dModelLoader;
import com.badlogic.gdx.graphics.g3d.loader.ObjLoader;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.utils.Array;
import net.mslivo.core.engine.tools.Tools;
import net.mslivo.core.engine.ui_engine.media.UIEngineBaseMedia_8x8;
import net.mslivo.core.engine.ui_engine.rendering.ExtendedAnimation;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Admin on 07.02.2019.
 */
public final class MediaManager {
    public static final String DIR_MUSIC = "music/", DIR_GRAPHICS = "sprites/", DIR_SOUND = "sound/", DIR_MODELS = "models/";
    public static final int MEDIAMANGER_INDEX_NONE = -1;
    public static final int FONT_CUSTOM_SYMBOL_OFFSET = 512;
    private static final String ERROR_NOT_LOADED = "CMedia File \"%s\": is not loaded into MediaManager";
    private static final String ERROR_ALREADY_LOADED_OTHER = "CMedia File \"%s\": Already loaded in another MediaManager";
    private static final String ERROR_DUPLICATE = "CMedia File \"%s\": Duplicate file detected";
    private static final String ERROR_FILE_NOT_FOUND = "CMedia File \"%s\": Does not exist";
    private static final String ERROR_UNKNOWN_FORMAT = "CMedia File \"%s\": class \"%s\" not supported";
    private static final GlyphLayout glyphLayout = new GlyphLayout();
    private static final int DEFAULT_PAGE_WIDTH = 4096;
    private static final int DEFAULT_PAGE_HEIGHT = 4096;
    private static final GridPoint2[] PIXEL_DIRECTIONS = new GridPoint2[]{
            new GridPoint2(-1, 0), new GridPoint2(-1, 1), new GridPoint2(0, 1), new GridPoint2(1, 1),
            new GridPoint2(1, 0), new GridPoint2(1, -1), new GridPoint2(0, -1), new GridPoint2(-1, -1)
    };
    private static final String FONT_FILE_DATA = "char id=%d      x=%d   y=%d   width=%d   height=%d   xoffset=%d   yoffset=%d   xadvance=%d    page=0   chnl=0" + System.lineSeparator();
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
        return prepareCMedia(UIEngineBaseMedia_8x8.ALL);
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


    private Pixmap createFontModifyPixmapAddOutline(Pixmap pixmap, Color outlineColor, boolean outlineOnly) {
        pixmap.setBlending(Pixmap.Blending.None);
        // detect outline
        final ArrayDeque<GridPoint2> outLinePoints = new ArrayDeque<>();
        final ArrayDeque<GridPoint2> removePoints = new ArrayDeque<>();
        for (int ix = 0; ix < pixmap.getWidth(); ix++) {
            for (int iy = 0; iy < pixmap.getHeight(); iy++) {
                int pixel = pixmap.getPixel(ix, iy);
                float a = getPixelAlpha(pixel);

                if (a == 0f) {
                    continue;
                }

                if (outlineOnly)
                    removePoints.add(new GridPoint2(ix, iy));

                for (int ip = 0; ip < PIXEL_DIRECTIONS.length; ip++) {
                    GridPoint2 direction = PIXEL_DIRECTIONS[ip];
                    int x = ix + direction.x;
                    int y = iy + direction.y;
                    if (x >= 0 && x < pixmap.getWidth() && y >= 0 && y < pixmap.getHeight()) {
                        if (getPixelAlpha(pixmap.getPixel(x, y)) == 0f)
                            outLinePoints.add(new GridPoint2(x, y));
                    }

                }
            }
        }

        // create outline
        final int outlineColorRGBA8888 = Color.rgba8888(outlineColor);
        while (!outLinePoints.isEmpty()) {
            GridPoint2 outlinePixel = outLinePoints.poll();
            pixmap.drawPixel(outlinePixel.x, outlinePixel.y, outlineColorRGBA8888);
        }
        // remove
        final int clearColorRGBA8888 = Color.rgba8888(Color.CLEAR);

        while (!removePoints.isEmpty()) {
            GridPoint2 removePixel = removePoints.poll();
            pixmap.drawPixel(removePixel.x, removePixel.y, clearColorRGBA8888);
        }
        return pixmap;
    }


    private CreateFontResult createFontAddSymbols(Pixmap pixmap, CMediaFontSymbol[] symbols) {
        // Load Symbols
        StringBuilder fntFileData = new StringBuilder();
        Pixmap[] symbolPixmaps = new Pixmap[symbols.length];
        for (int i = 0; i < symbols.length; i++) {
            symbolPixmaps[i] = createTexturePixmap(symbols[i].file);
        }

        int symbolAreaHeight = 0;
        int symbolHeightMax = 0;
        int xCurrent = 0;
        for (int i = 0; i < symbolPixmaps.length; i++) {
            Pixmap symbolPixmap = symbolPixmaps[i];
            symbolHeightMax = Math.max(symbolHeightMax, symbolPixmap.getHeight());
            if ((xCurrent + symbolPixmap.getWidth()) >= pixmap.getWidth()) {
                symbolAreaHeight += symbolHeightMax;
                symbolHeightMax = 0;
                xCurrent = 0;
            } else {
                xCurrent += symbolPixmap.getWidth();
            }
        }
        symbolAreaHeight += symbolHeightMax;

        // Create Symbol Area
        int newWidth = pixmap.getWidth();
        int originalHeight = pixmap.getHeight();
        int newHeight = originalHeight + symbolAreaHeight;
        Pixmap newPixmap = new Pixmap(newWidth, newHeight, pixmap.getFormat());
        newPixmap.setBlending(Pixmap.Blending.None);
        newPixmap.setColor(0, 0, 0, 0);
        newPixmap.fill();
        newPixmap.drawPixmap(pixmap, 0, 0);
        pixmap = newPixmap;

        // copy symbols
        xCurrent = 0;
        int yCurrent = originalHeight;
        for (int i = 0; i < symbolPixmaps.length; i++) {
            Pixmap symbolPixmap = symbolPixmaps[i];
            symbolHeightMax = Math.max(symbolHeightMax, symbolPixmap.getHeight());
            if ((xCurrent + symbolPixmap.getWidth()) >= pixmap.getWidth()) {
                yCurrent += symbolHeightMax;
                symbolHeightMax = 0;
                xCurrent = 0;
            } else {
                newPixmap.drawPixmap(symbolPixmap, xCurrent, yCurrent);
                fntFileData.append(String.format(FONT_FILE_DATA, FONT_CUSTOM_SYMBOL_OFFSET + symbols[i].id,
                        xCurrent, yCurrent, symbolPixmap.getWidth(),
                        symbolPixmap.getHeight(), -1,
                        12 - symbolPixmap.getHeight(),
                        symbolPixmap.getWidth() - 1));

                xCurrent += symbolPixmap.getWidth();
            }
        }

        // Dispose Symbol Pixmaps
        for (int i = 0; i < symbolPixmaps.length; i++)
            symbolPixmaps[i].dispose();


        return new CreateFontResult(pixmap, fntFileData.toString());
    }

    private CreateFontResult createFont(String fontTextureFileName, Color outlineColor, boolean outlineOnly,boolean outlineSymbols, CMediaFontSymbol[] symbols) {
        CreateFontResult result = new CreateFontResult(createTexturePixmap(fontTextureFileName), "");

        boolean outLine = !outlineColor.equals(Color.CLEAR);

        if (symbols.length > 0) {
            if(outLine && !outlineSymbols){
                createFontModifyPixmapAddOutline(result.pixmap, outlineColor, outlineOnly);
                // outline before adding symbols
            }

            result = createFontAddSymbols(result.pixmap, symbols);
        }

        if (outLine && outlineSymbols ) {
            // outline everything
            createFontModifyPixmapAddOutline(result.pixmap, outlineColor, outlineOnly);
        }

        return result;
    }


    private float getPixelAlpha(int pixel) {
        return (pixel & 0xFF) / 255f;
    }

    private Pixmap createTexturePixmap(String textureFileName) {
        TextureData textureData = TextureData.Factory.loadFromFile(Tools.File.findResource(textureFileName), null, false);
        textureData.prepare();
        return textureData.consumePixmap();
    }

    public boolean loadAssets(int pageWidth, int pageHeight, LoadProgress loadProgress, Texture.TextureFilter textureFilter) {
        if (loaded) return false;
        PixmapPacker pixmapPacker = new PixmapPacker(pageWidth, pageHeight, Pixmap.Format.RGBA8888, 4, true);
        ArrayList<CMedia> imageCMediaLoadStack = new ArrayList<>();
        ArrayList<CMedia> soundCMediaLoadStack = new ArrayList<>();
        HashMap<CMediaFont, String> createFontFNTFileData = new HashMap<>();
        HashMap<CMediaFont, String> createFontFNTPackedName = new HashMap<>();
        int step = 0;
        int stepsMax = 0;

        // split into Image and Sound data, skip duplicates, check format and index
        CMedia loadMedia;
        int imagesMax = 0, arraysMax = 0, animationsMax = 0, fontsMax = 0, soundMax = 0, musicMax = 0;
        int imagesIdx = 0, arraysIdx = 0, animationsIdx = 0, fontsIdx = 0, soundIdx = 0, musicIdx = 0;
        while ((loadMedia = loadMediaList.poll()) != null) {
            if (!Tools.File.findResource(loadMedia.file).exists()) {
                throw new RuntimeException(String.format(ERROR_FILE_NOT_FOUND, loadMedia.file()));
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
            stepsMax++;
        }
        medias_images = new TextureRegion[imagesMax];
        medias_arrays = new TextureRegion[arraysMax][];
        medias_animations = new ExtendedAnimation[animationsMax];
        medias_fonts = new BitmapFont[fontsMax];
        medias_sounds = new Sound[soundMax];
        medias_music = new Music[musicMax];

        // 2. Load Image Data Into Pixmap Packer
        int fontCount = 1;
        for (int i = 0; i < imageCMediaLoadStack.size(); i++) {
            CMedia imageMedia = imageCMediaLoadStack.get(i);

            String textureFileName = imageMedia instanceof CMediaFont ? getFontTextureName(imageMedia.file()) : imageMedia.file();

            Pixmap pixmap;
            if (imageMedia instanceof CMediaFont cMediaFont) {
                CreateFontResult result = createFont(textureFileName, cMediaFont.outlineColor, cMediaFont.outlineOnly, cMediaFont.outlineSymbols,cMediaFont.symbols);
                // pack
                String packedName = textureFileName.replace(".png","_"+fontCount+".png");
                createFontFNTFileData.put(cMediaFont, result.fontFileData);
                createFontFNTPackedName.put(cMediaFont, packedName);
                pixmapPacker.pack(packedName, result.pixmap);
                result.pixmap.dispose();
                fontCount++;
            } else {
                if (pixmapPacker.getRect(textureFileName) == null) {
                    pixmap = createTexturePixmap(textureFileName);
                    pixmapPacker.pack(textureFileName, pixmap);
                    pixmap.dispose();
                }
            }


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
                    medias_images[imagesIdx++] = new TextureRegion(textureAtlas.findRegion(cMediaImage.file()));
                }
                case CMediaArray cMediaArray -> {
                    cMediaArray.setMediaManagerIndex(arraysIdx);
                    medias_arrays[arraysIdx++] = splitFrames(cMediaArray.file(), cMediaArray.regionWidth, cMediaArray.regionHeight,
                            cMediaArray.frameOffset, cMediaArray.frameLength).toArray(TextureRegion.class);
                }
                case CMediaAnimation cMediaAnimation -> {
                    cMediaAnimation.setMediaManagerIndex(animationsIdx);
                    medias_animations[animationsIdx++] = new ExtendedAnimation(cMediaAnimation.animationSpeed,
                            splitFrames(cMediaAnimation.file(), cMediaAnimation.regionWidth, cMediaAnimation.regionHeight, cMediaAnimation.frameOffset, cMediaAnimation.frameLength),
                            cMediaAnimation.playMode
                    );
                }
                case CMediaFont cMediaFont -> {
                    BitmapFont bitmapFont = new BitmapFont(
                            new FontFileHandle(Tools.File.findResource(cMediaFont.file()), createFontFNTFileData.get(cMediaFont)),
                            new TextureRegion(textureAtlas.findRegion(createFontFNTPackedName.get(cMediaFont)))
                    );
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
        createFontFNTFileData.clear();
        createFontFNTPackedName.clear();

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

    private String getFontTextureName(String fontFile) {
        return fontFile.replace(".fnt", ".png");
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
        return create_CMediaFont(file, offset_x, offset_y, true,null, null, false, false);
    }

    public static CMediaFont create_CMediaFont(String file, int offset_x, int offset_y, boolean markupEnabled) {
        return create_CMediaFont(file, offset_x, offset_y, markupEnabled,null, null, false, false);
    }

    public static CMediaFont create_CMediaFont(String file, int offset_x, int offset_y, boolean markupEnabled, CMediaFontSymbol[] symbols) {
        return create_CMediaFont(file, offset_x, offset_y, markupEnabled,symbols, null, false, false);
    }

    public static CMediaFont create_CMediaFont(String file, int offset_x, int offset_y, boolean markupEnabled, CMediaFontSymbol[] symbols, Color outlineColor, boolean outlineOnly, boolean outlineSymbols) {
        CMediaFont cMediaFont = new CMediaFont(file, offset_x, offset_y, markupEnabled, symbols, outlineColor, outlineOnly,outlineSymbols);
        return cMediaFont;
    }

    public static CMediaFontSymbol create_CMediaFontSymbol(int id, String file) {
        return new CMediaFontSymbol(id, file);
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

    public TextureRegion sprite(CMediaSprite cMediaSprite, int arrayIndex, float animationTimer) {
        return switch (cMediaSprite) {
            case CMediaImage cMediaImage -> medias_images[cMediaImage.mediaManagerIndex()];
            case CMediaAnimation cMediaAnimation ->
                    medias_animations[cMediaAnimation.mediaManagerIndex()].getKeyFrame(animationTimer);
            case CMediaArray cMediaArray -> medias_arrays[cMediaArray.mediaManagerIndex()][arrayIndex];
        };
    }

    public TextureRegion image(CMediaImage cMedia) {
        return medias_images[cMedia.mediaManagerIndex()];
    }

    public ExtendedAnimation animation(CMediaAnimation cMedia) {
        return medias_animations[cMedia.mediaManagerIndex()];
    }

    public TextureRegion array(CMediaArray cMedia, int arrayIndex) {
        return medias_arrays[cMedia.mediaManagerIndex()][arrayIndex];
    }

    public Sound sound(CMediaSound cMediaSound) {
        return medias_sounds[cMediaSound.mediaManagerIndex()];
    }

    public Music music(CMediaMusic cMediaMusic) {
        return medias_music[cMediaMusic.mediaManagerIndex()];
    }

    public int spriteWidth(CMediaSprite cMedia) {
        return switch (cMedia) {
            case CMediaImage __ -> medias_images[cMedia.mediaManagerIndex()].getRegionWidth();
            case CMediaArray array -> array.regionWidth;
            case CMediaAnimation animation -> animation.regionWidth;
            default -> throw new IllegalStateException("Unexpected value: " + cMedia);
        };
    }

    public int imageWidth(CMediaImage cMedia) {
        return medias_images[cMedia.mediaManagerIndex()].getRegionWidth();
    }

    public int arrayWidth(CMediaArray array) {
        return array.regionWidth;
    }

    public int animationWidth(CMediaAnimation animation) {
        return animation.regionWidth;
    }

    public int spriteHeight(CMediaSprite cMedia) {
        return switch (cMedia) {
            case CMediaImage __ -> medias_images[cMedia.mediaManagerIndex()].getRegionHeight();
            case CMediaArray array -> array.regionHeight;
            case CMediaAnimation animation -> animation.regionHeight;
            default -> throw new IllegalStateException("Unexpected value: " + cMedia);
        };
    }

    public int imageHeight(CMediaImage cMedia) {
        return medias_images[cMedia.mediaManagerIndex()].getRegionHeight();
    }

    public int arrayHeight(CMediaArray array) {
        return array.regionHeight;
    }

    public int animationHeight(CMediaAnimation animation) {
        return animation.regionHeight;
    }

    public int arraySize(CMediaArray cMedia) {
        return medias_arrays[cMedia.mediaManagerIndex()].length;
    }

    public BitmapFont font(CMediaFont cMedia) {
        return medias_fonts[cMedia.mediaManagerIndex()];
    }

    public int fontTextWidth(CMediaFont font, String text) {
        glyphLayout.setText(font(font), text);
        return (int) glyphLayout.width;
    }

    public int fontTextHeight(CMediaFont font, String text) {
        glyphLayout.setText(font(font), text);
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

    private class CreateFontResult {
        public final Pixmap pixmap;
        public final String fontFileData;

        public CreateFontResult(Pixmap pixmap, String fontFileData) {
            this.pixmap = pixmap;
            this.fontFileData = fontFileData;
        }
    }

    private class FontFileHandle extends FileHandle {
        private final byte[] modifiedData;

        public FontFileHandle(FileHandle originalFile, String additionalData) {
            super(originalFile.file());

            // Read original file content
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            try (InputStream inputStream = originalFile.read()) {
                int bytesRead;
                byte[] buffer = new byte[1024];
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }

                // Append additional data
                outputStream.write(additionalData.getBytes());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            // Store the combined data
            this.modifiedData = outputStream.toByteArray();
        }

        @Override
        public InputStream read() {
            // Provide the modified data as an InputStream
            return new ByteArrayInputStream(modifiedData);
        }
    }

}
