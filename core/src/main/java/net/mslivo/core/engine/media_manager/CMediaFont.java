package net.mslivo.core.engine.media_manager;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;

public final class CMediaFont extends CMedia implements Serializable {
    public boolean markupEnabled;
    public CMediaFontOutline outline;
    public CMediaFontSymbol[] symbols;

    public CMediaFont(){
    }

    public CMediaFont(String file) {
        this(file, true, null, null);
    }

    public CMediaFont(String file,  boolean markupEnabled) {
        this(file, markupEnabled, null, null);
    }

    public CMediaFont(String file, boolean markupEnabled, CMediaFontSymbol[] symbols) {
        this(file, markupEnabled, symbols, null);
    }

    public CMediaFont(String filename,  boolean markupEnabled,CMediaFontSymbol[] symbols, CMediaFontOutline outline) {
        super(filename);
        this.markupEnabled = markupEnabled;

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

        if(outline != null){
            this.outline = new CMediaFontOutline(outline.color,outline.directions,outline.withSymbols,outline.outlineOnly);
        }else{
            this.outline = null;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        CMediaFont that = (CMediaFont) o;
        return markupEnabled == that.markupEnabled && Objects.equals(outline, that.outline) && Arrays.equals(symbols, that.symbols);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + Boolean.hashCode(markupEnabled);
        result = 31 * result + Objects.hashCode(outline);
        result = 31 * result + Arrays.hashCode(symbols);
        return result;
    }
}
