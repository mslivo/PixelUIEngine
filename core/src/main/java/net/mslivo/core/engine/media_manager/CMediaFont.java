package net.mslivo.core.engine.media_manager;

import com.badlogic.gdx.graphics.Color;

import java.io.Serializable;
import java.util.Objects;

public final class CMediaFont extends CMedia implements Serializable {
    private final int hash;
    public final int offset_x;
    public final int offset_y;
    public final boolean markupEnabled;
    public final Color outlineColor;
    public final boolean outlineOnly;
    public final boolean outlineSymbols;
    public final CMediaFontSymbol[] symbols;

    CMediaFont(String filename, int offset_x, int offset_y, boolean markupEnabled,CMediaFontSymbol[] symbols, Color outlineColor, boolean outlineOnly,boolean outlineSymbols) {
        super(filename);
        this.offset_x = offset_x;
        this.offset_y = offset_y;
        this.markupEnabled = markupEnabled;

        this.outlineSymbols = outlineSymbols;
        int symbolHash = 0;
        if(symbols != null) {
            this.symbols = new CMediaFontSymbol[symbols.length];
            for (int i = 0; i < symbols.length; i++) {
                this.symbols[i] = new CMediaFontSymbol(symbols[i].id, symbols[i].file);
                symbolHash += this.symbols[i].hashCode();
            }
        }else{
            this.symbols = new CMediaFontSymbol[0];
        }

        this.outlineColor = outlineColor != null ? outlineColor.cpy() : Color.CLEAR.cpy();
        this.outlineOnly = outlineOnly;

        this.hash = Objects.hash(filename, offset_x, offset_y, markupEnabled, outlineColor, outlineOnly, outlineSymbols, symbolHash);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        return o.hashCode() == this.hash;
    }

    @Override
    public int hashCode() {
        return hash;
    }

}
