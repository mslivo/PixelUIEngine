package net.mslivo.core.engine.media_manager;

import com.badlogic.gdx.graphics.Color;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;

public final class CMediaFont extends CMedia implements Serializable {
    public int offset_x;
    public int offset_y;
    public boolean markupEnabled;
    public Color outlineColor;
    public boolean outlineOnly;
    public boolean outlineSymbols;
    public CMediaFontSymbol[] symbols;

    public CMediaFont(){

    }

    public CMediaFont(String file, int offset_x, int offset_y) {
        this(file, offset_x, offset_y, true, null, Color.CLEAR, false, false);
    }

    public CMediaFont(String file, int offset_x, int offset_y, boolean markupEnabled) {
        this(file, offset_x, offset_y, markupEnabled, null, Color.CLEAR, false, false);
    }

    public CMediaFont(String file, int offset_x, int offset_y, boolean markupEnabled, CMediaFontSymbol[] symbols) {
        this(file, offset_x, offset_y, markupEnabled, symbols, Color.CLEAR, false, false);
    }

    public CMediaFont(String filename, int offset_x, int offset_y, boolean markupEnabled,CMediaFontSymbol[] symbols, Color outlineColor, boolean outlineOnly,boolean outlineSymbols) {
        super(filename);
        this.offset_x = offset_x;
        this.offset_y = offset_y;
        this.markupEnabled = markupEnabled;

        this.outlineSymbols = outlineSymbols;
        if(symbols != null) {
            this.symbols = new CMediaFontSymbol[symbols.length];
            for (int i = 0; i < symbols.length; i++) {
                this.symbols[i] = new CMediaFontSymbol(symbols[i].id, symbols[i].file);
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
        return offset_x == that.offset_x && offset_y == that.offset_y && markupEnabled == that.markupEnabled && outlineOnly == that.outlineOnly && outlineSymbols == that.outlineSymbols && outlineColor.equals(that.outlineColor) && Arrays.equals(symbols, that.symbols);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + offset_x;
        result = 31 * result + offset_y;
        result = 31 * result + Boolean.hashCode(markupEnabled);
        result = 31 * result + outlineColor.hashCode();
        result = 31 * result + Boolean.hashCode(outlineOnly);
        result = 31 * result + Boolean.hashCode(outlineSymbols);
        result = 31 * result + Arrays.hashCode(symbols);
        return result;
    }
}
