package net.mslivo.core.engine.media_manager;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;

public final class CMediaFont extends CMedia implements Serializable {
    public boolean markupEnabled;
    public CMediaFontOutline outline;
    public CMediaFontSymbol[] symbols;
    public boolean useAtlas;

    public CMediaFont() {
    }

    public CMediaFont(String file) {
        this(file, true, null, null);
    }

    public CMediaFont(String file, boolean markupEnabled) {
        this(file, markupEnabled, null, null);
    }

    public CMediaFont(String file, boolean markupEnabled, CMediaFontSymbol[] symbols) {
        this(file, markupEnabled, symbols, null, true);
    }

    public CMediaFont(String filename, boolean markupEnabled, CMediaFontSymbol[] symbols, CMediaFontOutline outline) {
        this(filename, markupEnabled, symbols, outline, true);
    }

    public CMediaFont(String filename, boolean markupEnabled, CMediaFontSymbol[] symbols, CMediaFontOutline outline, boolean useAtlas) {
        super(filename);
        this.markupEnabled = markupEnabled;
        this.useAtlas = useAtlas;
        if (symbols != null) {
            this.symbols = new CMediaFontSymbol[symbols.length];
            for (int i = 0; i < symbols.length; i++)
                this.symbols[i] = symbols[i].copy();
        } else {
            this.symbols = new CMediaFontSymbol[0];
        }

        if (outline != null) {
            this.outline = new CMediaFontOutline(outline.color, outline.directions, outline.withSymbols, outline.outlineOnly);
        } else {
            this.outline = null;
        }
    }

    public CMediaFont copy(){
        CMediaFont copy = new CMediaFont();
        copy.copyFields(this);
        copy.markupEnabled = this.markupEnabled;
        copy.outline = this.outline.copy();
        copy.symbols = new CMediaFontSymbol[this.symbols.length];
        for(int i=0;i < this.symbols.length;i++)
            copy.symbols[i] = this.symbols[i].copy();
        copy.useAtlas = this.useAtlas;
        return copy;
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
