package net.mslivo.core.engine.media_manager;

import com.badlogic.gdx.graphics.Color;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;

public final class CMediaFont extends CMedia implements Serializable {
    public boolean markupEnabled;
    public Color outlineColor;
    public boolean outlineOnly;
    public boolean outlineSymbols;
    public CMediaFontSymbol[] symbols;

    public CMediaFont(){

    }

    public CMediaFont(String file) {
        this(file, true, null, Color.CLEAR, false, false);
    }

    public CMediaFont(String file,  boolean markupEnabled) {
        this(file, markupEnabled, null, Color.CLEAR, false, false);
    }

    public CMediaFont(String file, boolean markupEnabled, CMediaFontSymbol[] symbols) {
        this(file, markupEnabled, symbols, Color.CLEAR, false, false);
    }

    public CMediaFont(String filename,  boolean markupEnabled,CMediaFontSymbol[] symbols, Color outlineColor, boolean outlineOnly,boolean outlineSymbols) {
        super(filename);
        this.markupEnabled = markupEnabled;

        this.outlineSymbols = outlineSymbols;
        if(symbols != null) {
            this.symbols = new CMediaFontSymbol[symbols.length];
            for (int i = 0; i < symbols.length; i++) {
                this.symbols[i] = switch (symbols[i]){
                    case CMediaFontSingleSymbol cMediaFontSingleSymbol-> new CMediaFontSingleSymbol(cMediaFontSingleSymbol.id,cMediaFontSingleSymbol.file, cMediaFontSingleSymbol.yoffset);
                    case CMediaFontArraySymbol cMediaFontArraySymbol -> new CMediaFontArraySymbol(cMediaFontArraySymbol.ids, cMediaFontArraySymbol.file,cMediaFontArraySymbol.yoffset,
                            cMediaFontArraySymbol.regionWidth, cMediaFontArraySymbol.regionHeight, cMediaFontArraySymbol.frameOffset, cMediaFontArraySymbol.frameLength);
                };
            }
        }else{
            this.symbols = new CMediaFontSymbol[0];
        }

        this.outlineColor = outlineColor != null ? outlineColor.cpy() : Color.CLEAR.cpy();
        this.outlineOnly = outlineOnly;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        CMediaFont that = (CMediaFont) o;
        return markupEnabled == that.markupEnabled && outlineOnly == that.outlineOnly && outlineSymbols == that.outlineSymbols && Objects.equals(outlineColor, that.outlineColor) && Arrays.equals(symbols, that.symbols);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + Boolean.hashCode(markupEnabled);
        result = 31 * result + Objects.hashCode(outlineColor);
        result = 31 * result + Boolean.hashCode(outlineOnly);
        result = 31 * result + Boolean.hashCode(outlineSymbols);
        result = 31 * result + Arrays.hashCode(symbols);
        return result;
    }
}
