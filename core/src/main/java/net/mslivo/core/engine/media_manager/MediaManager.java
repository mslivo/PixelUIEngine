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
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntSet;
import com.badlogic.gdx.utils.ObjectMap;
import net.mslivo.core.engine.tools.Tools;
import net.mslivo.core.engine.ui_engine.media.UIEngineBaseMedia_8x8;
import net.mslivo.core.engine.ui_engine.rendering.ExtendedAnimation;

import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Admin on 07.02.2019.
 */
public final class MediaManager {
    public static final String DIR_MUSIC = "music/", DIR_GRAPHICS = "sprites/", DIR_SOUND = "sound/", DIR_MODELS = "models/";
    public static final int FONT_CUSTOM_SYMBOL_OFFSET = 512;
    private static final String ERROR_FILE_NOT_FOUND = "CMedia File \"%s\": Does not exist";
    private static final String ERROR_SPLIT_FRAMES = "Error splitting frames for: \"%s\": Negative frameCount = %d";
    private static final String ERROR_READ_FONT = "Error reading font file \"%s\"";
    private static final String ERROR_READ_FONT_FILE_DESCRIPTOR = "Error reading font file \"%s\": file= descriptor not found";
    private static final String ERROR_SYMBOL_ID_DUPLICATE = "Symbol \"%s\" id \"%d\" is already defined in font";
    private static final String ERROR_SYMBOL_NOT_ENOUGH_SPACE = "SymbolArray \"%s\" more symbols defined than available in texture";
    private static final String PACKED_FONT_NAME = "%s_%d.packed";
    private static final GlyphLayout glyphLayout = new GlyphLayout();
    private static final int DEFAULT_PAGE_WIDTH = 4096;
    private static final int DEFAULT_PAGE_HEIGHT = 4096;
    private static final Pattern FNT_FILE_PATTERN = Pattern.compile("file=\"([^\"]+)\"");
    private static final GridPoint2[] PIXEL_DIRECTIONS = new GridPoint2[]{
            new GridPoint2(-1, 0), new GridPoint2(-1, 1), new GridPoint2(0, 1), new GridPoint2(1, 1),
            new GridPoint2(1, 0), new GridPoint2(1, -1), new GridPoint2(0, -1), new GridPoint2(-1, -1)
    };
    private static final String FONT_FILE_DATA = "char id=%d      x=%d   y=%d   width=%d   height=%d   xoffset=%d   yoffset=%d   xadvance=%d    page=0   chnl=0" + System.lineSeparator();
    private boolean loaded = false;
    private ObjectMap<CMediaSoundEffect, Sound> medias_sounds = null;
    private ObjectMap<CMediaMusic, Music> medias_music = null;
    private ObjectMap<CMediaImage, TextureRegion> medias_images = null;
    private ObjectMap<CMediaFont, BitmapFont> medias_fonts = null;
    private ObjectMap<CMediaArray, TextureRegion[]> medias_arrays = null;
    private ObjectMap<CMediaAnimation, ExtendedAnimation> medias_animations = null;
    private final ArrayDeque<CMedia> loadMediaList = new ArrayDeque<>();
    private ArrayList<CMedia> loadedMediaList = new ArrayList<>();
    private TextureAtlas textureAtlas = null;

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
        if(outlineColor.a > 0f){
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
        }
        return pixmap;
    }


    private CreateFontResult createFontAddSymbols(Pixmap resultPixMap, CMediaFontSymbol[] symbols) {
        // Load Symbols
        StringBuilder fntFileData = new StringBuilder();
        ObjectMap<CMediaFontSymbol, Pixmap[]> symbolToPixMap = new ObjectMap<>();
        IntSet uniqueSymbolIds = new IntSet();

        for (int i = 0; i < symbols.length; i++) {
            Pixmap symbolPixmap = createTexturePixmap(Tools.File.findResource(symbols[i].file));
            Pixmap[] symbolPixmapResults;
            switch (symbols[i]) {
                case CMediaFontSingleSymbol singleSymbol -> {
                    if (!uniqueSymbolIds.contains(singleSymbol.id)) {
                        symbolPixmapResults = new Pixmap[]{symbolPixmap};
                        uniqueSymbolIds.add(singleSymbol.id);
                    } else {
                        throw new RuntimeException(String.format(ERROR_SYMBOL_ID_DUPLICATE, singleSymbol.file, singleSymbol.id));
                    }
                }
                case CMediaFontArraySymbol arraySymbol -> {
                    int symbolsX = MathUtils.floor(symbolPixmap.getWidth() / (float) arraySymbol.regionWidth);
                    int symbolsY = MathUtils.floor(symbolPixmap.getHeight() / (float) arraySymbol.regionHeight);
                    int maxSymbolsInPixMap = symbolsX * symbolsY;
                    int symbolsMax = Math.min(arraySymbol.ids.length, arraySymbol.frameLength);

                    if (symbolsMax > maxSymbolsInPixMap) {
                        throw new RuntimeException(String.format(ERROR_SYMBOL_NOT_ENOUGH_SPACE, arraySymbol.file));
                    }

                    symbolPixmapResults = new Pixmap[symbolsMax];

                    // Skip offset
                    int currentX = 0;
                    int currentY = 0;
                    for (int i2 = 0; i2 < arraySymbol.frameOffset; i2++) {
                        currentX += arraySymbol.regionWidth;
                        if (currentX + arraySymbol.regionWidth > symbolPixmap.getWidth()) {
                            currentX = 0;
                            currentY += arraySymbol.regionHeight;
                        }
                    }

                    for (int i2 = 0; i2 < symbolsMax; i2++) {
                        int symbolID = arraySymbol.ids[i2];
                        if (!uniqueSymbolIds.contains(symbolID)) {
                            symbolPixmapResults[i2] = copyPixmap(symbolPixmap, arraySymbol.regionWidth, arraySymbol.regionHeight, currentX, currentY, arraySymbol.regionWidth, arraySymbol.regionHeight);
                            uniqueSymbolIds.add(symbolID);
                        } else {
                            throw new RuntimeException(String.format(ERROR_SYMBOL_ID_DUPLICATE, arraySymbol.file, symbolID));
                        }
                        currentX += arraySymbol.regionWidth;
                        if ((currentX + arraySymbol.regionWidth) > symbolPixmap.getWidth()) {
                            currentX = 0;
                            currentY += arraySymbol.regionHeight;
                        }
                    }
                }
            }
            symbolToPixMap.put(symbols[i], symbolPixmapResults);
        }

        // Extend Pixmap at bottom to create symbol Area
        int symbolAreaHeight = 0;
        int symbolHeightMax = 0;
        int xCurrent = 0;
        for (int i = 0; i < symbols.length; i++) {
            Pixmap[] symbolPixmaps = symbolToPixMap.get(symbols[i]);
            for (int i2 = 0; i2 < symbolPixmaps.length; i2++) {
                Pixmap symbolPixmap = symbolPixmaps[i2];

                symbolHeightMax = Math.max(symbolHeightMax, symbolPixmap.getHeight());
                xCurrent += symbolPixmap.getWidth();
                if ((xCurrent + symbolPixmap.getWidth()) >= resultPixMap.getWidth()) {
                    symbolAreaHeight += symbolHeightMax;
                    symbolHeightMax = 0;
                    xCurrent = 0;
                }
            }
        }
        symbolAreaHeight += symbolHeightMax;
        int resultPixmapWidth = resultPixMap.getWidth();
        int originalHeight = resultPixMap.getHeight();
        int reultPixMapHeight = originalHeight + symbolAreaHeight;
        resultPixMap = copyPixmap(resultPixMap, resultPixmapWidth, reultPixMapHeight, 0, 0, resultPixMap.getWidth(), resultPixMap.getHeight());


        // copy symbols
        xCurrent = 0;
        symbolHeightMax = 0;
        int yCurrent = originalHeight;
        for (int i = 0; i < symbols.length; i++) {
            Pixmap[] symbolPixmaps = symbolToPixMap.get(symbols[i]);
            for (int i2 = 0; i2 < symbolPixmaps.length; i2++) {
                Pixmap symbolPixmap = symbolPixmaps[i2];

                resultPixMap.drawPixmap(symbolPixmap, xCurrent, yCurrent);
                int symbolId = switch (symbols[i]) {
                    case CMediaFontSingleSymbol singleSymbol -> singleSymbol.id;
                    case CMediaFontArraySymbol arraySymbol -> arraySymbol.ids[i2];
                };
                fntFileData.append(String.format(FONT_FILE_DATA, FONT_CUSTOM_SYMBOL_OFFSET + symbolId,
                        xCurrent, yCurrent, symbolPixmap.getWidth(),
                        symbolPixmap.getHeight(), -1,
                        12 - symbolPixmap.getHeight(),
                        symbolPixmap.getWidth() - 1));


                symbolHeightMax = Math.max(symbolHeightMax, symbolPixmap.getHeight());
                xCurrent += symbolPixmap.getWidth();
                if ((xCurrent + symbolPixmap.getWidth()) >= resultPixMap.getWidth()) {
                    yCurrent += symbolHeightMax;
                    symbolHeightMax = 0;
                    xCurrent = 0;
                }

            }
        }

        // Dispose Symbol Pixmaps
        symbolToPixMap.values().forEach(pixmaps -> {
            for (int i = 0; i < pixmaps.length; i++)
                pixmaps[i].dispose();
        });

        return new CreateFontResult(resultPixMap, fntFileData.toString());
    }

    private Pixmap copyPixmap(Pixmap originalPixmap, int newWidth, int newHeight, int srcX, int sryY, int srcWidth, int srcHeight) {
        Pixmap result = new Pixmap(newWidth, newHeight, originalPixmap.getFormat());
        result.setBlending(Pixmap.Blending.None);
        result.setColor(0, 0, 0, 0);
        result.fill();
        result.drawPixmap(originalPixmap, 0, 0, srcX, sryY, srcWidth, srcHeight);
        return result;
    }

    private CreateFontResult createFont(FileHandle textureFileHandle, Color outlineColor, boolean outlineOnly, boolean outlineSymbols, CMediaFontSymbol[] symbols) {
        CreateFontResult result = new CreateFontResult(createTexturePixmap(textureFileHandle), "");

        boolean outLine = !outlineColor.equals(Color.CLEAR);

        if (symbols.length > 0) {
            if (outLine && !outlineSymbols) {
                createFontModifyPixmapAddOutline(result.pixmap, outlineColor, outlineOnly);
                // outline before adding symbols
            }

            result = createFontAddSymbols(result.pixmap, symbols);
        }

        if (outLine && outlineSymbols) {
            // outline everything
            createFontModifyPixmapAddOutline(result.pixmap, outlineColor, outlineOnly);
        }

        return result;
    }


    private float getPixelAlpha(int pixel) {
        return (pixel & 0xFF) / 255f;
    }

    private Pixmap createTexturePixmap(FileHandle textureFileHandle) {
        TextureData textureData = TextureData.Factory.loadFromFile(textureFileHandle, Pixmap.Format.RGBA8888, false);
        textureData.prepare();
        return textureData.consumePixmap();
    }

    public boolean loadAssets(int pageWidth, int pageHeight, LoadProgress loadProgress, Texture.TextureFilter textureFilter) {
        if (loaded) return false;
        PixmapPacker pixmapPacker = new PixmapPacker(pageWidth, pageHeight, Pixmap.Format.RGBA8888, 4, true);
        ArrayList<CMediaFont> fontCMediaLoadStack = new ArrayList<>();
        ArrayList<CMediaSprite> spriteCMediaLoadStack = new ArrayList<>();
        ArrayList<CMediaSound> soundCMediaLoadStack = new ArrayList<>();
        HashMap<CMediaFont, String> createFontFNTFileData = new HashMap<>();
        HashMap<CMediaFont, String> createFontFNTPackedName = new HashMap<>();
        int step = 0;
        int stepsMax = 0;

        // split into Image and Sound data, skip duplicates, check format and index
        CMedia loadMedia;
        while ((loadMedia = loadMediaList.poll()) != null) {
            if (!Tools.File.findResource(loadMedia.file).exists()) {
                throw new RuntimeException(String.format(ERROR_FILE_NOT_FOUND, loadMedia.file));
            }

            switch (loadMedia) {
                case CMediaSprite cMediaSprite -> spriteCMediaLoadStack.add(cMediaSprite);
                case CMediaSound cMediaSound -> soundCMediaLoadStack.add(cMediaSound);
                case CMediaFont cMediaFont -> fontCMediaLoadStack.add(cMediaFont);
            }

            stepsMax++;
        }
        medias_images = new ObjectMap<>();
        medias_arrays = new ObjectMap<>();
        medias_animations = new ObjectMap<>();
        medias_fonts = new ObjectMap<>();
        medias_sounds = new ObjectMap<>();
        medias_music = new ObjectMap<>();

        // Load Sprite Data Into Pixmap Packer
        for (int i = 0; i < spriteCMediaLoadStack.size(); i++) {
            CMediaSprite cMediaSprite = spriteCMediaLoadStack.get(i);

            FileHandle textureFileHandle = Tools.File.findResource(cMediaSprite.file);
            String packedTextureName = cMediaSprite.file;
            if (pixmapPacker.getRect(packedTextureName) == null) {
                Pixmap pixmap = createTexturePixmap(textureFileHandle);
                pixmapPacker.pack(packedTextureName, pixmap);
                pixmap.dispose();
            }

            step++;
            if (loadProgress != null) loadProgress.onLoadStep(cMediaSprite.file, step, stepsMax);
        }

        // Create and Load Font Data Into Pixmap Packer
        int fontCount = 1;
        for (int i = 0; i < fontCMediaLoadStack.size(); i++) {
            CMediaFont cMediaFont = fontCMediaLoadStack.get(i);

            FileHandle textureFileHandle = getBitmapFontTextureHandle(Tools.File.findResource(cMediaFont.file));
            String packedFontTextureName = String.format(PACKED_FONT_NAME, cMediaFont.file, fontCount);
            CreateFontResult fontResult = createFont(textureFileHandle, cMediaFont.outlineColor, cMediaFont.outlineOnly, cMediaFont.outlineSymbols, cMediaFont.symbols);

            // pack
            createFontFNTFileData.put(cMediaFont, fontResult.fontFileData);
            createFontFNTPackedName.put(cMediaFont, packedFontTextureName);
            pixmapPacker.pack(packedFontTextureName, fontResult.pixmap);
            fontResult.pixmap.dispose();
            fontCount++;
            step++;
            if (loadProgress != null) loadProgress.onLoadStep(cMediaFont.file, step, stepsMax);
        }

        // Create TextureAtlas
        this.textureAtlas = new TextureAtlas();
        pixmapPacker.updateTextureAtlas(textureAtlas, textureFilter, textureFilter, false);
        pixmapPacker.dispose();

        // Fill Sprite CMedia Arrays with TextureAtlas Data
        for (int i = 0; i < spriteCMediaLoadStack.size(); i++) {
            CMediaSprite cMediaSprite = spriteCMediaLoadStack.get(i);
            switch (cMediaSprite) {
                case CMediaImage cMediaImage -> {

                    medias_images.put(cMediaImage, new TextureRegion(textureAtlas.findRegion(cMediaImage.file)));
                }
                case CMediaArray cMediaArray -> {
                    medias_arrays.put(cMediaArray, splitFrames(cMediaArray.file, cMediaArray.regionWidth, cMediaArray.regionHeight,
                            cMediaArray.frameOffset, cMediaArray.frameLength).toArray(TextureRegion.class));
                }
                case CMediaAnimation cMediaAnimation -> {
                    medias_animations.put(cMediaAnimation, new ExtendedAnimation(cMediaAnimation.animationSpeed,
                            splitFrames(cMediaAnimation.file, cMediaAnimation.regionWidth, cMediaAnimation.regionHeight, cMediaAnimation.frameOffset, cMediaAnimation.frameLength),
                            cMediaAnimation.playMode
                    ));
                }
            }
            loadedMediaList.add(cMediaSprite);
        }
        // Fill Font CMedia Arrays with TextureAtlas Data

        for (int i = 0; i < fontCMediaLoadStack.size(); i++) {
            CMediaFont cMediaFont = fontCMediaLoadStack.get(i);
            BitmapFont bitmapFont = new BitmapFont(
                    new FontFileHandle(Tools.File.findResource(cMediaFont.file), createFontFNTFileData.get(cMediaFont)),
                    new TextureRegion(textureAtlas.findRegion(createFontFNTPackedName.get(cMediaFont)))
            );
            bitmapFont.setColor(Color.GRAY);
            bitmapFont.getData().markupEnabled = cMediaFont.markupEnabled;
            medias_fonts.put(cMediaFont, bitmapFont);
        }

        // 6. Fill CMedia Arrays with Sound Data
        for (int i = 0; i < soundCMediaLoadStack.size(); i++) {
            CMediaSound soundMedia = soundCMediaLoadStack.get(i);
            switch (soundMedia) {
                case CMediaSoundEffect cMediaSoundEffect -> {
                    medias_sounds.put(cMediaSoundEffect, Gdx.audio.newSound(Tools.File.findResource(cMediaSoundEffect.file)));
                }
                case CMediaMusic cMediaMusic -> {
                    medias_music.put(cMediaMusic, Gdx.audio.newMusic(Tools.File.findResource(soundMedia.file)));
                }
            }
            loadedMediaList.add(soundMedia);
            step++;
            if (loadProgress != null) loadProgress.onLoadStep(soundMedia.file, step, stepsMax);
        }
        soundCMediaLoadStack.clear();

        // 7. Clean up & Finish
        spriteCMediaLoadStack.clear();
        createFontFNTFileData.clear();
        createFontFNTPackedName.clear();
        this.loaded = true;
        return true;
    }

    private FileHandle getBitmapFontTextureHandle(FileHandle fontFileHandle) {
        try (BufferedReader bufferedReader = fontFileHandle.reader(1024, Charset.defaultCharset().name())) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {

                Matcher matcher = FNT_FILE_PATTERN.matcher(line);
                if (matcher.find()) {
                    return Tools.File.findResource(fontFileHandle.parent() + "/" + matcher.group(1));
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(ERROR_READ_FONT, e);
        }

        throw new RuntimeException(ERROR_READ_FONT_FILE_DESCRIPTOR);
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
            throw new RuntimeException(String.format(ERROR_SPLIT_FRAMES, file, frameCount));


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

        // Dispose and null
        medias_sounds.values().forEach(sound -> sound.dispose());
        medias_music.values().forEach(music -> music.dispose());
        medias_fonts.values().forEach(bitmapFont -> bitmapFont.dispose());

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

    public TextureRegion sprite(CMediaSprite cMediaSprite, int arrayIndex, float animationTimer) {
        return switch (cMediaSprite) {
            case CMediaImage cMediaImage -> medias_images.get(cMediaImage);
            case CMediaAnimation cMediaAnimation -> medias_animations.get(cMediaAnimation).getKeyFrame(animationTimer);
            case CMediaArray cMediaArray -> medias_arrays.get(cMediaArray)[arrayIndex];
        };
    }

    public TextureRegion image(CMediaImage cMediaImage) {
        return medias_images.get(cMediaImage);
    }

    public ExtendedAnimation animation(CMediaAnimation cMediaAnimation) {
        return medias_animations.get(cMediaAnimation);
    }

    public TextureRegion array(CMediaArray cMediaArray, int arrayIndex) {
        return medias_arrays.get(cMediaArray)[arrayIndex];
    }

    public Sound sound(CMediaSoundEffect cMediaSoundEffect) {
        return medias_sounds.get(cMediaSoundEffect);
    }

    public Music music(CMediaMusic cMediaMusic) {
        return medias_music.get(cMediaMusic);
    }

    public int spriteWidth(CMediaSprite cMedia) {
        return switch (cMedia) {
            case CMediaImage cMediaImage -> medias_images.get(cMediaImage).getRegionWidth();
            case CMediaArray cMediaArray -> cMediaArray.regionWidth;
            case CMediaAnimation cMediaAnimation -> cMediaAnimation.regionWidth;
            default -> throw new IllegalStateException("Unexpected value: " + cMedia);
        };
    }

    public int imageWidth(CMediaImage cMediaImage) {
        return medias_images.get(cMediaImage).getRegionWidth();
    }

    public int arrayWidth(CMediaArray array) {
        return array.regionWidth;
    }

    public int animationWidth(CMediaAnimation animation) {
        return animation.regionWidth;
    }

    public int spriteHeight(CMediaSprite cMedia) {
        return switch (cMedia) {
            case CMediaImage cMediaImage -> medias_images.get(cMediaImage).getRegionHeight();
            case CMediaArray cMediaArray -> cMediaArray.regionHeight;
            case CMediaAnimation cMediaAnimation -> cMediaAnimation.regionHeight;
            default -> throw new IllegalStateException("Unexpected value: " + cMedia);
        };
    }

    public int imageHeight(CMediaImage cMediaImage) {
        return medias_images.get(cMediaImage).getRegionHeight();
    }

    public int arrayHeight(CMediaArray array) {
        return array.regionHeight;
    }

    public int animationHeight(CMediaAnimation animation) {
        return animation.regionHeight;
    }

    public int arraySize(CMediaArray cMediaArray) {
        return medias_arrays.get(cMediaArray).length;
    }

    public BitmapFont font(CMediaFont cMediaFont) {
        return medias_fonts.get(cMediaFont);
    }

    public int fontTextWidth(CMediaFont font, String text) {
        glyphLayout.setText(font(font), text);
        int width = (int) glyphLayout.width;
        return width;
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
